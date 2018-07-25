/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.linsol.chol.CholeskyOuterSolver_DDRB;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * Wrapper that allows {@link DMatrixRBlock} to implements {@link LinearSolverDense}.  It works
 * by converting {@link DMatrixRMaj} into {@link DMatrixRBlock} and calling the equivalent
 * functions.  Since a local copy is made all input matrices are never modified.
 *
 * @author Peter Abeles
 */
public class LinearSolver_DDRB_to_DDRM implements LinearSolverDense<DMatrixRMaj> {
    protected LinearSolverDense<DMatrixRBlock> alg = new CholeskyOuterSolver_DDRB();

    // block matrix copy of the system A matrix.
    protected DMatrixRBlock blockA = new DMatrixRBlock(1,1);
    // block matrix copy of B matrix passed into solve
    protected DMatrixRBlock blockB = new DMatrixRBlock(1,1);
    // block matrix copy of X matrix passed into solve
    protected DMatrixRBlock blockX = new DMatrixRBlock(1,1);

    public LinearSolver_DDRB_to_DDRM(LinearSolverDense<DMatrixRBlock> alg) {
        this.alg = alg;
    }

    /**
     * Converts 'A' into a block matrix and call setA() on the block matrix solver.
     *
     * @param A The A matrix in the linear equation. Not modified. Reference saved.
     * @return true if it can solve the system.
     */
    @Override
    public boolean setA(DMatrixRMaj A) {
        blockA.reshape(A.numRows,A.numCols,false);
        MatrixOps_DDRB.convert(A,blockA);

        return alg.setA(blockA);
    }

    @Override
    public /**/double quality() {
        return alg.quality();
    }

    /**
     * Converts B and X into block matrices and calls the block matrix solve routine.
     *
     * @param B A matrix &real; <sup>m &times; p</sup>.  Not modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    @Override
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
        X.reshape(blockA.numCols,B.numCols);
        blockB.reshape(B.numRows,B.numCols,false);
        blockX.reshape(X.numRows,X.numCols,false);
        MatrixOps_DDRB.convert(B,blockB);

        alg.solve(blockB,blockX);

        MatrixOps_DDRB.convert(blockX,X);
    }

    /**
     * Creates a block matrix the same size as A_inv, inverts the matrix and copies the results back
     * onto A_inv.
     * 
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    @Override
    public void invert(DMatrixRMaj A_inv) {
        blockB.reshape(A_inv.numRows,A_inv.numCols,false);

        alg.invert(blockB);

        MatrixOps_DDRB.convert(blockB,A_inv);
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public <D extends DecompositionInterface> D getDecomposition() {
        return alg.getDecomposition();
    }
}
