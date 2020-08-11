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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_FDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_FDRM;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_FDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_FDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * <p>
 * QR decomposition can be used to solve for systems.  However, this is not as computationally efficient
 * as LU decomposition and costs about 3n<sup>2</sup> flops.
 * </p>
 * <p>
 * It solve for x by first multiplying b by the transpose of Q then solving for the result.
 * <br>
 * QRx=b<br>
 * Rx=Q^T b<br>
 * </p>
 *
 * <p>
 * A column major decomposition is used in this solver.
 * <p>
 *
 * @author Peter Abeles
 */
public class LinearSolverQrHouseCol_FDRM extends LinearSolverAbstract_FDRM {

    private QRDecompositionHouseholderColumn_FDRM decomposer;

    private FMatrixRMaj a = new FMatrixRMaj(1,1);
    private FMatrixRMaj temp = new FMatrixRMaj(1,1);

    protected int maxRows = -1;
    protected int maxCols = -1;

    private float[][] QR; // a column major QR matrix
    private FMatrixRMaj R = new FMatrixRMaj(1,1);
    private float gammas[];

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouseCol_FDRM() {
        decomposer = new QRDecompositionHouseholderColumn_FDRM();
    }

    public void setMaxSize( int maxRows , int maxCols )
    {
        this.maxRows = maxRows; this.maxCols = maxCols;
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(FMatrixRMaj A) {
        if( A.numRows < A.numCols )
            throw new IllegalArgumentException("Can't solve for wide systems.  More variables than equations.");
        if( A.numRows > maxRows || A.numCols > maxCols )
            setMaxSize(A.numRows,A.numCols);

        R.reshape(A.numCols,A.numCols);
        a.reshape(A.numRows,1);
        temp.reshape(A.numRows,1);

        _setA(A);
        if( !decomposer.decompose(A) )
            return false;

        gammas = decomposer.getGammas();
        QR = decomposer.getQR();
        decomposer.getR(R,true);
        return true;
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_FDRM.qualityTriangular(R);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        if( B.numRows != numRows )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numRows);
        X.reshape(numCols,B.numCols);

        int BnumCols = B.numCols;
        
        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                a.data[i] = B.data[i*BnumCols + colB];
            }

            // Solve Qa=b
            // a = Q'b
            // a = Q_{n-1}...Q_2*Q_1*b
            //
            // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
            for( int n = 0; n < numCols; n++ ) {
                float []u = QR[n];

                float vv = u[n];
                u[n] = 1;
                QrHelperFunctions_FDRM.rank1UpdateMultR(a, u, gammas[n], 0, n, numRows, temp.data);
                u[n] = vv;
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_FDRM.solveU(R.data,a.data,numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.data[i*X.numCols+colB] = a.data[i];
            }
        }
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public QRDecomposition<FMatrixRMaj> getDecomposition() {
        return decomposer;
    }
}