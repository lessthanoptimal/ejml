/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.factory;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.linsol.AdjustableLinearSolver_R64;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverFactory_R64 {

    Random rand = new Random(234);

    @Test
    public void general() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 y = new RowMatrix_F64(5,1);

        LinearSolver<RowMatrix_F64> solver = LinearSolverFactory_R64.general(A.numRows, A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void linear() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(4,4,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 y = new RowMatrix_F64(4,1);

        LinearSolver<RowMatrix_F64> solver = LinearSolverFactory_R64.linear(A.numRows);

        standardTest(A, x, y, solver);
    }

    @Test
    public void leastSquares() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 y = new RowMatrix_F64(5,1);

        LinearSolver<RowMatrix_F64> solver = LinearSolverFactory_R64.leastSquares(A.numRows,A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void symmetric() {
        RowMatrix_F64 A = RandomMatrices_R64.createSymmPosDef(5,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(5,1,rand);
        RowMatrix_F64 y = new RowMatrix_F64(5,1);

        LinearSolver<RowMatrix_F64> solver = LinearSolverFactory_R64.symmPosDef(A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void adjustable() {
        RowMatrix_F64 A = RandomMatrices_R64.createRandom(5,4,rand);
        RowMatrix_F64 x = RandomMatrices_R64.createRandom(4,1,rand);
        RowMatrix_F64 y = new RowMatrix_F64(5,1);

        AdjustableLinearSolver_R64 solver = LinearSolverFactory_R64.adjustable();

        standardTest(A, x, y, solver);

        // remove the last observation
        solver.removeRowFromA(y.numRows-1);

        // compute the adjusted solution
        y.numRows--;
        RowMatrix_F64 x_adj = new RowMatrix_F64(4,1);
        solver.solve(y,x_adj);

        // The solution should still be the same
        assertTrue(MatrixFeatures_R64.isIdentical(x,x_adj, UtilEjml.TEST_F64));
    }

    /**
     * Given A and x it computes the value of y.  This is then compared against what the solver computes
     * x should be.
     */
    private void standardTest(RowMatrix_F64 a, RowMatrix_F64 x, RowMatrix_F64 y,
                              LinearSolver<RowMatrix_F64> solver) {
        solver = new LinearSolverSafe<RowMatrix_F64>(solver);

        CommonOps_R64.mult(a,x,y);

        RowMatrix_F64 x_found = new RowMatrix_F64(x.numRows,1);

        assertTrue(solver.setA(a));
        solver.solve(y,x_found);

        assertTrue(MatrixFeatures_R64.isIdentical(x,x_found,UtilEjml.TEST_F64));
    }
}
