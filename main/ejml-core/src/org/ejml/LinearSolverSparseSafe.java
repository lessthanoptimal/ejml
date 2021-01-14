/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixSparse;
import org.ejml.data.ReshapeMatrix;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.jetbrains.annotations.Nullable;

/**
 * Ensures that any linear solver it is wrapped around will never modify
 * the input matrices.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class LinearSolverSparseSafe<S extends DMatrixSparse, D extends ReshapeMatrix>
        implements LinearSolverSparse<S, D> {

    // the solver it is wrapped around
    private final LinearSolverSparse<S, D> alg;

    // local copies of input matrices that can be modified.
    private @Nullable S A;
    private @Nullable D B;

    /**
     * @param alg The solver it is wrapped around.
     */
    public LinearSolverSparseSafe( LinearSolverSparse<S, D> alg ) {
        this.alg = alg;
    }

    @Override
    public boolean setA( S A ) {

        if (alg.modifiesA()) {
            this.A = UtilEjml.reshapeOrDeclare(this.A, A);
            this.A.setTo(A);
            return alg.setA(this.A);
        }

        return alg.setA(A);
    }

    @Override
    public /**/double quality() {
        return alg.quality();
    }

    @Override
    public void solve( D B, D X ) {
        if (alg.modifiesB()) {
            this.B = UtilEjml.reshapeOrDeclare(this.B, B);
            this.B.setTo(B);
            B = this.B;
        }

        alg.solve(B, X);
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
    public <Decomposition extends DecompositionInterface> Decomposition getDecomposition() {
        return alg.getDecomposition();
    }

    @Override
    public void solveSparse( S B, S X ) {
        alg.solveSparse(B, X);
    }

    @Override
    public void setStructureLocked( boolean locked ) {
        alg.setStructureLocked(locked);
    }

    @Override
    public boolean isStructureLocked() {
        return alg.isStructureLocked();
    }
}
