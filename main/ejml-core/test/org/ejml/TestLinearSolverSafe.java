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

package org.ejml;

import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public class TestLinearSolverSafe {

    Random rand = new Random(234);

    DMatrixRMaj Ainput = new DMatrixRMaj(1,1);
    DMatrixRMaj Binput = new DMatrixRMaj(1,1);

    /**
     * Checks to see if the input matrix is copied after multiple calls.  This was an actual bug.
     */
    @Test
    public void multipleCalls_setA() {
        DummySolver dummy = new DummySolver(true,false);
        dummy.expectedA = 5;

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        Ainput.set(0,5);
        s.setA(Ainput);
        // call it a second time and see if the input matrix has been reset to the
        // correct value
        s.setA(Ainput);

        assertNotSame(dummy.passedin, Ainput);
    }

    /**
     * Checks to see if the input matrix is copied after multiple calls.  This was an actual bug.
     */
    @Test
    public void multipleCalls_setB() {
        DummySolver dummy = new DummySolver(false,true);
        dummy.expectedB = 5;

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        Binput.set(0,5);
        s.solve(Binput,new DMatrixRMaj(1,1));
        // call it a second time and see if the input matrix has been reset to the
        // correct value
        s.solve(Binput,new DMatrixRMaj(1,1));

        assertNotSame(dummy.passedin, Ainput);
    }

    @Test
    public void testSetA_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        s.setA(Ainput);

        assertSame(dummy.passedin, Ainput);
    }

    @Test
    public void testSetA_mod() {
        DummySolver dummy = new DummySolver(true,false);

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        s.setA(Ainput);

        assertNotSame(dummy.passedin, Ainput);
    }

    @Test
    public void testSolver_notMod() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        s.solve(Binput,new DMatrixRMaj(1,1));

        assertSame(dummy.passedin, Binput);
    }

    @Test
    public void testSolver_mod() {
        DummySolver dummy = new DummySolver(false,true);

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        s.solve(Binput,new DMatrixRMaj(1,1));

        assertNotSame(dummy.passedin, Binput);
    }

    @Test
    public void quality() {
        DummySolver dummy = new DummySolver(false,false);

        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(dummy);

        assertEquals(dummy.quality(), s.quality());
    }

    @Test
    public void modifies() {
        LinearSolverDense<DMatrixRMaj> s = new LinearSolverSafe<>(null);

        assertFalse(s.modifiesA());
        assertFalse(s.modifiesB());

    }

    private class DummySolver implements LinearSolverDense<DMatrixRMaj>
    {
        boolean modifiesA;
        boolean modifiesB;

        DMatrixRMaj passedin;

        // the expected value of the input matrix
        double expectedA = Double.NaN;
        double expectedB = Double.NaN;

        private DummySolver(boolean modifiesA, boolean modifiesB) {
            this.modifiesA = modifiesA;
            this.modifiesB = modifiesB;
        }

        @Override
        public boolean setA(DMatrixRMaj A) {
            passedin = A;

            // the input matrix has an expected input value
            if( !Double.isNaN(expectedA))
                assertEquals(expectedA,A.get(0), UtilEjml.TEST_F64);

            if( modifiesA ) {
                A.set(0,0,rand.nextDouble());
            }

            return true;
        }

        @Override
        public /**/double quality() {
            return 1.1;
        }

        @Override
        public void solve(DMatrixRMaj B, DMatrixRMaj X) {
            passedin = B;

            // the input matrix has an expected input value
            if( !Double.isNaN(expectedB))
                assertEquals(expectedB,B.get(0),UtilEjml.TEST_F64);

            if( modifiesB ) {
                B.set(0,0,rand.nextDouble());
            }
        }

        @Override
        public void invert(DMatrixRMaj A_inv) {

        }

        @Override
        public boolean modifiesA() {
            return modifiesA;
        }

        @Override
        public boolean modifiesB() {
            return modifiesB;
        }

        @Override
        public <D extends DecompositionInterface> D getDecomposition() {
            return null;
        }
    }
}
