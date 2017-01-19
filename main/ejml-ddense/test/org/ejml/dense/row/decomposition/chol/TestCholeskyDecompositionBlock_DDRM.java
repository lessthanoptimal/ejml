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

package org.ejml.dense.row.decomposition.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.junit.Test;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM.checkModifiedInput;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionBlock_DDRM extends GenericCholeskyTests_DDRM {

    public TestCholeskyDecompositionBlock_DDRM(){
        canR = false;
    }

    @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionBlock_DDRM(2));
    }

    @Override
    public CholeskyDecomposition_F64<DMatrixRMaj> create(boolean lower) {
        if( !lower )
            throw new IllegalArgumentException("Doesn't support upper form");

        return new CholeskyDecompositionBlock_DDRM(1);
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
        checkDMatrixBlock(W, B);
    }

    /**
     * The block size and the matrix width are not perfectly divisible.  see if this is handled correctly.
     */
    @Test
    public void testWithBlocksNotDivisible() {
        int W = 4;
        int B = 3;
        checkDMatrixBlock(W, B);
    }

    /**
     * The block size is bigger than the matrix.
     */
    @Test
    public void testWithBlocksBiggerThanMatrix() {
        int W = 4;
        int B = 10;
        checkDMatrixBlock(W, B);
    }

    private void checkDMatrixBlock(int w, int b) {
        DMatrixRMaj A = new DMatrixRMaj(w, w, true, 4, 2, 6, 14, 2, 26, 13, 12, 6, 13, 29, 47, 14, 12, 47, 95);

        DMatrixRMaj A_inv = new DMatrixRMaj(w, w);
        DMatrixRMaj A_inv_block = new DMatrixRMaj(w, w);

        CholeskyDecompositionBlock_DDRM algBlock = new CholeskyDecompositionBlock_DDRM(b);
        LinearSolver<DMatrixRMaj> solver = new LinearSolverChol_DDRM(algBlock);
        solver = new LinearSolverSafe<DMatrixRMaj>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv_block);

        CholeskyDecompositionInner_DDRM alg = new CholeskyDecompositionInner_DDRM(true);
        solver = new LinearSolverChol_DDRM(alg);
        solver = new LinearSolverSafe<DMatrixRMaj>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        EjmlUnitTests.assertEquals(A_inv,A_inv_block, UtilEjml.TEST_F64_SQ);
    }
}