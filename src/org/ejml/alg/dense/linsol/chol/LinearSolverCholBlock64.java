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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.linsol.chol.BlockCholeskyOuterSolver;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * A wrapper around {@link org.ejml.alg.block.decomposition.BlockCholeskyDecomposition} that allows
 * it to be easily used with {@link org.ejml.data.DenseMatrix64F}.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholBlock64 implements LinearSolver {

    BlockCholeskyOuterSolver alg = new BlockCholeskyOuterSolver();

    BlockMatrix64F blockA = new BlockMatrix64F(1,1);
    BlockMatrix64F blockB = new BlockMatrix64F(1,1);
    DenseMatrix64F A;

    public LinearSolverCholBlock64() {
        // reduce the amount of memory that needs to be declared and copied
        alg.setOverwriteB(true);
    }

    @Override
    public DenseMatrix64F getA() {
        return A;
    }

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

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        blockB.reshape(B.numRows,B.numCols,false);
        BlockMatrixOps.convert(B,blockB);

        // since overwrite B is true X does not need to be passed in
        alg.solve(blockB,null);

        BlockMatrixOps.convert(blockB,X);
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        blockB.reshape(A_inv.numRows,A_inv.numCols,false);

        alg.invert(blockB);

        BlockMatrixOps.convert(blockB,A_inv);
    }
}
