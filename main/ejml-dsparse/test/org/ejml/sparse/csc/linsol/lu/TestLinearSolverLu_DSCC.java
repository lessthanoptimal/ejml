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

package org.ejml.sparse.csc.linsol.lu;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.LinearSolverSparse;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_DSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_DSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_DSCC;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverLu_DSCC extends GenericLinearSolverSparseTests_DSCC {

    public TestLinearSolverLu_DSCC() {
        canDecomposeZeros = false;
        canLockStructure = false;
    }

    @Override
    public LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> createSolver(FillReducing permutation) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        LuUpLooking_DSCC lu = new LuUpLooking_DSCC(cp);
        return new LinearSolverLu_DSCC(lu);
    }

    @Override
    public DMatrixSparseCSC createA(int size) {

        int nz = RandomMatrices_DSCC.nonzero(size,size,0.05,0.7,rand);
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(size,size,nz,rand);
        RandomMatrices_DSCC.ensureNotSingular(A,rand);

        return A;
    }
}