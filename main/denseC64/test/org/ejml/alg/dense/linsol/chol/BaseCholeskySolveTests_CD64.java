/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
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

    public abstract LinearSolver<CDenseMatrix64F> createSolver();

    public LinearSolver<CDenseMatrix64F> createSafeSolver() {
        LinearSolver<CDenseMatrix64F> solver = createSolver();
        return new LinearSolverSafe<CDenseMatrix64F>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver();

        try {
            CDenseMatrix64F A = CRandomMatrices.createRandom(4, 5, rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver();

        CDenseMatrix64F A = CRandomMatrices.createHermPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(4,3,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(5,2,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(5,2,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolver<CDenseMatrix64F> solver = createSolver();

        for (int N = 1; N <= 4; N++) {
            CDenseMatrix64F A = CRandomMatrices.createHermPosDef(N,rand);
            CDenseMatrix64F x = CRandomMatrices.createRandom(N,1,rand);
            CDenseMatrix64F b = new CDenseMatrix64F(N,1);
            CDenseMatrix64F x_expected = x.copy();

            CCommonOps.mult(A,x_expected,b);

            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F B_orig = b.copy();

            assertTrue(solver.setA(A));
            solver.solve(b,x);

            assertTrue(CMatrixFeatures.isIdentical(x, x_expected, 1e-8));

            // see if input was modified
            assertEquals(!solver.modifiesA(),CMatrixFeatures.isIdentical(A,A_orig,1e-8));
            assertEquals(!solver.modifiesB(),CMatrixFeatures.isIdentical(b,B_orig,1e-8));
        }
    }

    @Test
    public void testInvert() {

        LinearSolver<CDenseMatrix64F> solver = createSolver();

        for (int N = 1; N <= 5; N++) {
            CDenseMatrix64F A = CRandomMatrices.createHermPosDef(N,rand);
            CDenseMatrix64F A_orig = A.copy();
            CDenseMatrix64F A_inv = new CDenseMatrix64F(N,N);
            CDenseMatrix64F found = new CDenseMatrix64F(N,N);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            CCommonOps.mult(A_inv,A_orig,found);
            assertTrue(CMatrixFeatures.isIdentity(found, 1e-8));

            // see if input was modified
            assertEquals(!solver.modifiesA(),CMatrixFeatures.isIdentical(A,A_orig,1e-8));
        }
    }

    @Test
    public void testQuality() {

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver();

        CDenseMatrix64F A = CCommonOps.diag(3,0, 2,0, 1,0    );
        CDenseMatrix64F B = CCommonOps.diag(3,0, 2,0, 0.001,0);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver();

        CDenseMatrix64F A = CCommonOps.diag(3,0 ,2,0 ,1,0);
        CDenseMatrix64F B = A.copy();
        CCommonOps.elementMultiply(B,0.001,0,B);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertEquals(qualityB,qualityA,1e-8);
    }
}
