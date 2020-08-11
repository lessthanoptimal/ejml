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

package org.ejml.sparse.csc.linsol.qr;

import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_FSCC;
import org.ejml.sparse.csc.factory.FillReductionFactory_FSCC;
import org.ejml.sparse.csc.linsol.GenericLinearSolverSparseTests_FSCC;

/**
 * @author Peter Abeles
 */
public class TestLinearSolverQrLeftLooking_FSCC extends GenericLinearSolverSparseTests_FSCC {

    public TestLinearSolverQrLeftLooking_FSCC() {
        canHandleWide = false;
        canDecomposeZeros = false;
    }

    @Override
    public LinearSolverSparse<FMatrixSparseCSC, FMatrixRMaj> createSolver(FillReducing permutation) {
        ComputePermutation<FMatrixSparseCSC> cp = FillReductionFactory_FSCC.create(permutation);
        QrLeftLookingDecomposition_FSCC qr = new QrLeftLookingDecomposition_FSCC(cp);
        return new LinearSolverQrLeftLooking_FSCC(qr);
    }

    @Override
    public FMatrixSparseCSC createA(int size) {
        int cols = size;
        int rows = size + rand.nextInt(6);

//        int nz = RandomMatrices_FSCC.nonzero(rows,cols,0.05f,0.6f,rand);
//
//        FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(rows,cols,nz,rand);
//        RandomMatrices_FSCC.ensureNotSingular(A,rand);

        FMatrixSparseCSC spd = RandomMatrices_FSCC.symmetricPosDef(cols,0.25f,rand);
        FMatrixSparseCSC top = new FMatrixSparseCSC(rows-cols,cols);
        for (int i = cols; i < rows; i++) {
            top.set(i-cols,i%cols,1.0f + (float)(rand.nextFloat()*0.1f) );
        }
        FMatrixSparseCSC rect = new FMatrixSparseCSC(rows,cols);
        CommonOps_FSCC.concatRows(spd,top,rect);
        return rect;
    }
}