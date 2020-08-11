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
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks_CDRM {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected float tol = UtilEjml.TEST_F32;

    @Test
    public void solve_dimensionCheck() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(10, 4, rand);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(4,2,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(9,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(4,3,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(5,2,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(10,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}


        try {
            CMatrixRMaj x = RandomMatrices_CDRM.rectangle(4,2,rand);
            CMatrixRMaj b = RandomMatrices_CDRM.rectangle(10,1,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    /**
     * Checks to see if the modifyA() flag is set correctly
     */
    @Test
    public void modifiesA() {
        CMatrixRMaj A_orig = RandomMatrices_CDRM.rectangle(4,4,rand);
        CMatrixRMaj A = A_orig.copy();

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        boolean modified = !MatrixFeatures_CDRM.isEquals(A_orig,A);

        assertTrue(modified == solver.modifiesA());
    }

    /**
     * Checks to see if the modifyB() flag is set correctly
     */
    @Test
    public void modifiesB() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,4,rand);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);

        assertTrue(solver.setA(A));

        CMatrixRMaj B = RandomMatrices_CDRM.rectangle(4,2,rand);
        CMatrixRMaj B_orig = B.copy();
        CMatrixRMaj X = new CMatrixRMaj(A.numRows,B.numCols);

        solver.solve(B,X);

        boolean modified = !MatrixFeatures_CDRM.isEquals(B_orig,B);

        assertTrue(modified == solver.modifiesB());
    }

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        CMatrixRMaj A_good = CommonOps_CDRM.diag(4,0,3,0,2,0,1,0);
        CMatrixRMaj A_bad = CommonOps_CDRM.diag(4,0,3,0,2,0,0.1f,0);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A_good);

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
        CMatrixRMaj A = CommonOps_CDRM.diag(4,0,3,0,2,0,10,0);
        CMatrixRMaj Asmall = A.copy();
        CommonOps_CDRM.elementMultiply(Asmall, 0.01f, 0, Asmall);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);

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

        assertEquals(q_small,q,UtilEjml.TEST_F32);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 5,0, 2,0, 3,0, 1.5f,0, -2,0, 8,0, -3,0, 4.7f,0, -0.5f,0);
        CMatrixRMaj b = new CMatrixRMaj(3,1, true, 18,0, 21.5f,0, 4.9000f,0);
        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(3,1,rand);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        CMatrixRMaj found = new CMatrixRMaj(3,1);
        CommonOps_CDRM.mult(A,x,found);

        CMatrixRMaj x_expected = new CMatrixRMaj(3,1, true, 1,0, 2,0, 3,0);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F32);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 0,0, 1,0, 2,0, -2,0, 4,0, 9,0, 0.5f,0, 0,0, 5,0);
        CMatrixRMaj x_expected = new CMatrixRMaj(3,1, true, 8,-2, 33,1.6f, 15.5f,-5.7f);
        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(3,1,rand);
        CMatrixRMaj b = RandomMatrices_CDRM.rectangle(3,1,rand);

        CommonOps_CDRM.mult(A,x_expected,b);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));
        solver.solve(b,x);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F32);
    }

    @Test
    public void square_singular() {
        CMatrixRMaj A = new CMatrixRMaj(3,3);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);
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

        float t[] = new float[7*2];

        for (int i = 0; i < t.length; i++) {
            t[i] = rand.nextFloat()*2-1.0f;
        }

        float vals[] = new float[t.length];
        Complex_F32 a = new Complex_F32(1,-1);
        Complex_F32 b = new Complex_F32(2,-0.4f);
        Complex_F32 c = new Complex_F32(3,0.9f);

        for( int i = 0; i < t.length; i+= 2 ) {
            Complex_F32 T = new Complex_F32(t[i],t[i+1]);

            Complex_F32 result = a.plus( b.times(T) ).plus( c.times(T.times(T)));

            vals[i] = result.real;
            vals[i+1] = result.imaginary;
        }

        CMatrixRMaj B = new CMatrixRMaj(t.length/2,1, true, vals);
        CMatrixRMaj A = createPolyA(t,3);
        CMatrixRMaj x = RandomMatrices_CDRM.rectangle(3,1,rand);

        LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a.real,     x.getReal(0, 0),tol);
        assertEquals(a.imaginary,x.getImag(0, 0),tol);
        assertEquals(b.real,     x.getReal(1, 0),tol);
        assertEquals(b.imaginary,x.getImag(1, 0),tol);
        assertEquals(c.real,     x.getReal(2, 0),tol);
        assertEquals(c.imaginary,x.getImag(2, 0),tol);
    }

    private CMatrixRMaj createPolyA(float t[] , int dof ) {
        CMatrixRMaj A = new CMatrixRMaj(t.length/2,dof);

        Complex_F32 power = new Complex_F32();
        Complex_F32 T = new Complex_F32();

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
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,rand);
            CMatrixRMaj A_inv = RandomMatrices_CDRM.rectangle(i,i,rand);

            LinearSolverDense<CMatrixRMaj> solver = createSafeSolver(A);

            assertTrue(solver.setA(A));
            solver.invert(A_inv);

            CMatrixRMaj I = RandomMatrices_CDRM.rectangle(i,i,rand);

            CommonOps_CDRM.mult(A, A_inv, I);

            assertTrue(MatrixFeatures_CDRM.isIdentity(I,UtilEjml.TEST_F32));
        }
    }

    protected LinearSolverDense<CMatrixRMaj> createSafeSolver(CMatrixRMaj A ) {
        return new LinearSolverSafe<CMatrixRMaj>( createSolver(A));
    }

    protected abstract LinearSolverDense<CMatrixRMaj> createSolver(CMatrixRMaj A );
}
