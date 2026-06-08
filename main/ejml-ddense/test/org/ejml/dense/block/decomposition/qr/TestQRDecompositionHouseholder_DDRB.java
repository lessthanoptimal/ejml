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

package org.ejml.dense.block.decomposition.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderTran_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholder_DDRB extends EjmlStandardJUnit {
    // the block length
    int r = 3;

    @Test
    void decomposeQR_block_col() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(r*2 + r - 1, r, -1, 1, rand);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A, r);

        QRDecompositionHouseholderTran_DDRM algTest = new QRDecompositionHouseholderTran_DDRM();
        assertTrue(algTest.decompose(A));

        double[] gammas = new double[A.numCols];
        QRDecompositionHouseholder_DDRB.decomposeQR_block_col(r, new DSubmatrixD1(Ab), gammas);

        DMatrixRMaj expected = CommonOps_DDRM.transpose(algTest.getQR(), null);

        assertTrue(GenericMatrixOps_F64.isEquivalent(expected, Ab, UtilEjml.TEST_F64));
    }

    @Test
    public void generic() {
        QRDecompositionHouseholder_DDRB decomp = new QRDecompositionHouseholder_DDRB();

        GenericBlock64QrDecompositionTests_DDRB tests;
        tests = new GenericBlock64QrDecompositionTests_DDRB(decomp);

        tests.allTests();
    }

    @Test
    public void genericSaveW() {
        QRDecompositionHouseholder_DDRB decomp = new QRDecompositionHouseholder_DDRB();
        decomp.setSaveW(true);

        GenericBlock64QrDecompositionTests_DDRB tests;
        tests = new GenericBlock64QrDecompositionTests_DDRB(decomp);

        tests.allTests();
    }
}
