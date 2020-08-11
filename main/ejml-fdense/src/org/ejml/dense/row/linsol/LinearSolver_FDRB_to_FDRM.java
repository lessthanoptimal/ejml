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

import org.ejml.data.FMatrixRBlock;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.block.MatrixOps_FDRB;
import org.ejml.dense.block.linsol.chol.CholeskyOuterSolver_FDRB;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * Wrapper that allows {@link FMatrixRBlock} to implements {@link LinearSolverDense}.  It works
 * by converting {@link FMatrixRMaj} into {@link FMatrixRBlock} and calling the equivalent
 * functions.  Since a local copy is made all input matrices are never modified.
 *
 * @author Peter Abeles
 */
public class LinearSolver_FDRB_to_FDRM implements LinearSolverDense<FMatrixRMaj> {
    protected LinearSolverDense<FMatrixRBlock> alg = new CholeskyOuterSolver_FDRB();

    // block matrix copy of the system A matrix.
    protected FMatrixRBlock blockA = new FMatrixRBlock(1,1);
    // block matrix copy of B matrix passed into solve
    protected FMatrixRBlock blockB = new FMatrixRBlock(1,1);
    // block matrix copy of X matrix passed into solve
    protected FMatrixRBlock blockX = new FMatrixRBlock(1,1);

    public LinearSolver_FDRB_to_FDRM(LinearSolverDense<FMatrixRBlock> alg) {
        this.alg = alg;
    }

    /**
     * Converts 'A' into a block matrix and call setA() on the block matrix solver.
     *
     * @param A The A matrix in the linear equation. Not modified. Reference saved.
     * @return true if it can solve the system.
     */
    @Override
    public boolean setA(FMatrixRMaj A) {
        blockA.reshape(A.numRows,A.numCols,false);
        MatrixOps_FDRB.convert(A,blockA);

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
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        X.reshape(blockA.numCols,B.numCols);
        blockB.reshape(B.numRows,B.numCols,false);
        blockX.reshape(X.numRows,X.numCols,false);
        MatrixOps_FDRB.convert(B,blockB);

        alg.solve(blockB,blockX);

        MatrixOps_FDRB.convert(blockX,X);
    }

    /**
     * Creates a block matrix the same size as A_inv, inverts the matrix and copies the results back
     * onto A_inv.
     * 
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    @Override
    public void invert(FMatrixRMaj A_inv) {
        blockB.reshape(A_inv.numRows,A_inv.numCols,false);

        alg.invert(blockB);

        MatrixOps_FDRB.convert(blockB,A_inv);
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
