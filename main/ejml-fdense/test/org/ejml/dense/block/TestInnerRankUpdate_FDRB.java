/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FSubmatrixD1;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.generic.GenericMatrixOps_F32;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInnerRankUpdate_FDRB {

    Random rand = new Random(234234);

    int N = 4;

    /**
     * Tests rankNUpdate with various sized input matrices
     */
    @Test
    public void rankNUpdate() {
        // the matrix being updated is a whole block
        checkRankNUpdate(N, N-2);

        // the matrix being updated is multiple blocks + a fraction
        checkRankNUpdate(N*2+1, N-2);

        // matrix being updated is less than a block
        checkRankNUpdate(N-1, N-2);

    }

    private void checkRankNUpdate(int lengthA, int heightB) {
        float alpha = -2.0f;
        SimpleMatrix origA = SimpleMatrix.random_FDRM(lengthA,lengthA,-1.0f , 1.0f ,rand);
        SimpleMatrix origB = SimpleMatrix.random_FDRM(heightB,lengthA,-1.0f , 1.0f ,rand);

        FMatrixRBlock blockA = MatrixOps_FDRB.convert(origA.getFDRM(),N);
        FMatrixRBlock blockB = MatrixOps_FDRB.convert(origB.getFDRM(),N);

        FSubmatrixD1 subA = new FSubmatrixD1(blockA,0, origA.numRows(), 0, origA.numCols());
        FSubmatrixD1 subB = new FSubmatrixD1(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(alpha));
        InnerRankUpdate_FDRB.rankNUpdate(N,alpha,subA,subB);

        assertTrue(GenericMatrixOps_F32.isEquivalent(expected.getFDRM(),blockA, UtilEjml.TEST_F32));
    }

    /**
     * Tests symmRankNMinus_U with various sized input matrices
     */
    @Test
    public void symmRankNMinus_U() {
        // the matrix being updated is a whole block
        checkSymmRankNMinus_U(N, N-2);

        // the matrix being updated is multiple blocks + a fraction
        checkSymmRankNMinus_U(N*2+1, N-2);

        // matrix being updated is less than a block
        checkSymmRankNMinus_U(N-1, N-2);
    }

    private void checkSymmRankNMinus_U(int lengthA, int heightB) {
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices_FDRM.symmetricPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random_FDRM(heightB,lengthA, -1.0f , 1.0f ,rand);

        FMatrixRBlock blockA = MatrixOps_FDRB.convert(origA.getFDRM(),N);
        FMatrixRBlock blockB = MatrixOps_FDRB.convert(origB.getFDRM(),N);

        FSubmatrixD1 subA = new FSubmatrixD1(blockA,0, origA.numRows(), 0, origA.numCols());
        FSubmatrixD1 subB = new FSubmatrixD1(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(-1));
        InnerRankUpdate_FDRB.symmRankNMinus_U(N,subA,subB);

        assertTrue(GenericMatrixOps_F32.isEquivalentTriangle(true,expected.getFDRM(),blockA,UtilEjml.TEST_F32));
    }

    @Test
    public void symmRankNMinus_L() {
        // the matrix being updated is a whole block
        checkSymmRankNMinus_L(N, N-2);

        // the matrix being updated is multiple blocks + a fraction
        checkSymmRankNMinus_L(N*2+1, N-2);

        // matrix being updated is less than a block
        checkSymmRankNMinus_L(N-1, N-2);
    }

    private void checkSymmRankNMinus_L(int lengthA, int widthB) {
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices_FDRM.symmetricPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random_FDRM(lengthA,widthB, -1.0f , 1.0f ,rand);

        FMatrixRBlock blockA = MatrixOps_FDRB.convert(origA.getFDRM(),N);
        FMatrixRBlock blockB = MatrixOps_FDRB.convert(origB.getFDRM(),N);

        FSubmatrixD1 subA = new FSubmatrixD1(blockA,0, origA.numRows(), 0, origA.numCols());
        FSubmatrixD1 subB = new FSubmatrixD1(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.mult(origB.transpose()).scale(-1));
        InnerRankUpdate_FDRB.symmRankNMinus_L(N,subA,subB);

//        expected.print();
//        blockA.print();

        assertTrue(GenericMatrixOps_F32.isEquivalentTriangle(false,expected.getFDRM(),blockA,UtilEjml.TEST_F32));
    }
}
