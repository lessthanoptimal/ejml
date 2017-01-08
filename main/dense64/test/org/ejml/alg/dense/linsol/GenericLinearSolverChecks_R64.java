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
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_R64 {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = UtilEjml.TEST_F64;

    @Test
    public void solve_dimensionCheck() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(10,4,rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,2,rand);
            RowMatrix_F64 b = RandomMatrices_R64.createRandom(9,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,3,rand);
            RowMatrix_F64 b = RandomMatrices_R64.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_F64 x = RandomMatrices_R64.createRandom(5,2,rand);
            RowMatrix_F64 b = RandomMatrices_R64.createRandom(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}


        try {
            RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,2,rand);
            RowMatrix_F64 b = RandomMatrices_R64.createRandom(10,1,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        RowMatrix_F64 A_orig = RandomMatrices_R64.createRandom(4,4,rand);
        RowMatrix_F64 A = A_orig.copy();

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_R64.isEquals(A_orig,A);

        assertTrue(modified == solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4, 4, rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        RowMatrix_F64 B = RandomMatrices_R64.createRandom(4,2,rand);
        RowMatrix_F64 B_orig = B.copy();
        RowMatrix_F64 X = new RowMatrix_F64(A.numRows,B.numCols);

        solver.solve(B, X);

        boolean modified = !MatrixFeatures_R64.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        RowMatrix_F64 A_good = CommonOps_R64.diag(4,3,2,1);
        RowMatrix_F64 A_bad = CommonOps_R64.diag(4, 3, 2, 0.1);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A_good);

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
        RowMatrix_F64 A = CommonOps_R64.diag(4,3,2,1);
        RowMatrix_F64 Asmall = A.copy();
        CommonOps_R64.scale(0.01,Asmall);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);

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

        assertEquals(q_small, q, UtilEjml.TEST_F64);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        RowMatrix_F64 A = new RowMatrix_F64(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);
        RowMatrix_F64 b = new RowMatrix_F64(3,1, true, 18, 21.5, 4.9000);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(3,1,rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        RowMatrix_F64 x_expected = new RowMatrix_F64(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        RowMatrix_F64 A = new RowMatrix_F64(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        RowMatrix_F64 b = new RowMatrix_F64(3,1, true, 8, 33, 15.5);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(3,1,rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        RowMatrix_F64 x_expected = new RowMatrix_F64(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F64);
    }

    @Test
    public void square_singular() {
        RowMatrix_F64 A = new RowMatrix_F64(3,3);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);
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

        RowMatrix_F64 B = new RowMatrix_F64(7,1, true, vals);
        RowMatrix_F64 A = createPolyA(t,3);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(3,1,rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a,x.get(0,0),tol);
        assertEquals(b,x.get(1,0),tol);
        assertEquals(c,x.get(2,0),tol);
    }

    private RowMatrix_F64 createPolyA(double t[] , int dof ) {
        RowMatrix_F64 A = new RowMatrix_F64(t.length,3);

        for( int j = 0; j < t.length; j++ ) {
            double val = t[j];

            for( int i = 0; i < dof; i++ ) {
                A.set(j,i, Math.pow(val,i) );
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        RowMatrix_F64 A = new RowMatrix_F64(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        RowMatrix_F64 A_inv = RandomMatrices_R64.createRandom(3, 3, rand);

        LinearSolver<RowMatrix_F64> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        RowMatrix_F64 I = RandomMatrices_R64.createRandom(3,3,rand);

        CommonOps_R64.mult(A,A_inv,I);

        for( int i = 0; i < I.numRows; i++ ) {
            for( int j = 0; j < I.numCols; j++ ) {
                if( i == j )
                    assertEquals(1,I.get(i,j),tol);
                else
                    assertEquals(0,I.get(i,j),tol);
            }
        }
    }

    protected LinearSolver<RowMatrix_F64>  createSafeSolver(RowMatrix_F64 A ) {
        return new LinearSolverSafe<RowMatrix_F64>( createSolver(A));
    }

    protected abstract LinearSolver<RowMatrix_F64> createSolver(RowMatrix_F64 A );
}
