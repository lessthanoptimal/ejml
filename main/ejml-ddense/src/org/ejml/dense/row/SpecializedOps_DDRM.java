/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.dense.row;

import org.ejml.data.DMatrix1Row;
import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * This contains less common or more specialized matrix operations.
 *
 * @author Peter Abeles
 */
public class SpecializedOps_DDRM {

    /**
     * <p>
     * Creates a reflector from the provided vector.<br>
     * <br>
     * Q = I - &gamma; u u<sup>T</sup><br>
     * &gamma; = 2/||u||<sup>2</sup>
     * </p>
     *
     * <p>
     * In practice {@link VectorVectorMult_DDRM#householder(double, DMatrixD1, DMatrixD1, DMatrixD1)}  multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector. Not modified.
     * @return An orthogonal reflector.
     */
    public static DMatrixRMaj createReflector(DMatrix1Row u ) {
        if( !MatrixFeatures_DDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        double norm = NormOps_DDRM.fastNormF(u);
        double gamma = -2.0/(norm*norm);

        DMatrixRMaj Q = CommonOps_DDRM.identity(u.getNumElements());
        CommonOps_DDRM.multAddTransB(gamma,u,u,Q);

        return Q;
    }

    /**
     * <p>
     * Creates a reflector from the provided vector and gamma.<br>
     * <br>
     * Q = I - &gamma; u u<sup>T</sup><br>
     * </p>
     *
     * <p>
     * In practice {@link VectorVectorMult_DDRM#householder(double, DMatrixD1, DMatrixD1, DMatrixD1)}  multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector.  Not modified.
     * @param gamma To produce a reflector gamma needs to be equal to 2/||u||.
     * @return An orthogonal reflector.
     */
    public static DMatrixRMaj createReflector(DMatrixRMaj u , double gamma) {
        if( !MatrixFeatures_DDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        DMatrixRMaj Q = CommonOps_DDRM.identity(u.getNumElements());
        CommonOps_DDRM.multAddTransB(-gamma,u,u,Q);

        return Q;
    }

    /**
     * Creates a copy of a matrix but swaps the rows as specified by the order array.
     *
     * @param order Specifies which row in the dest corresponds to a row in the src. Not modified.
     * @param src The original matrix. Not modified.
     * @param dst A Matrix that is a row swapped copy of src. Modified.
     */
    public static DMatrixRMaj copyChangeRow(int order[] , DMatrixRMaj src , DMatrixRMaj dst )
    {
        if( dst == null ) {
            dst = new DMatrixRMaj(src.numRows,src.numCols);
        } else if( src.numRows != dst.numRows || src.numCols != dst.numCols ) {
            throw new IllegalArgumentException("src and dst must have the same dimensions.");
        }

        for( int i = 0; i < src.numRows; i++ ) {
            int indexDst = i*src.numCols;
            int indexSrc = order[i]*src.numCols;

            System.arraycopy(src.data,indexSrc,dst.data,indexDst,src.numCols);
        }

        return dst;
    }

    /**
     * Copies just the upper or lower triangular portion of a matrix.
     *
     * @param src Matrix being copied. Not modified.
     * @param dst Where just a triangle from src is copied.  If null a new one will be created. Modified.
     * @param upper If the upper or lower triangle should be copied.
     * @return The copied matrix.
     */
    public static DMatrixRMaj copyTriangle(DMatrixRMaj src , DMatrixRMaj dst , boolean upper ) {
        if( dst == null ) {
            dst = new DMatrixRMaj(src.numRows,src.numCols);
        } else if( src.numRows != dst.numRows || src.numCols != dst.numCols ) {
            throw new IllegalArgumentException("src and dst must have the same dimensions.");
        }

        if( upper ) {
            int N = Math.min(src.numRows,src.numCols);
            for( int i = 0; i < N; i++ ) {
                int index = i*src.numCols+i;
                System.arraycopy(src.data,index,dst.data,index,src.numCols-i);
            }
        } else {
            for( int i = 0; i < src.numRows; i++ ) {
                int length = Math.min(i+1,src.numCols);
                int index = i*src.numCols;
                System.arraycopy(src.data,index,dst.data,index,length);
            }
        }

        return dst;
    }

    /**
     * Performs L = L*L<sup>T</sup>
     */
    public static void multLowerTranB( DMatrixRMaj mat ) {
        int m = mat.numCols;
        double[] L = mat.data;
        for( int i = 0; i < m; i++ ) {
            for( int j = m-1; j >= i; j-- ) {
                double val = 0;
                for( int k = 0; k <= i; k++ ) {
                    val += L[ i*m + k] * L[ j*m + k ];
                }
                L[ i*m + j ] = val;
            }
        }
        // copy the results into the lower portion
        for( int i = 0; i < m; i++ ) {
            for (int j = 0; j < i; j++) {
                L[i*m+j] = L[j*m+i];
            }
        }
    }
    /**
     * Performs L = L<sup>T</sup>*L
     */
    public static void multLowerTranA( DMatrixRMaj mat ) {
        int m = mat.numCols;
        double[] L = mat.data;
        for( int i = 0; i < m; i++ ) {
            for( int j = m-1; j >= i; j-- ) {
                double val = 0;
                for (int k = j; k < m; k++) {
                    val += L[ k*m + i] * L[ k*m + j ];
                }
                L[ i*m + j ] = val;
            }
        }
        // copy the results into the lower portion
        for( int i = 0; i < m; i++ ) {
            for (int j = 0; j < i; j++) {
                L[i*m+j] = L[j*m+i];
            }
        }
    }

    /**
     * <p>
     * Computes the F norm of the difference between the two Matrices:<br>
     * <br>
     * Sqrt{&sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> ( a<sub>ij</sub> - b<sub>ij</sub>)<sup>2</sup>}
     * </p>
     * <p>
     * This is often used as a cost function.
     * </p>
     *
     * @see NormOps_DDRM#fastNormF
     *
     * @param a m by n matrix. Not modified.
     * @param b m by n matrix. Not modified.
     *
     * @return The F normal of the difference matrix.
     */
    public static double diffNormF(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        DMatrixRMaj diff = new DMatrixRMaj(size,1);

        for( int i = 0; i < size; i++ ) {
            diff.set(i , b.get(i) - a.get(i));
        }
        return NormOps_DDRM.normF(diff);
    }

    public static double diffNormF_fast(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        double total=0;
        for( int i = 0; i < size; i++ ) {
            double diff = b.get(i) - a.get(i);
            total += diff*diff;
        }
        return Math.sqrt(total);
    }

    /**
     * <p>
     * Computes the p=1 p-norm of the difference between the two Matrices:<br>
     * <br>
     * &sum;<sub>i=1:m</sub> &sum;<sub>j=1:n</sub> | a<sub>ij</sub> - b<sub>ij</sub>| <br>
     * <br>
     * where |x| is the absolute value of x.
     * </p>
     * <p>
     * This is often used as a cost function.
     * </p>
     *
     * @param a m by n matrix. Not modified.
     * @param b m by n matrix. Not modified.
     *
     * @return The p=1 p-norm of the difference matrix.
     */
    public static double diffNormP1(DMatrixD1 a , DMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        double total=0;
        for( int i = 0; i < size; i++ ) {
            total += Math.abs(b.get(i) - a.get(i));
        }
        return total;
    }

    /**
     * <p>
     * Performs the following operation:<br>
     * <br>
     * B = A + &alpha;I
     * <p> 
     *
     * @param A A square matrix.  Not modified.
     * @param B A square matrix that the results are saved to.  Modified.
     * @param alpha Scaling factor for the identity matrix.
     */
    public static void addIdentity(DMatrix1Row A , DMatrix1Row B , double alpha )
    {
        if( A.numCols != A.numRows )
            throw new IllegalArgumentException("A must be square");
        if( B.numCols != A.numCols || B.numRows != A.numRows )
            throw new IllegalArgumentException("B must be the same shape as A");

        int n = A.numCols;

        int index = 0;
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < n; j++ , index++) {
                if( i == j ) {
                    B.set( index , A.get(index) + alpha);
                } else {
                    B.set( index , A.get(index) );
                }
            }
        }
    }

