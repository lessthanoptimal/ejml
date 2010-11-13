/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.linsol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Contains a series of tests where it solves equations from a known set problems.
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverChecks {

    protected Random rand = new Random(0xff);

    // by default have everything run
    protected boolean shouldFailSingular = true;
    protected boolean shouldWorkRectangle = true;

    protected double tol = 1e-8;

    /**
     * See if a matrix that is more singular has a lower quality.
     */
    @Test
    public void checkQuality() {
        DenseMatrix64F A_good = CommonOps.diag(4,3,2,1);
        DenseMatrix64F A_bad = CommonOps.diag(4,3,2,0.1);

        LinearSolver solver = createSolver(4,4);

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
    }

    /**
     * See if quality is scale invariant
     */
    @Test
    public void checkQuality_scale() {
        DenseMatrix64F A = CommonOps.diag(4,3,2,1);
        DenseMatrix64F Asmall = A.copy();
        CommonOps.scale(0.01,Asmall);

        LinearSolver solver = createSolver(4,4);

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
     * Makes sure that the quality is scale invariant
     */
    @Test
    public void checkQuality_Scale() {
        DenseMatrix64F A = new DenseMatrix64F(2,2,true,0.1,-3,5,-4.3);

        LinearSolver solver = createSolver(4,4);

        assertTrue(solver.setA(A));
        // see if quality is supported
        try {
            solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        double qualityOrig = solver.quality();

        CommonOps.scale(5.6,A);

        assertTrue(solver.setA(A));

        double qualityScaled = solver.quality();

        assertEquals(qualityOrig,qualityScaled,1e-8);
    }

    /**
     * A very easy matrix to decompose
     */
    @Test
    public void square_trivial() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 18, 21.5, 4.9000);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);

        LinearSolver solver = createSolver(3,3);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        UtilTestMatrix.checkEquals(x_expected,x);
    }

    /**
     * This test checks to see if it can solve a system that will require some algorithms to
     * perform a pivot.  Pivots can change the data structure and can cause solve to fail if not
     * handled correctly.
     */
    @Test
    public void square_pivot() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, 8, 33, 15.5);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);

        LinearSolver solver = createSolver(3,3);
        assertTrue(solver.setA(A));
        solver.solve(b,x);


        DenseMatrix64F x_expected = new DenseMatrix64F(3,1, true, 1, 2, 3);

        UtilTestMatrix.checkEquals(x_expected,x);
    }

    @Test
    public void square_singular() {
        DenseMatrix64F A = new DenseMatrix64F(3,3);

        LinearSolver solver = createSolver(3,3);
        assertTrue(shouldFailSingular == !solver.setA(A));
    }

    /**
     * Have it solve for the coeffients in a polynomial
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

        DenseMatrix64F B = new DenseMatrix64F(7,1, true, vals);
        DenseMatrix64F A = createPolyA(t,3);
        DenseMatrix64F x = RandomMatrices.createRandom(3,1,rand);

        LinearSolver solver = createSolver(7,3);
        assertTrue(solver.setA(A));

        solver.solve(B,x);

        assertEquals(a,x.get(0,0),tol);
        assertEquals(b,x.get(1,0),tol);
        assertEquals(c,x.get(2,0),tol);
    }

    private DenseMatrix64F createPolyA( double t[] , int dof ) {
        DenseMatrix64F A = new DenseMatrix64F(t.length,3);

        for( int j = 0; j < t.length; j++ ) {
            double val = t[j];

            for( int i = 0; i < dof; i++ ) {
                A.set(j,i,Math.pow(val,i));
            }
        }

        return A;
    }

    @Test
    public void inverse() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);
        DenseMatrix64F A_inv = RandomMatrices.createRandom(3,3,rand);

        LinearSolver solver = createSolver(3,3);

        solver.setA(A);
        solver.invert(A_inv);

        DenseMatrix64F I = RandomMatrices.createRandom(3,3,rand);

        CommonOps.mult(A,A_inv,I);

        for( int i = 0; i < I.numRows; i++ ) {
            for( int j = 0; j < I.numCols; j++ ) {
                if( i == j )
                    assertEquals(1,I.get(i,j),tol);
                else
                    assertEquals(0,I.get(i,j),tol);
            }
        }
    }

    @Test
    public void getA() {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 0, 1, 2, -2, 4, 9, 0.5, 0, 5);

        LinearSolver solver = createSolver(3,3);

        solver.setA(A);

        assertTrue(MatrixFeatures.isIdentical(A,solver.getA(),1e-8) );
    }

    protected abstract LinearSolver createSolver( int numRows , int numCols );
}
