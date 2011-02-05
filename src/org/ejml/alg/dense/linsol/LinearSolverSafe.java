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

import org.ejml.data.Matrix64F;


/**
 * Ensures that any linear solver it is wrapped around will never modify
 * the input matrices.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class LinearSolverSafe<T extends Matrix64F> implements LinearSolver<T> {

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
            } else if( this.A.numRows != A.numRows || this.A.numCols != A.numCols ) {
                this.A.reshape(A.numRows,A.numCols,false);
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
            } else if( this.B.numRows != B.numRows || this.B.numCols != B.numCols ) {
                this.B.reshape(A.numRows,B.numCols,false);
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
