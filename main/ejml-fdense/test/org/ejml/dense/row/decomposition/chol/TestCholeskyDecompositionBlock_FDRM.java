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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_FDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F32;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM.checkModifiedInput;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionBlock_FDRM extends GenericCholeskyTests_FDRM {

    public TestCholeskyDecompositionBlock_FDRM(){
        canR = false;
    }

    @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionBlock_FDRM(2));
    }

    @Override
    public CholeskyDecomposition_F32<FMatrixRMaj> create(boolean lower) {
        if( !lower )
            throw new IllegalArgumentException("Doesn't support upper form");

        return new CholeskyDecompositionBlock_FDRM(1);
    }

    /**
     *
     * L =
     *
     * 2   0   0   0
     * 1   5   0   0
     * 3   2   4   0
     * 7   1   6   3
     *
     */
    @Test
    public void testWithBlocks() {
        int W = 4;
        int B = 2;
        checkFMatrixBlock(W, B);
    }

    /**
     * The block size and the matrix width are not perfectly divisible.  see if this is handled correctly.
     */
    @Test
    public void testWithBlocksNotDivisible() {
        int W = 4;
        int B = 3;
        checkFMatrixBlock(W, B);
    }

    /**
     * The block size is bigger than the matrix.
     */
    @Test
    public void testWithBlocksBiggerThanMatrix() {
        int W = 4;
        int B = 10;
        checkFMatrixBlock(W, B);
    }

    private void checkFMatrixBlock(int w, int b) {
        FMatrixRMaj A = new FMatrixRMaj(w, w, true, 4, 2, 6, 14, 2, 26, 13, 12, 6, 13, 29, 47, 14, 12, 47, 95);

        FMatrixRMaj A_inv = new FMatrixRMaj(w, w);
        FMatrixRMaj A_inv_block = new FMatrixRMaj(w, w);

        CholeskyDecompositionBlock_FDRM algBlock = new CholeskyDecompositionBlock_FDRM(b);
        LinearSolverDense<FMatrixRMaj> solver = new LinearSolverChol_FDRM(algBlock);
        solver = new LinearSolverSafe<FMatrixRMaj>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv_block);

        CholeskyDecompositionInner_FDRM alg = new CholeskyDecompositionInner_FDRM(true);
        solver = new LinearSolverChol_FDRM(alg);
        solver = new LinearSolverSafe<FMatrixRMaj>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        EjmlUnitTests.assertEquals(A_inv,A_inv_block, UtilEjml.TEST_F32_SQ);
    }
}