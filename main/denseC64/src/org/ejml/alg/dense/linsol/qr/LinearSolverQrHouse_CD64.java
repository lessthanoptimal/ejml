/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decompose.CTriangularSolver;
import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholder_CD64;
import org.ejml.alg.dense.linsol.LinearSolverAbstract_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CSpecializedOps;


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
 * @author Peter Abeles
 */
public class LinearSolverQrHouse_CD64 extends LinearSolverAbstract_CD64 {

    private QRDecompositionHouseholder_CD64 decomposer;

    private double []a,u;

    private int maxRows = -1;

    private CDenseMatrix64F QR;
    private double gammas[];

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouse_CD64() {
        decomposer = new QRDecompositionHouseholder_CD64();


    }

    public void setMaxSize( int maxRows ) {
        this.maxRows = maxRows;

        a = new double[ maxRows*2 ];
        u = new double[ maxRows*2 ];
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(CDenseMatrix64F A) {
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
        return CSpecializedOps.qualityTriangular(QR);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve(CDenseMatrix64F B, CDenseMatrix64F X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                int indexB = (i*BnumCols + colB)*2;
                a[i*2]   = B.data[indexB];
                a[i*2+1] = B.data[indexB+1];
            }

            // Solve Qa=b
            // a = Q'b
            // a = Q_{n-1}...Q_2*Q_1*b
            //
            // Q_n*b = (I-gamma*u*u^H)*b = b - u*(gamma*U^H*b)
            for( int n = 0; n < numCols; n++ ) {
                u[n*2] = 1;
                u[n*2+1] = 0;

                double realUb = a[2*n];
                double imagUb = a[2*n+1];
                // U^H*b
                for( int i = n+1; i < numRows; i++ ) {
                    int indexQR = (i*QR.numCols+n)*2;
                    double realU = u[i*2]   = QR.data[indexQR];
                    double imagU = u[i*2+1] = QR.data[indexQR+1];

                    double realB = a[i*2];
                    double imagB = a[i*2+1];

                    realUb += realU*realB + imagU*imagB;
                    imagUb += realU*imagB - imagU*realB;
                }

                // gamma*U^H*b
                realUb *= gammas[n];
                imagUb *= gammas[n];

                for( int i = n; i < numRows; i++ ) {
                    double realU = u[i*2];
                    double imagU = u[i*2+1];

                    a[i*2  ] -= realU*realUb - imagU*imagUb;
                    a[i*2+1] -= realU*imagUb + imagU*realUb;
                }
            }

            // solve for Rx = b using the standard upper triangular solver
            CTriangularSolver.solveU(QR.data, a, numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                int indexX = (i*X.numCols+colB)*2;

                X.data[indexX]   = a[i*2];
                X.data[indexX+1] = a[i*2+1];
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
    public QRDecomposition<CDenseMatrix64F> getDecomposition() {
        return decomposer;
    }
}
