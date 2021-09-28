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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestInnerRankUpdate_MT_DDRB extends EjmlStandardJUnit {
    int N = 10;

    @Test
    void rankNUpdate() {
        // the matrix being updated is a whole block
        checkRankNUpdate(N, N - 2);

        // the matrix being updated is multiple blocks + a fraction
        checkRankNUpdate(N*2 + 1, N - 2);

        // matrix being updated is less than a block
        checkRankNUpdate(N - 1, N - 2);
    }

    private void checkRankNUpdate( int lengthA, int heightB ) {
        double alpha = -2.0;
        DMatrixRMaj origA = RandomMatrices_DDRM.rectangle(lengthA, lengthA, -1.0, 1.0, rand);
        DMatrixRMaj origB = RandomMatrices_DDRM.rectangle(heightB, lengthA, -1.0, 1.0, rand);

        DMatrixRBlock expectedA = MatrixOps_DDRB.convert(origA, N);
        DMatrixRBlock foundA = MatrixOps_DDRB.convert(origA, N);

        DMatrixRBlock blockB = MatrixOps_DDRB.convert(origB, N);

        DSubmatrixD1 subExpected = new DSubmatrixD1(expectedA, 0, origA.numRows, 0, origA.numCols);
        DSubmatrixD1 subFound = new DSubmatrixD1(foundA, 0, origA.numRows, 0, origA.numCols);

        DSubmatrixD1 subB = new DSubmatrixD1(blockB, 0, origB.numRows, 0, origB.numCols);

        InnerRankUpdate_DDRB.rankNUpdate(N, alpha, subFound, subB);
        InnerRankUpdate_MT_DDRB.rankNUpdate(N, alpha, subExpected, subB);

        assertTrue(GenericMatrixOps_F64.isEquivalent(expectedA, foundA, UtilEjml.TEST_F64));
    }

    @Test
    void symmRankNMinus_U() {
        // the matrix being updated is a whole block
        checkSymmRankNMinus_U(N, N - 2);

        // the matrix being updated is multiple blocks + a fraction
        checkSymmRankNMinus_U(N*2 + 1, N - 2);

        // matrix being updated is less than a block
        checkSymmRankNMinus_U(N - 1, N - 2);
    }

    private void checkSymmRankNMinus_U( int lengthA, int heightB ) {
        DMatrixRBlock expectA = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetricPosDef(lengthA, rand));
        DMatrixRBlock foundA = expectA.copy();
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(heightB, lengthA, -1.0, 1.0, rand, N);

        DSubmatrixD1 subExpect = new DSubmatrixD1(expectA, 0, expectA.numRows, 0, expectA.numCols);
        DSubmatrixD1 subFound = new DSubmatrixD1(foundA, 0, expectA.numRows, 0, expectA.numCols);
        DSubmatrixD1 subB = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);

        InnerRankUpdate_DDRB.symmRankNMinus_U(N, subExpect, subB);
        InnerRankUpdate_MT_DDRB.symmRankNMinus_U(N, subFound, subB);

        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(true, expectA, foundA, UtilEjml.TEST_F64));
    }

    @Test
    void symmRankNMinus_L() {
        // the matrix being updated is a whole block
        checkSymmRankNMinus_L(N, N - 2);

        // the matrix being updated is multiple blocks + a fraction
        checkSymmRankNMinus_L(N*2 + 1, N - 2);

        // matrix being updated is less than a block
        checkSymmRankNMinus_L(N - 1, N - 2);
    }

    private void checkSymmRankNMinus_L( int lengthA, int widthB ) {
        DMatrixRBlock expectA = MatrixOps_DDRB.convert(RandomMatrices_DDRM.symmetricPosDef(lengthA, rand));
        DMatrixRBlock foundA = expectA.copy();
        DMatrixRBlock B = MatrixOps_DDRB.createRandom(lengthA, widthB, -1.0, 1.0, rand, N);

        DSubmatrixD1 subExpect = new DSubmatrixD1(expectA, 0, expectA.numRows, 0, expectA.numCols);
        DSubmatrixD1 subFound = new DSubmatrixD1(foundA, 0, expectA.numRows, 0, expectA.numCols);
        DSubmatrixD1 subB = new DSubmatrixD1(B, 0, B.numRows, 0, B.numCols);

        InnerRankUpdate_DDRB.symmRankNMinus_L(N, subExpect, subB);
        InnerRankUpdate_MT_DDRB.symmRankNMinus_L(N, subFound, subB);

        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(false, expectA, foundA, UtilEjml.TEST_F64));
    }
}

