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

package org.ejml.dense.row.linsol.chol;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.linsol.chol.CholeskyOuterSolver_DDRB;
import org.ejml.dense.row.linsol.LinearSolver_DDRB_to_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;


/**
 * A wrapper around {@link CholeskyDecomposition_F64}(DMatrixRBlock) that allows
 * it to be easily used with {@link DMatrixRMaj}.
 *
 * @author Peter Abeles
 */
public class LinearSolverChol_DDRB extends LinearSolver_DDRB_to_DDRM {

    public LinearSolverChol_DDRB() {
        super(new CholeskyOuterSolver_DDRB());
    }

    /**
     * Only converts the B matrix and passes that onto solve.  Te result is then copied into
     * the input 'X' matrix.
     * 
     * @param B A matrix &real; <sup>m &times; p</sup>.  Not modified.
     * @param X A matrix &real; <sup>n &times; p</sup>, where the solution is written to.  Modified.
     */
    @Override
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
        blockB.reshape(B.numRows,B.numCols,false);
        MatrixOps_DDRB.convert(B,blockB);

        // since overwrite B is true X does not need to be passed in
        alg.solve(blockB,null);

        MatrixOps_DDRB.convert(blockB,X);
    }

}