    /**
     * <p>
     * Extracts a row or column vector from matrix A.  The first element in the matrix is at element (rowA,colA).
     * The next 'length' elements are extracted along a row or column.  The results are put into vector 'v'
     * start at its element v0.
     * </p>
     *
     * @param A Matrix that the vector is being extracted from.  Not modified.
     * @param rowA Row of the first element that is extracted.
     * @param colA Column of the first element that is extracted.
     * @param length Length of the extracted vector.
     * @param row If true a row vector is extracted, otherwise a column vector is extracted.
     * @param offsetV First element in 'v' where the results are extracted to.
     * @param v Vector where the results are written to. Modified.
     */
    public static void subvector(DMatrix1Row A, int rowA, int colA, int length , boolean row, int offsetV, DMatrix1Row v) {
        if( row ) {
            for( int i = 0; i < length; i++ ) {
                v.set( offsetV +i , A.get(rowA,colA+i) );
            }
        } else {
            for( int i = 0; i < length; i++ ) {
                v.set( offsetV +i , A.get(rowA+i,colA));
            }
        }
    }

    /**
     * Takes a matrix and splits it into a set of row or column vectors.
     *
     * @param A original matrix.
     * @param column If true then column vectors will be created.
     * @return Set of vectors.
     */
    public static DMatrixRMaj[] splitIntoVectors(DMatrix1Row A , boolean column )
    {
        int w = column ? A.numCols : A.numRows;

        int M = column ? A.numRows : 1;
        int N = column ? 1 : A.numCols;

        int o = Math.max(M,N);

        DMatrixRMaj[] ret  = new DMatrixRMaj[w];

        for( int i = 0; i < w; i++ ) {
            DMatrixRMaj a = new DMatrixRMaj(M,N);

            if( column )
                subvector(A,0,i,o,false,0,a);
            else
                subvector(A,i,0,o,true,0,a);

            ret[i] = a;
        }

        return ret;
    }

