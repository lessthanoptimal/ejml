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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.interfaces.decomposition.QRPDecomposition_F64;

/**
 * <p>
 * A pseudo inverse solver for a generic QR column pivot decomposition algorithm.  See
 * {@link BaseLinearSolverQrp_DDRM} for technical details on the algorithm.
 * </p>
 *
 * @author Peter Abeles
 */
public class SolvePseudoInverseQrp_DDRM extends BaseLinearSolverQrp_DDRM {

    // stores the orthogonal Q matrix from QR decomposition
    private DMatrixRMaj Q=new DMatrixRMaj(1,1);

    // storage for basic solution
    private DMatrixRMaj x_basic =new DMatrixRMaj(1,1);

    /**
     * Configure and provide decomposition
     *
     * @param decomposition Decomposition used.
     * @param norm2Solution If true the basic solution will be returned, false the minimal 2-norm solution.
     */
    public SolvePseudoInverseQrp_DDRM(QRPDecomposition_F64<DMatrixRMaj> decomposition,
                                     boolean norm2Solution) {
        super(decomposition,norm2Solution);
    }

    @Override
    public boolean setA(DMatrixRMaj A) {
        if( !super.setA(A))
            return false;

        Q.reshape(A.numRows, A.numRows);

        decomposition.getQ(Q, false);

        return true;
    }

    @Override
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
        if( B.numRows != numRows )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        X.reshape(numCols,B.numCols);


        int BnumCols = B.numCols;

        // get the pivots and transpose them
        int pivots[] = decomposition.getColPivots();
        
        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {
            x_basic.reshape(numRows, 1);
            Y.reshape(numRows,1);

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                Y.data[i] = B.get(i,colB);
            }

            // Solve Q*a=b => a = Q'*b
            CommonOps_DDRM.multTransA(Q, Y, x_basic);

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_DDRM.solveU(R11.data, x_basic.data, rank);

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
