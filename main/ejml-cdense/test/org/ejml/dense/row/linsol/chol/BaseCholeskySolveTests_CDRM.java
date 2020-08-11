/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
* @author Peter Abeles
*/
public abstract class BaseCholeskySolveTests_CDRM {

    Random rand = new Random(0x45);

    public void standardTests() {

        solve_dimensionCheck();
        testSolve();
        testInvert();
        testQuality();
        testQuality_scale();
    }

    public abstract LinearSolverDense<CMatrixRMaj> createSolver();

    public LinearSolverDense<CMatrixRMaj> createSafeSolver() {
        LinearSolverDense<CMatrixRMaj> solver = createSolver();
        return new LinearSolverSafe<CMatrixRMaj>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver();

        try {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4, 5, rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver();

        CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(4,3,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(5,2,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(5,2,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolverDense<CMatrixRMaj> solver = createSolver();

        for (int N = 1; N <= 4; N++) {
            CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(N,rand);
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(N,1,rand);
            CMatrixRMaj b = new CMatrixRMaj(N,1);
            CMatrixRMaj x_expected = x.copy();

            CommonOps_CDRM.mult(A,x_expected,b);

            CMatrixRMaj A_orig = A.copy();
            CMatrixRMaj B_orig = b.copy();

            assertTrue(solver.setA(A));
            solver.solve(b,x);

            assertTrue(MatrixFeatures_CDRM.isIdentical(x, x_expected, UtilEjml.TEST_F32));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_CDRM.isIdentical(A,A_orig,UtilEjml.TEST_F32));
            assertEquals(!solver.modifiesB(), MatrixFeatures_CDRM.isIdentical(b,B_orig,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void testInvert() {

        LinearSolverDense<CMatrixRMaj> solver = createSolver();

        for (int N = 1; N <= 5; N++) {
            CMatrixRMaj A = RandomMatrices_CDRM.hermitianPosDef(N,rand);
            CMatrixRMaj A_orig = A.copy();
            CMatrixRMaj A_inv = new CMatrixRMaj(N,N);
            CMatrixRMaj found = new CMatrixRMaj(N,N);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            CommonOps_CDRM.mult(A_inv,A_orig,found);
            assertTrue(MatrixFeatures_CDRM.isIdentity(found, UtilEjml.TEST_F32));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_CDRM.isIdentical(A,A_orig,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void testQuality() {

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver();

        CMatrixRMaj A = CommonOps_CDRM.diag(3,0, 2,0, 1,0    );
        CMatrixRMaj B = CommonOps_CDRM.diag(3,0, 2,0, 0.001f,0);

        assertTrue(solver.setA(A));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(B));
        float qualityB = (float)solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver();

        CMatrixRMaj A = CommonOps_CDRM.diag(3,0 ,2,0 ,1,0);
        CMatrixRMaj B = A.copy();
        CommonOps_CDRM.elementMultiply(B,0.001f,0,B);

        assertTrue(solver.setA(A));
        float qualityA = (float)solver.quality();

        assertTrue(solver.setA(B));
        float qualityB = (float)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F32);
    }
}
