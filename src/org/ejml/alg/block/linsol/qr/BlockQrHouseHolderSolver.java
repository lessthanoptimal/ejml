/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.block.linsol.qr;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.block.BlockTriangularSolver;
import org.ejml.alg.block.decomposition.qr.BlockMatrix64HouseholderQR;
import org.ejml.alg.block.linsol.LinearSolverBlock;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.D1Submatrix64F;
import org.ejml.ops.SpecializedOps;


/**
 * <p>
 * A solver for {@link BlockMatrix64HouseholderQR}.  Systems are solved for using the standard
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
public class BlockQrHouseHolderSolver implements LinearSolverBlock {

    // QR decomposition algorithm
    protected BlockMatrix64HouseholderQR decomp = new BlockMatrix64HouseholderQR();

    // the input matrix which has been decomposed
    protected BlockMatrix64F QR;

    // block aligned triangular copies of B and U
    protected BlockMatrix64F tempB = new BlockMatrix64F(1,1,1);
    protected BlockMatrix64F tempU = new BlockMatrix64F(1,1,1);

    // can the input B matrix be modified?
    // If it can be then a matrix copy can be avoided
    protected boolean modifyB = false;

    public BlockQrHouseHolderSolver() {
        decomp.setSaveW(true);
    }

    public void setModifyB(boolean modifyB) {
        this.modifyB = modifyB;
    }

    @Override
    public BlockMatrix64F getA() {
        return QR;
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
        return SpecializedOps.qualityTriangular(true,decomp.getQR());
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

        //  Copy B since it can't be modified
        if( !modifyB ) {
            tempB.reshape(B.numRows,B.numCols,B.blockLength,false);
            tempB.set(B);
            B = tempB;
        }

        // The system being solved for can be described as:
        // Q*R*X = B

        // First apply householder reflectors to B
        // Y = Q^T*B
        decomp.applyQTran(B);

        // Second solve for Y using the upper triangle matrix R and the just computed Y
        // X = R^-1 * Y

        // BlockTriangularSolver.solve() can't handle a triangle which is a partial inner block.
        // to get around that issue both the triangle and X matrix need to be copied into another
        // matrix.
        // TODO make solvers that can handle partial blocks and not sacrifice speed

        BlockMatrixOps.extractAligned(B,X);

        // extract a block aligned matrix
        int M = Math.min(QR.numRows,QR.numCols);
        tempU.reshape(M,M,QR.blockLength,false);
        BlockMatrixOps.copyTriangle(true,QR,tempU);


        BlockTriangularSolver.solve(QR.blockLength,true,
                new D1Submatrix64F(tempU),new D1Submatrix64F(X),false);

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

        // Need to copy the R matrix since solve requires whole blocks
        // todo improve
        BlockMatrix64F U = new BlockMatrix64F(M,M,QR.blockLength);
        BlockMatrixOps.copyTriangle(true,QR,U);

        BlockTriangularSolver.solve(QR.blockLength,true,
                new D1Submatrix64F(U),new D1Submatrix64F(A_inv),false);
    }

    @Override
    public boolean inputModified() {
        return decomp.inputModified();
    }
}
