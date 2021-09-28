/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixSparseTriplet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_DSTL extends EjmlStandardJUnit {
    int numRows = 6;
    int numCols = 7;

    @Test
    public void uniform() {

        DMatrixSparseTriplet a = RandomMatrices_DSTL.uniform(numRows,numCols,10,-1,1,rand);

        assertEquals(numRows,a.numRows);
        assertEquals(numCols,a.numCols);
        assertEquals(10,a.nz_length);

        int count = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                double value = a.get(row,col);

                if( value == 0 )
                    continue;
                if( value > 1 || value < -1 )
                    fail("Out of expected range");
                count++;
            }
        }

        assertEquals(10, count);
    }
}
