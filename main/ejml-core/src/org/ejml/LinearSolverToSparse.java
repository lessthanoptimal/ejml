/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.Matrix;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.LinearSolverSparse;

/**
 * Wrapper which allows a regular linear solver to act like a sparse solver
 *
 * @author Peter Abeles
 */
public class LinearSolverToSparse<D extends Matrix>
    implements LinearSolverSparse<D,D>
{
    LinearSolverDense<D> solver;

    // locking the structure has no meaning, but need to meet the contract
    boolean locked=false;

    public LinearSolverToSparse(LinearSolverDense<D> solver) {
        this.solver = solver;
    }

    @Override
    public void solveSparse(D B, D X) {
        solver.solve(B,X);
    }

    @Override
    public void setStructureLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isStructureLocked() {
        return locked;
    }

    @Override
    public boolean setA(D A) {
        return solver.setA(A);
    }

    @Override
    public double quality() {
        return solver.quality();
    }

    @Override
    public void solve(D B, D X) {
        solver.solve(B,X);
    }

    @Override
    public boolean modifiesA() {
        return solver.modifiesA();
    }

    @Override
    public boolean modifiesB() {
        return solver.modifiesB();
    }

    @Override
    public <D1 extends DecompositionInterface> D1 getDecomposition() {
        return solver.getDecomposition();
    }
}
