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
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholder_FDRM;
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
 * @author Peter Abeles
 */
public class LinearSolverQrHouse_FDRM extends LinearSolverAbstract_FDRM {

    private QRDecompositionHouseholder_FDRM decomposer;

    private float []a,u;

    private int maxRows = -1;

    private FMatrixRMaj QR;
    private float gammas[];

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouse_FDRM() {
        decomposer = new QRDecompositionHouseholder_FDRM();


    }

    public void setMaxSize( int maxRows ) {
        this.maxRows = maxRows;

        a = new float[ maxRows ];
        u = new float[ maxRows ];
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(FMatrixRMaj A) {
        if( A.numRows > maxRows ) {
            setMaxSize(A.numRows);
        }

        _setA(A);
        if( !decomposer.decompose(A) )
            return false;
        
        gammas = decomposer.getGammas();
        QR = decomposer.getQR();

        return true;
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_FDRM.qualityTriangular(QR);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        if( B.numRows != numRows )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        X.reshape(numCols,B.numCols);

        int BnumCols = B.numCols;

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                a[i] = B.data[i*BnumCols + colB];
            }

            // Solve Qa=b
            // a = Q'b
            // a = Q_{n-1}...Q_2*Q_1*b
            //
            // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
            for( int n = 0; n < numCols; n++ ) {
                u[n] = 1;
                float ub = a[n];
                // U^T*b
                for( int i = n+1; i < numRows; i++ ) {
                    ub += (u[i] = QR.unsafe_get(i,n))*a[i];
                }

                // gamma*U^T*b
                ub *= gammas[n];
 
                for( int i = n; i < numRows; i++ ) {
                    a[i] -= u[i]*ub;
                }
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_FDRM.solveU(QR.data,a,numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.data[i*X.numCols+colB] = a[i];
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
