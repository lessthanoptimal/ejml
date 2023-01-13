/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverFactory_DDRM extends EjmlStandardJUnit {
    @Test void general() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4,1,rand);
        DMatrixRMaj y = new DMatrixRMaj(5,1);

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.general(A.numRows, A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test void linear() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,4,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4,1,rand);
        DMatrixRMaj y = new DMatrixRMaj(4,1);

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.linear(A.numRows);

        standardTest(A, x, y, solver);
    }

    @Test void leastSquares() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4,1,rand);
        DMatrixRMaj y = new DMatrixRMaj(5,1);

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.leastSquares(A.numRows,A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test void symmetric() {
        DMatrixRMaj A = RandomMatrices_DDRM.symmetricPosDef(5,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(5,1,rand);
        DMatrixRMaj y = new DMatrixRMaj(5,1);

        LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.symmPosDef(A.numCols);

        standardTest(A, x, y, solver);
    }

    @Test void adjustable() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,4,rand);
        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(4,1,rand);
        DMatrixRMaj y = new DMatrixRMaj(5,1);

        AdjustableLinearSolver_DDRM solver = LinearSolverFactory_DDRM.adjustable();

        standardTest(A, x, y, solver);

        // remove the last observation
        solver.removeRowFromA(y.numRows-1);

        // compute the adjusted solution
        y.numRows--;
        DMatrixRMaj x_adj = new DMatrixRMaj(4,1);
        solver.solve(y,x_adj);

        // The solution should still be the same
        assertTrue(MatrixFeatures_DDRM.isIdentical(x,x_adj, UtilEjml.TEST_F64));
    }

    /**
     * Given A and x it computes the value of y. This is then compared against what the solver computes
     * x should be.
     */
    private void standardTest(DMatrixRMaj a, DMatrixRMaj x, DMatrixRMaj y,
                              LinearSolverDense<DMatrixRMaj> solver) {
        solver = new LinearSolverSafe<DMatrixRMaj>(solver);

        CommonOps_DDRM.mult(a,x,y);

        DMatrixRMaj x_found = new DMatrixRMaj(x.numRows,1);

        assertTrue(solver.setA(a));
        solver.solve(y,x_found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(x,x_found,UtilEjml.TEST_F64));
    }
}
