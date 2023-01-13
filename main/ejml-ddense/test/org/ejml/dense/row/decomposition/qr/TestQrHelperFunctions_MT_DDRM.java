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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestQrHelperFunctions_MT_DDRM extends EjmlStandardJUnit {
    final int N = 200;
    DMatrixRMaj Q;
    public double[] u = new double[N];
    public double[] temp = new double[N];

    @BeforeEach
    void init() {
        Q = RandomMatrices_DDRM.rectangle(N, N, -1, 1, rand);
        for (int i = 0; i < N; i++) {
            u[i] = (double)rand.nextGaussian();
        }
    }

    @Test void rank1UpdateMultR_u0() {
        DMatrixRMaj expected = Q.copy();

        QrHelperFunctions_MT_DDRM.rank1UpdateMultR_u0(Q, u, 0.9, 1.2, 1, 0, N, temp);
        QrHelperFunctions_DDRM.rank1UpdateMultR_u0(expected, u, 0.9, 1.2, 1, 0, N, temp);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, Q, UtilEjml.TEST_F64));
    }

    @Test void rank1UpdateMultR() {
        DMatrixRMaj expected = Q.copy();

        QrHelperFunctions_MT_DDRM.rank1UpdateMultR(Q, u, 1.2, 1, 0, N, temp);
        QrHelperFunctions_DDRM.rank1UpdateMultR(expected, u, 1.2, 1, 0, N, temp);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, Q, UtilEjml.TEST_F64));
    }

    @Test void rank1UpdateMultR_offU() {
        DMatrixRMaj expected = Q.copy();

        QrHelperFunctions_MT_DDRM.rank1UpdateMultR(Q, u, 0, 1.2, 0, 1, N, temp);
        QrHelperFunctions_DDRM.rank1UpdateMultR(expected, u, 0, 1.2, 0, 1, N, temp);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, Q, UtilEjml.TEST_F64));
    }

    @Test void rank1UpdateMultL() {
        DMatrixRMaj expected = Q.copy();

        QrHelperFunctions_MT_DDRM.rank1UpdateMultL(Q, u, 1.2, 1, 0, N);
        QrHelperFunctions_DDRM.rank1UpdateMultL(expected, u, 1.2, 1, 0, N);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, Q, UtilEjml.TEST_F64));
    }
}

