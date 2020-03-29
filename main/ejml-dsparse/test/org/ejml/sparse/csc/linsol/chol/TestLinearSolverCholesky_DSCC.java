/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_DSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_DSCC;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverCholesky_DSCC extends GenericLinearSolverSparseTests_DSCC {

    public TestLinearSolverCholesky_DSCC() {
        equalityTolerance = UtilEjml.TEST_F64;
        canHandleWide = false;
        canHandleTall = false;
        canDecomposeZeros = false;

        permutationTests = new FillReducing[]{FillReducing.NONE, FillReducing.IDENTITY}; // todo add a cholesky specific
    }

    @Override
    public LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> createSolver(FillReducing permutation) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        CholeskyUpLooking_DSCC cholesky = new CholeskyUpLooking_DSCC();
        return new LinearSolverCholesky_DSCC(cholesky,cp);
    }

    @Override
    public DMatrixSparseCSC createA(int N) {
        // turns out it's not trivial to create a SPD matrix with elements randomly zero that isn't nearly singular
        // this was messing up tests
        return RandomMatrices_DSCC.symmetricPosDef(N,0.25,rand);
    }
}
