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

import org.ejml.UtilEjml;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSMatrixTriplet_F64 {

    @Test
    public void constructor() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(10);

        assertEquals(0,m.getLength());
        assertEquals(10,m.data.length);
        for (int i = 0; i < m.data.length; i++) {
            assertTrue(m.data[i] != null);
        }
    }

    @Test
    public void grow() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(10);

        m.growData(4);
        assertEquals(0,m.getLength());
        assertEquals(10,m.data.length);

        m.growData(12);
        assertEquals(0,m.getLength());
        assertEquals(12,m.data.length);
        for (int i = 0; i < m.data.length; i++) {
            assertTrue(m.data[i] != null);
        }
    }

    @Test
    public void add() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(2);

        m.add(1,2,3);
        m.add(1,3,4);

        assertEquals(2,m.length);
        assertEquals(2,m.data.length);

        check(m.data[0],1,2,3);
        check(m.data[1],1,3,4);

        // now force it to grow
        m.add(2,3,5);
        assertEquals(3,m.length);
        assertTrue(m.data.length >= 3);

        check(m.data[0],1,2,3);
        check(m.data[1],1,3,4);
        check(m.data[2],2,3,5);
    }

    private void check(SMatrixTriplet_F64.Element e , int row , int col , double value ) {
        assertEquals(row,e.row);
        assertEquals(col,e.col);
        assertEquals(value,e.value, UtilEjml.TEST_F64);
    }

    @Test
    public void find() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(3,4, 5);

        m.add(1,2, 5);

        assertTrue( null == m.find(0,1));
        check( m.find(1,2), 1,2,5);
    }

    @Test
    public void set() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(3,4, 5);

        m.set( 1, 2, 10);
        m.set( 1, 2, 15);

        assertEquals(15, m.get(1,2), UtilEjml.TEST_F64);
    }

    @Test
    public void get() {
        SMatrixTriplet_F64 m = new SMatrixTriplet_F64(3,4, 5);

        m.add(1,2, 5);
        m.add(1,2, 7); // should return the first it finds

        for (int row = 0; row < m.numRows; row++) {
            for (int col = 0; col < m.numCols; col++) {
                double found = m.get(row,col);
                if( row == 1 && col == 2)
                    assertEquals(5, found, UtilEjml.TEST_F64);
                else
                    assertEquals(0, found, UtilEjml.TEST_F64);
            }
        }
    }
}
