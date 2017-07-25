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

package org.ejml.sparse.csc.linsol.chol;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;

/**
 * Linear solver using a sparse Cholesky decomposition.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholesky_DSCC implements LinearSolver<DMatrixSparseCSC> {

    CholeskyUpLooking_DSCC cholesky;

    public LinearSolverCholesky_DSCC(CholeskyUpLooking_DSCC cholesky) {
        this.cholesky = cholesky;
    }

    @Override
    public boolean setA(DMatrixSparseCSC A) {
        return cholesky.decompose(A);
    }

    @Override
    public double quality() {
        return 0;
    }

    @Override
    public void solve(DMatrixSparseCSC B, DMatrixSparseCSC X) {

    }

    @Override
    public void invert(DMatrixSparseCSC A_inv) {

    }

    @Override
    public boolean modifiesA() {
        return cholesky.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public <D extends DecompositionInterface> D getDecomposition() {
        return (D)cholesky;
    }
}
