/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.D1Submatrix_F64;
import org.ejml.data.DMatrixBlock_F64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestInnerRankUpdate_B64 {

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
        double alpha = -2.0;
        SimpleMatrix origA = SimpleMatrix.random_F64(lengthA,lengthA,-1.0 , 1.0 ,rand);
        SimpleMatrix origB = SimpleMatrix.random_F64(heightB,lengthA,-1.0 , 1.0 ,rand);

        DMatrixBlock_F64 blockA = MatrixOps_B64.convert(origA.matrix_F64(),N);
        DMatrixBlock_F64 blockB = MatrixOps_B64.convert(origB.matrix_F64(),N);

        D1Submatrix_F64 subA = new D1Submatrix_F64(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix_F64 subB = new D1Submatrix_F64(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(alpha));
        InnerRankUpdate_B64.rankNUpdate(N,alpha,subA,subB);

        assertTrue(GenericMatrixOps_F64.isEquivalent(expected.matrix_F64(),blockA, UtilEjml.TEST_F64));
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
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices_R64.createSymmPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random_F64(heightB,lengthA, -1.0 , 1.0 ,rand);

        DMatrixBlock_F64 blockA = MatrixOps_B64.convert(origA.matrix_F64(),N);
        DMatrixBlock_F64 blockB = MatrixOps_B64.convert(origB.matrix_F64(),N);

        D1Submatrix_F64 subA = new D1Submatrix_F64(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix_F64 subB = new D1Submatrix_F64(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(-1));
        InnerRankUpdate_B64.symmRankNMinus_U(N,subA,subB);

        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(true,expected.matrix_F64(),blockA,UtilEjml.TEST_F64));
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
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices_R64.createSymmPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random_F64(lengthA,widthB, -1.0 , 1.0 ,rand);

        DMatrixBlock_F64 blockA = MatrixOps_B64.convert(origA.matrix_F64(),N);
        DMatrixBlock_F64 blockB = MatrixOps_B64.convert(origB.matrix_F64(),N);

        D1Submatrix_F64 subA = new D1Submatrix_F64(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix_F64 subB = new D1Submatrix_F64(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.mult(origB.transpose()).scale(-1));
        InnerRankUpdate_B64.symmRankNMinus_L(N,subA,subB);

//        expected.print();
//        blockA.print();

        assertTrue(GenericMatrixOps_F64.isEquivalentTriangle(false,expected.matrix_F64(),blockA,UtilEjml.TEST_F64));
    }
}
