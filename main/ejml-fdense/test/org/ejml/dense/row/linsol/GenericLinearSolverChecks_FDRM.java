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

package org.ejml.dense.row.linsol;

import org.ejml.EjmlUnitTests;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_FDRM {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected float tol = UtilEjml.TEST_F32;

    @Test
    public void solve_dimensionCheck() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(10,4,rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,2,rand);
            FMatrixRMaj b = RandomMatrices_FDRM.rectangle(9,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4, 3, rand);
            FMatrixRMaj b = RandomMatrices_FDRM.rectangle(10, 2, rand);
            solver.solve(b, x);
            if( shouldWorkRectangle ) {
                assertEquals(4, x.numRows);
                assertEquals(2, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch( RuntimeException ignore ) {}
        try {
            FMatrixRMaj x = RandomMatrices_FDRM.rectangle(5, 2, rand);
            FMatrixRMaj b = RandomMatrices_FDRM.rectangle(10, 2, rand);
            solver.solve(b, x);
            if( shouldWorkRectangle ) {
                assertEquals(4, x.numRows);
                assertEquals(2, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch( RuntimeException ignore ) {}

        try {
            FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,2,rand);
            FMatrixRMaj b = RandomMatrices_FDRM.rectangle(10,1,rand);
            solver.solve(b,x);
            if( shouldWorkRectangle ) {
                assertEquals(4, x.numRows);
                assertEquals(1, x.numCols);
            } else {
                fail("Should have thrown an exception");
            }
        } catch( RuntimeException ignore ) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        FMatrixRMaj A_orig = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj A = A_orig.copy();

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_FDRM.isEquals(A_orig,A);

        assertTrue(modified == solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4, 4, rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(4,2,rand);
        FMatrixRMaj B_orig = B.copy();
        FMatrixRMaj X = new FMatrixRMaj(A.numRows,B.numCols);

        solver.solve(B, X);

        boolean modified = !MatrixFeatures_FDRM.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        FMatrixRMaj A_good = CommonOps_FDRM.diag(4,3,2,1);
        FMatrixRMaj A_bad = CommonOps_FDRM.diag(4, 3, 2, 0.1f);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A_good);

        assertTrue(solver.setA(A_good));
        float q_good;
        try {
            q_good = (float)solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(A_bad));
        float q_bad = (float)solver.quality();

        assertTrue(q_bad < q_good);

        assertEquals(q_bad*10.0f,q_good, UtilEjml.TEST_F32);
    }

    /**
     * See if quality is scale invariant
     */
    @Test
    public void checkQuality_scale() {
        FMatrixRMaj A = CommonOps_FDRM.diag(4,3,2,1);
        FMatrixRMaj Asmall = A.copy();
        CommonOps_FDRM.scale(0.01f,Asmall);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        float q;
        try {
            q = (float)solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(Asmall));
        float q_small = (float)solver.quality();

        assertEquals(q_small, q, UtilEjml.TEST_F32);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 5, 2, 3, 1.5f, -2, 8, -3, 4.7f, -0.5f);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, 18, 21.5f, 4.9000f);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(3,1,rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        FMatrixRMaj x_expected = new FMatrixRMaj(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F32);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 0, 1, 2, -2, 4, 9, 0.5f, 0, 5);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, 8, 33, 15.5f);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(3,1,rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        FMatrixRMaj x_expected = new FMatrixRMaj(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected, x, UtilEjml.TEST_F32);
    }

    @Test
    public void square_singular() {
        FMatrixRMaj A = new FMatrixRMaj(3,3);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);
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

        float t[] = new float[]{-1,-0.75f,-0.5f,0,0.25f,0.5f,0.75f};
        float vals[] = new float[7];
        float a=1,b=1.5f,c=1.7f;
        for( int i = 0; i < t.length; i++ ) {
            vals[i] = a + b*t[i] + c*t[i]*t[i];
        }

        FMatrixRMaj B = new FMatrixRMaj(7,1, true, vals);
        FMatrixRMaj A = createPolyA(t,3);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(3,1,rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a,x.get(0,0),tol);
        assertEquals(b,x.get(1,0),tol);
        assertEquals(c,x.get(2,0),tol);
    }

    private FMatrixRMaj createPolyA(float t[] , int dof ) {
        FMatrixRMaj A = new FMatrixRMaj(t.length,3);

        for( int j = 0; j < t.length; j++ ) {
            float val = t[j];

            for( int i = 0; i < dof; i++ ) {
                A.set(j,i, (float)Math.pow(val,i) );
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 0, 1, 2, -2, 4, 9, 0.5f, 0, 5);
        FMatrixRMaj A_inv = RandomMatrices_FDRM.rectangle(3, 3, rand);

        LinearSolverDense<FMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));
        solver.invert(A_inv);

        FMatrixRMaj I = RandomMatrices_FDRM.rectangle(3,3,rand);

        CommonOps_FDRM.mult(A,A_inv,I);

        for( int i = 0; i < I.numRows; i++ ) {
            for( int j = 0; j < I.numCols; j++ ) {
                if( i == j )
                    assertEquals(1,I.get(i,j),tol);
                else
                    assertEquals(0,I.get(i,j),tol);
            }
        }
    }

    protected LinearSolverDense<FMatrixRMaj> createSafeSolver(FMatrixRMaj A ) {
        return new LinearSolverSafe<>( createSolver(A));
    }

    protected abstract LinearSolverDense<FMatrixRMaj> createSolver(FMatrixRMaj A );
}
