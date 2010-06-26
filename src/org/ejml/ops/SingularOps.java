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
     *
     * <p>
     * TODO This is inefficient.  The shuffle should be computed first then each row copied at most once
     * </p>
     *
     * @param U Matrix. Modified.
     * @param S Diagonal matrix with singular values. Modified.
     * @param V Matrix. Modified.
     */
    public static void descendingOrder( DenseMatrix64F U , DenseMatrix64F S , DenseMatrix64F V )
    {
        int N = Math.min(S.numRows,S.numCols);

        for( int i = 0; i < N; i++ ) {
            double bigValue=-Double.MAX_VALUE;
            int bigIndex=-1;

            // find the smallest singular value in the submatrix
            for( int j = i; j < N; j++ ) {
                double v = S.get(j,j);

                if( v > bigValue ) {
                    bigValue = v;
                    bigIndex = j;
                }
            }

            // only swap if the current index is not the smallest
            if( bigIndex == i)
                continue;

            double tmp = S.get(i,i);
            S.set(i,i,bigValue);
            S.set(bigIndex,bigIndex,tmp);

            if( V != null ) {
                // swap the columns
                for( int row = 0; row < V.numRows; row++ ) {
                    tmp = V.get(row,i);
                    V.set(row,i,V.get(row,bigIndex));
                    V.set(row,bigIndex,tmp);
                }
            }

            if( U != null ) {
                // swap the columns
                for( int row = 0; row < U.numRows; row++ ) {
                    tmp = U.get(row,i);
                    U.set(row,i,U.get(row,bigIndex));
                    U.set(row,bigIndex,tmp);
                }
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
    public static DenseMatrix64F nullSpace( SingularValueDecomposition svd , DenseMatrix64F v )
    {
        int N = svd.numberOfSingularValues();
        double s[] = svd.getSingularValues();

        DenseMatrix64F V = svd.getV();

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

        // copy the row from v
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
