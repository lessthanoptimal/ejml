/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestHouseholder_MT_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test void computeWCol() {
        double[] betas = new double[]{1.2, 2, 3};
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);

        DMatrixRBlock Ws = new DMatrixRBlock(A.numRows, A.numCols, r);
        DMatrixRBlock Wc = new DMatrixRBlock(A.numRows, A.numCols, r);

        // Y is not modified, so the same input drives both the serial and concurrent build of W
        Householder_DDRB.computeWCol(r, new DSubmatrixD1(A), new DSubmatrixD1(Ws), null, betas, 0);
        Householder_MT_DDRB.computeWCol(r, new DSubmatrixD1(A), new DSubmatrixD1(Wc), null, betas, 0);

        assertTrue(MatrixOps_DDRB.isEquals(Ws, Wc, UtilEjml.TEST_F64));
    }

    @Test void multPlus_TriLL0() {
        DMatrixRBlock Y = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(r, r*2, -1, 1, rand, r);
        DMatrixRBlock Cs = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2, -1, 1, rand, r);
        DMatrixRBlock Cc = Cs.copy();

        Householder_DDRB.multPlus_TriLL0(r, new DSubmatrixD1(Y), new DSubmatrixD1(B), new DSubmatrixD1(Cs));
        Householder_MT_DDRB.multPlus_TriLL0(r, new DSubmatrixD1(Y), new DSubmatrixD1(B), new DSubmatrixD1(Cc));

        assertTrue(MatrixOps_DDRB.isEquals(Cs, Cc, UtilEjml.TEST_F64));
    }

    @Test void multTransA_TriLL0() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2, -1, 1, rand, r);
        DMatrixRBlock Cs = MatrixOps_DDRB.createRandom(r, r*2, -1, 1, rand, r);
        DMatrixRBlock Cc = Cs.copy();

        Householder_DDRB.multTransA_TriLL0(r, new DSubmatrixD1(A), new DSubmatrixD1(B), new DSubmatrixD1(Cs));
        Householder_MT_DDRB.multTransA_TriLL0(r, new DSubmatrixD1(A), new DSubmatrixD1(B), new DSubmatrixD1(Cc));

        assertTrue(MatrixOps_DDRB.isEquals(Cs, Cc, UtilEjml.TEST_F64));
    }

    @Test void multPlusTransA_symm() {
        DMatrixRBlock U = MatrixOps_DDRB.createRandom(r, r*3, -1, 1, rand, r);
        DMatrixRBlock V = MatrixOps_DDRB.createRandom(r, r*3, -1, 1, rand, r);
        DMatrixRBlock Cs = MatrixOps_DDRB.createRandom(r*3, r*3, -1, 1, rand, r);
        DMatrixRBlock Cc = Cs.copy();

        Householder_DDRB.multPlusTransA_symm(r, new DSubmatrixD1(U), new DSubmatrixD1(V), new DSubmatrixD1(Cs));
        Householder_MT_DDRB.multPlusTransA_symm(r, new DSubmatrixD1(U), new DSubmatrixD1(V), new DSubmatrixD1(Cc));

        assertTrue(MatrixOps_DDRB.isEquals(Cs, Cc, UtilEjml.TEST_F64));
    }

    @Test void rank1UpdateMultR_Col() {
        double gamma = 2.5;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 - 1, -1, 1, rand, r);
        DMatrixRBlock AA = A.copy();

        Householder_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(A), 1, gamma);
        Householder_MT_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(AA), 1, gamma);

        assertTrue(MatrixOps_DDRB.isEquals(A, AA, UtilEjml.TEST_F64));
    }
}
