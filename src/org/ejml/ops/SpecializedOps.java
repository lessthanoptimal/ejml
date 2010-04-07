/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.ops;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import org.ejml.data.DenseMatrix64F;


/**
 * This contains less common or more specialized matrix operations.
 *
 * @author Peter Abeles
 */
public class SpecializedOps {

    /**
     * <p>
     * Creates a reflector from the provided vector.<br>
     * <br>
     * Q = I - &gamma; u u<sup>T</sup><br>
     * &gamma; = 2/||u||<sup>2</sup>
     * </p>
     *
     * <p>
     * In practice {@link org.ejml.alg.dense.mult.VectorVectorMult#householder(double, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F) multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector. Not modified.
     * @return An orthogonal reflector.
     */
    public static DenseMatrix64F createReflector( DenseMatrix64F u ) {
        if( !MatrixFeatures.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        double norm = NormOps.fastNormF(u);
        double gamma = -2.0/(norm*norm);

        DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
        CommonOps.multAddTransB(gamma,u,u,Q);

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
     * In practice {@link org.ejml.alg.dense.mult.VectorVectorMult#householder(double, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F) multHouseholder}
     * should be used for performance reasons since there is no need to calculate Q explicitly.
     * </p>
     *
     * @param u A vector.  Not modified.
     * @param gamma To produce a reflector gamma needs to be equal to 2/||u||.
     * @return An orthogonal reflector.
     */
    public static DenseMatrix64F createReflector( DenseMatrix64F u , double gamma) {
        if( !MatrixFeatures.isVector(u))
            throw new IllegalArgumentException("u must be a vector");

        DenseMatrix64F Q = CommonOps.identity(u.getNumElements());
        CommonOps.multAddTransB(-gamma,u,u,Q);

        return Q;
    }

    /**
     * Creates a copy of a matrix but swaps the rows as specified by the order array.
     *
     * @param order Specifies which row in the dest corresponds to a row in the src. Not modified.
     * @param src The original matrix. Not modified.
     * @param dst A Matrix that is a row swapped copy of src. Modified.
     */
    public static DenseMatrix64F copyChangeRow( int order[] , DenseMatrix64F src , DenseMatrix64F dst )
    {
        if( dst == null ) {
            dst = new DenseMatrix64F(src.numRows,src.numCols);
        } else if( src.numRows != dst.numRows || src.numCols != dst.numCols ) {
            throw new IllegalArgumentException("src and dst must have the same dimensions.");
        }

        double dataSrc[] = src.data;
        double dataDst[] = dst.data;

        for( int i = 0; i < src.numRows; i++ ) {
            int indexDst = i*src.numCols;
            int indexSrc = order[i]*src.numCols;

            System.arraycopy(dataSrc,indexSrc,dataDst,indexDst,src.numCols);
        }

        return dst;
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
     * @see NormOps#fastNormF
     *
     * @param a m by n matrix. Not modified.
     * @param b m by n matrix. Not modified.
     *
     * @return The F normal of the difference matrix.
     */
    public static double diffNormF( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int size = a.getNumElements();

        DenseMatrix64F diff = new DenseMatrix64F(size,1);

        for( int i = 0; i < size; i++ ) {
            diff.data[i] = dataB[i] - dataA[i];
        }
        return NormOps.normF(diff);
    }

    public static double diffNormF_fast( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int size = a.getNumElements();

        double total=0;
        for( int i = 0; i < size; i++ ) {
            double diff = dataB[i] - dataA[i];
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
    public static double diffNormP1( DenseMatrix64F a , DenseMatrix64F b )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            throw new IllegalArgumentException("Both matrices must have the same shape.");
        }

        final double dataA[] = a.data;
        final double dataB[] = b.data;

        final int size = a.getNumElements();

        double total=0;
        for( int i = 0; i < size; i++ ) {
            total += Math.abs(dataB[i] - dataA[i]);
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
    public static void addIdentity( DenseMatrix64F A , DenseMatrix64F B , double alpha )
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
                    B.data[index] = A.data[index] + alpha;
                } else {
                    B.data[index] = A.data[index];
                }
            }
        }
    }

    /**
     * <p>
     * Sets 'sub' equal to the specified submatrix of 'orig'.  Sub is
     * reshaped if neccisary.  The submatrix will contains columns x0 to x1 and rows y0 to y1, all
     * inclusive.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &le; y1 and x0 &le; j &le; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * @param orig The original matrix which is to be copied.  Not modified.
     * @param x0 Start column.
     * @param x1 Stop column.
     * @param y0 Start row.
     * @param y1 Stop row.
     * @param sub Where the submatrix are stored.  Modified.
     */
    public static void extract( DenseMatrix64F orig ,
                                  int y0 , int y1 ,
                                  int x0 , int x1 ,
                                  DenseMatrix64F sub )
    {
        sub.reshape(y1-y0+1,x1-x0+1, false);
        int y_sub = 0;
        for( int y = y0; y <= y1; y++ ) {
            int x_sub = 0;
            for( int x = x0; x <= x1; x++ ,x_sub++) {
                double v = orig.get(y,x);
                sub.set(y_sub,x_sub,v);
            }
            y_sub++;
        }
    }

    /**
     * <p>
     * Creates a linear solver that is appropriate for a matrix of the specified size.<br>
     * <br>
     * Ax = b
     * </p>
     *
     * @param numRows number of rows in the A matrix.
     * @param numCols number of columns in the A matrix.
     * @return A linear solver.
     */
    public static LinearSolver createSolver( int numRows , int numCols ) {
        LinearSolver solver;

        if( numRows == numCols ) {
            LUDecompositionAlt alg = new LUDecompositionAlt();

            solver = new LinearSolverLu(alg);
        } else if( numRows > numCols ){
            solver = new LinearSolverQrHouseCol();
        } else {
            throw new IllegalArgumentException("Can't solve since there is an infinite number of solutions.");
        }

        return solver;
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
     * @param v0 First element in 'v' where the results are extracted to.
     * @param v Vector where the results are written to. Modified.
     */
    public static void subvector(DenseMatrix64F A, int rowA, int colA, int length , boolean row, int v0, DenseMatrix64F v) {
        if( row ) {
            for( int i = 0; i < length; i++ ) {
                v.data[v0+i] = A.get(rowA,colA+i);
            }
        } else {
            for( int i = 0; i < length; i++ ) {
                v.data[v0+i] = A.get(rowA+i,colA);
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
    public static DenseMatrix64F[] splitIntoVectors( DenseMatrix64F A , boolean column )
    {
        int w = column ? A.numCols : A.numRows;

        int M = column ? A.numRows : 1;
        int N = column ? 1 : A.numCols;

        int o = Math.max(M,N);

        DenseMatrix64F[] ret  = new DenseMatrix64F[w];

        for( int i = 0; i < w; i++ ) {
            DenseMatrix64F a = new DenseMatrix64F(M,N);

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
    public static DenseMatrix64F pivotMatrix(DenseMatrix64F ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new DenseMatrix64F(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            CommonOps.set(ret,0);
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
     * Inserts matrix 'src' into matrix 'dest' with the (0,0) of src at (row,col) in dest.
     *
     * @param src matrix that is being copied into dest
     * @param row starting location of the copy.
     * @param col starting location of the copy.
     * @param dest Where src is being copied into.
     */
    public static void insert(DenseMatrix64F src, int row, int col, DenseMatrix64F dest) {
        for( int i = row; i < row + src.numRows; i++ ) {
            for( int j = col; j < col + src.numCols; j++ ) {
                dest.set(i,j, src.get(i-row,j-col));
            }
        }
    }
}
