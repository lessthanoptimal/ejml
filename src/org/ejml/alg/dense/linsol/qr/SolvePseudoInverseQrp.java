/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * <p>
 * A pseudo inverse solver for a generic QR column pivot decomposition algorithm.  See
 * {@link BaseLinearSolverQrp} for technical details on the algorithm.
 * </p>
 *
 * @author Peter Abeles
 */
public class SolvePseudoInverseQrp extends BaseLinearSolverQrp {

    // stores the orthogonal Q matrix from QR decomposition
    private DenseMatrix64F Q=new DenseMatrix64F(1,1);

    // storage for basic solution
    private DenseMatrix64F x_basic =new DenseMatrix64F(1,1);

    /**
     * Configure and provide decomposition
     *
     * @param decomposition Decomposition used.
     * @param norm2Solution If true the basic solution will be returned, false the minimal 2-norm solution.
     */
    public SolvePseudoInverseQrp(QRPDecomposition<DenseMatrix64F> decomposition,
                                 boolean norm2Solution) {
        super(decomposition,norm2Solution);
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        if( !super.setA(A))
            return false;

        Q.reshape(A.numRows, A.numRows);

        decomposition.getQ(Q, false);

        return true;
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;

        // get the pivots and transpose them
        int pivots[] = decomposition.getPivots();
        
        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {
            x_basic.reshape(numRows, 1);
            Y.reshape(numRows,1);

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                Y.data[i] = B.get(i,colB);
            }

            // Solve Q*a=b => a = Q'*b
            CommonOps.multTransA(Q, Y, x_basic);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver.solveU(R11.data, x_basic.data, rank);

            // finish the basic solution by filling in zeros
            x_basic.reshape(numCols, 1, true);
            for( int i = rank; i < numCols; i++)
                x_basic.data[i] = 0;
            
            if( norm2Solution && rank < numCols )
                upgradeSolution(x_basic);
            
            // save the results
            for( int i = 0; i < numCols; i++ ) {
                X.set(pivots[i],colB,x_basic.data[i]);
            }
        }
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
