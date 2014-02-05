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

import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * Solver which uses an unrolled inverse to compute the inverse.  This can only invert matrices and not solve.
 * This is faster than LU inverse but only supports small matrices..
 *
 * @author Peter Abeles
 */
public class LinearSolverUnrolled implements LinearSolver<DenseMatrix64F> {
    DenseMatrix64F A;

    @Override
    public boolean setA(DenseMatrix64F A) {
        if( A.numRows != A.numCols)
            return false;

        this.A = A;
        return A.numRows <= UnrolledInverseFromMinor.MAX;
    }

    @Override
    public double quality() {
        throw new IllegalArgumentException("Not supported by this solver.");
    }

    @Override
    public void solve(DenseMatrix64F B, DenseMatrix64F X) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        if( A.numRows == 1 )
            A_inv.set(0,  1.0/A.get(0));
        UnrolledInverseFromMinor.inv(A,A_inv);
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
