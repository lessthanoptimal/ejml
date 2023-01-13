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

package org.ejml.dense.row.linsol.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLinearSolverQrHouseCol_MT_DDRM extends EjmlStandardJUnit {
    @Test void compare() {
        DMatrixRMaj A = new DMatrixRMaj(400, 90);
        DMatrixRMaj B = new DMatrixRMaj(400, 2);
        DMatrixRMaj expX = new DMatrixRMaj(1, 1);
        DMatrixRMaj fndX = new DMatrixRMaj(1, 1);

        var single = new LinearSolverQrHouseCol_DDRM();
        var thread = new LinearSolverQrHouseCol_MT_DDRM();

        assertFalse(single.modifiesA());
        assertFalse(thread.modifiesA());
        assertFalse(single.modifiesB());
        assertFalse(thread.modifiesB());

        for (int i = 0; i < 5; i++) {
            RandomMatrices_DDRM.fillUniform(A, -1, 1, rand);
            RandomMatrices_DDRM.fillUniform(B, -1, 1, rand);
            assertTrue(single.setA(A));
            assertTrue(thread.setA(A));

            single.solve(B, expX);
            thread.solve(B, fndX);

            EjmlUnitTests.assertEquals(expX, fndX);
        }
    }
}