    /**
     * <p>
     * Creates a pivot matrix that exchanges the rows in a matrix:
     * <br>
     * A' = P*A<br>
     * </p>
     * <p>
     * For example, if element 0 in 'pivots' is 2 then the first row in A' will be the 3rd row in A.
     * </p>
     *
     * @param ret If null then a new matrix is declared otherwise the results are written to it.  Is modified.
     * @param pivots Specifies the new order of rows in a matrix.
     * @param numPivots How many elements in pivots are being used.
     * @param transposed If the transpose of the matrix is returned.
     * @return A pivot matrix.
     */
    public static DMatrixRMaj pivotMatrix(DMatrixRMaj ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new DMatrixRMaj(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps_DDRM.fill(ret, 0);
        }

        if( transposed ) {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(pivots[i],i,1);
            }
        } else {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(i,pivots[i],1);
            }
        }

        return ret;
    }

    /**
     * Computes the product of the diagonal elements.  For a diagonal or triangular
     * matrix this is the determinant.
     *
     * @param T A matrix.
     * @return product of the diagonal elements.
     */
    public static double diagProd( DMatrix1Row T )
    {
        double prod = 1.0;
        int N = Math.min(T.numRows,T.numCols);
        for( int i = 0; i < N; i++ ) {
            prod *= T.unsafe_get(i,i);
        }

        return prod;
    }

    /**
     * <p>
     * Returns the absolute value of the digonal element in the matrix that has the largest absolute value.<br>
     * <br>
     * Max{ |a<sub>ij</sub>| } for all i and j<br>
     * </p>
     *
     * @param a A matrix. Not modified.
     * @return The max abs element value of the matrix.
     */
    public static double elementDiagonalMaxAbs( DMatrixD1 a ) {
        final int size = Math.min(a.numRows,a.numCols);

        double max = 0;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.get( i,i ));
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    /**
     * Computes the quality of a triangular matrix, where the quality of a matrix
     * is defined in {@link LinearSolverDense#quality()}.  In
     * this situation the quality os the absolute value of the product of
     * each diagonal element divided by the magnitude of the largest diagonal element.
     * If all diagonal elements are zero then zero is returned.
     *
     * @param T A matrix.
     * @return the quality of the system.
     */
    public static double qualityTriangular(DMatrixD1 T)
    {
        int N = Math.min(T.numRows,T.numCols);

        // TODO make faster by just checking the upper triangular portion
        double max = elementDiagonalMaxAbs(T);

        if( max == 0.0 )
            return 0.0;

        double quality = 1.0;
        for( int i = 0; i < N; i++ ) {
            quality *= T.unsafe_get(i,i)/max;
        }

        return Math.abs(quality);
    }

    /**
     * Sums up the square of each element in the matrix.  This is equivalent to the
     * Frobenius norm squared.
     *
     * @param m Matrix.
     * @return Sum of elements squared.
     */
    public static double elementSumSq( DMatrixD1 m  ) {

        // minimize round off error
        double maxAbs = CommonOps_DDRM.elementMaxAbs(m);
        if( maxAbs == 0)
            return 0;

        double total = 0;
        
        int N = m.getNumElements();
        for( int i = 0; i < N; i++ ) {
            double d = m.data[i]/maxAbs;
            total += d*d;
        }

        return maxAbs*total*maxAbs;
    }
}
