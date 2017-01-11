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

import org.ejml.data.SMatrixTriplet_F64;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class RandomMatrices_T64 {
    /**
     * Randomly generates matrix with the specified number of matrix elements filled with values from min to max.
     *
     * @param numRows Number of rows
     * @param numCols Number of columns
     * @param length Number of elements filled in
     * @param min Minimum value
     * @param max maximum value
     * @param rand Random number generated
     * @return Randomly generated matrix
     */
    public static SMatrixTriplet_F64 uniform(int numRows , int numCols , int length ,
                                             double min , double max , Random rand ) {
        // Create a list of all the possible element values
        int N = numCols*numRows;
        int selected[] = new int[N];
        for (int i = 0; i < N; i++) {
            selected[i] = i;
        }

        // select elements to NOT be in the output sparse matrix and move to the end of the list
        int M = N-length;
        for (int i = 0; i < M; i++) {
            int s = rand.nextInt(N-i);
            int tmp = selected[s];
            selected[s] = selected[N-i-1];
            selected[N-i-1] = tmp;
        }

        // Create a sparse matrix
        SMatrixTriplet_F64 ret = new SMatrixTriplet_F64(numRows,numCols,length);

        for (int i = 0; i < length; i++) {
            int row = selected[i]/numCols;
            int col = selected[i]%numCols;

            double value = rand.nextDouble()*(max-min)+min;

            ret.addItem(row,col, value);
        }

        return ret;
    }
}
