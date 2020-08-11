/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.decomposition.chol.CholeskyUpLooking_FSCC;
import org.ejml.sparse.csc.misc.ApplyFillReductionPermutation_FSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_FSCC;

import static org.ejml.UtilEjml.adjust;

/**
 * Linear solver using a sparse Cholesky decomposition.
 *
 * @author Peter Abeles
 */
public class LinearSolverCholesky_FSCC implements LinearSolverSparse<FMatrixSparseCSC,FMatrixRMaj> {

    CholeskyUpLooking_FSCC cholesky;

    ApplyFillReductionPermutation_FSCC reduce;

    FGrowArray gb = new FGrowArray();
    FGrowArray gx = new FGrowArray();
    IGrowArray gw = new IGrowArray();

    FMatrixSparseCSC tmp = new FMatrixSparseCSC(1,1,1);

    public LinearSolverCholesky_FSCC(CholeskyUpLooking_FSCC cholesky , ComputePermutation<FMatrixSparseCSC> fillReduce) {
        this.cholesky = cholesky;
        this.reduce = new ApplyFillReductionPermutation_FSCC(fillReduce,true);
    }

    @Override
    public boolean setA(FMatrixSparseCSC A) {
        FMatrixSparseCSC C = reduce.apply(A);
        return cholesky.decompose(C);
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_FSCC.qualityTriangular(cholesky.getL());
    }

    @Override
    public void solveSparse(FMatrixSparseCSC B, FMatrixSparseCSC X) {
        IGrowArray gw1 = cholesky.getGw();

        FMatrixSparseCSC L = cholesky.getL();

        tmp.reshape(L.numRows,B.numCols,1);
        int[] Pinv = reduce.getArrayPinv();

        TriangularSolver_FSCC.solve(L,true,B,tmp,Pinv,gx,gw,gw1);
        TriangularSolver_FSCC.solveTran(L,true,tmp,X,null,gx,gw,gw1);
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
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {

        FMatrixSparseCSC L = cholesky.getL();

        int N = L.numRows;

        float[] b = adjust(gb,N);
        float[] x = adjust(gx,N);

        int[] Pinv = reduce.getArrayPinv();

        for (int col = 0; col < B.numCols; col++) {
            int index = col;
            for( int i = 0; i < N; i++ , index += B.numCols ) b[i] = B.data[index];

            if( Pinv != null ) {
                CommonOps_FSCC.permuteInv(Pinv, b, x, N);
                TriangularSolver_FSCC.solveL(L, x);
                TriangularSolver_FSCC.solveTranL(L, x);
                CommonOps_FSCC.permute(Pinv, x, b, N);
            } else {
                TriangularSolver_FSCC.solveL(L, b);
                TriangularSolver_FSCC.solveTranL(L, b);
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
