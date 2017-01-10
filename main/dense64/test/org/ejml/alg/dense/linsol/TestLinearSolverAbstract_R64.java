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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverAbstract_R64 {
    @Test
    public void setA_getA() {
        DMatrixRow_F64 A = new DMatrixRow_F64(1,1);

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

        DMatrixRow_F64 A = new DMatrixRow_F64(1,1);

        solver.setA(A);
        solver.invert(A);

        assertTrue(solver.solveCalled);
    }

    private static class MySolver extends LinearSolverAbstract_R64
    {
        boolean solveCalled = false;

        @Override
        public boolean setA(DMatrixRow_F64 A) {
            _setA(A);

            return true;
        }

        @Override
        public /**/double quality() {
            throw new IllegalArgumentException("Not supported by this solver.");
        }

        @Override
        public void solve(DMatrixRow_F64 B, DMatrixRow_F64 X) {
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

        @Override
        public <D extends DecompositionInterface> D getDecomposition() {
            return null;
        }
    }
}
