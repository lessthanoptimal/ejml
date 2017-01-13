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

import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestSMatrixCC_F64 extends GenericTestsSparseMatrix_F64 {

    public TestSMatrixCC_F64() {
        assignable = false;
    }

    @Override
    public Matrix_F64 createSparse(int numRows, int numCols) {
        return new SMatrixCmpC_F64(numRows,numCols,10);
    }

    @Override
    public Matrix_F64 createSparse(SMatrixTriplet_F64 orig) {
        return ConvertSparseMatrix_F64.convert(orig,(SMatrixCmpC_F64)null);
    }

    @Test
    public void isRowOrderValid() {
        SMatrixTriplet_F64 orig = new SMatrixTriplet_F64(3,5,6);

        orig.addItem(0,0, 5);
        orig.addItem(1,0, 6);
        orig.addItem(2,0, 7);

        orig.addItem(1,2, 5);

        SMatrixCmpC_F64 a = ConvertSparseMatrix_F64.convert(orig,(SMatrixCmpC_F64)null);

        // test positive case first
        assertTrue(a.isRowOrderValid());

        // test negative case second
        a.nz_rows[1] = 3;
        assertFalse(a.isRowOrderValid());
    }

    @Test
    public void reshape_row_col_length() {
        SMatrixCmpC_F64 a = new SMatrixCmpC_F64(2,3,4);

        a.reshape(1,2,3);
        assertEquals(1,a.numRows);
        assertEquals(2,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(3,a.length);

        a.reshape(4,1,10);
        assertEquals(4,a.numRows);
        assertEquals(1,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(4,a.length);
    }
}
