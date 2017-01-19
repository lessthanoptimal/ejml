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

package org.ejml.dense.row.linsol.chol;

import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
public abstract class BaseCholeskySolveTests_ZDRM {

    Random rand = new Random(0x45);

    public void standardTests() {

        solve_dimensionCheck();
        testSolve();
        testInvert();
        testQuality();
        testQuality_scale();
    }

    public abstract LinearSolver<ZMatrixRMaj> createSolver();

    public LinearSolver<ZMatrixRMaj> createSafeSolver() {
        LinearSolver<ZMatrixRMaj> solver = createSolver();
        return new LinearSolverSafe<ZMatrixRMaj>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolver<ZMatrixRMaj> solver = createSafeSolver();

        try {
            ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4, 5, rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolver<ZMatrixRMaj> solver = createSafeSolver();

        ZMatrixRMaj A = RandomMatrices_ZDRM.hermitianPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(4,3,rand);
            ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(5,2,rand);
            ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(5,2,rand);
            ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolver<ZMatrixRMaj> solver = createSolver();

        for (int N = 1; N <= 4; N++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.hermitianPosDef(N,rand);
            ZMatrixRMaj x = RandomMatrices_ZDRM.rectangle(N,1,rand);
            ZMatrixRMaj b = new ZMatrixRMaj(N,1);
            ZMatrixRMaj x_expected = x.copy();

            CommonOps_ZDRM.mult(A,x_expected,b);

            ZMatrixRMaj A_orig = A.copy();
            ZMatrixRMaj B_orig = b.copy();

            assertTrue(solver.setA(A));
            solver.solve(b,x);

            assertTrue(MatrixFeatures_ZDRM.isIdentical(x, x_expected, UtilEjml.TEST_F64));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_ZDRM.isIdentical(A,A_orig,UtilEjml.TEST_F64));
            assertEquals(!solver.modifiesB(), MatrixFeatures_ZDRM.isIdentical(b,B_orig,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testInvert() {

        LinearSolver<ZMatrixRMaj> solver = createSolver();

        for (int N = 1; N <= 5; N++) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.hermitianPosDef(N,rand);
            ZMatrixRMaj A_orig = A.copy();
            ZMatrixRMaj A_inv = new ZMatrixRMaj(N,N);
            ZMatrixRMaj found = new ZMatrixRMaj(N,N);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            CommonOps_ZDRM.mult(A_inv,A_orig,found);
            assertTrue(MatrixFeatures_ZDRM.isIdentity(found, UtilEjml.TEST_F64));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_ZDRM.isIdentical(A,A_orig,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testQuality() {

        LinearSolver<ZMatrixRMaj> solver = createSafeSolver();

        ZMatrixRMaj A = CommonOps_ZDRM.diag(3,0, 2,0, 1,0    );
        ZMatrixRMaj B = CommonOps_ZDRM.diag(3,0, 2,0, 0.001,0);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolver<ZMatrixRMaj> solver = createSafeSolver();

        ZMatrixRMaj A = CommonOps_ZDRM.diag(3,0 ,2,0 ,1,0);
        ZMatrixRMaj B = A.copy();
        CommonOps_ZDRM.elementMultiply(B,0.001,0,B);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F64);
    }
}
