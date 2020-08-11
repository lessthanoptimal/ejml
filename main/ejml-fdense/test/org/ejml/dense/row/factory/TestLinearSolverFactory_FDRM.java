/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.factory;

import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverFactory_FDRM {

    Random rand = new Random(234);

    @Test
    public void general() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj y = new FMatrixRMaj(5,1);

        LinearSolverDense<FMatrixRMaj> solver = LinearSolverFactory_FDRM.general(A.numRows, A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void linear() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj y = new FMatrixRMaj(4,1);

        LinearSolverDense<FMatrixRMaj> solver = LinearSolverFactory_FDRM.linear(A.numRows);

        standardTest(A, x, y, solver);
    }

    @Test
    public void leastSquares() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj y = new FMatrixRMaj(5,1);

        LinearSolverDense<FMatrixRMaj> solver = LinearSolverFactory_FDRM.leastSquares(A.numRows,A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void symmetric() {
        FMatrixRMaj A = RandomMatrices_FDRM.symmetricPosDef(5,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(5,1,rand);
        FMatrixRMaj y = new FMatrixRMaj(5,1);

        LinearSolverDense<FMatrixRMaj> solver = LinearSolverFactory_FDRM.symmPosDef(A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test
    public void adjustable() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,4,rand);
        FMatrixRMaj x = RandomMatrices_FDRM.rectangle(4,1,rand);
        FMatrixRMaj y = new FMatrixRMaj(5,1);

        AdjustableLinearSolver_FDRM solver = LinearSolverFactory_FDRM.adjustable();

        standardTest(A, x, y, solver);

        // remove the last observation
        solver.removeRowFromA(y.numRows-1);

        // compute the adjusted solution
        y.numRows--;
        FMatrixRMaj x_adj = new FMatrixRMaj(4,1);
        solver.solve(y,x_adj);

        // The solution should still be the same
        assertTrue(MatrixFeatures_FDRM.isIdentical(x,x_adj, UtilEjml.TEST_F32));
    }

    /**
     * Given A and x it computes the value of y.  This is then compared against what the solver computes
     * x should be.
     */
    private void standardTest(FMatrixRMaj a, FMatrixRMaj x, FMatrixRMaj y,
                              LinearSolverDense<FMatrixRMaj> solver) {
        solver = new LinearSolverSafe<FMatrixRMaj>(solver);

        CommonOps_FDRM.mult(a,x,y);

        FMatrixRMaj x_found = new FMatrixRMaj(x.numRows,1);

        assertTrue(solver.setA(a));
        solver.solve(y,x_found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(x,x_found,UtilEjml.TEST_F32));
    }
}
