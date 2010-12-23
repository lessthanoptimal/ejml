/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * Selects different implementations of QR decomposition solvers depending on the
 * size of the input matrix and its selected parameters.  Eat time {@link #setA} is called
 * it determines which implementation to use.  A new implementation is only declared when it needs to
 * change implementations.
 * </p>
 *
 * @author Peter Abeles
 */
public class SmartSolverQr implements LinearSolver<DenseMatrix64F>  {

    // instance of the solver it is currently using
    private LinearSolver s;
    // the type of solver which is currently being used.
    private Type type;

    public SmartSolverQr() {
    }

    @Override
    public boolean setA(DenseMatrix64F A) {

        Type selected;

        if( A.numCols < EjmlParameters.SWITCH_BLOCK64_QR )  {
            selected = Type.REGULAR;
        } else {
            selected = Type.BLOCK64;
        }

        if( s == null || selected != type ) {
            switch( selected ) {
                case REGULAR:
                    s = new LinearSolverQrHouseCol();
                    break;

                case BLOCK64:
                    if( EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER )
//                        s = new LinearSolverQrBlock64();
                        s = new LinearSolverQrHouseCol();
                    else
                        s = new LinearSolverQrHouseCol();
                    break;
            }
            type = selected;
        }

        return s.setA(A);
    }

    @Override
    public double quality() {
        return s.quality();
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        s.solve(B,X);
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        s.invert(A_inv);
    }

    @Override
    public boolean modifiesA() {
        return false;
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    public LinearSolver getS() {
        return s;
    }

    public void setS(LinearSolver s) {
        this.s = s;
    }

    /**
     * Different implementation types.
     */
    static enum Type {
        REGULAR,
        BLOCK64
    }
}
