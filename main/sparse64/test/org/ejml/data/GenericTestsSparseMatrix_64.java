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

package org.ejml.data;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public abstract class GenericTestsSparseMatrix_64 extends GenericTestsDenseMatrix_F64
{
    Random rand = new Random(234);

    public abstract Matrix_64 createSparse(SMatrixTriplet_64 orig , int numRows , int numCols );

    @Override
    protected Matrix_64 createMatrix(int numRows, int numCols) {

        // define a sparse matrix with every element filled.  It should act low a slow and inefficient
        // dense matrix now
        SMatrixTriplet_64 t = new SMatrixTriplet_64(numRows,numCols,numRows*numCols);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                t.add(row,col, rand.nextGaussian());
            }
        }

        return createSparse(t,numRows,numCols);
    }
}
