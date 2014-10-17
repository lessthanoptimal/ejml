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

package org.ejml.alg.dense.linsol;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericCLinearSolverChecks {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = 1e-8;

    @Test
    public void solve_dimensionCheck() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(10, 4, rand);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(4,2,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(9,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(4,3,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(5,2,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}


        try {
            CDenseMatrix64F x = CRandomMatrices.createRandom(4,2,rand);
            CDenseMatrix64F b = CRandomMatrices.createRandom(10,1,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        CDenseMatrix64F A_orig = CRandomMatrices.createRandom(4,4,rand);
        CDenseMatrix64F A = A_orig.copy();

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !CMatrixFeatures.isEquals(A_orig,A);

        assertTrue(modified == solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(4,4,rand);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        CDenseMatrix64F B = CRandomMatrices.createRandom(4,2,rand);
        CDenseMatrix64F B_orig = B.copy();
        CDenseMatrix64F X = new CDenseMatrix64F(A.numRows,B.numCols);

        solver.solve(B,X);

        boolean modified = !CMatrixFeatures.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        CDenseMatrix64F A_good = CCommonOps.diag(4,0,3,0,2,0,1,0);
        CDenseMatrix64F A_bad = CCommonOps.diag(4,0,3,0,2,0,0.1,0);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A_good);

        assertTrue(solver.setA(A_good));
        double q_good;
        try {
            q_good = solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(A_bad));
        double q_bad = solver.quality();

        assertTrue(q_bad < q_good);

        assertEquals(q_bad*10.0,q_good,1e-8);
    }

    /**
     * See if quality is scale invariant
     */
    @Test
    public void checkQuality_scale() {
        CDenseMatrix64F A = CCommonOps.diag(4,0,3,0,2,0,10,0);
        CDenseMatrix64F Asmall = A.copy();
        CCommonOps.elementMultiply(Asmall, 0.01, 0, Asmall);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        double q;
        try {
            q = solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(Asmall));
        double q_small = solver.quality();

        assertEquals(q_small,q,1e-8);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 5,0, 2,0, 3,0, 1.5,0, -2,0, 8,0, -3,0, 4.7,0, -0.5,0);
        CDenseMatrix64F b = new CDenseMatrix64F(3,1, true, 18,0, 21.5,0, 4.9000,0);
        CDenseMatrix64F x = CRandomMatrices.createRandom(3,1,rand);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        CDenseMatrix64F found = new CDenseMatrix64F(3,1);
        CCommonOps.mult(A,x,found);

        CDenseMatrix64F x_expected = new CDenseMatrix64F(3,1, true, 1,0, 2,0, 3,0);

        EjmlUnitTests.assertEquals(x_expected,x,1e-8);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5,0, 0,0, 5,0);
        CDenseMatrix64F b = new CDenseMatrix64F(3,1, true, 8,0, 33,0, 15.5,0);
        CDenseMatrix64F x = CRandomMatrices.createRandom(3,1,rand);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        CDenseMatrix64F x_expected = new CDenseMatrix64F(3,1, true, 1,0, 2,0, 3,0);

        EjmlUnitTests.assertEquals(x_expected,x,1e-8);
    }

    @Test
    public void square_singular() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);
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

        double t[] = new double[]{-1,-0.75,-0.5,0,0.25,0.5,0.75};
        double vals[] = new double[7];
        double a=1,b=1.5,c=1.7;
        for( int i = 0; i < t.length; i++ ) {
            vals[i] = a + b*t[i] + c*t[i]*t[i];
        }

        CDenseMatrix64F B = new CDenseMatrix64F(7,1, true, vals);
        CDenseMatrix64F A = createPolyA(t,3);
        CDenseMatrix64F x = CRandomMatrices.createRandom(3,1,rand);

        LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a,x.getReal(0, 0),tol);
        assertEquals(0,x.getImaginary(0, 0),tol);
        assertEquals(b,x.getReal(1, 0),tol);
        assertEquals(0,x.getImaginary(1, 0),tol);
        assertEquals(c,x.getReal(2, 0),tol);
        assertEquals(0,x.getImaginary(2, 0),tol);
    }

    private CDenseMatrix64F createPolyA( double t[] , int dof ) {
        CDenseMatrix64F A = new CDenseMatrix64F(t.length,3);

        for( int j = 0; j < t.length; j++ ) {
            double val = t[j];

            for( int i = 0; i < dof; i++ ) {
                A.set(j,i,Math.pow(val,i),0);
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        for (int i = 2; i < 10; i++) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,rand);
            CDenseMatrix64F A_inv = CRandomMatrices.createRandom(i,i,rand);

            LinearSolver<CDenseMatrix64F> solver = createSafeSolver(A);

            solver.setA(A);
            solver.invert(A_inv);

            CDenseMatrix64F I = CRandomMatrices.createRandom(i,i,rand);

            CCommonOps.mult(A, A_inv, I);

            assertTrue(CMatrixFeatures.isIdentity(I,1e-8));
        }
    }

    protected LinearSolver<CDenseMatrix64F>  createSafeSolver( CDenseMatrix64F A ) {
        return new LinearSolverSafe<CDenseMatrix64F>( createSolver(A));
    }

    protected abstract LinearSolver<CDenseMatrix64F> createSolver( CDenseMatrix64F A );
}
