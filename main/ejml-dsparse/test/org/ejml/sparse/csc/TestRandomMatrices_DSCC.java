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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_DSCC {
    Random rand = new Random(324);

    int numRows = 6;
    int numCols = 7;

    @Test
    public void shuffle() {
        int m[] = new int[200];
        for (int i = 0; i < m.length; i++) {
            m[i] = i;
        }
        int N = m.length-5;
        RandomMatrices_DSCC.shuffle(m,N,40,rand);

        // end should be untouched
        for (int i = N; i < m.length; i++) {
            assertEquals(i,m[i]);
        }

        // should be sorted
        for (int i = 1; i < 40; i++) {
            assertTrue(m[i-1] < m[i]);
        }

        // the order should be drastically changed
        int numOrdered = 0;
        for (int i = 0; i < 40; i++) {
            if( m[i] == i ) {
                numOrdered++;
            }
        }
        assertTrue(numOrdered<10);

    }

    @Test
    public void uniform() {

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows,numCols,10,-1,1,rand);

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
        assertTrue(CommonOps_DSCC.checkSortedFlag(a));
    }

    @Test
    public void createLowerTriangular() {

        DMatrixSparseCSC L;
        for (int trial = 0; trial < 20; trial++) {
            for( int length : new int[]{0,2,6,12,20} ) {
                L = RandomMatrices_DSCC.triangleLower(6, 0, length,-1,1, rand);
//            L.print();
                assertEquals(Math.max(6,length),L.nz_length);
                assertTrue(CommonOps_DSCC.checkSortedFlag(L));
                assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,0, UtilEjml.TEST_F64));

                L = RandomMatrices_DSCC.triangleLower(6, 1, length,-1,1, rand);
//                L.print();
                assertEquals(Math.max(5,length),L.nz_length);
                assertTrue(CommonOps_DSCC.checkSortedFlag(L));
                assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L,1, UtilEjml.TEST_F64));

                assertFalse(CommonOps_DSCC.checkDuplicateElements(L));
            }

        }
    }

    @Test
    public void symmetric() {
        for (int N = 1; N <= 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)(N*N*0.5*(rand.nextDouble()*0.5+0.1)+0.5);
                DMatrixSparseCSC A = RandomMatrices_DSCC.symmetric(N,  nz,-1,1, rand);

                // Check to see if the matrix is setup correctly
                assertTrue(CommonOps_DSCC.checkSortedFlag(A)); // doesn't matter if it's sorted or not..
                assertFalse(CommonOps_DSCC.checkDuplicateElements(A));

                // Check the matrix properties
                assertTrue(MatrixFeatures_DSCC.isSymmetric(A,UtilEjml.TEST_F64));
            }
        }
    }
}
