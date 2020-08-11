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

package org.ejml.sparse.triplet;

import org.ejml.data.FMatrixSparseTriplet;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class RandomMatrices_FSTL {
    /**
     * Randomly generates matrix with the specified number of matrix elements filled with values from min to max.
     *
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @param nz_total Total number of non-zero elements in the matrix
     * @param min Minimum value
     * @param max maximum value
     * @param rand Random number generated
     * @return Randomly generated matrix
     */
    public static FMatrixSparseTriplet uniform(int numRows , int numCols , int nz_total ,
                                               float min , float max , Random rand ) {
        // Create a list of all the possible element values
        int N = numCols*numRows;
        if( N < 0 )
            throw new IllegalArgumentException("matrix size is too large");
        nz_total = Math.min(N,nz_total);

        int selected[] = new int[N];
        for (int i = 0; i < N; i++) {
            selected[i] = i;
        }

        for (int i = 0; i < nz_total; i++) {
            int s = rand.nextInt(N);
            int tmp = selected[s];
            selected[s] = selected[i];
            selected[i] = tmp;
        }

        // Create a sparse matrix
        FMatrixSparseTriplet ret = new FMatrixSparseTriplet(numRows,numCols,nz_total);

        for (int i = 0; i < nz_total; i++) {
            int row = selected[i]/numCols;
            int col = selected[i]%numCols;

            float value = rand.nextFloat()*(max-min)+min;

            ret.addItem(row,col, value);
        }

        return ret;
    }
}
