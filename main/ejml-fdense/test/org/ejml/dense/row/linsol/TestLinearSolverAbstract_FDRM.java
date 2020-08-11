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

import org.ejml.data.FMatrixRMaj;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverAbstract_FDRM {
    @Test
    public void setA_getA() {
        FMatrixRMaj A = new FMatrixRMaj(1,1);

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

        FMatrixRMaj A = new FMatrixRMaj(1,1);

        solver.setA(A);
        solver.invert(A);

        assertTrue(solver.solveCalled);
    }

    private static class MySolver extends LinearSolverAbstract_FDRM
    {
        boolean solveCalled = false;

        @Override
        public boolean setA(FMatrixRMaj A) {
            _setA(A);

            return true;
        }

        @Override
        public /**/double quality() {
            throw new IllegalArgumentException("Not supported by this solver.");
        }

        @Override
        public void solve(FMatrixRMaj B, FMatrixRMaj X) {
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
