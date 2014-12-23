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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionBase_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;


/**
 * To avoid cpu cache issues the order in which the arrays are traversed have been changed.
 * There seems to be no performance benit relative to {@link LinearSolverLu_D64} in this approach
 * and b and x can't be the same instance, which means it has slightly less functionality.
 *
 * @author Peter Abeles
 */
public class LinearSolverLuKJI_D64 extends LinearSolverLuBase_D64 {

    private double []dataLU;
    private int[] pivot;

    public LinearSolverLuKJI_D64(LUDecompositionBase_D64 decomp) {
        super(decomp);

    }

    @Override
    public boolean setA(DenseMatrix64F A) {
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
     * @param b A matrix that is n by m.  Not modified.
     * @param x An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve(DenseMatrix64F b, DenseMatrix64F x) {
        if( b.numCols != x.numCols || b.numRows != numRows || x.numRows != numCols) {
            throw new IllegalArgumentException("Unexpected matrix size");
        }

        if( b != x ) {
            SpecializedOps.copyChangeRow(pivot,b,x);
        } else {
            throw new IllegalArgumentException("Current doesn't support using the same matrix instance");
        }

        // Copy right hand side with pivoting
        int nx = b.numCols;
        double[] dataX = x.data;

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