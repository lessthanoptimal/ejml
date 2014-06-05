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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class BaseCholeskySolveTests {

    Random rand = new Random(0x45);

    public void standardTests() {

        solve_dimensionCheck();
        testSolve();
        testInvert();
        testQuality();
        testQuality_scale();
    }

    public abstract LinearSolver<DenseMatrix64F> createSolver();

    public LinearSolver<DenseMatrix64F> createSafeSolver() {
        LinearSolver<DenseMatrix64F> solver = createSolver();
        return new LinearSolverSafe<DenseMatrix64F>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        try {
            DenseMatrix64F A = RandomMatrices.createRandom(4,5,rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        DenseMatrix64F A = RandomMatrices.createSymmPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            DenseMatrix64F x = RandomMatrices.createRandom(4,3,rand);
            DenseMatrix64F b = RandomMatrices.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            DenseMatrix64F x = RandomMatrices.createRandom(5,2,rand);
            DenseMatrix64F b = RandomMatrices.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            DenseMatrix64F x = RandomMatrices.createRandom(5,2,rand);
            DenseMatrix64F b = RandomMatrices.createRandom(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 17, 97, 320);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);
        DenseMatrix64F A_orig = A.copy();
        DenseMatrix64F B_orig = b.copy();

        assertTrue(solver.setA(A));
        solver.solve(b,x);

        // see if the input got modified
        EjmlUnitTests.assertEquals(A,A_orig,1e-5);
        EjmlUnitTests.assertEquals(b,B_orig,1e-5);

        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,1e-6);
    }

    @Test
    public void testInvert() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        DenseMatrix64F found = new DenseMatrix64F(A.numRows,A.numCols);

        assertTrue(solver.setA(A));
        solver.invert(found);

        DenseMatrix64F A_inv = new DenseMatrix64F(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);

        EjmlUnitTests.assertEquals(A_inv,found,1e-5);
    }

    @Test
    public void testQuality() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        DenseMatrix64F A = CommonOps.diag(3,2,1);
        DenseMatrix64F B = CommonOps.diag(3,2,0.001);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolver<DenseMatrix64F> solver = createSafeSolver();

        DenseMatrix64F A = CommonOps.diag(3,2,1);
        DenseMatrix64F B = A.copy();
        CommonOps.scale(0.001,B);

        assertTrue(solver.setA(A));
        double qualityA = solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = solver.quality();

        assertEquals(qualityB,qualityA,1e-8);
    }
}
