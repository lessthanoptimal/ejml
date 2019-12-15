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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.decomposition.lu.LuUpLooking_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import static org.ejml.UtilEjml.adjust;

/**
 * LU Decomposition based solver for square matrices. Uses {@link LuUpLooking_DSCC} internally.
 *
 * @author Peter Abeles
 */
public class LinearSolverLu_DSCC implements LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> {

    LuUpLooking_DSCC decomposition;

    private DGrowArray gx = new DGrowArray();
    private DGrowArray gb = new DGrowArray();

    DMatrixSparseCSC Bp = new DMatrixSparseCSC(1,1,1);
    DMatrixSparseCSC tmp = new DMatrixSparseCSC(1,1,1);

    public LinearSolverLu_DSCC(LuUpLooking_DSCC decomposition) {
        this.decomposition = decomposition;
    }

    @Override
    public boolean setA(DMatrixSparseCSC A) {
        return decomposition.decompose(A);
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_DSCC.qualityTriangular(decomposition.getU());
    }

    @Override
    public void solveSparse(DMatrixSparseCSC B, DMatrixSparseCSC X) {

        DMatrixSparseCSC L = decomposition.getL();
        DMatrixSparseCSC U = decomposition.getU();

        // these are row pivots
        Bp.reshape(B.numRows,B.numCols,B.nz_length);
        int[] Pinv = decomposition.getPinv();
        CommonOps_DSCC.permute(Pinv,B,null,Bp);

        IGrowArray gw = decomposition.getGw();
        IGrowArray gw1 = decomposition.getGxi();

        tmp.reshape(L.numRows,B.numCols,1);

        TriangularSolver_DSCC.solve(L,true,Bp,tmp,null,gx,gw,gw1);
        TriangularSolver_DSCC.solve(U,false,tmp,X,null,gx,gw,gw1);
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
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
//        if( B.numCols != X.numCols || B.numRows != numRows || X.numRows != numCols) {
//            throw new IllegalArgumentException("Unexpected matrix size");
//        }

        int pinv[] = decomposition.getPinv();
        int q[] = decomposition.getReducePermutation();
        double[] x = adjust(gx,X.numRows);
        double[] b = adjust(gb,B.numRows);

        DMatrixSparseCSC L = decomposition.getL();
        DMatrixSparseCSC U = decomposition.getU();

        boolean reduceFill = decomposition.getReduceFill() != null;

        // process each column in X and B individually
        for (int colX = 0; colX < X.numCols; colX++) {
            int index = colX;
            for (int i = 0; i < B.numRows; i++, index += X.numCols) b[i] = B.data[index];

            CommonOps_DSCC.permuteInv(pinv,b,x,X.numRows);
            TriangularSolver_DSCC.solveL(L,x);
            TriangularSolver_DSCC.solveU(U,x);
            double d[];
            if( reduceFill ) {
                CommonOps_DSCC.permute(q, x, b, X.numRows);
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
