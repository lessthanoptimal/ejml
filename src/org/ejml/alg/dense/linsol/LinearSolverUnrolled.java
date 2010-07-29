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

package org.ejml.alg.dense.linsol;

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.data.DenseMatrix64F;


/**
 * Solver which uses an unrolled inverse to compute the inverse.  This can only invert matrices and not solve.
 * This is faster than LU inverse but only supports small matrices..
 *
 * @author Peter Abeles
 */
public class LinearSolverUnrolled implements LinearSolver {
    DenseMatrix64F A;

    @Override
    public DenseMatrix64F getA() {
        return A;
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        if( A.numRows != A.numCols)
            return false;

        this.A = A;
        return A.numRows <= UnrolledInverseFromMinor.MAX;
    }

    @Override
    public double quality() {
        return Double.NaN;
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        if( A.numRows == 1 )
            A_inv.data[0] = 1.0/A.data[0];
        UnrolledInverseFromMinor.inv(A,A_inv);
    }
}
