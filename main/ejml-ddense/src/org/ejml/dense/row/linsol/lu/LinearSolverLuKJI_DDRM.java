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

package org.ejml.dense.row.linsol.lu;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionBase_DDRM;


/**
 * To avoid cpu cache issues the order in which the arrays are traversed have been changed.
 * There seems to be no performance benit relative to {@link LinearSolverLu_DDRM} in this approach
 * and b and x can't be the same instance, which means it has slightly less functionality.
 *
 * @author Peter Abeles
 */
public class LinearSolverLuKJI_DDRM extends LinearSolverLuBase_DDRM {

    private double []dataLU;
    private int[] pivot;

    public LinearSolverLuKJI_DDRM(LUDecompositionBase_DDRM decomp) {
        super(decomp);

    }

    @Override
    public boolean setA(DMatrixRMaj A) {
        boolean ret = super.setA(A);

        pivot = decomp.getPivot();
        dataLU = decomp.getLU().data;

        return ret;
    }

    /**
     * An other implementation of solve() that processes the matrices in a different order.
     * It seems to have the same runtime performance as {@link #solve} and is more complicated.
     * It is being kept around to avoid future replication of work.
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve(DMatrixRMaj B, DMatrixRMaj X) {
        if( B.numRows != numRows) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }
        X.reshape(numCols,B.numCols);

        if( B != X ) {
            SpecializedOps_DDRM.copyChangeRow(pivot,B,X);
        } else {
            throw new IllegalArgumentException("Current doesn't support using the same matrix instance");
        }

        // Copy right hand side with pivoting
        int nx = B.numCols;
        double[] dataX = X.data;

        // Solve L*Y = B(piv,:)
        for (int k = 0; k < numCols; k++) {
            for (int i = k+1; i < numCols; i++) {
                for (int j = 0; j < nx; j++) {
                    dataX[i*nx+j] -= dataX[k*nx+j]*dataLU[i* numCols +k];
                }
            }
        }
        // Solve U*X = Y;
        for (int k = numCols -1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                dataX[k*nx+j] /= dataLU[k* numCols +k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    dataX[i*nx+j] -= dataX[k*nx+j]*dataLU[i* numCols +k];
                }
            }
        }
    }
}