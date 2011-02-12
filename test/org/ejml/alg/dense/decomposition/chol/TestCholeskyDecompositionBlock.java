/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface.checkModifiedInput;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestCholeskyDecompositionBlock extends GenericCholeskyTests {

    public TestCholeskyDecompositionBlock(){
        canR = false;
    }

    @Test
    public void checkModifyInput() {
        checkModifiedInput(new CholeskyDecompositionBlock(2));
    }

    @Override
    public CholeskyDecomposition<DenseMatrix64F> create(boolean lower) {
        if( !lower )
            throw new IllegalArgumentException("Doesn't support upper form");

        return new CholeskyDecompositionBlock(1);
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
        checkBlockMatrix(W, B);
    }

    /**
     * The block size and the matrix width are not perfectly divisible.  see if this is handled correctly.
     */
    @Test
    public void testWithBlocksNotDivisible() {
        int W = 4;
        int B = 3;
        checkBlockMatrix(W, B);
    }

    /**
     * The block size is bigger than the matrix.
     */
    @Test
    public void testWithBlocksBiggerThanMatrix() {
        int W = 4;
        int B = 10;
        checkBlockMatrix(W, B);
    }

    private void checkBlockMatrix(int w, int b) {
        DenseMatrix64F A = new DenseMatrix64F(w, w, true, 4, 2, 6, 14, 2, 26, 13, 12, 6, 13, 29, 47, 14, 12, 47, 95);

        DenseMatrix64F A_inv = new DenseMatrix64F(w, w);
        DenseMatrix64F A_inv_block = new DenseMatrix64F(w, w);

        CholeskyDecompositionBlock algBlock = new CholeskyDecompositionBlock(b);
        LinearSolver<DenseMatrix64F> solver = new LinearSolverChol(algBlock);
        solver = new LinearSolverSafe<DenseMatrix64F>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv_block);

        CholeskyDecompositionInner alg = new CholeskyDecompositionInner(true);
        solver = new LinearSolverChol(alg);
        solver = new LinearSolverSafe<DenseMatrix64F>(solver);
        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        EjmlUnitTests.assertEquals(A_inv,A_inv_block,1e-5);
    }
}