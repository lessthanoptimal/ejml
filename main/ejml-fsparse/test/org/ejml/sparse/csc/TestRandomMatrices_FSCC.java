/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixSparseCSC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_FSCC {
    Random rand = new Random(324);

    int numRows = 6;
    int numCols = 7;

    @Test
    public void uniform() {

        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(numRows,numCols,10,-1,1,rand);

        assertEquals(numRows,a.numRows);
        assertEquals(numCols,a.numCols);
        assertEquals(10,a.nz_length);
        assertTrue(CommonOps_FSCC.checkStructure(a));

        int count = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                float value = a.get(row,col);

                if( value == 0 )
                    continue;
                if( value > 1 || value < -1 )
                    fail("Out of expected range");
                count++;
            }
        }

        assertEquals(10, count);
        assertTrue(CommonOps_FSCC.checkSortedFlag(a));
    }

    @Test
    public void createLowerTriangular() {
        FMatrixSparseCSC L;
        for (int trial = 0; trial < 20; trial++) {
            for( int length : new int[]{0,2,6,12,20} ) {
                L = RandomMatrices_FSCC.triangleLower(6, 0, length,-1,1, rand);

                assertEquals(Math.max(6,length),L.nz_length);
                assertTrue(CommonOps_FSCC.checkStructure(L));
                assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,0, 0.0f ));

                L = RandomMatrices_FSCC.triangleLower(6, 1, length,-1,1, rand);
                assertEquals(Math.max(5,length),L.nz_length);
                assertTrue(CommonOps_FSCC.checkStructure(L));
                assertTrue(MatrixFeatures_FSCC.isLowerTriangle(L,1, 0.0f ));

                assertFalse(CommonOps_FSCC.checkDuplicateElements(L));
            }
        }
    }

    @Test
    public void symmetric() {
        for (int N = 1; N <= 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)(N*N*0.5f*(rand.nextFloat()*0.5f+0.1f)+0.5f);
                FMatrixSparseCSC A = RandomMatrices_FSCC.symmetric(N,  nz,-1,1, rand);

                assertTrue(CommonOps_FSCC.checkStructure(A));

                // Check the matrix properties
                assertTrue(MatrixFeatures_FSCC.isSymmetric(A,UtilEjml.TEST_F32));
            }
        }
    }

    @Test
    public void symmetricPosDef() {
        for (int N = 1; N <= 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                FMatrixSparseCSC A = RandomMatrices_FSCC.symmetricPosDef(N,0.25f,rand);

                assertTrue(CommonOps_FSCC.checkStructure(A));

                assertTrue(MatrixFeatures_FSCC.isPositiveDefinite(A));
            }
        }
    }
}
