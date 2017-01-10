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

package org.ejml.dense.row.linsol.qr;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.linsol.AdjustableLinearSolver_R64;
import org.ejml.dense.row.linsol.GenericLinearSolverChecks_R64;
import org.ejml.dense.row.mult.SubmatrixOps_R64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr_R64 extends GenericLinearSolverChecks_R64 {


    @Test
    public void addRowToA() {
        int insert = 2;
        int m = 5;
        int n = 3;

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(m,n,rand);
        double row[] = new double[]{1,2,3};

        // create the modified A
        DMatrixRow_F64 A_e = RandomMatrices_R64.createRandom(m+1,n,rand);
        SubmatrixOps_R64.setSubMatrix(A,A_e,0,0,0,0,insert,n);
        System.arraycopy(row, 0, A_e.data, insert * n, n);
        SubmatrixOps_R64.setSubMatrix(A,A_e,insert,0,insert+1,0,m-insert,n);

        // Compute the solution to the modified  system
        DMatrixRow_F64 X = RandomMatrices_R64.createRandom(n,2,rand);
        DMatrixRow_F64 Y = new DMatrixRow_F64(A_e.numRows,X.numCols);
        CommonOps_R64.mult(A_e,X,Y);

        // create the solver from A then add a A.  The solver
        // should be equivalent to one created from A_e
        AdjustableLinearSolver_R64 adjSolver = new AdjLinearSolverQr_R64();

        assertTrue(adjSolver.setA(A));
        adjSolver.addRowToA(row,insert);

        // solve the system and see if it gets the expected solution
        DMatrixRow_F64 X_found = RandomMatrices_R64.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_R64.isIdentical(X_found,X, UtilEjml.TEST_F64));
    }

    @Test
    public void removeRowFromA() {
        int remove = 2;
        int m = 5;
        int n = 3;

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(m,n,rand);

        // create the modified A
        DMatrixRow_F64 A_e = RandomMatrices_R64.createRandom(m-1,n,rand);
        SubmatrixOps_R64.setSubMatrix(A,A_e,0,0,0,0,remove,n);
        SubmatrixOps_R64.setSubMatrix(A,A_e,remove+1,0,remove,0,m-remove-1,n);

        // Compute the solution to the modified system
        DMatrixRow_F64 X = RandomMatrices_R64.createRandom(n,2,rand);
        DMatrixRow_F64 Y = new DMatrixRow_F64(A_e.numRows,X.numCols);
        CommonOps_R64.mult(A_e,X,Y);

        // create the solver from the original system then modify it
        AdjustableLinearSolver_R64 adjSolver = new AdjLinearSolverQr_R64();

        adjSolver.setA(A);
        adjSolver.removeRowFromA(remove);

        // see if it produces the epected results

        // solve the system and see if it gets the expected solution
        DMatrixRow_F64 X_found = RandomMatrices_R64.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures_R64.isIdentical(X_found,X,UtilEjml.TEST_F64));
    }

    @Override
    protected LinearSolver createSolver( DMatrixRow_F64 A ) {
        return new AdjLinearSolverQr_R64();
    }
}
