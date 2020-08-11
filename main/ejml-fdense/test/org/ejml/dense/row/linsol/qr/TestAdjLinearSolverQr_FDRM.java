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

package org.ejml.dense.row.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_FDRM;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_FDRM;
import org.ejml.dense.row.mult.SubmatrixOps_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr_FDRM extends GenericLinearSolverChecks_FDRM {


    @Test
    public void addRowToA() {
        int insert = 2;
        int m = 5;
        int n = 3;

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(m,n,rand);
        float row[] = new float[]{1,2,3};

        // create the modified A
        FMatrixRMaj A_e = RandomMatrices_FDRM.rectangle(m+1,n,rand);
        SubmatrixOps_FDRM.setSubMatrix(A,A_e,0,0,0,0,insert,n);
        System.arraycopy(row, 0, A_e.data, insert * n, n);
        SubmatrixOps_FDRM.setSubMatrix(A,A_e,insert,0,insert+1,0,m-insert,n);

        // Compute the solution to the modified  system
        FMatrixRMaj X = RandomMatrices_FDRM.rectangle(n,2,rand);
        FMatrixRMaj Y = new FMatrixRMaj(A_e.numRows,X.numCols);
        CommonOps_FDRM.mult(A_e,X,Y);

        // create the solver from A then add a A.  The solver
        // should be equivalent to one created from A_e
        AdjustableLinearSolver_FDRM adjSolver = new AdjLinearSolverQr_FDRM();

        assertTrue(adjSolver.setA(A));
        adjSolver.addRowToA(row,insert);

        // solve the system and see if it gets the expected solution
        FMatrixRMaj X_found = RandomMatrices_FDRM.rectangle(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_FDRM.isIdentical(X_found,X, UtilEjml.TEST_F32));
    }

    @Test
    public void removeRowFromA() {
        int remove = 2;
        int m = 5;
        int n = 3;

        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(m,n,rand);

        // create the modified A
        FMatrixRMaj A_e = RandomMatrices_FDRM.rectangle(m-1,n,rand);
        SubmatrixOps_FDRM.setSubMatrix(A,A_e,0,0,0,0,remove,n);
        SubmatrixOps_FDRM.setSubMatrix(A,A_e,remove+1,0,remove,0,m-remove-1,n);

        // Compute the solution to the modified system
        FMatrixRMaj X = RandomMatrices_FDRM.rectangle(n,2,rand);
        FMatrixRMaj Y = new FMatrixRMaj(A_e.numRows,X.numCols);
        CommonOps_FDRM.mult(A_e,X,Y);

        // create the solver from the original system then modify it
        AdjustableLinearSolver_FDRM adjSolver = new AdjLinearSolverQr_FDRM();

        adjSolver.setA(A);
        adjSolver.removeRowFromA(remove);

        // see if it produces the epected results

        // solve the system and see if it gets the expected solution
        FMatrixRMaj X_found = RandomMatrices_FDRM.rectangle(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_FDRM.isIdentical(X_found,X,UtilEjml.TEST_F32));
    }

    @Override
    protected LinearSolverDense createSolver(FMatrixRMaj A ) {
        return new AdjLinearSolverQr_FDRM();
    }
}
