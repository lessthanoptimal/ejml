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

package org.ejml.data;

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestFMatrixSparseTriplet extends GenericTestsFMatrixSparse {

    @Test
    public void constructor() {
        FMatrixSparseTriplet m = new FMatrixSparseTriplet(1,1,10);

        assertEquals(0,m.getLength());
        assertEquals(10,m.nz_value.data.length);
        assertEquals(20,m.nz_rowcol.data.length);
    }

    @Test
    public void addItem() {
        FMatrixSparseTriplet m = new FMatrixSparseTriplet(1,1,2);

        m.addItem(1,2,3);
        m.addItem(1,3,4);

        assertEquals(2,m.nz_length);
        assertTrue(2 <= m.nz_value.data.length);
        assertTrue(4 <= m.nz_rowcol.data.length);

        check(m,0,1,2,3);
        check(m,1,1,3,4);

        // now force it to grow
        m.addItem(2,3,5);
        assertEquals(3,m.nz_length);
        assertTrue(m.nz_value.data.length >= 3);

        check(m,0,1,2,3);
        check(m,1,1,3,4);
        check(m,2,2,3,5);
    }

    private void check(FMatrixSparseTriplet m , int index , int row , int col , float value ) {
        assertEquals(row,m.nz_rowcol.data[index*2]);
        assertEquals(col,m.nz_rowcol.data[index*2+1]);
        assertEquals(value,m.nz_value.data[index], UtilEjml.TEST_F32);
    }

    @Test
    public void findItem() {
        FMatrixSparseTriplet m = new FMatrixSparseTriplet(3,4, 5);

        m.addItem(1,2, 5);

        assertEquals(-1, m.nz_index(0, 1));
        check( m, m.nz_index(1,2), 1,2,5);
    }

    @Override
    public FMatrixSparse createSparse(int numRows, int numCols) {
        return new FMatrixSparseTriplet(numRows,numCols,10);
    }

    @Override
    public FMatrixSparse createSparse(FMatrixSparseTriplet orig) {
        return new FMatrixSparseTriplet(orig);
    }

    @Override
    public boolean isStructureValid(FMatrixSparse m) {
        return true;
    }
}
