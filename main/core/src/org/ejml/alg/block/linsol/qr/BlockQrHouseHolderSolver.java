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

package org.ejml.alg.block.linsol.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.BlockTriangularSolver;
import org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.SpecializedOps;


/**
 * <p>
 * A solver for {@link org.ejml.alg.block.decomposition.qr.QRDecompositionHouseholder_B64}.  Systems are solved for using the standard
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
public class BlockQrHouseHolderSolver implements LinearSolver<BlockMatrix64F> {

    // QR decomposition algorithm
    protected QRDecompositionHouseholder_B64 decomp = new QRDecompositionHouseholder_B64();

    // the input matrix which has been decomposed
    protected BlockMatrix64F QR;


    public BlockQrHouseHolderSolver() {
        decomp.setSaveW(false);
    }

    /**
     * Computes the QR decomposition of A and store the results in A.
     *
     * @param A The A matrix in the linear equation. Modified. Reference saved.
     * @return true if the decomposition was successful.
     */
    @Override
    public boolean setA(BlockMatrix64F A) {
        if( A.numRows < A.numCols )
            throw new IllegalArgumentException("Number of rows must be more than or equal to the number of columns.  " +
                    "Can't solve an underdetermined system.");

        if( !decomp.decompose(A))
            return false;

        this.QR = decomp.getQR();

        return true;
    }

    /**
     * Computes the quality using diagonal elements the triangular R matrix in the QR decomposition.
     *
     * @return Solutions quality.
     */
    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(decomp.getQR());
    }

    @Override
    public void solve(BlockMatrix64F B, BlockMatrix64F X) {

        if( B.numCols != X.numCols )
            throw new IllegalArgumentException("Columns of B and X do not match");
        if( QR.numCols != X.numRows )
            throw new IllegalArgumentException("Rows in X do not match the columns in A");
        if( QR.numRows != B.numRows )
            throw new IllegalArgumentException("Rows in B do not match the rows in A.");
        if( B.blockLength != QR.blockLength || X.blockLength != QR.blockLength )
            throw new IllegalArgumentException("All matrices must have the same block length.");

        // The system being solved for can be described as:
        // Q*R*X = B

        // First apply householder reflectors to B
        // Y = Q^T*B
        decomp.applyQTran(B);

        // Second solve for Y using the upper triangle matrix R and the just computed Y
        // X = R^-1 * Y
        BlockMatrixOps.extractAligned(B,X);

        // extract a block aligned matrix
        int M = Math.min(QR.numRows,QR.numCols);

        BlockTriangularSolver.solve(QR.blockLength,true,
                new D1Submatrix64F(QR,0,M,0,M),new D1Submatrix64F(X),false);

    }

    /**
     * Invert by solving for against an identity matrix.
     *
     * @param A_inv Where the inverted matrix saved. Modified.
     */
    @Override
    public void invert(BlockMatrix64F A_inv) {
        int M = Math.min(QR.numRows,QR.numCols);
        if( A_inv.numRows != M || A_inv.numCols != M )
            throw new IllegalArgumentException("A_inv must be square an have dimension "+M);


        // Solve for A^-1
        // Q*R*A^-1 = I

        // Apply householder reflectors to the identity matrix
        // y = Q^T*I = Q^T
        BlockMatrixOps.setIdentity(A_inv);
        decomp.applyQTran(A_inv);

        // Solve using upper triangular R matrix
        // R*A^-1 = y
        // A^-1 = R^-1*y
        BlockTriangularSolver.solve(QR.blockLength,true,
                new D1Submatrix64F(QR,0,M,0,M),new D1Submatrix64F(A_inv),false);
    }

    @Override
    public boolean modifiesA() {
        return decomp.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return true;
    }
}
