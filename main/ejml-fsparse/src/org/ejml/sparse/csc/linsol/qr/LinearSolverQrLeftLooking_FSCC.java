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

import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.decomposition.qr.QrHelperFunctions_FSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_FSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_FSCC;

import static org.ejml.UtilEjml.adjust;

/**
 * Sparse linear solver implemented using {@link QrLeftLookingDecomposition_FSCC}.
 *
 * @author Peter Abeles
 */
public class LinearSolverQrLeftLooking_FSCC implements LinearSolverSparse<FMatrixSparseCSC,FMatrixRMaj> {

    private QrLeftLookingDecomposition_FSCC qr;
    private int m,n;

    private FGrowArray gb = new FGrowArray();
    private FGrowArray gbp = new FGrowArray();
    private FGrowArray gx = new FGrowArray();
    private IGrowArray gw = new IGrowArray();

    private FMatrixSparseCSC tmp = new FMatrixSparseCSC(1,1,1);

    public LinearSolverQrLeftLooking_FSCC(QrLeftLookingDecomposition_FSCC qr) {
        this.qr = qr;
    }

    @Override
    public boolean setA(FMatrixSparseCSC A) {
        if( A.numCols > A.numRows )
            throw new IllegalArgumentException("Can't handle wide matrices");
        this.m = A.numRows;
        this.n = A.numCols;
        return qr.decompose(A) && !qr.isSingular();
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_FSCC.qualityTriangular(qr.getR());
    }

    @Override
    public void solveSparse(FMatrixSparseCSC B, FMatrixSparseCSC X) {
        IGrowArray gw1 = qr.getGwork();

        // Don't modify the input
        tmp.set(B);
        B = tmp;
        FMatrixSparseCSC B_tmp = B.createLike();
        FMatrixSparseCSC swap;

        // Apply permutation to B
        int pinv[] = qr.getStructure().getPinv();
        CommonOps_FSCC.permuteRowInv(pinv,B,B_tmp);
        swap = B_tmp;
        B_tmp = B;
        B = swap;

        // Apply house holders to B
        FMatrixSparseCSC V = qr.getV();
        for (int i = 0; i < n; i++) {
            QrHelperFunctions_FSCC.rank1UpdateMultR(V,i,qr.getBeta(i),B,B_tmp,gw,gx);
            swap = B_tmp;
            B_tmp = B;
            B = swap;
        }

        // Solve for X
        FMatrixSparseCSC R = qr.getR();
        TriangularSolver_FSCC.solve(R,false,B,X,null,gx,gw,gw1);
    }

    @Override
    public void setStructureLocked( boolean locked ) {
        qr.setStructureLocked( locked );
    }

    @Override
    public boolean isStructureLocked() {
        return qr.isStructureLocked();
    }

    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        float[] b = adjust(gb,B.numRows);
        float[] bp = adjust(gbp,B.numRows);
        float[] x = adjust(gx,n);

        int[] pinv = qr.getStructure().getPinv();

        // process each column in X and B individually
        for (int colX = 0; colX < B.numCols; colX++) {
            int index = colX;
            for( int i = 0; i < B.numRows; i++ , index += X.numCols ) b[i] = B.data[index];

            // apply row pivots
            CommonOps_FSCC.permuteInv(pinv, b, bp, m);

            // apply Householder reflectors
            for (int j = 0; j < n; j++) {
                QrHelperFunctions_FSCC.applyHouseholder(qr.getV(),j,qr.getBeta(j),bp);
            }
            // Solve for R*x = b
            TriangularSolver_FSCC.solveU(qr.getR(),bp);

            // undo the permutation
            float out[];
            if( qr.isFillPermutated()) {
                CommonOps_FSCC.permute(qr.getFillPermutation(), bp, x, X.numRows);
                out = x;
            } else {
                out = bp;
            }

            index = colX;
            for( int i = 0; i < X.numRows; i++ , index += X.numCols ) X.data[index] = out[i];
        }
    }

    @Override
    public boolean modifiesA() {
        return qr.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public <D extends DecompositionInterface> D getDecomposition() {
        return (D)qr;
    }
}
