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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestSolvePseudoInverse extends GenericLinearSolverChecks{

    public TestSolvePseudoInverse() {
        this.shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<DenseMatrix64F> createSolver( DenseMatrix64F A ) {
        return new SolvePseudoInverse(A.numRows,A.numCols);
    }

    /**
     * The pseudo inverse should never fail and every matrix has an inverse
     */
    @Test
    public void singularMatrix() {
        DenseMatrix64F A = new DenseMatrix64F(2,3,true,1,2,3,4,5,6);
        DenseMatrix64F A_pinv = new DenseMatrix64F(3,2);
        SolvePseudoInverse solver = new SolvePseudoInverse();
        assertTrue(solver.setA(A));

        solver.invert(A_pinv);

        DenseMatrix64F C = new DenseMatrix64F(2,2);
        CommonOps.mult(A,A_pinv,C);

        assertTrue(MatrixFeatures.isIdentity(C,1e-8));
    }
}
