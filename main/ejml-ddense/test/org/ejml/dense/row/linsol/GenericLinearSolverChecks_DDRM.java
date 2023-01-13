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
import org.ejml.EjmlUnitTests;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_DDRM extends EjmlStandardJUnit {
    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = UtilEjml.TEST_F64;

    @Test void solve_dimensionCheck() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(10, 4, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4, 2, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(9, 2, rand);
            solver.solve(b, x);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }

        try {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4, 3, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(10, 2, rand);
            solver.solve(b, x);
            if (shouldWorkRectangle) {
                assertEquals(4, x.numRows);
                assertEquals(2, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch (RuntimeException ignore) {
        }
        try {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(5, 2, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(10, 2, rand);
            solver.solve(b, x);
            if (shouldWorkRectangle) {
                assertEquals(4, x.numRows);
                assertEquals(2, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch (RuntimeException ignore) {
        }

        try {
            DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4, 2, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(10, 1, rand);
            solver.solve(b, x);
            if (shouldWorkRectangle) {
                assertEquals(4, x.numRows);
                assertEquals(1, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch (RuntimeException ignore) {
        }
    }

    /**
     * Should accept a 0x0 matrix.
     */
    @Test void zeroSizedMatrix() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(0, 0, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(0, 0, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(0, 0, rand);

        solver.solve(b, x);
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test void modifiesA() {
        DMatrixRMaj A_orig = RandomMatrices_DDRM.rectangle(4, 4, rand);
        DMatrixRMaj A = A_orig.copy();

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_DDRM.isEquals(A_orig, A);

        assertEquals(modified, solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test void modifiesB() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4, 4, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(4, 2, rand);
        DMatrixRMaj B_orig = B.copy();
        DMatrixRMaj X = new DMatrixRMaj(A.numRows, B.numCols);

        solver.solve(B, X);

        boolean modified = !MatrixFeatures_DDRM.isEquals(B_orig, B);

        assertEquals(modified, solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test void checkQuality() {
        DMatrixRMaj A_good = CommonOps_DDRM.diag(4, 3, 2, 1);
        DMatrixRMaj A_bad = CommonOps_DDRM.diag(4, 3, 2, 0.1);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A_good);

        assertTrue(solver.setA(A_good));
        double q_good;
        try {
            q_good = (double)solver.quality();
        } catch (IllegalArgumentException e) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(A_bad));
        double q_bad = (double)solver.quality();

        assertTrue(q_bad < q_good);

        assertEquals(q_bad*10.0, q_good, UtilEjml.TEST_F64);
    }

    /**
     * See if quality is scale invariant
     */
    @Test void checkQuality_scale() {
        DMatrixRMaj A = CommonOps_DDRM.diag(4, 3, 2, 1);
        DMatrixRMaj Asmall = A.copy();
        CommonOps_DDRM.scale(0.01, Asmall);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        double q;
        try {
            q = (double)solver.quality();
        } catch (IllegalArgumentException e) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(Asmall));
        double q_small = (double)solver.quality();

        assertEquals(q_small, q, UtilEjml.TEST_F64);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test void square_trivial() {
        DMatrixRMaj A = new DMatrixRMaj(3, 3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);
        DMatrixRMaj b = new DMatrixRMaj(3, 1, true, 18, 21.5, 4.9000);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3, 1, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b, x);


        DMatrixRMaj x_expected = new DMatrixRMaj(3, 1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F64);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot. Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test void square_pivot() {
        DMatrixRMaj A = new DMatrixRMaj(3, 3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DMatrixRMaj b = new DMatrixRMaj(3, 1, true, 8, 33, 15.5);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3, 1, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b, x);


        DMatrixRMaj x_expected = new DMatrixRMaj(3, 1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F64);
    }

    @Test void square_singular() {
        DMatrixRMaj A = new DMatrixRMaj(3, 3);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertEquals(shouldFailSingular, !solver.setA(A));
    }

    /**
     * Have it solve for the coefficients in a polynomial
     */
    @Test public void rectangular() {
        if (!shouldWorkRectangle) {
            // skip this test
            return;
        }

        double[] t = new double[]{-1, -0.75, -0.5, 0, 0.25, 0.5, 0.75};
        double[] vals = new double[7];
        double a = 1, b = 1.5, c = 1.7;
        for (int i = 0; i < t.length; i++) {
            vals[i] = a + b*t[i] + c*t[i]*t[i];
        }

        DMatrixRMaj B = new DMatrixRMaj(7, 1, true, vals);
        DMatrixRMaj A = createPolyA(t, 3);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3, 1, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B, x);

        assertEquals(a, x.get(0, 0), tol);
        assertEquals(b, x.get(1, 0), tol);
        assertEquals(c, x.get(2, 0), tol);
    }

    private DMatrixRMaj createPolyA( double[] t, int dof ) {
        DMatrixRMaj A = new DMatrixRMaj(t.length, 3);

        for (int j = 0; j < t.length; j++) {
            double val = t[j];

            for (int i = 0; i < dof; i++) {
                A.set(j, i, Math.pow(val, i));
            }
        }

        return A;
    }

    @Test void inverse() {
        DMatrixRMaj A = new DMatrixRMaj(3, 3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DMatrixRMaj A_inv = RandomMatrices_DDRM.rectangle(3, 3, rand);

        LinearSolverDense<DMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        DMatrixRMaj I = RandomMatrices_DDRM.rectangle(3, 3, rand);

        CommonOps_DDRM.mult(A, A_inv, I);

        for (int i = 0; i < I.numRows; i++) {
            for (int j = 0; j < I.numCols; j++) {
                if (i == j)
                    assertEquals(1, I.get(i, j), tol);
                else
                    assertEquals(0, I.get(i, j), tol);
            }
        }
    }

    protected LinearSolverDense<DMatrixRMaj> createSafeSolver( DMatrixRMaj A ) {
        return new LinearSolverSafe<>(createSolver(A));
    }

    protected abstract LinearSolverDense<DMatrixRMaj> createSolver( DMatrixRMaj A );
}
