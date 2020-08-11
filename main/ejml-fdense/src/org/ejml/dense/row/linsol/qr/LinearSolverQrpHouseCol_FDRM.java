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

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.decomposition.TriangularSolver_FDRM;
import org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_FDRM;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_FDRM;

/**
 * <p>
 * Performs a pseudo inverse solver using the {@link org.ejml.dense.row.decomposition.qr.QRColPivDecompositionHouseholderColumn_FDRM} decomposition
 * directly.  For details on how the pseudo inverse is computed see {@link BaseLinearSolverQrp_FDRM}.
 * </p>
 * 
 * @author Peter Abeles
 */
public class LinearSolverQrpHouseCol_FDRM extends BaseLinearSolverQrp_FDRM {

    // Computes the QR decomposition
    private QRColPivDecompositionHouseholderColumn_FDRM decomposition;

    // storage for basic solution
    private FMatrixRMaj x_basic = new FMatrixRMaj(1,1);

    public LinearSolverQrpHouseCol_FDRM(QRColPivDecompositionHouseholderColumn_FDRM decomposition,
                                       boolean norm2Solution)
    {
        super(decomposition,norm2Solution);
        this.decomposition = decomposition;
    }

    @Override
    public void solve(FMatrixRMaj B, FMatrixRMaj X) {
        if( B.numRows != numRows )
            throw new IllegalArgumentException("Unexpected dimensions for X: X rows = "+X.numRows+" expected = "+numCols);
        X.reshape(numCols,B.numCols);

        int BnumCols = B.numCols;

        // get the pivots and transpose them
        int pivots[] = decomposition.getColPivots();
        
        float qr[][] = decomposition.getQR();
        float gammas[] = decomposition.getGammas();

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
                float u[] = qr[i];

                float vv = u[i];
                u[i] = 1;
                QrHelperFunctions_FDRM.rank1UpdateMultR(x_basic, u, gammas[i], 0, i, numRows, Y.data);
                u[i] = vv;
            }

            // solve for Rx = b using the standard upper triangular solver
            TriangularSolver_FDRM.solveU(R11.data, x_basic.data, rank);

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
