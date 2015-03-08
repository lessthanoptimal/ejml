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
import org.ejml.alg.dense.decompose.qr.QRDecompositionHouseholderTran_CD64;
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
 * <p>
 * A column major decomposition is used in this solver.
 * <p>
 *
 * @author Peter Abeles
 */
public class LinearSolverQrHouseTran_CD64 extends LinearSolverAbstract_CD64 {

    private QRDecompositionHouseholderTran_CD64 decomposer;

    private double []a;

    protected int maxRows = -1;
    protected int maxCols = -1;

    private CDenseMatrix64F QR; // a column major QR matrix
    private CDenseMatrix64F U;

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouseTran_CD64() {
        decomposer = new QRDecompositionHouseholderTran_CD64();
    }

    public void setMaxSize( int maxRows , int maxCols )
    {
        this.maxRows = maxRows; this.maxCols = maxCols;

        a = new double[ maxRows*2 ];
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(CDenseMatrix64F A) {
        if( A.numRows > maxRows || A.numCols > maxCols )
            setMaxSize(A.numRows,A.numCols);

        _setA(A);
        if( !decomposer.decompose(A) )
            return false;

        QR = decomposer.getQR();
        return true;
    }

    @Override
    public double quality() {
        // even those it is transposed the diagonal elements are at the same
        // elements
        return CSpecializedOps.qualityTriangular(QR);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve(CDenseMatrix64F B, CDenseMatrix64F X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        U = decomposer.getR(U,true);
        final double gammas[] = decomposer.getGammas();
        final double dataQR[] = QR.data;

        final int BnumCols = B.numCols;

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
                int indexU = (n*numRows + n + 1)*2;

                double realUb = a[n*2];
                double imagUb = a[n*2+1];

                // U^H*b
                for( int i = n+1; i < numRows; i++ ) {
                    double realU = dataQR[indexU++];
                    double imagU = -dataQR[indexU++];

                    double realB = a[i*2];
                    double imagB = a[i*2+1];

                    realUb += realU*realB - imagU*imagB;
                    imagUb += realU*imagB + imagU*realB;
                }

                // gamma*U^T*b
                realUb *= gammas[n];
                imagUb *= gammas[n];

                a[n*2]   -= realUb;
                a[n*2+1] -= imagUb;

                indexU = (n*numRows + n + 1)*2;
                for( int i = n+1; i < numRows; i++) {
                    double realU = dataQR[indexU++];
                    double imagU = dataQR[indexU++];

                    a[i*2]   -= realU*realUb - imagU*imagUb;
                    a[i*2+1] -= realU*imagUb + imagU*realUb;
                }
            }

            // solve for Rx = b using the standard upper triangular solver
            CTriangularSolver.solveU(U.data, a, numCols);

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
        return decomposer.inputModified();
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