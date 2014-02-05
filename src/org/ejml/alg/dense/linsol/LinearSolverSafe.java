/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.ReshapeMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * Ensures that any linear solver it is wrapped around will never modify
 * the input matrices.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class LinearSolverSafe<T extends ReshapeMatrix64F> implements LinearSolver<T> {

    // the solver it is wrapped around
    private LinearSolver<T> alg;

    // local copies of input matrices that can be modified.
    private T A;
    private T B;

    /**
     *
     * @param alg The solver it is wrapped around.
     */
    public LinearSolverSafe(LinearSolver<T> alg) {
        this.alg = alg;
    }

    @Override
    public boolean setA(T A) {

        if( alg.modifiesA() ) {
            if( this.A == null ) {
                this.A = (T)A.copy();
            } else {
                if( this.A.numRows != A.numRows || this.A.numCols != A.numCols ) {
                    this.A.reshape(A.numRows,A.numCols,false);
                }
                this.A.set(A);
            }
            return alg.setA(this.A);
        }

        return alg.setA(A);
    }

    @Override
    public double quality() {
        return alg.quality();
    }

    @Override
    public void solve(T B, T X) {
        if( alg.modifiesB() ) {
            if( this.B == null ) {
                this.B = (T)B.copy();
            } else {
                if( this.B.numRows != B.numRows || this.B.numCols != B.numCols ) {
                    this.B.reshape(A.numRows,B.numCols,false);
                }
                this.B.set(B);
            }
            B = this.B;
        }

        alg.solve(B,X);
    }

    @Override
    public void invert(T A_inv) {
        alg.invert(A_inv);
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
