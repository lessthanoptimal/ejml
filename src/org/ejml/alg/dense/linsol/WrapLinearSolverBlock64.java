/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.linsol.LinearSolverBlock;
import org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * Wrapper that allows {@link LinearSolverBlock} to implements {@link LinearSolver}.  It works
 * by converting {@link DenseMatrix64F} into {@link BlockMatrix64F} and calling the equivalent
 * functions.  Since a local copy is made all input matrices are never modified.
 *
 * @author Peter Abeles
 */
public class WrapLinearSolverBlock64 implements LinearSolver {
    protected LinearSolverBlock alg = new BlockCholeskyOuterSolver();

    // block matrix copy of the system A matrix.
    protected BlockMatrix64F blockA = new BlockMatrix64F(1,1);
    // block matrix copy of B matrix passed into solve
    protected BlockMatrix64F blockB = new BlockMatrix64F(1,1);
    // block matrix copy of X matrix passed into solve
    protected BlockMatrix64F blockX = new BlockMatrix64F(1,1);
    // original input A matrix.
    protected DenseMatrix64F A;

    public WrapLinearSolverBlock64( LinearSolverBlock alg ) {
        this.alg = alg;
    }

    @Override
    public DenseMatrix64F getA() {
        return A;
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
        this.A = A;

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
}
