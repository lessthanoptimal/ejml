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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.decomposition.TriangularSolver;
import org.ejml.alg.dense.linsol.LinearSolverAbstract_D64;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.decomposition.QRPDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.SpecializedOps;

/**
 * <p>
 * Base class for QR pivot based pseudo inverse classes.  It will return either the
 * basic of minimal 2-norm solution. See [1] for details.  The minimal 2-norm solution refers to the solution
 * 'x' whose 2-norm is the smallest making it unique, not some other error function.
 * </p>
 *
 * <p>
 * <pre>
 * R = [ R12  R12 ] r      P^T*x = [ y ] r       Q^T*b = [ c ] r
 *     [  0    0  ] m-r            [ z ] n -r            [ d ] m-r
 *        r   n-r
 *
 * where r is the rank of the matrix and (m,n) is the dimension of the linear system.
 * </pre>
 * </p>
 *
 * <p>
 * <pre>
 * The solution 'x' is found by solving the system below.  The basic solution is found by setting z=0
 *
 *     [ R_11^-1*(c - R12*z) ]
 * x = [          z          ]
 * </pre>
 * </p>
 *
 * <p>
 * NOTE: The matrix rank is determined using the provided QR decomposition. [1] mentions that this will not always
 * work and could cause some problems.
 * </p>
 *
 * <p>
 * [1] See page 258-259 in Gene H. Golub and Charles F. Van Loan "Matrix Computations" 3rd Ed, 1996
 * </p>
 *
 * @author Peter Abeles
 */
public abstract class BaseLinearSolverQrp_D64 extends LinearSolverAbstract_D64 {

    QRPDecomposition<DenseMatrix64F> decomposition;

    // if true then only the basic solution will be found
    protected boolean norm2Solution;

    protected DenseMatrix64F Y = new DenseMatrix64F(1,1);
    protected DenseMatrix64F R = new DenseMatrix64F(1,1);
    
    // stores sub-matrices inside the R matrix
    protected DenseMatrix64F R11 = new DenseMatrix64F(1,1);
    
    // store an identity matrix for computing the inverse
    protected DenseMatrix64F I = new DenseMatrix64F(1,1);

    // rank of the system matrix
    protected int rank;

    protected LinearSolver<DenseMatrix64F> internalSolver = LinearSolverFactory.leastSquares(1, 1);

    // used to compute optimal 2-norm solution
    private DenseMatrix64F W = new DenseMatrix64F(1,1);

    /**
     * Configures internal parameters.
     *
     * @param decomposition Used to solve the linear system.
     * @param norm2Solution If true then the optimal 2-norm solution will be computed for degenerate systems.
     */
    protected BaseLinearSolverQrp_D64(QRPDecomposition<DenseMatrix64F> decomposition,
                                      boolean norm2Solution)
    {
        this.decomposition = decomposition;
        this.norm2Solution = norm2Solution;

        if( internalSolver.modifiesA() )
            internalSolver = new LinearSolverSafe<DenseMatrix64F>(internalSolver);
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        _setA(A);

        if( !decomposition.decompose(A) )
            return false;

        rank = decomposition.getRank();

        R.reshape(numRows,numCols);
        decomposition.getR(R,false);

        // extract the r11 triangle sub matrix
        R11.reshape(rank, rank);
        CommonOps.extract(R, 0, rank, 0, rank, R11, 0, 0);

        if( norm2Solution && rank < numCols ) {
            // extract the R12 sub-matrix
            W.reshape(rank,numCols - rank);
            CommonOps.extract(R,0,rank,rank,numCols,W,0,0);

            // W=inv(R11)*R12
            TriangularSolver.solveU(R11.data, 0, R11.numCols, R11.numCols, W.data, 0, W.numCols, W.numCols);

            // set the identity matrix in the upper portion
            W.reshape(numCols, W.numCols,true);

            for( int i = 0; i < numCols-rank; i++ ) {
                for( int j = 0; j < numCols-rank; j++ ) {
                    if( i == j )
                        W.set(i+rank,j,-1);
                    else
                        W.set(i+rank,j,0);
                }
            }
        }

        return true;
    }

    @Override
    public double quality() {
        return SpecializedOps.qualityTriangular(R);
    }

    /**
     * <p>
     * Upgrades the basic solution to the optimal 2-norm solution.
     * </p>
     *
     * <pre>
     * First solves for 'z'
     *
     *       || x_b - P*[ R_11^-1 * R_12 ] * z ||2
     * min z ||         [ - I_{n-r}      ]     ||
     *
     * </pre>
     *
     * @param X basic solution, also output solution
     */
    protected void upgradeSolution( DenseMatrix64F X ) {
        DenseMatrix64F z = Y; // recycle Y

        // compute the z which will minimize the 2-norm of X
        // because of the identity matrix tacked onto the end 'A' should never be singular
        if( !internalSolver.setA(W) )
            throw new RuntimeException("This should never happen.  Is input NaN?");
        z.reshape(numCols-rank,1);
        internalSolver.solve(X, z);

        // compute X by tweaking the original
        CommonOps.multAdd(-1, W, z, X);
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        if( A_inv.numCols != numRows || A_inv.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for A_inv");

        I.reshape(numRows, numRows);
        CommonOps.setIdentity(I);

        solve(I, A_inv);
    }

    public QRPDecomposition<DenseMatrix64F> getDecomposition() {
        return decomposition;
    }
}
