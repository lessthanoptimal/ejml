/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.cmpcol;

import org.ejml.data.SMatrixCmpC_F64;

import java.util.Arrays;
import java.util.Random;

import static org.ejml.sparse.cmpcol.misc.ImplCommonOps_DSCC.colsum;

/**
 * @author Peter Abeles
 */
public class RandomMatrices_DSCC {

    /**
     * Randomly generates matrix with the specified number of non-zero elements filled with values from min to max.
     *
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @param nz_total Total number of non-zero elements in the matrix
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static SMatrixCmpC_F64 rectangle(int numRows , int numCols , int nz_total ,
                                            double min , double max , Random rand ) {

        nz_total = Math.min(numCols*numRows,nz_total);
        int[] selected = selectElements(numRows*numCols, nz_total, rand);

        SMatrixCmpC_F64 ret = new SMatrixCmpC_F64(numRows,numCols,nz_total);
        ret.indicesSorted = true;

        // compute the number of elements in each column
        int hist[] = new int[ numCols ];
        for (int i = 0; i < nz_total; i++) {
            hist[selected[i]/numRows]++;
        }

        // define col_idx
        colsum(ret,hist);

        for (int i = 0; i < nz_total; i++) {
            int row = selected[i]%numRows;

            ret.nz_rows[i] = row;
            ret.nz_values[i] = rand.nextDouble()*(max-min)+min;
        }

        return ret;
    }

    private static int[] selectElements(int N, int nz_total, Random rand) {
        // Create a list of all the possible element values
        if( N < 0 )
            throw new IllegalArgumentException("matrix size is too large");

        int selected[] = new int[N];
        for (int i = 0; i < N; i++) {
            selected[i] = i;
        }

        for (int i = 0; i < nz_total; i++) {
            int swapIdx = rand.nextInt(N);
            int tmp = selected[swapIdx];
            selected[swapIdx] = selected[i];
            selected[i] = tmp;
        }

        // put the indexes in order
        Arrays.sort(selected,0,nz_total);
        return selected;
    }

    public static SMatrixCmpC_F64 rectangle(int numRows , int numCols , int nz_total , Random rand ) {
        return rectangle(numRows, numCols, nz_total, -1,1,rand);
    }

    /**
     * Randomly generates lower triangular (or hessenberg) matrix with the specified number of of non-zero
     * elements.  The diagonal elements must be non-zero.
     *
     * @param dimen Number of rows and columns
     * @param hessenberg Hessenberg degree. 0 is triangular and 1 or more is Hessenberg.
     * @param nz_total Total number of non-zero elements in the matrix.  Adjust to meet matrix size constraints.
     * @param min Minimum element value, inclusive
     * @param max Maximum element value, inclusive
     * @param rand Random number generator
     * @return Randomly generated matrix
     */
    public static SMatrixCmpC_F64 triangleLower(int dimen , int hessenberg, int nz_total,
                                                double min , double max , Random rand ) {

        // number of elements which are along the diagonal
        int diag_total = dimen-hessenberg;

        // pre compute element count in each row
        int rowStart[] = new int[dimen];
        int rowEnd[] = new int[dimen];
        // diagonal is manditory and these indexes refer to a triangle -1 dimension
        int N = 0;
        for (int i = 0; i < dimen; i++) {
            if( i < dimen-1+hessenberg) rowStart[i] = N;
            N += i < hessenberg ? dimen : dimen-1-i+hessenberg;;
            if( i < dimen-1+hessenberg) rowEnd[i] = N;
        }
        N += dimen-hessenberg;

        // contrain the total number of non-zero elements
        nz_total = Math.min(N,nz_total);
        nz_total = Math.max(diag_total,nz_total);

        // number of elements which are not the diagonal elements
        int off_total = nz_total-diag_total;

        int[] selected = selectElements(N-diag_total, off_total, rand);

        SMatrixCmpC_F64 L = new SMatrixCmpC_F64(dimen,dimen,nz_total);

        // compute the number of elements in each column
        int hist[] = new int[ dimen ];
        int s_index = 0;
        for (int col = 0; col < dimen; col++) {
            if( col >= hessenberg )
                hist[col]++;
            while( s_index < off_total && selected[s_index] < rowEnd[col]  ) {
                hist[col]++;
                s_index++;
            }
        }

        // define col_idx
        colsum(L,hist);

        int nz_index = 0;
        s_index = 0;
        for (int col = 0; col < dimen; col++) {
            int offset = col >= hessenberg ? col - hessenberg+1 : 0;

            // assign the diagonal element a value
            if( col >= hessenberg ) {
                L.nz_rows[nz_index] = col-hessenberg;
                L.nz_values[nz_index++] = rand.nextDouble() * (max - min) + min;
            }

            // assign the other elements values
            while( s_index < off_total && selected[s_index] < rowEnd[col]  ) {
                // the extra + 1 is because random elements were not allowed along the diagonal
                int row = selected[s_index++] - rowStart[col] + offset;

                L.nz_rows[nz_index] = row;
                L.nz_values[nz_index++] = rand.nextDouble()*(max-min)+min;
            }
        }

        return L;
    }

    public static SMatrixCmpC_F64 triangleUpper(int dimen , int hessenberg, int nz_total,
                                                double min , double max , Random rand ) {
        SMatrixCmpC_F64 L = triangleLower(dimen, hessenberg, nz_total, min, max, rand);
        SMatrixCmpC_F64 U = L.createLike();

        CommonOps_DSCC.transpose(L,U,null);
        return U;
    }
}
