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

package org.ejml.sparse.csc.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_DSCC;
import org.ejml.sparse.csc.misc.ApplyFillReductionPermutation_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;

/**
 * Linear solver using a sparse Cholesky decomposition.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholesky_DSCC implements LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> {

    CholeskyUpLooking_DSCC cholesky;

    ApplyFillReductionPermutation_DSCC reduce;

    DGrowArray gb = new DGrowArray();
    DGrowArray gx = new DGrowArray();
    IGrowArray gw = new IGrowArray();

    DMatrixSparseCSC tmp = new DMatrixSparseCSC(1, 1, 1);
    DMatrixSparseCSC tmp2 = new DMatrixSparseCSC(1, 1, 1);

    // Number of rows in A
    int AnumRows, AnumCols;

    public LinearSolverCholesky_DSCC( CholeskyUpLooking_DSCC cholesky, @Nullable ComputePermutation<DMatrixSparseCSC> fillReduce ) {
        this.cholesky = cholesky;
        this.reduce = new ApplyFillReductionPermutation_DSCC(fillReduce, true);
    }

    @Override
    public boolean setA( DMatrixSparseCSC A ) {
        this.AnumRows = A.numRows;
        this.AnumCols = A.numCols;
        DMatrixSparseCSC C = reduce.apply(A);
        return cholesky.decompose(C);
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_DSCC.qualityTriangular(cholesky.getL());
    }

    @Override
    @SuppressWarnings("NullAway") // rowPinv is not null when the fill reduction has been applied
    public void solveSparse( DMatrixSparseCSC B, DMatrixSparseCSC X ) {
        X.reshape(AnumCols, B.numCols, X.numRows);

        IGrowArray gw1 = cholesky.getGw();

        DMatrixSparseCSC L = cholesky.getL();

        // Cholesky has no numeric pivots, so the row permutation is just the fill reduction (null if none)
        int[] rowPinv = reduce.fillRowPermInv();

        // apply the fill reduction permutation to B
        DMatrixSparseCSC Bp = B;
        if (reduce.isApplied()) {
            tmp2.reshape(B.numRows, B.numCols, B.nz_length);
            CommonOps_DSCC.permute(rowPinv, B, null, tmp2);
            Bp = tmp2;
        }

        tmp.reshape(L.numRows, B.numCols, 1);
        TriangularSolver_DSCC.solve(L, true, Bp, tmp, null, gx, gw, gw1);
        if (reduce.isApplied()) {
            // solve into scratch storage, then undo the fill reduction permutation
            tmp2.reshape(L.numRows, B.numCols, 1);
            TriangularSolver_DSCC.solveTran(L, true, tmp, tmp2, null, gx, gw, gw1);
            reduce.undoColumnPermutation(tmp2, X);
        } else {
            TriangularSolver_DSCC.solveTran(L, true, tmp, X, null, gx, gw, gw1);
        }
    }

    @Override
    public void setStructureLocked( boolean locked ) {
        cholesky.setStructureLocked(locked);
    }

    @Override
    public boolean isStructureLocked() {
        return cholesky.isStructureLocked();
    }

    @Override
    @SuppressWarnings("NullAway") // rowPinv is not null when the fill reduction has been applied
    public void solve( DMatrixRMaj B, DMatrixRMaj X ) {
        UtilEjml.checkReshapeSolve(AnumRows, AnumCols, B, X);

        DMatrixSparseCSC L = cholesky.getL();

        int N = L.numRows;

        double[] b = adjust(gb, N);
        double[] x = adjust(gx, N);

        // Cholesky has no numeric pivots, so the row permutation is just the fill reduction (null if none)
        int[] rowPinv = reduce.fillRowPermInv();

        for (int col = 0; col < B.numCols; col++) {
            int index = col;
            for (int i = 0; i < N; i++, index += B.numCols) b[i] = B.data[index];

            if (reduce.isApplied()) {
                CommonOps_DSCC.permuteInv(rowPinv, b, x, N);
                TriangularSolver_DSCC.solveL(L, x);
                TriangularSolver_DSCC.solveTranL(L, x);
                reduce.undoColumnPermutation(x, b, N);
            } else {
                TriangularSolver_DSCC.solveL(L, b);
                TriangularSolver_DSCC.solveTranL(L, b);
            }

            index = col;
            for (int i = 0; i < N; i++, index += X.numCols) X.data[index] = b[i];
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
