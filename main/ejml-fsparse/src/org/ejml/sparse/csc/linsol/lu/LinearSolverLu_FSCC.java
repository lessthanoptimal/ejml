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

package org.ejml.sparse.csc.linsol.lu;

import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_FSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_FSCC;

import static org.ejml.UtilEjml.adjust;

/**
 * LU Decomposition based solver for square matrices. Uses {@link LuUpLooking_FSCC} internally.
 *
 * @author Peter Abeles
 */
public class LinearSolverLu_FSCC implements LinearSolverSparse<FMatrixSparseCSC,FMatrixRMaj> {

    LuUpLooking_FSCC decomposition;

    private FGrowArray gx = new FGrowArray();
    private FGrowArray gb = new FGrowArray();

    FMatrixSparseCSC Bp = new FMatrixSparseCSC(1,1,1);
    FMatrixSparseCSC tmp = new FMatrixSparseCSC(1,1,1);

    public LinearSolverLu_FSCC(LuUpLooking_FSCC decomposition) {
        this.decomposition = decomposition;
    }

    @Override
    public boolean setA(FMatrixSparseCSC A) {
        return decomposition.decompose(A);
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_FSCC.qualityTriangular(decomposition.getU());
    }

    @Override
    public void solveSparse(FMatrixSparseCSC B, FMatrixSparseCSC X) {

        FMatrixSparseCSC L = decomposition.getL();
        FMatrixSparseCSC U = decomposition.getU();

        // these are row pivots
        Bp.reshape(B.numRows,B.numCols,B.nz_length);
        int[] Pinv = decomposition.getPinv();
        CommonOps_FSCC.permute(Pinv,B,null,Bp);

        IGrowArray gw = decomposition.getGw();
        IGrowArray gw1 = decomposition.getGxi();

        tmp.reshape(L.numRows,B.numCols,1);

        TriangularSolver_FSCC.solve(L,true,Bp,tmp,null,gx,gw,gw1);
        TriangularSolver_FSCC.solve(U,false,tmp,X,null,gx,gw,gw1);
    }

    @Override
    public void setStructureLocked(boolean locked ) {
        decomposition.setStructureLocked(locked);
    }

    @Override
    public boolean isStructureLocked() {
        return decomposition.isStructureLocked();
    }

    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
//        if( B.numCols != X.numCols || B.numRows != numRows || X.numRows != numCols) {
//            throw new IllegalArgumentException("Unexpected matrix size");
//        }

        int pinv[] = decomposition.getPinv();
        int q[] = decomposition.getReducePermutation();
        float[] x = adjust(gx,X.numRows);
        float[] b = adjust(gb,B.numRows);

        FMatrixSparseCSC L = decomposition.getL();
        FMatrixSparseCSC U = decomposition.getU();

        boolean reduceFill = decomposition.getReduceFill() != null;

        // process each column in X and B individually
        for (int colX = 0; colX < X.numCols; colX++) {
            int index = colX;
            for (int i = 0; i < B.numRows; i++, index += X.numCols) b[i] = B.data[index];

            CommonOps_FSCC.permuteInv(pinv,b,x,X.numRows);
            TriangularSolver_FSCC.solveL(L,x);
            TriangularSolver_FSCC.solveU(U,x);
            float d[];
            if( reduceFill ) {
                CommonOps_FSCC.permute(q, x, b, X.numRows);
                d = b;
            } else {
                d = x;
            }
            index = colX;
            for( int i = 0; i < X.numRows; i++ , index += X.numCols ) X.data[index] = d[i];
        }
    }

    @Override
    public boolean modifiesA() {
        return decomposition.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public <D extends DecompositionInterface> D getDecomposition() {
        return (D)decomposition;
    }
}
