/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestDMatrixSparseCSC extends GenericTestsDMatrixSparse {

    @Override
    public DMatrixSparse createSparse( int numRows, int numCols ) {
        return new DMatrixSparseCSC(numRows, numCols, 10);
    }

    @Override
    public DMatrixSparse createSparse( DMatrixSparseTriplet orig ) {
        return DConvertMatrixStruct.convert(orig, (DMatrixSparseCSC)null);
    }

    @Override
    public boolean isStructureValid( DMatrixSparse m ) {
        return CommonOps_DSCC.checkStructure((DMatrixSparseCSC)m);
    }

    @Test
    void constructor_veryLarge() {
        DMatrixSparseCSC a = new DMatrixSparseCSC(1_000_000_000, 100_000_000, 4);

        assertEquals(0, a.nz_length);
        assertEquals(1_000_000_000, a.numRows);
        assertEquals(100_000_000, a.numCols);
        assertEquals(4, a.nz_values.length);
        assertEquals(4, a.nz_rows.length);
    }

    @Test
    void growMaxLength_veryLarge() {
        DMatrixSparseCSC a = new DMatrixSparseCSC(1_000_000_000, 100_000_000, 4);

        a.growMaxLength(10, false);
        assertEquals(0, a.nz_length);
        assertEquals(1_000_000_000, a.numRows);
        assertEquals(100_000_000, a.numCols);
        assertEquals(10, a.nz_values.length);
        assertEquals(10, a.nz_rows.length);
    }

    @Test
    void reshape_row_col_length() {
        DMatrixSparseCSC a = new DMatrixSparseCSC(2, 3, 4);

        a.reshape(1, 2, 3);
        assertTrue(CommonOps_DSCC.checkStructure(a));
        assertEquals(1, a.numRows);
        assertEquals(2, a.numCols);
        assertEquals(4, a.nz_values.length);
        assertEquals(0, a.nz_length);

        a.reshape(4, 1, 10);
        assertTrue(CommonOps_DSCC.checkStructure(a));
        assertEquals(4, a.numRows);
        assertEquals(1, a.numCols);
        assertEquals(10, a.nz_values.length);
        assertEquals(0, a.nz_length);
    }

    @Test
    void sortIndices() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(5, 4, 20, -1, 1, rand);

        // make sure it's not sorted correctly
        a.nz_rows[0] = 2;
        a.nz_rows[2] = 0;
        assertFalse(CommonOps_DSCC.checkIndicesSorted(a));
        a.indicesSorted = false;

        // now sort it and see if its fixed
        a.sortIndices(null);

        assertTrue(CommonOps_DSCC.checkIndicesSorted(a));
        assertTrue(a.indicesSorted);
    }

    @Test
    void growMaxColumns() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(5, 4, 20, -1, 1, rand);
        a.col_idx[0] = 5;
        a.col_idx[1] = 15;

        // grow when resize isn't needed
        a.growMaxColumns(3, false);
        assertEquals(5, a.col_idx[0]);
        assertEquals(15, a.col_idx[1]);

        // shouldn't declare a new array
        a.growMaxColumns(4, false);
        assertEquals(5, a.col_idx[0]);
        assertEquals(15, a.col_idx[1]);

        // resize is needed now
        a.growMaxColumns(5, true);
        assertEquals(5, a.col_idx[0]);
        assertEquals(15, a.col_idx[1]);

        a.growMaxColumns(6, false);
        assertEquals(0, a.col_idx[0]);
        assertEquals(0, a.col_idx[1]);
    }

    /**
     * The matrix is already sorted. See if it is still sorted after set has been called.
     */
    @Test
    void set_sorted() {
        DMatrixSparseCSC a = new DMatrixSparseCSC(4, 5, 0);
        a.indicesSorted = true;

        a.set(1, 2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(a));

        a.set(0, 2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(a));

        a.set(3, 2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(a));

        a.set(2, 2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_DSCC.checkStructure(a));
    }
}
