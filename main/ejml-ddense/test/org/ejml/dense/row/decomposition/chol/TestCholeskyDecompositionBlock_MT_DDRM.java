/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCholeskyDecompositionBlock_MT_DDRM extends EjmlStandardJUnit {
    @Test void compare() {
        int blockLength = 13;
        DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(150, new Random(234));
        DMatrixRMaj B = A.copy();

        var single = new CholeskyDecompositionBlock_DDRM(blockLength);
        var concurrent = new CholeskyDecompositionBlock_DDRM(blockLength);

        assertTrue(single.decompose(A));
        assertTrue(concurrent.decompose(B));

        assertTrue(MatrixFeatures_DDRM.isIdentical(A, B, UtilEjml.TEST_F64));

        assertTrue(MatrixFeatures_DDRM.isIdentical(single.getT(), concurrent.getT(), UtilEjml.TEST_F64));
    }
}