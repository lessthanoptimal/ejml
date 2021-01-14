/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.dense.row.decomposition.qr.QRDecompositionHouseholderColumn_MT_DDRM;
import org.ejml.dense.row.decomposition.qr.QrHelperFunctions_DDRM;
import pabeles.concurrency.GrowArray;

/**
 * <p>
 * Concurrent extension of {@link LinearSolverQrHouseCol_DDRM}.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class LinearSolverQrHouseCol_MT_DDRM extends LinearSolverQrHouseCol_DDRM {

    GrowArray<Work> workArrays = new GrowArray<>(Work::new);

    /**
     * Creates a linear solver that uses QR decomposition.
     */
    public LinearSolverQrHouseCol_MT_DDRM() {
        super(new QRDecompositionHouseholderColumn_MT_DDRM());
    }

    /**
     * Solves for X using the QR decomposition.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is written to.  Modified.
     */
    @Override
    public void solve( DMatrixRMaj B, DMatrixRMaj X ) {
        UtilEjml.checkReshapeSolve(numRows, numCols, B, X);

        int BnumCols = B.numCols;

        // solve each column one by one
        EjmlConcurrency.loopBlocks(0, BnumCols, workArrays, ( work, idx0, idx1 ) -> {
            work.a.reshape(numRows, 1);
            work.tmp.reshape(numRows);

            DMatrixRMaj a = work.a;
            double[] temp = work.tmp.data;

            for (int colB = idx0; colB < idx1; colB++) {
                // make a copy of this column in the vector
                for (int i = 0; i < numRows; i++) {
                    a.data[i] = B.data[i*BnumCols + colB];
                }

                // Solve Qa=b
                // a = Q'b
                // a = Q_{n-1}...Q_2*Q_1*b
                //
                // Q_n*b = (I-gamma*u*u^T)*b = b - u*(gamma*U^T*b)
                for (int n = 0; n < numCols; n++) {
                    double[] u = QR[n];
                    QrHelperFunctions_DDRM.rank1UpdateMultR_u0(a, u, 1.0, gammas[n], 0, n, numRows, temp);
                }

                // solve for Rx = b using the standard upper triangular solver
                TriangularSolver_DDRM.solveU(R.data, a.data, numCols);

                // save the results
                for (int i = 0; i < numCols; i++) {
                    X.data[i*X.numCols + colB] = a.data[i];
                }
            }
        });
    }

    private static class Work {
        public final DMatrixRMaj a = new DMatrixRMaj(1, 1);
        public final DMatrixRMaj u = new DMatrixRMaj(1, 1);
        public final DGrowArray tmp = new DGrowArray();
    }
}
