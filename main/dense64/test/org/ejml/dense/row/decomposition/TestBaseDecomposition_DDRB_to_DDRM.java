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

package org.ejml.dense.row.decomposition;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestBaseDecomposition_DDRB_to_DDRM {




    /**
     * Make sure the input is never modified.  Also checks to see if the matrix was correctly converted from
     * row into block format and back
     */
    @Test
    public void inputModified() {

        DMatrixRMaj A = new DMatrixRMaj(25,20);
        for (int i = 0; i < A.data.length; i++) {
            A.data[i] = i;
        }

        // Should not modify the input in this case
        BaseDecomposition_DDRB_to_DDRM alg = new BaseDecomposition_DDRB_to_DDRM(new DoNotModifyBlock(),10);

        assertFalse(alg.inputModified());
        assertTrue(alg.decompose(A));
        for (int i = 0; i < A.data.length; i++) {
            assertEquals(i,A.data[i], UtilEjml.TEST_F64);
        }

        // test it with a decomposition which modifies the input
        alg = new BaseDecomposition_DDRB_to_DDRM(new ModifyBlock(),10);

        assertTrue(alg.inputModified());
        assertTrue(alg.decompose(A));
    }

    private static class ModifyBlock implements DecompositionInterface<DMatrixRBlock> {

        @Override
        public boolean decompose(DMatrixRBlock orig) {

            // see if the input was correctly converted
            int val = 0;
            for (int i = 0; i < orig.numRows; i++) {
                for (int j = 0; j < orig.numCols; j++,val++) {
                    assertEquals(val,orig.get(i,j),UtilEjml.TEST_F64);
                }
            }

            // modify it now
            Arrays.fill(orig.data,1);

            return true;
        }

        @Override
        public boolean inputModified() {
            return true;
        }
    }

    private static class DoNotModifyBlock implements DecompositionInterface<DMatrixRBlock> {

        @Override
        public boolean decompose(DMatrixRBlock orig) {
            return true;
        }

        @Override
        public boolean inputModified() {
            return false;
        }
    }

}
