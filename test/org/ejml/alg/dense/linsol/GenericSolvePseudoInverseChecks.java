/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Tests the ability of a solver to handle different type of rank deficient matrices
 *
 * @author Peter Abeles
 */
public class GenericSolvePseudoInverseChecks {

    Random rand = new Random(234);

    LinearSolver<DenseMatrix64F> solver;

    public GenericSolvePseudoInverseChecks(LinearSolver<DenseMatrix64F> solver) {
        this.solver = new LinearSolverSafe<DenseMatrix64F>( solver );
    }

    public void all() {
        underDetermined_wide_solve();
        underDetermined_wide_inv();
        underDetermined_tall_solve();
        singular_solve();
        singular_inv();
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_wide_solve() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(2,1,true,4,7);
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(3,1);
        solver.solve(y,x);

        DenseMatrix64F found = new DenseMatrix64F(2,1);
        CommonOps.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures.isEquals(y, found, 1e-8));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void underDetermined_wide_inv() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(2,1,true,4,7);
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(3,1);
        solver.solve(y,x);

        // now test the pseudo inverse
        DenseMatrix64F A_pinv = new DenseMatrix64F(3,2);
        DenseMatrix64F found = new DenseMatrix64F(3,1);
        solver.invert(A_pinv);

        CommonOps.mult(A_pinv,y,found);

        assertTrue(MatrixFeatures.isEquals(x, found,1e-8));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_tall_solve() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(3,2,true,1,2,1,2,2,4);

        DenseMatrix64F y = new DenseMatrix64F(3,1,true,4,4,8);
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(2,1);
        solver.solve(y,x);

        DenseMatrix64F found = new DenseMatrix64F(3,1);
        CommonOps.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures.isEquals(y, found, 1e-8));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void singular_solve() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(3,3,true,1,2,3,2,3,4,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(3,1,true,4,7,7);
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(3,1);
        solver.solve(y,x);

        DenseMatrix64F found = new DenseMatrix64F(3,1);
        CommonOps.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures.isEquals(y, found, 1e-8));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void singular_inv() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(3,3,true,1,2,3,2,3,4,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(3,1,true,4,7,7);
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(3,1);
        solver.solve(y,x);

        // now test the pseudo inverse
        DenseMatrix64F A_pinv = new DenseMatrix64F(3,3);
        DenseMatrix64F found = new DenseMatrix64F(3,1);
        solver.invert(A_pinv);

        CommonOps.mult(A_pinv,y,found);

        assertTrue(MatrixFeatures.isEquals(x, found,1e-8));
    }
}
