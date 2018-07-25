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

package org.ejml.dense.block.linsol.qr;

import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.TriangularSolver_DDRB;
import org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_DDRB;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * <p>
 * A solver for {@link org.ejml.dense.block.decomposition.qr.QRDecompositionHouseholder_DDRB}.  Systems are solved for using the standard
 * QR decomposition method, sketched below.
 * </p>
 *
 * <p>
 * A = Q*R<br>
 * A*x = b<br>
 * Q*R*x = b <br>
 * R*x = y = Q<sup>T</sup>b<br>
 * x = R<sup>-1</sup>y<br>
 * <br>
 * Where A is the m by n matrix being decomposed. Q is an orthogonal matrix. R is upper triangular matrix.
 * </p>
 *
 * @author Peter Abeles
 */
public class QrHouseHolderSolver_DDRB implements LinearSolverDense<DMatrixRBlock> {

    // QR decomposition algorithm
    protected QRDecompositionHouseholder_DDRB decomposer = new QRDecompositionHouseholder_DDRB();

    // the input matrix which has been decomposed
    protected DMatrixRBlock QR;


    public QrHouseHolderSolver_DDRB() {
        decomposer.setSaveW(false);
    }

    /**
     * Computes the QR decomposition of A and store the results in A.
     *
     * @param A The A matrix in the linear equation. Modified. Reference saved.
     * @return true if the decomposition was successful.
     */
    @Override
    public boolean setA(DMatrixRBlock A) {
        if( A.numRows < A.numCols )
            throw new IllegalArgumentException("Number of rows must be more than or equal to the number of columns.  " +
                    "Can't solve an underdetermined system.");

        if( !decomposer.decompose(A))
            return false;

        this.QR = decomposer.getQR();

        return true;
    }

    /**
     * Computes the quality using diagonal elements the triangular R matrix in the QR decomposition.
     *
     * @return Solutions quality.
     */
    @Override
    public /**/double quality() {
        return SpecializedOps_DDRM.qualityTriangular(decomposer.getQR());
    }

    @Override
    public void solve(DMatrixRBlock B, DMatrixRBlock X) {

        if( B.numRows != QR.numRows )
            throw new IllegalArgumentException("Row of B and A do not match");

        X.reshape(QR.numCols,B.numCols);

        // The system being solved for can be described as:
        // Q*R*X = B

        // First apply householder reflectors to B
        // Y = Q^T*B
        decomposer.applyQTran(B);

        // Second solve for Y using the upper triangle matrix R and the just computed Y
        // X = R^-1 * Y
        MatrixOps_DDRB.extractAligned(B,X);

        // extract a block aligned matrix
        int M = Math.min(QR.numRows,QR.numCols);

        TriangularSolver_DDRB.solve(QR.blockLength,true,
                new DSubmatrixD1(QR,0,M,0,M),new DSubmatrixD1(X),false);

    }

    /**
     * Invert by solving for against an identity matrix.
     *
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    @Override
    public void invert(DMatrixRBlock A_inv) {
        int M = Math.min(QR.numRows,QR.numCols);
        if( A_inv.numRows != M || A_inv.numCols != M )
            throw new IllegalArgumentException("A_inv must be square an have dimension "+M);


        // Solve for A^-1
        // Q*R*A^-1 = I

        // Apply householder reflectors to the identity matrix
        // y = Q^T*I = Q^T
        MatrixOps_DDRB.setIdentity(A_inv);
        decomposer.applyQTran(A_inv);

        // Solve using upper triangular R matrix
        // R*A^-1 = y
        // A^-1 = R^-1*y
        TriangularSolver_DDRB.solve(QR.blockLength,true,
                new DSubmatrixD1(QR,0,M,0,M),new DSubmatrixD1(A_inv),false);
    }

    @Override
    public boolean modifiesA() {
        return decomposer.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return true;
    }

    @Override
    public QRDecomposition<DMatrixRBlock> getDecomposition() {
        return decomposer;
    }
}
