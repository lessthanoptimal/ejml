/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTridiagonalDecompositionHouseholder_MT_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test
    void compareToSingle() {

        var single = new TridiagonalDecompositionHouseholder_DDRB();
        var concurrent = new TridiagonalDecompositionHouseholder_MT_DDRB();

        for (int width = 1; width <= r*10; width += 4) {
//        for (int width = 500; width <= 520; width += 4) {
            DMatrixRBlock A = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetric(width, -1, 1, rand), r);
            DMatrixRBlock AA = A.copy();

            assertTrue(single.decompose(A));
            assertTrue(concurrent.decompose(AA));

            assertTrue(MatrixOps_DDRB.isEquals(A, AA, UtilEjml.TEST_F64));
            assertTrue(MatrixOps_DDRB.isEquals(single.getT(null), concurrent.getT(null), UtilEjml.TEST_F64));
            assertTrue(MatrixOps_DDRB.isEquals(
                    single.getQ(null,true), concurrent.getQ(null,true), UtilEjml.TEST_F64));
        }
    }
}

