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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSafeSvd_DDRM extends EjmlStandardJUnit {
   @Test void getSafety() {
        DMatrixRMaj A = new DMatrixRMaj(3, 4);

        // it will need to create a copy in this case
        Dummy dummy = new Dummy(2, true, true, 2, 3);

        SingularValueDecomposition<DMatrixRMaj> decomp = new SafeSvd_DDRM(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertNotSame(A, dummy.passedInMatrix);

        // now no need to make a copy
        dummy = new Dummy(2, true, false, 2, 3);
        decomp = new SafeSvd_DDRM(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertSame(A, dummy.passedInMatrix);
    }

   @Test void checkOtherFunctions() {
        Dummy dummy = new Dummy(2, true, true, 2, 3);

        SingularValueDecomposition<DMatrixRMaj> decomp = new SafeSvd_DDRM(dummy);

        assertTrue(decomp.isCompact());
        assertEquals(2, decomp.numberOfSingularValues());

        assertFalse(dummy.getU_called);
        assertFalse(dummy.getV_called);
        assertFalse(dummy.getW_called);

        decomp.getU(null, false);
        assertTrue(dummy.getU_called);
        decomp.getV(null, false);
        assertTrue(dummy.getV_called);
        decomp.getW(null);
        assertTrue(dummy.getW_called);

        assertEquals(2, decomp.numCols());
        assertEquals(3, decomp.numRows());
    }

    @SuppressWarnings({"NullAway"})
    protected static class Dummy implements SingularValueDecomposition_F64<DMatrixRMaj> {

        DMatrix passedInMatrix;

        boolean compact;
        double[] singular;
        boolean getU_called;
        boolean getV_called;
        boolean getW_called;

        int numRow, numCol;
        boolean inputModified;

        private Dummy( int numSingular,
                       boolean compact,
                       boolean inputModified,
                       int numCol, int numRow ) {
            singular = new double[numSingular];
            this.compact = compact;
            this.inputModified = inputModified;
            this.numCol = numCol;
            this.numRow = numRow;
        }

        @Override public double[] getSingularValues() {
            return singular;
        }

        @Override public int numberOfSingularValues() {
            return singular.length;
        }

        @Override public boolean isCompact() {
            return compact;
        }

        @Override public DMatrixRMaj getU( DMatrixRMaj U, boolean transposed ) {
            getU_called = true;
            return null;
        }

        @Override public DMatrixRMaj getV( DMatrixRMaj V, boolean transposed ) {
            getV_called = true;
            return null;
        }

        @Override public DMatrixRMaj getW( DMatrixRMaj W ) {
            getW_called = true;
            return null;
        }

        @Override public int numRows() {
            return numRow;
        }

        @Override public int numCols() {
            return numCol;
        }

        @Override public boolean decompose( DMatrixRMaj orig ) {
            this.passedInMatrix = orig;
            return true;
        }

        @Override public boolean inputModified() {
            return inputModified;
        }
    }
}
