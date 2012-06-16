/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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
import org.ejml.factory.LinearSolver;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverSafe {

    Random rand = new Random(234);

    DenseMatrix64F Ainput = new DenseMatrix64F(1,1);
    DenseMatrix64F Binput = new DenseMatrix64F(1,1);

    /**
     * Checks to see if the input matrix is copied after multiple calls.  This was an actual bug.
     */
    @Test
    public void multipleCalls_setA() {
        DummySolver dummy = new DummySolver(true,false);
        dummy.expectedA = 5;

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        Ainput.set(0,5);
        s.setA(Ainput);
        // call it a second time and see if the input matrix has been reset to the
        // correct value
        s.setA(Ainput);

        assertTrue(dummy.passedin != Ainput);
    }

    /**
     * Checks to see if the input matrix is copied after multiple calls.  This was an actual bug.
     */
    @Test
    public void multipleCalls_setB() {
        DummySolver dummy = new DummySolver(false,true);
        dummy.expectedB = 5;

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        Binput.set(0,5);
        s.solve(Binput,new DenseMatrix64F(1,1));
        // call it a second time and see if the input matrix has been reset to the
        // correct value
        s.solve(Binput,new DenseMatrix64F(1,1));

        assertTrue(dummy.passedin != Ainput);
    }

    @Test
    public void testSetA_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        s.setA(Ainput);

        assertTrue(dummy.passedin == Ainput);
    }

    @Test
    public void testSetA_mod() {
        DummySolver dummy = new DummySolver(true,false);

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        s.setA(Ainput);

        assertTrue(dummy.passedin != Ainput);
    }

    @Test
    public void testSolver_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        s.solve(Binput,new DenseMatrix64F(1,1));

        assertTrue(dummy.passedin == Binput);
    }

    @Test
    public void testSolver_mod() {
        DummySolver dummy = new DummySolver(false,true);

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        s.solve(Binput,new DenseMatrix64F(1,1));

        assertTrue(dummy.passedin != Binput);
    }

    @Test
    public void quality() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(dummy);

        assertTrue(s.quality()==dummy.quality());
    }

    @Test
    public void modifies() {
        LinearSolver<DenseMatrix64F> s = new LinearSolverSafe<DenseMatrix64F>(null);

        assertFalse(s.modifiesA());
        assertFalse(s.modifiesB());

    }

    private class DummySolver implements LinearSolver<DenseMatrix64F>
    {
        boolean modifiesA;
        boolean modifiesB;

        DenseMatrix64F passedin;

        // the expected value of the input matrix
        double expectedA = Double.NaN;
        double expectedB = Double.NaN;

        private DummySolver(boolean modifiesA, boolean modifiesB) {
            this.modifiesA = modifiesA;
            this.modifiesB = modifiesB;
        }

        @Override
        public boolean setA(DenseMatrix64F A) {
            passedin = A;

            // the input matrix has an expected input value
            if( !Double.isNaN(expectedA))
                assertEquals(expectedA,A.get(0),1e-8);

            if( modifiesA ) {
                A.set(0,0,rand.nextDouble());
            }

            return true;
        }

        @Override
        public double quality() {
            return 1.1;
        }

        @Override
        public void solve(DenseMatrix64F B, DenseMatrix64F X) {
            passedin = B;

            // the input matrix has an expected input value
            if( !Double.isNaN(expectedB))
                assertEquals(expectedB,B.get(0),1e-8);

            if( modifiesB ) {
                B.set(0,0,rand.nextDouble());
            }
        }

        @Override
        public void invert(DenseMatrix64F A_inv) {

        }

        @Override
        public boolean modifiesA() {
            return modifiesA;
        }

        @Override
        public boolean modifiesB() {
            return modifiesB;
        }
    }
}
