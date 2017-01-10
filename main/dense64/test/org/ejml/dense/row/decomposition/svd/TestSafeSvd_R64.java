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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.Matrix_F64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestSafeSvd_R64 {

    @Test
    public void getSafety() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,4);

        // it will need to create a copy in this case
        Dummy dummy = new Dummy(2,true,true,2,3);

        SingularValueDecomposition decomp = new SafeSvd_R64(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertTrue(A != dummy.passedInMatrix);

        // now no need to make a copy
        dummy = new Dummy(2,true,false,2,3);
        decomp = new SafeSvd_R64(dummy);
        assertFalse(decomp.inputModified());

        decomp.decompose(A);

        assertTrue(A == dummy.passedInMatrix);
    }

    @Test
    public void checkOtherFunctions() {
        Dummy dummy = new Dummy(2,true,true,2,3);

        SingularValueDecomposition decomp = new SafeSvd_R64(dummy);

        assertTrue(decomp.isCompact());
        assertEquals(2, decomp.numberOfSingularValues());

        assertFalse(dummy.getU_called);
        assertFalse(dummy.getV_called);
        assertFalse(dummy.getW_called);

        decomp.getU(null,false);
        assertTrue(dummy.getU_called);
        decomp.getV(null, false);
        assertTrue(dummy.getV_called);
        decomp.getW(null);
        assertTrue(dummy.getW_called);

        assertEquals(2,decomp.numCols());
        assertEquals(3,decomp.numRows());
    }

    protected static class Dummy implements SingularValueDecomposition_F64<DMatrixRow_F64> {

        Matrix_F64 passedInMatrix;

        boolean compact;
        double singular[];
        boolean getU_called;
        boolean getV_called;
        boolean getW_called;

        int numRow,numCol;
        boolean inputModified;

        private Dummy( int numSingular ,
                       boolean compact,
                       boolean inputModified,
                       int numCol, int numRow) {
            singular = new double[ numSingular ];
            this.compact = compact;
            this.inputModified = inputModified;
            this.numCol = numCol;
            this.numRow = numRow;
        }

        @Override
        public double[] getSingularValues() {
            return singular;
        }

        @Override
        public int numberOfSingularValues() {
            return singular.length;
        }

        @Override
        public boolean isCompact() {
            return compact;
        }

        @Override
        public DMatrixRow_F64 getU(DMatrixRow_F64 U, boolean transposed) {
            getU_called = true;
            return null;
        }

        @Override
        public DMatrixRow_F64 getV(DMatrixRow_F64 V, boolean transposed) {
            getV_called = true;
            return null;
        }

        @Override
        public DMatrixRow_F64 getW(DMatrixRow_F64 W) {
            getW_called = true;
            return null;
        }

        @Override
        public int numRows() {
            return numRow;
        }

        @Override
        public int numCols() {
            return numCol;
        }

        @Override
        public boolean decompose(DMatrixRow_F64 orig) {
            this.passedInMatrix = orig;
            return true;
        }

        @Override
        public boolean inputModified() {
            return inputModified;
        }
    }
}
