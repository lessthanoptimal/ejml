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

import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestFMatrixSparseCSC extends GenericTestsFMatrixSparse {

    @Override
    public FMatrixSparse createSparse(int numRows, int numCols) {
        return new FMatrixSparseCSC(numRows,numCols,10);
    }

    @Override
    public FMatrixSparse createSparse(FMatrixSparseTriplet orig) {
        return ConvertFMatrixStruct.convert(orig,(FMatrixSparseCSC)null);
    }

    @Override
    public boolean isStructureValid(FMatrixSparse m) {
        return CommonOps_FSCC.checkStructure((FMatrixSparseCSC)m);
    }

    @Test
    public void constructor_veryLarge() {
        FMatrixSparseCSC a = new FMatrixSparseCSC(1_000_000_000,100_000_000,4);

        assertEquals(0,a.nz_length);
        assertEquals(1_000_000_000,a.numRows);
        assertEquals(100_000_000,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(4,a.nz_rows.length);
    }

    @Test
    public void growMaxLength_veryLarge() {
        FMatrixSparseCSC a = new FMatrixSparseCSC(1_000_000_000,100_000_000,4);

        a.growMaxLength(10,false);
        assertEquals(0,a.nz_length);
        assertEquals(1_000_000_000,a.numRows);
        assertEquals(100_000_000,a.numCols);
        assertEquals(10,a.nz_values.length);
        assertEquals(10,a.nz_rows.length);
    }

    @Test
    public void reshape_row_col_length() {
        FMatrixSparseCSC a = new FMatrixSparseCSC(2,3,4);

        a.reshape(1,2,3);
        assertTrue(CommonOps_FSCC.checkStructure(a));
        assertEquals(1,a.numRows);
        assertEquals(2,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(0,a.nz_length);

        a.reshape(4,1,10);
        assertTrue(CommonOps_FSCC.checkStructure(a));
        assertEquals(4,a.numRows);
        assertEquals(1,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(0,a.nz_length);
    }

    @Test
    public void sortIndices() {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(5,4,20,-1,1,rand);

        // make sure it's not sorted correctly
        a.nz_rows[0]=2;
        a.nz_rows[2]=0;
        assertFalse(CommonOps_FSCC.checkIndicesSorted(a));
        a.indicesSorted = false;

        // now sort it and see if its fixed
        a.sortIndices(null);

        assertTrue(CommonOps_FSCC.checkIndicesSorted(a));
        assertTrue(a.indicesSorted);
    }

    @Test
    public void growMaxColumns() {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(5,4,20,-1,1,rand);
        a.col_idx[0] = 5;
        a.col_idx[1] = 15;

        // grow when resize isn't needed
        a.growMaxColumns(3,false);
        assertEquals(5,a.col_idx[0]);
        assertEquals(15,a.col_idx[1]);

        // shouldn't declare a new array
        a.growMaxColumns(4,false);
        assertEquals(5,a.col_idx[0]);
        assertEquals(15,a.col_idx[1]);

        // resize is needed now
        a.growMaxColumns(5,true);
        assertEquals(5,a.col_idx[0]);
        assertEquals(15,a.col_idx[1]);

        a.growMaxColumns(6,false);
        assertEquals(0,a.col_idx[0]);
        assertEquals(0,a.col_idx[1]);
    }

    /**
     * The matrix is already sorted.  See if it is still sorted after set has been called.
     */
    @Test
    public void set_sorted() {
        FMatrixSparseCSC a = new FMatrixSparseCSC(4,5,0);
        a.indicesSorted = true;

        a.set(1,2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(a));

        a.set(0,2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(a));

        a.set(3,2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(a));

        a.set(2,2, 1);
        assertTrue(a.indicesSorted);
        assertTrue(CommonOps_FSCC.checkStructure(a));
    }
}
