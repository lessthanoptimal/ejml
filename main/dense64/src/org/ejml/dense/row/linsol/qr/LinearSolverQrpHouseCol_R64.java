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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.decomposition.TriangularSolver_R64;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_R64;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_R64;

/**
 * <p>
 * Performs a pseudo inverse solver using the {@link org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_R64} decomposition
 * directly.  For details on how the pseudo inverse is computed see {@link BaseLinearSolverQrp_R64}.
 * </p>
 * 
 * @author Peter Abeles
 */
public class LinearSolverQrpHouseCol_R64 extends BaseLinearSolverQrp_R64 {

    // Computes the QR decomposition
    private QRColPivDecompositionHouseholderColumn_R64 decomposition;

    // storage for basic solution
    private DMatrixRow_F64 x_basic = new DMatrixRow_F64(1,1);

    public LinearSolverQrpHouseCol_R64(QRColPivDecompositionHouseholderColumn_R64 decomposition,
                                       boolean norm2Solution)
    {
        super(decomposition,norm2Solution);
        this.decomposition = decomposition;
    }

    @Override
    public void solve(DMatrixRow_F64 B, DMatrixRow_F64 X) {
        if( X.numRows != numCols )
            throw new IllegalArgumentException("Unexpected dimensions for X");
        else if( B.numRows != numRows || B.numCols != X.numCols )
            throw new IllegalArgumentException("Unexpected dimensions for B");

        int BnumCols = B.numCols;

        // get the pivots and transpose them
        int pivots[] = decomposition.getPivots();
        
        double qr[][] = decomposition.getQR();
        double gammas[] = decomposition.getGammas();

        // solve each column one by one
        for( int colB = 0; colB < BnumCols; colB++ ) {
            x_basic.reshape(numRows, 1);
            Y.reshape(numRows,1);

            // make a copy of this column in the vector
            for( int i = 0; i < numRows; i++ ) {
                x_basic.data[i] = B.get(i,colB);
            }

            // Solve Q*x=b => x = Q'*b
            // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
            for( int i = 0; i < rank; i++ ) {
                double u[] = qr[i];

                double vv = u[i];
                u[i] = 1;
                QrHelperFunctions_R64.rank1UpdateMultR(x_basic, u, gammas[i], 0, i, numRows, Y.data);
                u[i] = vv;
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_R64.solveU(R11.data, x_basic.data, rank);

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
