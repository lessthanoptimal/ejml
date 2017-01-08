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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
public abstract class BaseCholeskySolveTests_CD64 {

    Random rand = new Random(0x45);

    public void standardTests() {

        solve_dimensionCheck();
        testSolve();
        testInvert();
        testQuality();
        testQuality_scale();
    }

    public abstract LinearSolver<RowMatrix_C64> createSolver();

    public LinearSolver<RowMatrix_C64> createSafeSolver() {
        LinearSolver<RowMatrix_C64> solver = createSolver();
        return new LinearSolverSafe<RowMatrix_C64>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolver<RowMatrix_C64> solver = createSafeSolver();

        try {
            RowMatrix_C64 A = RandomMatrices_CD64.createRandom(4, 5, rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolver<RowMatrix_C64> solver = createSafeSolver();

        RowMatrix_C64 A = RandomMatrices_CD64.createHermPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            RowMatrix_C64 x = RandomMatrices_CD64.createRandom(4,3,rand);
            RowMatrix_C64 b = RandomMatrices_CD64.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_C64 x = RandomMatrices_CD64.createRandom(5,2,rand);
            RowMatrix_C64 b = RandomMatrices_CD64.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_C64 x = RandomMatrices_CD64.createRandom(5,2,rand);
            RowMatrix_C64 b = RandomMatrices_CD64.createRandom(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolver<RowMatrix_C64> solver = createSolver();

        for (int N = 1; N <= 4; N++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createHermPosDef(N,rand);
            RowMatrix_C64 x = RandomMatrices_CD64.createRandom(N,1,rand);
            RowMatrix_C64 b = new RowMatrix_C64(N,1);
            RowMatrix_C64 x_expected = x.copy();

            CommonOps_CD64.mult(A,x_expected,b);

            RowMatrix_C64 A_orig = A.copy();
            RowMatrix_C64 B_orig = b.copy();

            assertTrue(solver.setA(A));
            solver.solve(b,x);

            assertTrue(MatrixFeatures_CD64.isIdentical(x, x_expected, UtilEjml.TEST_F64));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_CD64.isIdentical(A,A_orig,UtilEjml.TEST_F64));
            assertEquals(!solver.modifiesB(), MatrixFeatures_CD64.isIdentical(b,B_orig,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testInvert() {

        LinearSolver<RowMatrix_C64> solver = createSolver();

        for (int N = 1; N <= 5; N++) {
            RowMatrix_C64 A = RandomMatrices_CD64.createHermPosDef(N,rand);
            RowMatrix_C64 A_orig = A.copy();
            RowMatrix_C64 A_inv = new RowMatrix_C64(N,N);
            RowMatrix_C64 found = new RowMatrix_C64(N,N);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            CommonOps_CD64.mult(A_inv,A_orig,found);
            assertTrue(MatrixFeatures_CD64.isIdentity(found, UtilEjml.TEST_F64));

            // see if input was modified
            assertEquals(!solver.modifiesA(), MatrixFeatures_CD64.isIdentical(A,A_orig,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void testQuality() {

        LinearSolver<RowMatrix_C64> solver = createSafeSolver();

        RowMatrix_C64 A = CommonOps_CD64.diag(3,0, 2,0, 1,0    );
        RowMatrix_C64 B = CommonOps_CD64.diag(3,0, 2,0, 0.001,0);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolver<RowMatrix_C64> solver = createSafeSolver();

        RowMatrix_C64 A = CommonOps_CD64.diag(3,0 ,2,0 ,1,0);
        RowMatrix_C64 B = A.copy();
        CommonOps_CD64.elementMultiply(B,0.001,0,B);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA,UtilEjml.TEST_F64);
    }
}
