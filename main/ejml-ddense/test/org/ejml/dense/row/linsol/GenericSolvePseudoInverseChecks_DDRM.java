/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol;

import org.ejml.EjmlStandardJUnit;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the ability of a solver to handle different type of rank deficient matrices
 *
 * @author Peter Abeles
 */
public class GenericSolvePseudoInverseChecks_DDRM extends EjmlStandardJUnit {
    LinearSolverDense<DMatrixRMaj> solver;

    public GenericSolvePseudoInverseChecks_DDRM( LinearSolverDense<DMatrixRMaj> solver ) {
        this.solver = new LinearSolverSafe<DMatrixRMaj>(solver);
    }

    public void all() {
        zeroMatrix();
        underDetermined_wide_solve();
        underDetermined_wide_inv();
        underDetermined_tall_solve();
        singular_solve();
        singular_inv();
    }

    /**
     * Shouldn't blow if it the input matrix is zero. But there is no solution...
     */
    public void zeroMatrix() {
        DMatrixRMaj A = new DMatrixRMaj(3, 3);
        DMatrixRMaj y = new DMatrixRMaj(3, 1, true, 4, 7, 8);

        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(3, 1);
        solver.solve(y, x);

        assertFalse(MatrixFeatures_DDRM.hasUncountable(x));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_wide_solve() {
        // create a matrix where two rows are linearly dependent
        DMatrixRMaj A = new DMatrixRMaj(2, 3, true, 1, 2, 3, 2, 3, 4);

        DMatrixRMaj y = new DMatrixRMaj(2, 1, true, 4, 7);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(3, 1);
        solver.solve(y, x);

        DMatrixRMaj found = new DMatrixRMaj(2, 1);
        CommonOps_DDRM.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_DDRM.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void underDetermined_wide_inv() {
        // create a matrix where two rows are linearly dependent
        DMatrixRMaj A = new DMatrixRMaj(2, 3, true, 1, 2, 3, 2, 3, 4);

        DMatrixRMaj y = new DMatrixRMaj(2, 1, true, 4, 7);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(3, 1);
        solver.solve(y, x);

        // now test the pseudo inverse
        DMatrixRMaj A_pinv = new DMatrixRMaj(3, 2);
        DMatrixRMaj found = new DMatrixRMaj(3, 1);
        solver.invert(A_pinv);

        CommonOps_DDRM.mult(A_pinv, y, found);

        assertTrue(MatrixFeatures_DDRM.isEquals(x, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_tall_solve() {
        // create a matrix where two rows are linearly dependent
        DMatrixRMaj A = new DMatrixRMaj(3, 2, true, 1, 2, 1, 2, 2, 4);

        DMatrixRMaj y = new DMatrixRMaj(3, 1, true, 4, 4, 8);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(2, 1);
        solver.solve(y, x);

        DMatrixRMaj found = new DMatrixRMaj(3, 1);
        CommonOps_DDRM.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_DDRM.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void singular_solve() {
        // create a matrix where two rows are linearly dependent
        DMatrixRMaj A = new DMatrixRMaj(3, 3, true, 1, 2, 3, 2, 3, 4, 2, 3, 4);

        DMatrixRMaj y = new DMatrixRMaj(3, 1, true, 4, 7, 7);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(3, 1);
        solver.solve(y, x);

        DMatrixRMaj found = new DMatrixRMaj(3, 1);
        CommonOps_DDRM.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_DDRM.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void singular_inv() {
        // create a matrix where two rows are linearly dependent
        DMatrixRMaj A = new DMatrixRMaj(3, 3, true, 1, 2, 3, 2, 3, 4, 2, 3, 4);

        DMatrixRMaj y = new DMatrixRMaj(3, 1, true, 4, 7, 7);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = new DMatrixRMaj(3, 1);
        solver.solve(y, x);

        // now test the pseudo inverse
        DMatrixRMaj A_pinv = new DMatrixRMaj(3, 3);
        DMatrixRMaj found = new DMatrixRMaj(3, 1);
        solver.invert(A_pinv);

        CommonOps_DDRM.mult(A_pinv, y, found);

        assertTrue(MatrixFeatures_DDRM.isEquals(x, found, UtilEjml.TEST_F64));
    }
}
