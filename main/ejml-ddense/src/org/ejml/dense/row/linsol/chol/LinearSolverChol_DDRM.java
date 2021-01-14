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
package org.ejml.dense.row.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionCommon_DDRM;
import org.ejml.dense.row.linsol.LinearSolverAbstract_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;

/**
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class LinearSolverChol_DDRM extends LinearSolverAbstract_DDRM {

    CholeskyDecompositionCommon_DDRM decomposer;
    double[] vv;
    double[] t;

    public LinearSolverChol_DDRM( CholeskyDecompositionCommon_DDRM decomposer ) {
        this.decomposer = decomposer;
    }

    @Override
    public boolean setA( DMatrixRMaj A ) {
        if (A.numRows != A.numCols)
            throw new IllegalArgumentException("Matrix must be square");

        _setA(A);

        if (decomposer.decompose(A)) {
            vv = decomposer._getVV();
            t = decomposer.getT().data;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_DDRM.qualityTriangular(decomposer.getT());
    }

    /**
     * <p>
     * Using the decomposition, finds the value of 'X' in the linear equation below:<br>
     *
     * A*x = b<br>
     *
     * where A has dimension of n by n, x and b are n by m dimension.
     * </p>
     * <p>
     * *Note* that 'b' and 'x' can be the same matrix instance.
     * </p>
     *
     * @param B A matrix that is n by m.  Not modified.
     * @param X An n by m matrix where the solution is writen to.  Modified.
     */
    @Override
    public void solve( DMatrixRMaj B, DMatrixRMaj X ) {
        UtilEjml.checkReshapeSolve(numRows, numCols, B, X);

        if (A == null)
            throw new RuntimeException("Must call setA() first");

        if (decomposer.isLower()) {
            solveLower(A, B, X, vv);
        } else {
            throw new RuntimeException("Implement");
        }
    }

    public static void solveLower( DMatrixRMaj L, DMatrixRMaj B, DMatrixRMaj X, double[] vv ) {
        final int numCols = B.numCols;
        final int N = L.numCols;
        for (int j = 0; j < numCols; j++) {
            for (int i = 0; i < N; i++) vv[i] = B.data[i*numCols + j];
            // solve L*y=b storing y in x
            TriangularSolver_DDRM.solveL(L.data, vv, N);

            // solve L^T*x=y
            TriangularSolver_DDRM.solveTranL(L.data, vv, N);
            for (int i = 0; i < N; i++) X.data[i*numCols + j] = vv[i];
        }
    }

    /**
     * Sets the matrix 'inv' equal to the inverse of the matrix that was decomposed.
     *
     * @param inv Where the value of the inverse will be stored.  Modified.
     */
    @Override
    public void invert( DMatrixRMaj inv ) {
        if (inv.numRows != numCols || inv.numCols != numCols) {
            throw new RuntimeException("Unexpected matrix dimension");
        }
        if (inv.data == t) {
            throw new IllegalArgumentException("Passing in the same matrix that was decomposed.");
        }

        double[] a = inv.data;

        if (decomposer.isLower()) {
            setToInverseL(a);
        } else {
            throw new RuntimeException("Implement");
        }
    }

    /**
     * Sets the matrix to the inverse using a lower triangular matrix.
     */
    public void setToInverseL( double[] a ) {
        final int n = numCols;
        // TODO reorder these operations to avoid cache misses

        // inverts the lower triangular system and saves the result
        // in the upper triangle to minimize cache misses
        for (int i = 0; i < n; i++) {
            double el_ii = t[i*n + i];
            for (int j = 0; j <= i; j++) {
                double sum = (i == j) ? 1.0 : 0;
                for (int k = i - 1; k >= j; k--) {
                    sum -= t[i*n + k]*a[j*n + k];
                }
                a[j*n + i] = sum/el_ii;
            }
        }
        // solve the system and handle the previous solution being in the upper triangle
        // takes advantage of symmetry
        for (int i = n - 1; i >= 0; i--) {
            double el_ii = t[i*n + i];

            for (int j = 0; j <= i; j++) {
                double sum = (i < j) ? 0 : a[j*n + i];
                for (int k = i + 1; k < n; k++) {
                    sum -= t[k*n + i]*a[j*n + k];
                }
                a[i*n + j] = a[j*n + i] = sum/el_ii;
            }
        }
    }

    @Override
    public boolean modifiesA() {
        return decomposer.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public CholeskyDecomposition_F64<DMatrixRMaj> getDecomposition() {
        return decomposer;
    }
}
