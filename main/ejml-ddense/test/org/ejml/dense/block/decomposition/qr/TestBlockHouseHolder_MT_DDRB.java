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
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBlockHouseHolder_MT_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test
    void decomposeQR_block_col() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock AA = A.copy();

        double[] gammas = new double[A.numCols];
        BlockHouseHolder_DDRB.decomposeQR_block_col(r, new DSubmatrixD1(A), gammas);

        double[] gammasC = new double[A.numCols];
        BlockHouseHolder_MT_DDRB.decomposeQR_block_col(r, new DSubmatrixD1(AA), gammasC);

        for (int i = 0; i < gammas.length; i++) {
            assertEquals(gammas[i], gammasC[i]);
        }

        assertTrue(MatrixOps_DDRB.isEquals(A, AA, UtilEjml.TEST_F64));
    }

    @Test
    void rank1UpdateMultR_Col() {
        double gamma = 2.5;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 - 1, -1, 1, rand, r);
        DMatrixRBlock AA = A.copy();

        BlockHouseHolder_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(A), 1, gamma);
        BlockHouseHolder_MT_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(AA), 1, gamma);

        assertTrue(MatrixOps_DDRB.isEquals(A, AA, UtilEjml.TEST_F64));
    }
}

