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

package org.ejml.dense.row.linsol.qr;

import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_CDRM;
import org.ejml.dense.row.decompose.TriangularSolver_CDRM;
import org.ejml.dense.row.decompose.qr.QRDecompositionHouseholderColumn_CDRM;
import org.ejml.dense.row.decompose.qr.QrHelperFunctions_CDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_CDRM;


/**
 * <p>
 * QR decomposition can be used to solve for systems.  However, this is not as computationally efficient
 * as LU decomposition and costs about 3n<sup>2</sup> flops.
 * </p>
 * <p>
 * It solve for x by first multiplying b by the transpose of Q then solving for the result.
 * <br>
 * QRx=b<br>
 * Rx=Q^H b<br>
 * </p>
 *
 * <p>
 * A column major decomposition is used in this solver.
 * <p>
 *
 * @author Peter Abeles
 */
public class LinearSolverQrHouseCol_CDRM extends LinearSolverAbstract_CDRM {

    private QRDecompositionHouseholderColumn_CDRM decomposer;

    private CMatrixRMaj a = new CMatrixRMaj(1,1);
    private CMatrixRMaj temp = new CMatrixRMaj(1,1);

    protected int maxRows = -1;
    protected int maxCols = -1;

    private float[][] QR; // a column major QR matrix
    private CMatrixRMaj R = new CMatrixRMaj(1,1);
    private float gammas[];

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouseCol_CDRM() {
        decomposer = new QRDecompositionHouseholderColumn_CDRM();
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
    public boolean setA(CMatrixRMaj A) {
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
        return SpecializedOps_CDRM.qualityTriangular(R);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve(CMatrixRMaj B, CMatrixRMaj X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;
        
        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                int indexB = (i*BnumCols + colB)*2;
                a.data[i*2]   = B.data[indexB];
                a.data[i*2+1] = B.data[indexB+1];
            }

            // Solve Qa=b
            // a = Q'b
            // a = Q_{n-1}...Q_2*Q_1*b
            //
            // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
            for( int n = 0; n < numCols; n++ ) {
                float []u = QR[n];

                float realVV = u[n*2];
                float imagVV = u[n*2+1];

                u[n*2]   = 1;
                u[n*2+1] = 0;

                QrHelperFunctions_CDRM.rank1UpdateMultR(a, u,0, gammas[n], 0, n, numRows, temp.data);

                u[n*2]   = realVV;
                u[n*2+1] = imagVV;
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_CDRM.solveU(R.data, a.data, numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                int indexB = (i*BnumCols + colB)*2;
                X.data[indexB]   = a.data[i*2];
                X.data[indexB+1] = a.data[i*2+1];
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
    public QRDecompositionHouseholderColumn_CDRM getDecomposition() {
        return decomposer;
    }
}