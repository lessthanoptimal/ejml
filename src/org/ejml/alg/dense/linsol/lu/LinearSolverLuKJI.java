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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionBase;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;


/**
 * To avoid cpu cache issues the order in which the arrays are traversed have been changed.
 * There seems to be no performance benit relative to {@link LinearSolverLu} in this approach
 * and b and x can't be the same instance, which means it has slightly less functionality.
 *
 * @author Peter Abeles
 */
public class LinearSolverLuKJI extends LinearSolverLuBase {

    private double []dataLU;
    private int[] pivot;

    public LinearSolverLuKJI( LUDecompositionBase decomp ) {
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
        if( b.numCols != x.numCols && b.numRows != numCols && x.numRows != numCols) {
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