/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverSafe {

    DenseMatrix64F Ainput = new DenseMatrix64F(1,1);
    DenseMatrix64F Binput = new DenseMatrix64F(1,1);

    @Test
    public void testSetA_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver s = new LinearSolverSafe(dummy);

        s.setA(Ainput);

        assertTrue(dummy.passedin == Ainput);
    }

    @Test
    public void testSetA_mod() {
        DummySolver dummy = new DummySolver(true,false);

        LinearSolver s = new LinearSolverSafe(dummy);

        s.setA(Ainput);

        assertTrue(dummy.passedin != Ainput);
    }

    @Test
    public void testSolver_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver s = new LinearSolverSafe(dummy);

        s.solve(Binput,new DenseMatrix64F(1,1));

        assertTrue(dummy.passedin == Binput);
    }

    @Test
    public void testSolver_mod() {
        DummySolver dummy = new DummySolver(false,true);

        LinearSolver s = new LinearSolverSafe(dummy);

        s.solve(Binput,new DenseMatrix64F(1,1));

        assertTrue(dummy.passedin != Binput);
    }

    @Test
    public void quality() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolver s = new LinearSolverSafe(dummy);

        assertTrue(s.quality()==dummy.quality());
    }

    @Test
    public void modifies() {
        LinearSolver s = new LinearSolverSafe(null);

        assertFalse(s.modifiesA());
        assertFalse(s.modifiesB());

    }

    private class DummySolver implements LinearSolver<DenseMatrix64F>
    {
        boolean modifiesA;
        boolean modifiesB;

        DenseMatrix64F passedin;

        private DummySolver(boolean modifiesA, boolean modifiesB) {
            this.modifiesA = modifiesA;
            this.modifiesB = modifiesB;
        }

        @Override
        public boolean setA(DenseMatrix64F A) {
            passedin = A;

            return true;
        }

        @Override
        public double quality() {
            return 1.1;
        }

        @Override
        public void solve(DenseMatrix64F B, DenseMatrix64F X) {
            passedin = B;
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
