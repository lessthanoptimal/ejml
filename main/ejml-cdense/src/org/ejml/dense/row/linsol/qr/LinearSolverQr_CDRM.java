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
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.SpecializedOps_CDRM;
import org.ejml.dense.row.decompose.TriangularSolver_CDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_CDRM;
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
 * Rx=Q^H b<br>
 * </p>
 *
 * @author Peter Abeles
 */
public class LinearSolverQr_CDRM extends LinearSolverAbstract_CDRM {

    private QRDecomposition<CMatrixRMaj> decomposer;

    protected int maxRows = -1;
    protected int maxCols = -1;

    protected CMatrixRMaj Q;
    protected CMatrixRMaj Qt;
    protected CMatrixRMaj R;

    private CMatrixRMaj Y,Z;

    /**
     * Creates a linear solver that uses QR decomposition.
     *
     */
    public LinearSolverQr_CDRM(QRDecomposition<CMatrixRMaj> decomposer) {
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

        Q = new CMatrixRMaj(maxRows,maxRows);
        Qt = new CMatrixRMaj(maxRows,maxRows);
        R = new CMatrixRMaj(maxRows,maxCols);

        Y = new CMatrixRMaj(maxRows,1);
        Z = new CMatrixRMaj(maxRows,1);
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(CMatrixRMaj A) {
        if( A.numRows > maxRows || A.numCols > maxCols ) {
            setMaxSize(A.numRows,A.numCols);
        }

        _setA(A);
        if( !decomposer.decompose(A) )
            return false;

        Q.reshape(numRows,numRows);
        R.reshape(numRows,numCols);
        decomposer.getQ(Q,false);
        decomposer.getR(R,false);
        CommonOps_CDRM.transposeConjugate(Q,Qt);

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
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;

        Y.reshape(numRows,1);
        Z.reshape(numRows,1);

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                int indexB = B.getIndex(i,colB);
                Y.data[i*2]   = B.data[indexB];
                Y.data[i*2+1] = B.data[indexB+1];
            }

            // Solve Qa=b
            // a = Q'b
            CommonOps_CDRM.mult(Qt, Y, Z);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_CDRM.solveU(R.data, Z.data, numCols);

            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.set(i,colB,Z.data[i*2],Z.data[i*2+1]);
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
    public QRDecomposition<CMatrixRMaj> getDecomposition() {
        return decomposer;
    }

    public QRDecomposition<CMatrixRMaj> getDecomposer() {
        return decomposer;
    }

    public CMatrixRMaj getQ() {
        return Q;
    }

    public CMatrixRMaj getR() {
        return R;
    }
}