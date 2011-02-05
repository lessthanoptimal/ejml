/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderTran;
import org.ejml.alg.dense.linsol.LinearSolverAbstract;
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
 * <p>
 * A column major decomposition is used in this solver.
 * <p>
 *
 * @author Peter Abeles
 */
public class LinearSolverQrHouseTran extends LinearSolverAbstract {

    private QRDecompositionHouseholderTran decomposer;

    private double []a;

    protected int maxRows = -1;
    protected int maxCols = -1;

    private DenseMatrix64F QR; // a column major QR matrix
    private DenseMatrix64F U;

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouseTran() {
        decomposer = new QRDecompositionHouseholderTran();
    }

    public void setMaxSize( int maxRows , int maxCols )
    {
        this.maxRows = maxRows; this.maxCols = maxCols;

        a = new double[ maxRows ];
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(DenseMatrix64F A) {
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
        return SpecializedOps.qualityTriangular(true, QR);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
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
                a[i] = B.data[i*BnumCols + colB];
            }

            // Solve Qa=b
            // a = Q'b
            // a = Q_{n-1}...Q_2*Q_1*b
            //
            // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
            for( int n = 0; n < numCols; n++ ) {
                int indexU = n*numRows + n + 1;

                double ub = a[n];
                // U^T*b
                for( int i = n+1; i < numRows; i++ , indexU++ ) {
                    ub += dataQR[indexU]*a[i];
                }

                // gamma*U^T*b
                ub *= gammas[n];

                a[n] -= ub;
                indexU = n*numRows + n + 1;
                for( int i = n+1; i < numRows; i++ , indexU++) {
                    a[i] -= dataQR[indexU]*ub;
                }
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver.solveU(U.data,a,numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.data[i*X.numCols+colB] = a[i];
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
}