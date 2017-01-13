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

import static org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64.colsum;

/**
 * @author Peter Abeles
 */
public class RandomMatrices_O64 {

    /**
     * Randomly generates matrix with the specified number of matrix elements filled with values from min to max.
     *
     * @param numRows
     * @param numCols
     * @param length
     * @param min
     * @param max
     * @param rand
     * @return
     */
    public static SMatrixCmpC_F64 uniform(int numRows , int numCols , int length ,
                                          double min , double max , Random rand ) {

        // Create a list of all the possible element values
        int N = numCols*numRows;
        if( N < 0 )
            throw new IllegalArgumentException("matrix size is too large");
        length = Math.min(N,length);

        int selected[] = new int[N];
        for (int i = 0; i < N; i++) {
            selected[i] = i;
        }

        for (int i = 0; i < length; i++) {
            int swapIdx = rand.nextInt(N);
            int tmp = selected[swapIdx];
            selected[swapIdx] = selected[i];
            selected[i] = tmp;
        }

        // put the indexes in order
        Arrays.sort(selected,0,length);

        SMatrixCmpC_F64 ret = new SMatrixCmpC_F64(numRows,numCols,length);

        // compute the number of elements in each column
        int hist[] = new int[ numCols ];
        for (int i = 0; i < length; i++) {
            hist[selected[i]/numRows]++;
        }

        // define col_idx
        colsum(ret,hist);

        for (int i = 0; i < length; i++) {
            int row = selected[i]%numRows;

            ret.nz_rows[i] = row;
            ret.nz_values[i] = rand.nextDouble()*(max-min)+min;
        }

        return ret;
    }

    public static SMatrixCmpC_F64 uniform(int numRows , int numCols , int length , Random rand ) {
        return uniform(numRows, numCols, length, -1,1,rand);
    }
}
