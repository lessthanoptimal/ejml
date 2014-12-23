/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block;

import org.ejml.alg.generic.GenericMatrixOps;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestBlockInnerRankUpdate {

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
        SimpleMatrix origA = SimpleMatrix.random(lengthA,lengthA,-1,1,rand);
        SimpleMatrix origB = SimpleMatrix.random(heightB,lengthA,-1,1,rand);

        BlockMatrix64F blockA = BlockMatrixOps.convert(origA.getMatrix(),N);
        BlockMatrix64F blockB = BlockMatrixOps.convert(origB.getMatrix(),N);

        D1Submatrix64F subA = new D1Submatrix64F(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix64F subB = new D1Submatrix64F(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(alpha));
        BlockInnerRankUpdate.rankNUpdate(N,alpha,subA,subB);

        assertTrue(GenericMatrixOps.isEquivalent(expected.getMatrix(),blockA,1e-8));
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
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices.createSymmPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random(heightB,lengthA,-1,1,rand);

        BlockMatrix64F blockA = BlockMatrixOps.convert(origA.getMatrix(),N);
        BlockMatrix64F blockB = BlockMatrixOps.convert(origB.getMatrix(),N);

        D1Submatrix64F subA = new D1Submatrix64F(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix64F subB = new D1Submatrix64F(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.transpose().mult(origB).scale(-1));
        BlockInnerRankUpdate.symmRankNMinus_U(N,subA,subB);

        assertTrue(GenericMatrixOps.isEquivalentTriangle(true,expected.getMatrix(),blockA,1e-8));
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
        SimpleMatrix origA = SimpleMatrix.wrap(RandomMatrices.createSymmPosDef(lengthA,rand));
        SimpleMatrix origB = SimpleMatrix.random(lengthA,widthB,-1,1,rand);

        BlockMatrix64F blockA = BlockMatrixOps.convert(origA.getMatrix(),N);
        BlockMatrix64F blockB = BlockMatrixOps.convert(origB.getMatrix(),N);

        D1Submatrix64F subA = new D1Submatrix64F(blockA,0, origA.numRows(), 0, origA.numCols());
        D1Submatrix64F subB = new D1Submatrix64F(blockB,0, origB.numRows(), 0, origB.numCols());

        SimpleMatrix expected = origA.plus(origB.mult(origB.transpose()).scale(-1));
        BlockInnerRankUpdate.symmRankNMinus_L(N,subA,subB);

//        expected.print();
//        blockA.print();

        assertTrue(GenericMatrixOps.isEquivalentTriangle(false,expected.getMatrix(),blockA,1e-8));
    }
}
