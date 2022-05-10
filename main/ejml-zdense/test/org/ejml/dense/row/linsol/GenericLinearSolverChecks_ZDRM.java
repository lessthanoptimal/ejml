/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_ZDRM extends EjmlStandardJUnit {
    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = UtilEjml.TEST_F64;

    @Test
    public void solve_dimensionCheck() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(10, 4, rand);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        // It should resize x
        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(1, 1, rand);
        try {
            ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(9, 2, rand);
            solver.solve(b, x);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        ZMatrixRMaj A_orig = RandomMatrices_ZDRM.rectangle(4, 4, rand);
        ZMatrixRMaj A = A_orig.copy();

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_ZDRM.isEquals(A_orig, A);

        assertEquals(solver.modifiesA(), modified);
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4, 4, rand);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        ZMatrixRMaj B = RandomMatrices_ZDRM.rectangle(4, 2, rand);
        ZMatrixRMaj B_orig = B.copy();
        ZMatrixRMaj X = new ZMatrixRMaj(A.numRows, B.numCols);

        solver.solve(B, X);

        boolean modified = !MatrixFeatures_ZDRM.isEquals(B_orig, B);

        assertEquals(solver.modifiesB(), modified);
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        ZMatrixRMaj A_good = CommonOps_ZDRM.diag(4, 0, 3, 0, 2, 0, 1, 0);
        ZMatrixRMaj A_bad = CommonOps_ZDRM.diag(4, 0, 3, 0, 2, 0, 0.1, 0);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A_good);

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
    @Test
    public void checkQuality_scale() {
        ZMatrixRMaj A = CommonOps_ZDRM.diag(4, 0, 3, 0, 2, 0, 10, 0);
        ZMatrixRMaj Asmall = A.copy();
        CommonOps_ZDRM.elementMultiply(Asmall, 0.01, 0, Asmall);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);

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
    @Test
    public void square_trivial() {
        ZMatrixRMaj A = new ZMatrixRMaj(3, 3, true, 5, 0, 2, 0, 3, 0, 1.5, 0, -2, 0, 8, 0, -3, 0, 4.7, 0, -0.5, 0);
        ZMatrixRMaj b = new ZMatrixRMaj(3, 1, true, 18, 0, 21.5, 0, 4.9000, 0);
        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(1, 1, rand);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b, x);

        ZMatrixRMaj found = new ZMatrixRMaj(3, 1);
        CommonOps_ZDRM.mult(A, x, found);

        ZMatrixRMaj x_expected = new ZMatrixRMaj(3, 1, true, 1, 0, 2, 0, 3, 0);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F64);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot. Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        ZMatrixRMaj A = new ZMatrixRMaj(3, 3, true, 0, 0, 1, 0, 2, 0, -2, 0, 4, 0, 9, 0, 0.5, 0, 0, 0, 5, 0);
        ZMatrixRMaj x_expected = new ZMatrixRMaj(3, 1, true, 8, -2, 33, 1.6, 15.5, -5.7);
        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(1, 1, rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(3, 1, rand);

        CommonOps_ZDRM.mult(A, x_expected, b);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b, x);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F64);
    }

    @Test
    public void square_singular() {
        ZMatrixRMaj A = new ZMatrixRMaj(3, 3);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);
        assertEquals(!solver.setA(A), shouldFailSingular);
    }

    /**
     * Have it solve for the coefficients in a polynomial
     */
    @Test
    public void rectangular() {
        if (!shouldWorkRectangle) {
            // skip this test
            return;
        }

        double t[] = new double[7*2];

        for (int i = 0; i < t.length; i++) {
            t[i] = rand.nextDouble()*2 - 1.0;
        }

        double vals[] = new double[t.length];
        Complex_F64 a = new Complex_F64(1, -1);
        Complex_F64 b = new Complex_F64(2, -0.4);
        Complex_F64 c = new Complex_F64(3, 0.9);

        for (int i = 0; i < t.length; i += 2) {
            Complex_F64 T = new Complex_F64(t[i], t[i + 1]);

            Complex_F64 result = a.plus(b.times(T)).plus(c.times(T.times(T)));

            vals[i] = result.real;
            vals[i + 1] = result.imaginary;
        }

        ZMatrixRMaj B = new ZMatrixRMaj(t.length/2, 1, true, vals);
        ZMatrixRMaj A = createPolyA(t, 3);
        ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(1, 1, rand);

        LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B, x);

        assertEquals(a.real, x.getReal(0, 0), tol);
        assertEquals(a.imaginary, x.getImag(0, 0), tol);
        assertEquals(b.real, x.getReal(1, 0), tol);
        assertEquals(b.imaginary, x.getImag(1, 0), tol);
        assertEquals(c.real, x.getReal(2, 0), tol);
        assertEquals(c.imaginary, x.getImag(2, 0), tol);
    }

    private ZMatrixRMaj createPolyA( double t[], int dof ) {
        ZMatrixRMaj A = new ZMatrixRMaj(t.length/2, dof);

        Complex_F64 power = new Complex_F64();
        Complex_F64 T = new Complex_F64();

        for (int j = 0; j < A.numRows; j++) {
            T.setTo(t[j*2], t[j*2 + 1]);
            power.setTo(1, 0);

            for (int i = 0; i < dof; i++) {
                A.set(j, i, power.real, power.imaginary);
                power = power.times(T);
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        for (int i = 2; i < 10; i++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(i, i, rand);
            ZMatrixRMaj A_inv = RandomMatrices_ZDRM.rectangle(i, 2, rand);

            LinearSolverDense<ZMatrixRMaj> solver = createSafeSolver(A);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            ZMatrixRMaj I = RandomMatrices_ZDRM.rectangle(i, i, rand);

            CommonOps_ZDRM.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_ZDRM.isIdentity(I, UtilEjml.TEST_F64));
        }
    }

    protected LinearSolverDense<ZMatrixRMaj> createSafeSolver( ZMatrixRMaj A ) {
        return new LinearSolverSafe<ZMatrixRMaj>(createSolver(A));
    }

    protected abstract LinearSolverDense<ZMatrixRMaj> createSolver( ZMatrixRMaj A );
}
