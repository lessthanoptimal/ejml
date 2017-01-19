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

import org.ejml.ops.ConvertDMatrixSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestDMatrixSparseCSC extends GenericTestsDMatrixSparse {

    @Override
    public DMatrixSparse createSparse(int numRows, int numCols) {
        return new DMatrixSparseCSC(numRows,numCols,10);
    }

    @Override
    public DMatrixSparse createSparse(DMatrixSparseTriplet orig) {
        return ConvertDMatrixSparse.convert(orig,(DMatrixSparseCSC)null);
    }

    @Override
    public boolean isStructureValid(DMatrixSparse m) {
        return true;
    }

    @Test
    public void reshape_row_col_length() {
        DMatrixSparseCSC a = new DMatrixSparseCSC(2,3,4);

        a.reshape(1,2,3);
        assertEquals(1,a.numRows);
        assertEquals(2,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(3,a.nz_length);

        a.reshape(4,1,10);
        assertEquals(4,a.numRows);
        assertEquals(1,a.numCols);
        assertEquals(4,a.nz_values.length);
        assertEquals(4,a.nz_length);
    }

    @Test
    public void sortIndices() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(5,4,20,-1,1,rand);

        // make sure it's not sorted correctly
        a.nz_rows[0]=2;
        a.nz_rows[2]=0;
        assertFalse(CommonOps_DSCC.checkIndicesSorted(a));
        a.indicesSorted = false;

        // now sort it and see if its fixed
        a.sortIndices(null);

        assertTrue(CommonOps_DSCC.checkIndicesSorted(a));
        assertTrue(a.indicesSorted);
    }
}
