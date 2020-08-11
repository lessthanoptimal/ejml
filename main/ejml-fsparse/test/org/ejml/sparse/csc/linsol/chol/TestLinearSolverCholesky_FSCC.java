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
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_FSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_FSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_FSCC;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverCholesky_FSCC extends GenericLinearSolverSparseTests_FSCC {

    public TestLinearSolverCholesky_FSCC() {
        equalityTolerance = UtilEjml.TEST_F32;
        canHandleWide = false;
        canHandleTall = false;
        canDecomposeZeros = false;

        permutationTests = new FillReducing[]{FillReducing.NONE, FillReducing.IDENTITY}; // todo add a cholesky specific
    }

    @Override
    public LinearSolverSparse<FMatrixSparseCSC, FMatrixRMaj> createSolver(FillReducing permutation) {
        ComputePermutation<FMatrixSparseCSC> cp = FillReductionFactory_FSCC.create(permutation);
        CholeskyUpLooking_FSCC cholesky = new CholeskyUpLooking_FSCC();
        return new LinearSolverCholesky_FSCC(cholesky,cp);
    }

    @Override
    public FMatrixSparseCSC createA(int N) {
        // turns out it's not trivial to create a SPD matrix with elements randomly zero that isn't nearly singular
        // this was messing up tests
        return RandomMatrices_FSCC.symmetricPosDef(N,0.25f,rand);
    }
}
