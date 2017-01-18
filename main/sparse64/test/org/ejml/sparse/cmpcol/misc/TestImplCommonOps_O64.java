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

package org.ejml.sparse.cmpcol.misc;

import org.ejml.UtilEjml;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.sparse.cmpcol.CommonOps_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplCommonOps_O64 {

    Random rand = new Random(324);

    int numRows = 6;
    int numCols = 7;
    int length = 15;

    @Test
    public void transpose() {
        SMatrixCmpC_F64 a = RandomMatrices_O64.uniform(numRows,numCols,length, -1, 1, rand);
        SMatrixCmpC_F64 b = RandomMatrices_O64.uniform(numCols,numRows,length, -1, 1, rand);

        ImplCommonOps_O64.transpose(a,b,null);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                double expected = a.get(row,col);
                double found = b.get(col,row);

                assertEquals(row+" "+col,expected, found, UtilEjml.TEST_F64);
            }
        }
        assertTrue(CommonOps_O64.checkSortedFlag(b));
    }

    @Test
    public void add() {
        double alpha = 1.5;
        double beta = 2.3;

        for( int numRows : new int[]{2,4,6,10}) {
            for( int numCols : new int[]{2,4,6,10}) {
                SMatrixCmpC_F64 a = RandomMatrices_O64.uniform(numRows,numCols,7, -1, 1, rand);
                SMatrixCmpC_F64 b = RandomMatrices_O64.uniform(numRows,numCols,8, -1, 1, rand);
                SMatrixCmpC_F64 c = RandomMatrices_O64.uniform(numRows,numCols,3, -1, 1, rand);

                ImplCommonOps_O64.add(alpha,a,beta,b,c,null);

                for (int row = 0; row < numRows; row++) {
                    for (int col = 0; col < numCols; col++) {
                        double valA = a.get(row,col);
                        double valB = b.get(row,col);
                        double found = alpha*valA + beta*valB;

                        double expected = c.get(row,col);

                        assertEquals(row+" "+col,expected, found, UtilEjml.TEST_F64);
                    }
                }
                assertTrue(CommonOps_O64.checkSortedFlag(c));
            }
        }
    }
}
