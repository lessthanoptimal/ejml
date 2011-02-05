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

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverAbstract {
    @Test
    public void setA_getA() {
        DenseMatrix64F A = new DenseMatrix64F(1,1);

        MySolver s = new MySolver();
        s.setA(A);

        assertTrue(A==s.getA());
    }

    /**
     * Checks to see if solve is called by the default invert.
     */
    @Test
    public void invert() {
        MySolver solver = new MySolver();

        DenseMatrix64F A = new DenseMatrix64F(1,1);

        solver.setA(A);
        solver.invert(A);

        assertTrue(solver.solveCalled);
    }

    private static class MySolver extends LinearSolverAbstract
    {
        boolean solveCalled = false;

        @Override
        public boolean setA(DenseMatrix64F A) {
            _setA(A);

            return true;
        }

        @Override
        public double quality() {
            throw new IllegalArgumentException("Not supported by this solver.");
        }

        @Override
        public void solve(DenseMatrix64F B, DenseMatrix64F X) {
              solveCalled = true;
        }

        @Override
        public boolean modifiesA() {
            return false;
        }

        @Override
        public boolean modifiesB() {
            return false;
        }
    }
}
