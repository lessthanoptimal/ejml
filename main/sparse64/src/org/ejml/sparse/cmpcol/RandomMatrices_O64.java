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

import org.ejml.data.SMatrixCC_F64;
import org.ejml.data.SMatrixTriplet_F64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.ejml.sparse.triplet.RandomMatrices_T64;

import java.util.Random;

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
    public static SMatrixCC_F64 uniform( int numRows , int numCols , int length ,
                                         double min , double max , Random rand ) {

        // easier to generate the matrix in triplet format first then convert it
        SMatrixTriplet_F64 triplet = RandomMatrices_T64.uniform(numRows, numCols, length, min, max, rand);
        return ConvertSparseMatrix_F64.convert(triplet, (SMatrixCC_F64)null, null, null);
    }

    public static SMatrixCC_F64 uniform( int numRows , int numCols , int length , Random rand ) {
        return uniform(numRows, numCols, length, -1,1,rand);
    }
}
