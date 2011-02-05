/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;


/**
 * Operations related to singular value decomposition.
 *
 * @author Peter Abeles
 */
public class SingularOps {

    /**
     * <p>
     * Adjusts the matrices so that the singular values are in descending order.
     * </p>
     *
     * <p>
     * In most implementations of SVD the singular values are automatically arranged in in descending
     * order.  In EJML this is not the case since it is often not needed and some computations can
     * be saved by not doing that.
     * </p>

     * @param U Matrix. Modified.
     * @param tranU is U transposed or not.
     * @param W Diagonal matrix with singular values. Modified.
     * @param V Matrix. Modified.
     * @param tranV is V transposed or not.
     */
    // TODO the number of copies can probably be reduced here
    public static void descendingOrder( DenseMatrix64F U , boolean tranU ,
                                        DenseMatrix64F W ,
                                        DenseMatrix64F V , boolean tranV )
    {
        int numSingular = Math.min(W.numRows,W.numCols);

        checkSvdMatrixSize(U, tranU, W, V, tranV);

        for( int i = 0; i < numSingular; i++ ) {
            double bigValue=-1;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < numSingular; j++ ) {
                double v = W.get(j,j);

                if( v > bigValue ) {
                    bigValue = v;
                    bigIndex = j;
                }
            }

            // only swap if the current index is not the smallest
            if( bigIndex == i)
                continue;

            if( bigIndex == -1 ) {
                // there is at least one uncountable singular value.  just stop here
                break;
            }

            double tmp = W.get(i,i);
            W.set(i,i,bigValue);
            W.set(bigIndex,bigIndex,tmp);

            if( V != null ) {
                swapRowOrCol(V, tranV, i, bigIndex);
            }

            if( U != null ) {
                swapRowOrCol(U, tranU, i, bigIndex);
            }
        }
    }

    /**
     * Checks to see if all the provided matrices are the expected size for an SVD.  If an error is encounted
     * then an exception is thrown.  This automatically handles compact and non-compact formats
     */
    public static void checkSvdMatrixSize(DenseMatrix64F U, boolean tranU, DenseMatrix64F W, DenseMatrix64F V, boolean tranV ) {
        int numSingular = Math.min(W.numRows,W.numCols);
        boolean compact = W.numRows == W.numCols;

        if( compact ) {
            if( U != null ) {
                if( tranU && U.numRows != numSingular )
                    throw new IllegalArgumentException("Unexpected size of matrix U");
                else if( !tranU && U.numCols != numSingular )
                    throw new IllegalArgumentException("Unexpected size of matrix U");
            }

            if( V != null ) {
            if( tranV && V.numRows != numSingular )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            else if( !tranV && V.numCols != numSingular )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            }
        } else {
            if( U != null && U.numRows != U.numCols )
                throw new IllegalArgumentException("Unexpected size of matrix U");
            if( V != null && V.numRows != V.numCols )
                throw new IllegalArgumentException("Unexpected size of matrix V");
            if( U != null && U.numRows != W.numRows )
                throw new IllegalArgumentException("Unexpected size of W");
            if( V != null && V.numRows != W.numCols )
                throw new IllegalArgumentException("Unexpected size of W");
        }
    }

    private static void swapRowOrCol(DenseMatrix64F M, boolean tran, int i, int bigIndex) {
        double tmp;
        if( tran ) {
            // swap the rows
            for( int col = 0; col < M.numCols; col++ ) {
                tmp = M.get(i,col);
                M.set(i,col,M.get(bigIndex,col));
                M.set(bigIndex,col,tmp);
            }
        } else {
            // swap the columns
            for( int row = 0; row < M.numRows; row++ ) {
                tmp = M.get(row,i);
                M.set(row,i,M.get(row,bigIndex));
                M.set(row,bigIndex,tmp);
            }
        }
    }

    /**
     * <p>
     * Computes the null space from the provided singular value.  The null space is found by
     * finding the row in V associated with the smallest singular value and it is assumed that there
     * is only one singular value close to zero.
     * </p>
     * <p>
     * No sanity check is done
     * to ensure that the smallest singular value is sufficiently small.  Depending on the matrix's
     * dimension a non-compact SVD might be required.
     * </p>
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param v Where the null space is written to. If null then a new matrix is declared.  Modified.
     * @return The null space.
     */
    public static DenseMatrix64F nullSpace( SingularValueDecomposition<DenseMatrix64F> svd , DenseMatrix64F v )
    {
        int N = svd.numberOfSingularValues();
        double s[] = svd.getSingularValues();

        DenseMatrix64F V = svd.getV(false);

        if( V.numCols != svd.numCols() ) {
            throw new IllegalArgumentException("Can't compute the null space using a compact SVD for a matrix of this size.");
        }

        if( v == null ) {
            v = new DenseMatrix64F(svd.numCols(),1);
        }

        // find the smallest singular value
        double smallestValue = Double.MAX_VALUE;
        int smallestIndex = -1;

        for( int i = 0; i < N; i++ ) {
            if( s[i] < smallestValue ) {
                smallestValue = s[i];
                smallestIndex = i;
            }
        }

        // copy the column from v
        SpecializedOps.subvector(V,0,smallestIndex,V.numCols,false,0,v);

        return v;
    }

    /**
     * Extracts the rank of a matrix using a preexisting decomposition.
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The rank of the decomposed matrix.
     */
    public static int rank( SingularValueDecomposition svd , double threshold ) {
        int numRank=0;

        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        for( int j = 0; j < N; j++ ) {
            if( w[j] > threshold)
                numRank++;
        }

        return numRank;
    }

    /**
     * Extracts the nullity of a matrix using a preexisting decomposition.
     *
     * @param svd A precomputed decomposition.  Not modified.
     * @param threshold Tolerance used to determine of a singular value is singular.
     * @return The nullity of the decomposed matrix.
     */
    public static int nullity( SingularValueDecomposition svd , double threshold ) {
        int ret = 0;

        double w[]= svd.getSingularValues();

        int N = svd.numberOfSingularValues();

        for( int j = 0; j < N; j++ ) {
            if( w[j] <= threshold) ret++;
        }
        return ret;
    }

}
