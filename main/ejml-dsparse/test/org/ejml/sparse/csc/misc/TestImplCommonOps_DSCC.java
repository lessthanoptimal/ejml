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

package org.ejml.sparse.csc.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplCommonOps_DSCC {

    Random rand = new Random(324);

    @Test
    public void transpose() {
        for (int rows = 1; rows <= 10; rows++) {
            for (int cols = 1; rows <= 10; rows++) {
                for (int mc = 0; mc < 20; mc++) {
                    int N =(int) Math.round(rows*cols*rand.nextDouble());
                    DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(rows,cols,N, -1, 1, rand);
                    DMatrixSparseCSC b = new DMatrixSparseCSC(0,0,0);

                    ImplCommonOps_DSCC.transpose(a,b,null);
                    assertEquals(cols,b.numRows);
                    assertEquals(rows,b.numCols);

                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            double expected = a.get(row,col);
                            double found = b.get(col,row);

                            assertEquals(row+" "+col,expected, found, UtilEjml.TEST_F64);
                        }
                    }
                    assertTrue(CommonOps_DSCC.checkSortedFlag(b));
                }
            }
        }
    }

    @Test
    public void add() {
        double alpha = 1.5;
        double beta = 2.3;

        for( int numRows : new int[]{2,4,6,10}) {
            for( int numCols : new int[]{2,4,6,10}) {
                DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows,numCols,7, -1, 1, rand);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(numRows,numCols,8, -1, 1, rand);
                DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(numRows,numCols,3, -1, 1, rand);

                ImplCommonOps_DSCC.add(alpha,a,beta,b,c, null, null);

                for (int row = 0; row < numRows; row++) {
                    for (int col = 0; col < numCols; col++) {
                        double valA = a.get(row,col);
                        double valB = b.get(row,col);
                        double found = alpha*valA + beta*valB;

                        double expected = c.get(row,col);

                        assertEquals(row+" "+col,expected, found, UtilEjml.TEST_F64);
                    }
                }
                assertTrue(CommonOps_DSCC.checkStructure(c));
            }
        }
    }

    @Test
    public void addColAppend() {
        double alpha = 1.5;
        double beta = 2.3;

        for( int numRows : new int[]{2,4,6,10}) {
            DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows,3,15, -1, 1, rand);
            DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(numRows,4,15, -1, 1, rand);
            DMatrixSparseCSC c = new DMatrixSparseCSC(numRows,0,0);


            ImplCommonOps_DSCC.addColAppend(alpha,a,1,beta,b,0,c, null);
            assertTrue(CommonOps_DSCC.checkStructure(c));
            assertEquals(1,c.numCols);

            for (int row = 0; row < numRows; row++) {
                double valA = a.get(row,1);
                double valB = b.get(row,0);
                double found = alpha*valA + beta*valB;

                double expected = c.get(row,0);
                assertEquals(expected, found, UtilEjml.TEST_F64);
            }

            ImplCommonOps_DSCC.addColAppend(alpha,a,2,beta,b,1,c, null);
            assertTrue(CommonOps_DSCC.checkStructure(c));
            assertEquals(2,c.numCols);

            for (int row = 0; row < numRows; row++) {
                double valA = a.get(row,2);
                double valB = b.get(row,1);
                double found = alpha*valA + beta*valB;

                double expected = c.get(row,1);
                assertEquals(expected, found, UtilEjml.TEST_F64);
            }
        }
    }
}
