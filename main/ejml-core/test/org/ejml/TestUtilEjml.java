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

package org.ejml;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestUtilEjml {

    Random rand = new Random(23423);


    @Test
    public void max_array() {
        double a[] = new double[]{-1,2,3,4,5,6,3,4,5,7,8,2,3,-5,-6};

        assertTrue(8==UtilEjml.max(a,0,a.length));
        assertTrue(5==UtilEjml.max(a,6,3));
    }

    /**
     * Provide it a couple of different strings to parse.  Then compare
     * the results against the expect answer
     */
    @Test
    public void parse_DDRM() {
        String a = "-0.779094   1.682750\n" +
                 "   1.304014  -1.880739\n";

        DMatrixRMaj m = UtilEjml.parse_DDRM(a,2);

        assertEquals(2,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , UtilEjml.TEST_F64);
        assertEquals(1.682750  , m.get(0,1) , UtilEjml.TEST_F64);
        assertEquals(1.304014  , m.get(1,0) , UtilEjml.TEST_F64);
        assertEquals(-1.880739 , m.get(1,1) , UtilEjml.TEST_F64);

        // give it a matrix with a space in the first element, see if that screws it up
        a = " -0.779094   1.682750  5\n" +
           "   1.304014  -1.880739  8\n";

        m = UtilEjml.parse_DDRM(a,3);
        assertEquals(3,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , UtilEjml.TEST_F64);
    }

    @Test
    public void parse_DSCC() {
        String a = "-0.779094   1.682750\n" +
                "   0  -1.880739\n";

        DMatrixSparseCSC m = UtilEjml.parse_DSCC(a, 2);

        assertEquals(3,m.nz_length);

        assertEquals(2,m.numCols);
        assertEquals(2,m.numRows);
        assertEquals(-0.779094 , m.get(0,0) , UtilEjml.TEST_F64);
        assertEquals(1.682750  , m.get(0,1) , UtilEjml.TEST_F64);
        assertEquals(0         , m.get(1,0) , UtilEjml.TEST_F64);
        assertEquals(-1.880739 , m.get(1,1) , UtilEjml.TEST_F64);
    }

    @Test
    public void shuffle() {
        int m[] = new int[200];
        for (int i = 0; i < m.length; i++) {
            m[i] = i;
        }
        int N = m.length-5;
        UtilEjml.shuffle(m,N,0,40,rand);

        // end should be untouched
        for (int i = N; i < m.length; i++) {
            assertEquals(i,m[i]);
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
}
