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
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_FDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_FDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;


/**
 * <p>
 * A solver for a generic QR decomposition algorithm.  This will in general be a bit slower than the
 * specialized once since the full Q and R matrices need to be extracted.
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
public class LinearSolverQr_FDRM extends LinearSolverAbstract_FDRM {

    private QRDecomposition<FMatrixRMaj> decomposer;

    protected int maxRows = -1;
    protected int maxCols = -1;

    protected FMatrixRMaj Q;
    protected FMatrixRMaj R;

    private FMatrixRMaj Y,Z;

    /**
     * Creates a linear solver that uses QR decomposition.
     *
     */
    public LinearSolverQr_FDRM(QRDecomposition<FMatrixRMaj> decomposer) {
        this.decomposer = decomposer;
    }

    /**
     * Changes the size of the matrix it can solve for
     *
     * @param maxRows Maximum number of rows in the matrix it will decompose.
     * @param maxCols Maximum number of columns in the matrix it will decompose.
     */
    public void setMaxSize( int maxRows , int maxCols )
    {
        this.maxRows = maxRows; this.maxCols = maxCols;

        Q = new FMatrixRMaj(maxRows,maxRows);
        R = new FMatrixRMaj(maxRows,maxCols);

        Y = new FMatrixRMaj(maxRows,1);
        Z = new FMatrixRMaj(maxRows,1);
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(FMatrixRMaj A) {
        if( A.numRows > maxRows || A.numCols > maxCols ) {
            setMaxSize(A.numRows,A.numCols);
        }

        _setA(A);
        if( !decomposer.decompose(A) )
            return false;

        Q.reshape(numRows,numRows, false);
        R.reshape(numRows,numCols, false);
        decomposer.getQ(Q,false);
        decomposer.getR(R,false);

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
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        X.reshape(numCols,B.numCols);

        int BnumCols = B.numCols;

        Y.reshape(numRows,1, false);
        Z.reshape(numRows,1, false);

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                Y.data[i] = B.get(i,colB);
            }

            // Solve Qa=b
            // a = Q'b
            CommonOps_FDRM.multTransA(Q,Y,Z);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_FDRM.solveU(R.data,Z.data,numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.set(i,colB,Z.data[i]);
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
    public QRDecomposition<FMatrixRMaj> getDecomposition() {
        return decomposer;
    }

    public QRDecomposition<FMatrixRMaj> getDecomposer() {
        return decomposer;
    }

    public FMatrixRMaj getQ() {
        return Q;
    }

    public FMatrixRMaj getR() {
        return R;
    }
}