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

package org.ejml.dense.row.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_DDRM;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_DDRM;
import org.ejml.dense.row.mult.SubmatrixOps_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr_DDRM extends GenericLinearSolverChecks_DDRM {
    @Test void addRowToA() {
        int insert = 2;
        int m = 5;
        int n = 3;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);
        double row[] = new double[]{1, 2, 3};

        // create the modified A
        DMatrixRMaj A_e = RandomMatrices_DDRM.rectangle(m + 1, n, rand);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, 0, 0, 0, 0, insert, n);
        System.arraycopy(row, 0, A_e.data, insert*n, n);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, insert, 0, insert + 1, 0, m - insert, n);

        // Compute the solution to the modified  system
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(n, 2, rand);
        DMatrixRMaj Y = new DMatrixRMaj(A_e.numRows, X.numCols);
        CommonOps_DDRM.mult(A_e, X, Y);

        // create the solver from A then add a A. The solver
        // should be equivalent to one created from A_e
        AdjustableLinearSolver_DDRM adjSolver = new AdjLinearSolverQr_DDRM();

        assertTrue(adjSolver.setA(A));
        adjSolver.addRowToA(row, insert);

        // solve the system and see if it gets the expected solution
        DMatrixRMaj X_found = RandomMatrices_DDRM.rectangle(X.numRows, X.numCols, rand);
        adjSolver.solve(Y, X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_DDRM.isIdentical(X_found, X, UtilEjml.TEST_F64));
    }

    @Test void removeRowFromA() {
        int remove = 2;
        int m = 5;
        int n = 3;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(m, n, rand);

        // create the modified A
        DMatrixRMaj A_e = RandomMatrices_DDRM.rectangle(m - 1, n, rand);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, 0, 0, 0, 0, remove, n);
        SubmatrixOps_DDRM.setSubMatrix(A, A_e, remove + 1, 0, remove, 0, m - remove - 1, n);

        // Compute the solution to the modified system
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(n, 2, rand);
        DMatrixRMaj Y = new DMatrixRMaj(A_e.numRows, X.numCols);
        CommonOps_DDRM.mult(A_e, X, Y);

        // create the solver from the original system then modify it
        AdjustableLinearSolver_DDRM adjSolver = new AdjLinearSolverQr_DDRM();

        adjSolver.setA(A);
        adjSolver.removeRowFromA(remove);

        // see if it produces the epected results

        // solve the system and see if it gets the expected solution
        DMatrixRMaj X_found = RandomMatrices_DDRM.rectangle(X.numRows, X.numCols, rand);
        adjSolver.solve(Y, X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_DDRM.isIdentical(X_found, X, UtilEjml.TEST_F64));
    }

    @Override
    protected LinearSolverDense createSolver( DMatrixRMaj A ) {
        return new AdjLinearSolverQr_DDRM();
    }
}
