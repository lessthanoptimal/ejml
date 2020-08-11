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

import org.ejml.data.FMatrix1Row;
import org.ejml.data.FMatrixD1;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.mult.VectorVectorMult_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * This contains less common or more specialized matrix operations.
 *
 * @author Peter Abeles
 */
public class SpecializedOps_FDRM {

    /**
     * <p>
     * Creates a reflector from the provided vector.<br>
     * <br>
     * Q = I - &gamma; u u<sup>T</sup><br>
     * &gamma; = 2/||u||<sup>2</sup>
     * </p>
     *
     * <p>
     * In practice {@link VectorVectorMult_FDRM#householder(float, FMatrixD1, FMatrixD1, FMatrixD1)}  multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector. Not modified.
     * @return An orthogonal reflector.
     */
    public static FMatrixRMaj createReflector(FMatrix1Row u ) {
        if( !MatrixFeatures_FDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        float norm = NormOps_FDRM.fastNormF(u);
        float gamma = -2.0f/(norm*norm);

        FMatrixRMaj Q = CommonOps_FDRM.identity(u.getNumElements());
        CommonOps_FDRM.multAddTransB(gamma,u,u,Q);

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
     * In practice {@link VectorVectorMult_FDRM#householder(float, FMatrixD1, FMatrixD1, FMatrixD1)}  multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector.  Not modified.
     * @param gamma To produce a reflector gamma needs to be equal to 2/||u||.
     * @return An orthogonal reflector.
     */
    public static FMatrixRMaj createReflector(FMatrixRMaj u , float gamma) {
        if( !MatrixFeatures_FDRM.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        FMatrixRMaj Q = CommonOps_FDRM.identity(u.getNumElements());
        CommonOps_FDRM.multAddTransB(-gamma,u,u,Q);

        return Q;
    }

    /**
     * Creates a copy of a matrix but swaps the rows as specified by the order array.
     *
     * @param order Specifies which row in the dest corresponds to a row in the src. Not modified.
     * @param src The original matrix. Not modified.
     * @param dst A Matrix that is a row swapped copy of src. Modified.
     */
    public static FMatrixRMaj copyChangeRow(int order[] , FMatrixRMaj src , FMatrixRMaj dst )
    {
        if( dst == null ) {
            dst = new FMatrixRMaj(src.numRows,src.numCols);
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
    public static FMatrixRMaj copyTriangle(FMatrixRMaj src , FMatrixRMaj dst , boolean upper ) {
        if( dst == null ) {
            dst = new FMatrixRMaj(src.numRows,src.numCols);
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
    public static void multLowerTranB( FMatrixRMaj mat ) {
        int m = mat.numCols;
        float[] L = mat.data;
        for( int i = 0; i < m; i++ ) {
            for( int j = m-1; j >= i; j-- ) {
                float val = 0;
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
    public static void multLowerTranA( FMatrixRMaj mat ) {
        int m = mat.numCols;
        float[] L = mat.data;
        for( int i = 0; i < m; i++ ) {
            for( int j = m-1; j >= i; j-- ) {
                float val = 0;
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
     * @see NormOps_FDRM#fastNormF
     *
     * @param a m by n matrix. Not modified.
     * @param b m by n matrix. Not modified.
     *
     * @return The F normal of the difference matrix.
     */
    public static float diffNormF(FMatrixD1 a , FMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        FMatrixRMaj diff = new FMatrixRMaj(size,1);

        for( int i = 0; i < size; i++ ) {
            diff.set(i , b.get(i) - a.get(i));
        }
        return NormOps_FDRM.normF(diff);
    }

    public static float diffNormF_fast(FMatrixD1 a , FMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        float total=0;
        for( int i = 0; i < size; i++ ) {
            float diff = b.get(i) - a.get(i);
            total += diff*diff;
        }
        return (float)Math.sqrt(total);
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
    public static float diffNormP1(FMatrixD1 a , FMatrixD1 b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final int size = a.getNumElements();

        float total=0;
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
    public static void addIdentity(FMatrix1Row A , FMatrix1Row B , float alpha )
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
    public static void subvector(FMatrix1Row A, int rowA, int colA, int length , boolean row, int offsetV, FMatrix1Row v) {
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
    public static FMatrixRMaj[] splitIntoVectors(FMatrix1Row A , boolean column )
    {
        int w = column ? A.numCols : A.numRows;

        int M = column ? A.numRows : 1;
        int N = column ? 1 : A.numCols;

        int o = Math.max(M,N);

        FMatrixRMaj[] ret  = new FMatrixRMaj[w];

        for( int i = 0; i < w; i++ ) {
            FMatrixRMaj a = new FMatrixRMaj(M,N);

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
    public static FMatrixRMaj pivotMatrix(FMatrixRMaj ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new FMatrixRMaj(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps_FDRM.fill(ret, 0);
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
    public static float diagProd( FMatrix1Row T )
    {
        float prod = 1.0f;
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
    public static float elementDiagonalMaxAbs( FMatrixD1 a ) {
        final int size = Math.min(a.numRows,a.numCols);

        float max = 0;
        for( int i = 0; i < size; i++ ) {
            float val = Math.abs(a.get( i,i ));
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
    public static float qualityTriangular(FMatrixD1 T)
    {
        int N = Math.min(T.numRows,T.numCols);

        // TODO make faster by just checking the upper triangular portion
        float max = elementDiagonalMaxAbs(T);

        if( max == 0.0f )
            return 0.0f;

        float quality = 1.0f;
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
    public static float elementSumSq( FMatrixD1 m  ) {

        // minimize round off error
        float maxAbs = CommonOps_FDRM.elementMaxAbs(m);
        if( maxAbs == 0)
            return 0;

        float total = 0;
        
        int N = m.getNumElements();
        for( int i = 0; i < N; i++ ) {
            float d = m.data[i]/maxAbs;
            total += d*d;
        }

        return maxAbs*total*maxAbs;
    }
}
