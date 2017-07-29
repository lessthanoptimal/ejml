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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.sparse.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;

/**
 * Linear solver using a sparse Cholesky decomposition.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholesky_DSCC implements LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> {

    CholeskyUpLooking_DSCC cholesky;

    DGrowArray gb = new DGrowArray();
    DGrowArray gx = new DGrowArray();

    public LinearSolverCholesky_DSCC(CholeskyUpLooking_DSCC cholesky) {
        this.cholesky = cholesky;
    }

    @Override
    public boolean setA(DMatrixSparseCSC A) {
        return cholesky.decompose(A);
    }

    @Override
    public double quality() {
        return TriangularSolver_DSCC.qualityTriangular(cholesky.getL());
    }

    @Override
    public void lockStructure() {
        throw new RuntimeException("Implement");
    }

    @Override
    public boolean isStructureLocked() {
        return false;
    }

    @Override
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {

        DMatrixSparseCSC L = cholesky.getL();

        int N = L.numRows;

        double[] b = adjust(gb,N);
        double[] x = adjust(gx,N);

        int[] Pinv = cholesky.getPinv();

        for (int col = 0; col < B.numCols; col++) {
            int index = col;
            for( int i = 0; i < N; i++ , index += B.numCols ) b[i] = B.data[index];

            if( Pinv != null ) {
                CommonOps_DSCC.permuteInv(Pinv, b, x, N);
                TriangularSolver_DSCC.solveL(L, x);
                TriangularSolver_DSCC.solveTranL(L, x);
                CommonOps_DSCC.permute(Pinv, x, b, N);
            } else {
                TriangularSolver_DSCC.solveL(L, b);
                TriangularSolver_DSCC.solveTranL(L, b);
            }

            index = col;
            for( int i = 0; i < N; i++ , index += X.numCols ) X.data[index] = b[i];
        }
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
