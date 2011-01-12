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
import org.ejml.alg.dense.linsol.WrapLinearSolverBlock64;
import org.ejml.data.DenseMatrix64F;


/**
 * A wrapper around {@link org.ejml.alg.dense.decomposition.CholeskyDecomposition}(BlockMatrix64F) that allows
 * it to be easily used with {@link org.ejml.data.DenseMatrix64F}.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholBlock64 extends WrapLinearSolverBlock64 {

    public LinearSolverCholBlock64() {
        super(new BlockCholeskyOuterSolver());
    }

    /**
     * Only converts the B matrix and passes that onto solve.  Te result is then copied into
     * the input 'X' matrix.
     * 
     * @param B A matrix &real; <sup>m &times; p</sup>.  Not modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        blockB.reshape(B.numRows,B.numCols,false);
        BlockMatrixOps.convert(B,blockB);

        // since overwrite B is true X does not need to be passed in
        alg.solve(blockB,null);

        BlockMatrixOps.convert(blockB,X);
    }

}
