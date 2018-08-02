/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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


/**
 * Ensures that any linear solver it is wrapped around will never modify
 * the input matrices.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class LinearSolverSparseSafe<S extends DMatrixSparse, D extends ReshapeMatrix>
        implements LinearSolverSparse<S,D> {

    // the solver it is wrapped around
    private LinearSolverSparse<S,D> alg;

    // local copies of input matrices that can be modified.
    private S A;
    private D B;
    private S Bsparse;
    /**
     *
     * @param alg The solver it is wrapped around.
     */
    public LinearSolverSparseSafe(LinearSolverSparse<S,D> alg) {
        this.alg = alg;
    }

    @Override
    public boolean setA(S A) {

        if( alg.modifiesA() ) {
            if( this.A == null ) {
                this.A = A.copy();
            } else {
                if( this.A.getNumRows() != A.getNumRows() || this.A.getNumCols() != A.getNumCols() ) {
                    this.A.reshape(A.getNumRows(),A.getNumCols(),1);
                }
                this.A.set(A);
            }
            return alg.setA(this.A);
        }

        return alg.setA(A);
    }

    @Override
    public /**/double quality() {
        return alg.quality();
    }

    @Override
    public void solve(D B, D X) {
        if( alg.modifiesB() ) {
            if( this.B == null ) {
                this.B = B.copy();
            } else {
                if( this.B.getNumRows() != B.getNumRows() || this.B.getNumCols() != B.getNumCols() ) {
                    this.B.reshape(A.getNumRows(),B.getNumCols());
                }
                this.B.set(B);
            }
            B = this.B;
        }

        alg.solve(B,X);
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
        return alg.getDecomposition();
    }

    @Override
    public void solveSparse(S B, S X) {
        alg.solveSparse(B,X);
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
