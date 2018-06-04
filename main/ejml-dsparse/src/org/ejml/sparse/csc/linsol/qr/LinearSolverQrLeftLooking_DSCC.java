/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.decomposition.qr.QrHelperFunctions_DSCC;
import org.ejml.sparse.csc.decomposition.qr.QrLeftLookingDecomposition_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;

/**
 * Sparse linear solver implemented using {@link QrLeftLookingDecomposition_DSCC}.
 *
 * @author Peter Abeles
 */
public class LinearSolverQrLeftLooking_DSCC implements LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> {

    private QrLeftLookingDecomposition_DSCC qr;
    private int m,n;

    private DGrowArray gb = new DGrowArray();
    private DGrowArray gbp = new DGrowArray();
    private DGrowArray gx = new DGrowArray();
    IGrowArray gw = new IGrowArray();

    DMatrixSparseCSC Bp = new DMatrixSparseCSC(1,1,1);
    DMatrixSparseCSC tmp = new DMatrixSparseCSC(1,1,1);

    public LinearSolverQrLeftLooking_DSCC(QrLeftLookingDecomposition_DSCC qr) {
        this.qr = qr;
    }

    @Override
    public boolean setA(DMatrixSparseCSC A) {
        if( A.numCols > A.numRows )
            throw new IllegalArgumentException("Can't handle wide matrices");
        this.m = A.numRows;
        this.n = A.numCols;
        return qr.decompose(A) && !qr.isSingular();
    }

    @Override
    public /**/double quality() {
        return TriangularSolver_DSCC.qualityTriangular(qr.getR());
    }

    @Override
    public void solveSparse(DMatrixSparseCSC B, DMatrixSparseCSC X) {

        throw new RuntimeException("Not yet supported. Triangular solve needs to be updated/fixed");
//        IGrowArray gw1 = qr.getGwork();
//
//        DMatrixSparseCSC Q = qr.getQ(null,false);
//        DMatrixSparseCSC R = qr.getR(null,false);
//
//        // TODO Apply householders instead of Q
//        // TODO use internal R from QR
//
//        // these are row pivots
//        Bp.reshape(B.numRows,B.numCols,B.nz_length);
//        int[] Pinv = qr.getStructure().getPinv();
//        CommonOps_DSCC.permute(Pinv,B,null,Bp);
//
//        tmp.reshape(Q.numRows,Bp.numCols,1);
//        CommonOps_DSCC.mult(Q,Bp,tmp,gw,gx);
//
//        TriangularSolver_DSCC.solve(R,false,tmp,X,null,gx,gw,gw1);
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
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
        double[] b = adjust(gb,B.numRows);
        double[] bp = adjust(gbp,B.numRows);
        double[] x = adjust(gx,X.numRows);

        int pinv[] = qr.getStructure().getPinv();

        // process each column in X and B individually
        for (int colX = 0; colX < X.numCols; colX++) {
            int index = colX;
            for( int i = 0; i < B.numRows; i++ , index += X.numCols ) b[i] = B.data[index];

            // apply row pivots
            CommonOps_DSCC.permuteInv(pinv, b, bp, m);

            // apply Householder reflectors
            for (int j = 0; j < n; j++) {
                QrHelperFunctions_DSCC.applyHouseholder(qr.getV(),j,qr.getBeta(j),bp);
            }
            // Solve for R*x = b
            TriangularSolver_DSCC.solveU(qr.getR(),bp);

            // undo the permutation
            double out[];
            if( qr.isFillPermutated()) {
                CommonOps_DSCC.permute(qr.getFillPermutation(), bp, x, X.numRows);
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
