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

import org.ejml.alg.dense.decomposition.QRPDecomposition;
import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.linsol.LinearSolverAbstract;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SpecializedOps;

/**
 * <p>
 * A pseudo inverse solver for a generic QR column pivot decomposition algorithm.  This will in general be a bit slower than the
 * specialized once since the full Q, R, and P matrices need to be extracted.
 * </p>
 * <p>
 * It solve for x by first multiplying b by the transpose of Q then solving for the result.
 * <br>
 * Q*R*P<sup>T</sup>x=b<br>
 * </p>
 *
 * <p>
 * Implementation of the basic solution for pseudo inverse.  See page 258-259 in
 * Gene H. Golub and Charles F. Van Loan "Matrix Computations" 3rd Ed, 1996
 * </p>
 * 
 * @author Peter Abeles
 */
public class LinearSolverQrp extends LinearSolverAbstract {

    QRPDecomposition<DenseMatrix64F> decomposition;

    DenseMatrix64F Q=new DenseMatrix64F(1,1);
    DenseMatrix64F R=new DenseMatrix64F(1,1);

    // store an identity matrix for computing the inverse
    DenseMatrix64F I = new DenseMatrix64F(1,1);

    // stores the upper left square triangle matrix
    DenseMatrix64F R11 = new DenseMatrix64F(1,1);

    private DenseMatrix64F Y=new DenseMatrix64F(1,1);
    private DenseMatrix64F Z=new DenseMatrix64F(1,1);

    public LinearSolverQrp(QRPDecomposition<DenseMatrix64F> decomposition) {
        if( decomposition.inputModified() )
            throw new RuntimeException("Modify this class so that it creates a copy of A");

        this.decomposition = decomposition;
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);
        if( !decomposition.decompose(A) )
            return false;

        Q.reshape(A.numRows, A.numRows);
        R.reshape(A.numRows, A.numCols);

        decomposition.getQ(Q, false);
        decomposition.getR(R, false);

        return true;
    }

    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(true, R);
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;

        Y.reshape(numRows,1, false);
        Z.reshape(numRows,1, false);

        // get the pivots and transpose them
        int pivots[] = decomposition.getPivots();

        int rank = decomposition.getRank();
        
        R11.reshape(rank, rank);

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                Y.data[i] = B.get(i,colB);
            }

            // Solve Qa=b
            // a = Q'b
            CommonOps.multTransA(Q, Y, Z);

            // extract the r11 triangle sub matrix
            CommonOps.extract(R,0,rank,0,rank,R11,0,0);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver.solveU(R11.data, Z.data, rank);

            // save the results
            for( int i = 0; i < rank; i++ ) {
                X.set(pivots[i],colB,Z.data[i]);
            }
            for( int i = rank; i < numCols; i++ ) {
                X.set(pivots[i],colB,0);
            }
        }
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        if( A_inv.numCols != A.numRows || A_inv.numRows != A.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for A_inv");
        
        I.reshape(A_inv.numCols, A_inv.numCols);
        CommonOps.setIdentity(I);

        solve(I, A_inv);
    }

    @Override
    public boolean modifiesA() {
        return decomposition.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }
}
