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

package org.ejml.alg.dense.linsol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_CR64 {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = UtilEjml.TEST_F64;

    @Test
    public void solve_dimensionCheck() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(10, 4, rand);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(4,2,rand);
            DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(9,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(4,3,rand);
            DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(5,2,rand);
            DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}


        try {
            DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(4,2,rand);
            DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(10,1,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        DMatrixRow_C64 A_orig = RandomMatrices_CR64.createRandom(4,4,rand);
        DMatrixRow_C64 A = A_orig.copy();

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_CR64.isEquals(A_orig,A);

        assertTrue(modified == solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(4,4,rand);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        DMatrixRow_C64 B = RandomMatrices_CR64.createRandom(4,2,rand);
        DMatrixRow_C64 B_orig = B.copy();
        DMatrixRow_C64 X = new DMatrixRow_C64(A.numRows,B.numCols);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_CR64.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        DMatrixRow_C64 A_good = CommonOps_CR64.diag(4,0,3,0,2,0,1,0);
        DMatrixRow_C64 A_bad = CommonOps_CR64.diag(4,0,3,0,2,0,0.1,0);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A_good);

        assertTrue(solver.setA(A_good));
        double q_good;
        try {
            q_good = (double)solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(A_bad));
        double q_bad = (double)solver.quality();

        assertTrue(q_bad < q_good);

        assertEquals(q_bad*10.0,q_good, UtilEjml.TEST_F64);
    }

    /**
     * See if quality is scale invariant
     */
    @Test
    public void checkQuality_scale() {
        DMatrixRow_C64 A = CommonOps_CR64.diag(4,0,3,0,2,0,10,0);
        DMatrixRow_C64 Asmall = A.copy();
        CommonOps_CR64.elementMultiply(Asmall, 0.01, 0, Asmall);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        double q;
        try {
            q = (double)solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(Asmall));
        double q_small = (double)solver.quality();

        assertEquals(q_small,q,UtilEjml.TEST_F64);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3, true, 5,0, 2,0, 3,0, 1.5,0, -2,0, 8,0, -3,0, 4.7,0, -0.5,0);
        DMatrixRow_C64 b = new DMatrixRow_C64(3,1, true, 18,0, 21.5,0, 4.9000,0);
        DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(3,1,rand);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        DMatrixRow_C64 found = new DMatrixRow_C64(3,1);
        CommonOps_CR64.mult(A,x,found);

        DMatrixRow_C64 x_expected = new DMatrixRow_C64(3,1, true, 1,0, 2,0, 3,0);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5,0, 0,0, 5,0);
        DMatrixRow_C64 x_expected = new DMatrixRow_C64(3,1, true, 8,-2, 33,1.6, 15.5,-5.7);
        DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(3,1,rand);
        DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(3,1,rand);

        CommonOps_CR64.mult(A,x_expected,b);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64);
    }

    @Test
    public void square_singular() {
        DMatrixRow_C64 A = new DMatrixRow_C64(3,3);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);
        assertTrue(shouldFailSingular == !solver.setA(A));
    }

    /**
     * Have it solve for the coefficients in a polynomial
     */
    @Test
    public void rectangular() {
        if( !shouldWorkRectangle ) {
            // skip this test
            return;
        }

        double t[] = new double[7*2];

        for (int i = 0; i < t.length; i++) {
            t[i] = rand.nextDouble()*2-1.0;
        }

        double vals[] = new double[t.length];
        Complex_F64 a = new Complex_F64(1,-1);
        Complex_F64 b = new Complex_F64(2,-0.4);
        Complex_F64 c = new Complex_F64(3,0.9);

        for( int i = 0; i < t.length; i+= 2 ) {
            Complex_F64 T = new Complex_F64(t[i],t[i+1]);

            Complex_F64 result = a.plus( b.times(T) ).plus( c.times(T.times(T)));

            vals[i] = result.real;
            vals[i+1] = result.imaginary;
        }

        DMatrixRow_C64 B = new DMatrixRow_C64(t.length/2,1, true, vals);
        DMatrixRow_C64 A = createPolyA(t,3);
        DMatrixRow_C64 x = RandomMatrices_CR64.createRandom(3,1,rand);

        LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a.real,     x.getReal(0, 0),tol);
        assertEquals(a.imaginary,x.getImag(0, 0),tol);
        assertEquals(b.real,     x.getReal(1, 0),tol);
        assertEquals(b.imaginary,x.getImag(1, 0),tol);
        assertEquals(c.real,     x.getReal(2, 0),tol);
        assertEquals(c.imaginary,x.getImag(2, 0),tol);
    }

    private DMatrixRow_C64 createPolyA(double t[] , int dof ) {
        DMatrixRow_C64 A = new DMatrixRow_C64(t.length/2,dof);

        Complex_F64 power = new Complex_F64();
        Complex_F64 T = new Complex_F64();

        for( int j = 0; j < A.numRows; j++ ) {
            T.set(t[j*2],t[j*2+1]);
            power.set(1,0);

            for( int i = 0; i < dof; i++ ) {
                A.set(j,i,power.real,power.imaginary);
                power = power.times(T);
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        for (int i = 2; i < 10; i++) {
            DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(i,i,rand);
            DMatrixRow_C64 A_inv = RandomMatrices_CR64.createRandom(i,i,rand);

            LinearSolver<DMatrixRow_C64> solver = createSafeSolver(A);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            DMatrixRow_C64 I = RandomMatrices_CR64.createRandom(i,i,rand);

            CommonOps_CR64.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_CR64.isIdentity(I,UtilEjml.TEST_F64));
        }
    }

    protected LinearSolver<DMatrixRow_C64>  createSafeSolver(DMatrixRow_C64 A ) {
        return new LinearSolverSafe<DMatrixRow_C64>( createSolver(A));
    }

    protected abstract LinearSolver<DMatrixRow_C64> createSolver(DMatrixRow_C64 A );
}
