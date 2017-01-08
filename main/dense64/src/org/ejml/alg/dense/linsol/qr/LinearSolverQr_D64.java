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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decomposition.TriangularSolver_D64;
import org.ejml.alg.dense.linsol.LinearSolverAbstract_D64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.SpecializedOps_D64;


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
public class LinearSolverQr_D64 extends LinearSolverAbstract_D64 {

    private QRDecomposition<RowMatrix_F64> decomposer;

    protected int maxRows = -1;
    protected int maxCols = -1;

    protected RowMatrix_F64 Q;
    protected RowMatrix_F64 R;

    private RowMatrix_F64 Y,Z;

    /**
     * Creates a linear solver that uses QR decomposition.
     *
     */
    public LinearSolverQr_D64(QRDecomposition<RowMatrix_F64> decomposer) {
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

        Q = new RowMatrix_F64(maxRows,maxRows);
        R = new RowMatrix_F64(maxRows,maxCols);

        Y = new RowMatrix_F64(maxRows,1);
        Z = new RowMatrix_F64(maxRows,1);
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(RowMatrix_F64 A) {
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
        return SpecializedOps_D64.qualityTriangular(R);
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve(RowMatrix_F64 B, RowMatrix_F64 X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

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
            CommonOps_D64.multTransA(Q,Y,Z);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_D64.solveU(R.data,Z.data,numCols);

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
    public QRDecomposition<RowMatrix_F64> getDecomposition() {
        return decomposer;
    }

    public QRDecomposition<RowMatrix_F64> getDecomposer() {
        return decomposer;
    }

    public RowMatrix_F64 getQ() {
        return Q;
    }

    public RowMatrix_F64 getR() {
        return R;
    }
}