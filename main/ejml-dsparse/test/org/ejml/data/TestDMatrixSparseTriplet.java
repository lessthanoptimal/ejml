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
public class TestDMatrixSparseTriplet extends GenericDMatrixSparse {

    @Test
    public void constructor() {
        DMatrixSparseTriplet m = new DMatrixSparseTriplet(1,1,10);

        assertEquals(0,m.getLength());
        assertEquals(10,m.nz_data.length);
        for (int i = 0; i < m.nz_data.length; i++) {
            assertTrue(m.nz_data[i] != null);
        }
    }

    @Test
    public void growData() {
        DMatrixSparseTriplet m = new DMatrixSparseTriplet(1,1,10);

        m.growData(4);
        assertEquals(0,m.getLength());
        assertEquals(10,m.nz_data.length);

        m.growData(12);
        assertEquals(0,m.getLength());
        assertEquals(12,m.nz_data.length);
        for (int i = 0; i < m.nz_data.length; i++) {
            assertTrue(m.nz_data[i] != null);
        }
    }

    @Test
    public void addItem() {
        DMatrixSparseTriplet m = new DMatrixSparseTriplet(1,1,2);

        m.addItem(1,2,3);
        m.addItem(1,3,4);

        assertEquals(2,m.nz_length);
        assertEquals(2,m.nz_data.length);

        check(m.nz_data[0],1,2,3);
        check(m.nz_data[1],1,3,4);

        // now force it to grow
        m.addItem(2,3,5);
        assertEquals(3,m.nz_length);
        assertTrue(m.nz_data.length >= 3);

        check(m.nz_data[0],1,2,3);
        check(m.nz_data[1],1,3,4);
        check(m.nz_data[2],2,3,5);
    }

    private void check(DMatrixSparseTriplet.Element e , int row , int col , double value ) {
        assertEquals(row,e.row);
        assertEquals(col,e.col);
        assertEquals(value,e.value, UtilEjml.TEST_F64);
    }

    @Test
    public void findItem() {
        DMatrixSparseTriplet m = new DMatrixSparseTriplet(3,4, 5);

        m.addItem(1,2, 5);

        assertTrue( -1 == m.nz_index(0,1));
        check( m.nz_data[m.nz_index(1,2)], 1,2,5);
    }

    @Override
    public DMatrixSparse createSparse(int numRows, int numCols) {
        return new DMatrixSparseTriplet(numRows,numCols,10);
    }

    @Override
    public DMatrixSparse createSparse(DMatrixSparseTriplet orig) {
        return new DMatrixSparseTriplet(orig);
    }

    @Override
    public boolean isStructureValid(DMatrixSparse m) {
        return true;
    }
}
