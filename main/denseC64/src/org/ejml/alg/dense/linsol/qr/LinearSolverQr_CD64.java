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
import org.ejml.alg.dense.linsol.LinearSolverAbstract_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CSpecializedOps;


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
public class LinearSolverQr_CD64 extends LinearSolverAbstract_CD64 {

    private QRDecomposition<CDenseMatrix64F> decomposer;

    protected int maxRows = -1;
    protected int maxCols = -1;

    protected CDenseMatrix64F Q;
    protected CDenseMatrix64F Qt;
    protected CDenseMatrix64F R;

    private CDenseMatrix64F Y,Z;

    /**
     * Creates a linear solver that uses QR decomposition.
     *
     */
    public LinearSolverQr_CD64(QRDecomposition<CDenseMatrix64F> decomposer) {
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

        Q = new CDenseMatrix64F(maxRows,maxRows);
        Qt = new CDenseMatrix64F(maxRows,maxRows);
        R = new CDenseMatrix64F(maxRows,maxCols);

        Y = new CDenseMatrix64F(maxRows,1);
        Z = new CDenseMatrix64F(maxRows,1);
    }

    /**
     * Performs QR decomposition on A
     *
     * @param A not modified.
     */
    @Override
    public boolean setA(CDenseMatrix64F A) {
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
        CCommonOps.transposeConjugate(Q,Qt);

        return true;
    }

    @Override
    public double quality() {
        return CSpecializedOps.qualityTriangular(R);
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
            CCommonOps.mult(Qt, Y, Z);

            // solve for Rx = b using the standard upper triangular solver
            CTriangularSolver.solveU(R.data, Z.data, numCols);

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
    public QRDecomposition<CDenseMatrix64F> getDecomposition() {
        return decomposer;
    }

    public QRDecomposition<CDenseMatrix64F> getDecomposer() {
        return decomposer;
    }

    public CDenseMatrix64F getQ() {
        return Q;
    }

    public CDenseMatrix64F getR() {
        return R;
    }
}