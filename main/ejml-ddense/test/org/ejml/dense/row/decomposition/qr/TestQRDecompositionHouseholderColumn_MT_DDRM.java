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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholderColumn_MT_DDRM extends EjmlStandardJUnit {
    @Test
    void compare() {
        DMatrixRMaj A = new DMatrixRMaj(200,50);
        DMatrixRMaj expQ = new DMatrixRMaj(1,1);
        DMatrixRMaj expR = new DMatrixRMaj(1,1);
        DMatrixRMaj fndQ = new DMatrixRMaj(1,1);
        DMatrixRMaj fndR = new DMatrixRMaj(1,1);

        var single = new QRDecompositionHouseholderColumn_DDRM();
        var thread = new QRDecompositionHouseholderColumn_MT_DDRM();

        assertFalse(single.inputModified());
        assertFalse(thread.inputModified());

        for (int i = 0; i < 5; i++) {
            RandomMatrices_DDRM.fillUniform(A,-1,1,rand);
            assertTrue(single.decompose(A));
            assertTrue(thread.decompose(A));

            single.getQ(expQ,true);
            single.getR(expR,true);

            thread.getQ(fndQ,true);
            thread.getR(fndR,true);

            EjmlUnitTests.assertEquals(expQ,fndQ);
            EjmlUnitTests.assertEquals(fndR,fndR);
        }
    }
}
