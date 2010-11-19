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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * Selects different implementations of Cholesky decomposition solvers depending on the
 * size of the input matrix and its selected parameters.  Eat time {@link #setA} is called
 * it determines which implementation to use.  A new implementation is only declared when it needs to
 * change implementations.
 * </p>
 *
 * @author Peter Abeles
 */
public class SmartSolverChol implements LinearSolver  {

    // instance of the solver it is currently using
    private LinearSolver s;
    // the type of solver which is currently being used.
    private Type type;
    // should the original matrix be written over by the decomposition
    private boolean decomposeOriginal = false;

    public SmartSolverChol(boolean decomposeOriginal ) {
        this.decomposeOriginal = decomposeOriginal;

        CholeskyDecompositionCommon d;
        d = new CholeskyDecompositionBlock(decomposeOriginal, EjmlParameters.BLOCK_WIDTH_CHOL);
        s = new LinearSolverChol(d);
        type = Type.BLOCK;
    }

    public SmartSolverChol() {
    }

    @Override
    public DenseMatrix64F getA() {
        return s.getA();
    }

    @Override
    public boolean setA(DenseMatrix64F A) {

        Type selected;

        if( A.numCols < EjmlParameters.SWITCH_BLOCK64_CHOLESKY ) {
            selected = Type.BLOCK;
        } else {
            selected = Type.BLOCK64;
        }

        if( selected != type ) {
            CholeskyDecompositionCommon d;
            switch( selected ) {
                case BLOCK:
                    d = new CholeskyDecompositionBlock(decomposeOriginal, EjmlParameters.BLOCK_WIDTH_CHOL);
                    s = new LinearSolverChol(d);
                    break;

                case BLOCK64:
                    s = new LinearSolverCholBlock64();
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

    public LinearSolver getS() {
        return s;
    }

    public void setS(LinearSolver s) {
        this.s = s;
    }

    public boolean isDecomposeOriginal() {
        return decomposeOriginal;
    }

    public void setDecomposeOriginal(boolean decomposeOriginal) {
        this.decomposeOriginal = decomposeOriginal;
    }

    /**
     * Different implementation types.
     */
    static enum Type {
        BLOCK,
        BLOCK64
    }
}
