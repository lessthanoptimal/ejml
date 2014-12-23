/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder_D64;
import org.ejml.alg.dense.linsol.LinearSolverAbstract_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;


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
public class LinearSolverQrHouse_D64 extends LinearSolverAbstract_D64 {

    private QRDecompositionHouseholder_D64 decomposer;

    private double []a,u;

    private int maxRows = -1;

    private DenseMatrix64F QR;
    private double gammas[];

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouse_D64() {
        decomposer = new QRDecompositionHouseholder_D64();


    }

    public void setMaxSize( int maxRows ) {
        this.maxRows = maxRows;

        a = new double[ maxRows ];
        u = new double[ maxRows ];
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(DenseMatrix64F A) {
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
    public double quality() {
        return SpecializedOps.qualityTriangular(QR);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

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
                double ub = a[n];
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
            TriangularSolver.solveU(QR.data,a,numCols);

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
}
