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

package org.ejml.alg.dense.linsol.svd;

import org.ejml.alg.dense.linsol.GenericLinearSolverChecks;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSolvePseudoInverse extends GenericLinearSolverChecks {

    public TestSolvePseudoInverse() {
        this.shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<DenseMatrix64F> createSolver( DenseMatrix64F A ) {
        return new SolvePseudoInverse(A.numRows,A.numCols);
    }

    /**
     * The solve should never fail and can handle singular matrices
     */
    @Test
    public void singularMatrix_solve() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(2,1,true,4,7);
        SolvePseudoInverse solver = new SolvePseudoInverse();
        assertTrue(solver.setA(A));

        DenseMatrix64F x = new DenseMatrix64F(3,1);
        solver.solve(y,x);
        
        DenseMatrix64F found = new DenseMatrix64F(2,1);
        CommonOps.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures.isEquals(y, found, 1e-8));
    }

    /**
     * The pseudo inverse should never fail and every matrix has an inverse
     */
    @Test
    public void singularMatrix_inv() {
        // create a matrix where two rows are linearly dependent
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,2,3,4);

        DenseMatrix64F y = new DenseMatrix64F(2,1,true,4,7);
        SolvePseudoInverse solver = new SolvePseudoInverse();
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
}
