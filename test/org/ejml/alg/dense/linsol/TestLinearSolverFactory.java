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
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverFactory {

    Random rand = new Random(234);

    @Test
    public void general() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,4,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F y = new DenseMatrix64F(5,1);

        LinearSolver solver = LinearSolverFactory.general(A.numRows,A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void linear() {
        DenseMatrix64F A = RandomMatrices.createRandom(4,4,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F y = new DenseMatrix64F(4,1);

        LinearSolver solver = LinearSolverFactory.linear(A.numRows);

        standardTest(A, x, y, solver);
    }

    @Test
    public void leastSquares() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,4,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F y = new DenseMatrix64F(5,1);

        LinearSolver solver = LinearSolverFactory.leastSquares(A.numRows,A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void symmetric() {
        DenseMatrix64F A = RandomMatrices.createSymmPosDef(5,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(5,1,rand);
        DenseMatrix64F y = new DenseMatrix64F(5,1);

        LinearSolver solver = LinearSolverFactory.symmetric();

        standardTest(A, x, y, solver);
    }

    @Test
    public void adjustable() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,4,rand);
        DenseMatrix64F x = RandomMatrices.createRandom(4,1,rand);
        DenseMatrix64F y = new DenseMatrix64F(5,1);

        AdjustableLinearSolver solver = LinearSolverFactory.adjustable();

        standardTest(A, x, y, solver);

        // remove the last observation
        solver.removeRowFromA(y.numRows-1);

        // compute the adjusted solution
        y.numRows--;
        DenseMatrix64F x_adj = new DenseMatrix64F(4,1);
        solver.solve(y,x_adj);

        // The solution should still be the same
        assertTrue(MatrixFeatures.isIdentical(x,x_adj,1e-8));
    }

    /**
     * Given A and x it computes the value of y.  This is then compared against what the solver computes
     * x should be.
     */
    private void standardTest(DenseMatrix64F a, DenseMatrix64F x, DenseMatrix64F y, LinearSolver solver) {
        CommonOps.mult(a,x,y);

        DenseMatrix64F x_found = new DenseMatrix64F(x.numRows,1);

        assertTrue(solver.setA(a));
        solver.solve(y,x_found);

        assertTrue(MatrixFeatures.isIdentical(x,x_found,1e-8));
    }
}
