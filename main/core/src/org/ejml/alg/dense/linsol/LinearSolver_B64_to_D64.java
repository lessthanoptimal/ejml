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

package org.ejml.alg.dense.linsol;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * Wrapper that allows {@link org.ejml.interfaces.linsol.LinearSolver <BlockMatrix64F>} to implements {@link org.ejml.interfaces.linsol.LinearSolver}.  It works
 * by converting {@link DenseMatrix64F} into {@link BlockMatrix64F} and calling the equivalent
 * functions.  Since a local copy is made all input matrices are never modified.
 *
 * @author Peter Abeles
 */
public class LinearSolver_B64_to_D64 implements LinearSolver<DenseMatrix64F> {
    protected LinearSolver<BlockMatrix64F> alg = new BlockCholeskyOuterSolver();

    // block matrix copy of the system A matrix.
    protected BlockMatrix64F blockA = new BlockMatrix64F(1,1);
    // block matrix copy of B matrix passed into solve
    protected BlockMatrix64F blockB = new BlockMatrix64F(1,1);
    // block matrix copy of X matrix passed into solve
    protected BlockMatrix64F blockX = new BlockMatrix64F(1,1);

    public LinearSolver_B64_to_D64(LinearSolver<BlockMatrix64F> alg) {
        this.alg = alg;
    }

    /**
     * Converts 'A' into a block matrix and call setA() on the block matrix solver.
     *
     * @param A The A matrix in the linear equation. Not modified. Reference saved.
     * @return true if it can solve the system.
     */
    @Override
    public boolean setA(DenseMatrix64F A) {
        blockA.reshape(A.numRows,A.numCols,false);
        BlockMatrixOps.convert(A,blockA);

        return alg.setA(blockA);
    }

    @Override
    public double quality() {
        return alg.quality();
    }

    /**
     * Converts B and X into block matrices and calls the block matrix solve routine.
     *
     * @param B A matrix &real; <sup>m &times; p</sup>.  Not modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        blockB.reshape(B.numRows,B.numCols,false);
        blockX.reshape(X.numRows,X.numCols,false);
        BlockMatrixOps.convert(B,blockB);

        alg.solve(blockB,blockX);

        BlockMatrixOps.convert(blockX,X);
    }

    /**
     * Creates a block matrix the same size as A_inv, inverts the matrix and copies the results back
     * onto A_inv.
     * 
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    @Override
    public void invert(DenseMatrix64F A_inv) {
        blockB.reshape(A_inv.numRows,A_inv.numCols,false);

        alg.invert(blockB);

        BlockMatrixOps.convert(blockB,A_inv);
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }
}
