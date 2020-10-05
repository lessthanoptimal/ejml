/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.linsol.qr;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_DSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_DSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_DSCC;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverQrLeftLooking_DSCC extends GenericLinearSolverSparseTests_DSCC {

    public TestLinearSolverQrLeftLooking_DSCC() {
        canHandleWide = false;
        canDecomposeZeros = false;
    }

    @Override
    public LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> createSolver( FillReducing permutation ) {
        ComputePermutation<DMatrixSparseCSC> cp = FillReductionFactory_DSCC.create(permutation);
        QrLeftLookingDecomposition_DSCC qr = new QrLeftLookingDecomposition_DSCC(cp);
        return new LinearSolverQrLeftLooking_DSCC(qr);
    }

    @Override
    public DMatrixSparseCSC createA( int size ) {
        int cols = size;
        int rows = size + rand.nextInt(6);

//        int nz = RandomMatrices_DSCC.nonzero(rows,cols,0.05,0.6,rand);
//
//        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(rows,cols,nz,rand);
//        RandomMatrices_DSCC.ensureNotSingular(A,rand);

        DMatrixSparseCSC spd = RandomMatrices_DSCC.symmetricPosDef(cols, 0.25, rand);
        DMatrixSparseCSC top = new DMatrixSparseCSC(rows - cols, cols);
        for (int i = cols; i < rows; i++) {
            top.set(i - cols, i%cols, 1.0 + (double)(rand.nextDouble()*0.1));
        }
        DMatrixSparseCSC rect = new DMatrixSparseCSC(rows, cols);
        CommonOps_DSCC.concatRows(spd, top, rect);
        return rect;
    }
}