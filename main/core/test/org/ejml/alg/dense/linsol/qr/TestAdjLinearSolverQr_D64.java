/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import org.ejml.alg.dense.linsol.GenericLinearSolverChecks;
import org.ejml.alg.dense.mult.SubmatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr_D64 extends GenericLinearSolverChecks {


    @Test
    public void addRowToA() {
        int insert = 2;
        int m = 5;
        int n = 3;

        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);
        double row[] = new double[]{1,2,3};

        // create the modified A
        DenseMatrix64F A_e = RandomMatrices.createRandom(m+1,n,rand);
        SubmatrixOps.setSubMatrix(A,A_e,0,0,0,0,insert,n);
        System.arraycopy(row, 0, A_e.data, insert * n, n);
        SubmatrixOps.setSubMatrix(A,A_e,insert,0,insert+1,0,m-insert,n);

        // Compute the solution to the modified  system
        DenseMatrix64F X = RandomMatrices.createRandom(n,2,rand);
        DenseMatrix64F Y = new DenseMatrix64F(A_e.numRows,X.numCols);
        CommonOps.mult(A_e,X,Y);

        // create the solver from A then add a A.  The solver
        // should be equivalent to one created from A_e
        AdjustableLinearSolver adjSolver = new AdjLinearSolverQr_D64();

        assertTrue(adjSolver.setA(A));
        adjSolver.addRowToA(row,insert);

        // solve the system and see if it gets the expected solution
        DenseMatrix64F X_found = RandomMatrices.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures.isIdentical(X_found,X,1e-8));
    }

    @Test
    public void removeRowFromA() {
        int remove = 2;
        int m = 5;
        int n = 3;

        DenseMatrix64F A = RandomMatrices.createRandom(m,n,rand);

        // create the modified A
        DenseMatrix64F A_e = RandomMatrices.createRandom(m-1,n,rand);
        SubmatrixOps.setSubMatrix(A,A_e,0,0,0,0,remove,n);
        SubmatrixOps.setSubMatrix(A,A_e,remove+1,0,remove,0,m-remove-1,n);

        // Compute the solution to the modified system
        DenseMatrix64F X = RandomMatrices.createRandom(n,2,rand);
        DenseMatrix64F Y = new DenseMatrix64F(A_e.numRows,X.numCols);
        CommonOps.mult(A_e,X,Y);

        // create the solver from the original system then modify it
        AdjustableLinearSolver adjSolver = new AdjLinearSolverQr_D64();

        adjSolver.setA(A);
        adjSolver.removeRowFromA(remove);

        // see if it produces the epected results

        // solve the system and see if it gets the expected solution
        DenseMatrix64F X_found = RandomMatrices.createRandom(X.numRows,X.numCols,rand);
        adjSolver.solve(Y,X_found);

        // see if they produce the same results
        assertTrue(MatrixFeatures.isIdentical(X_found,X,1e-8));
    }

    @Override
    protected LinearSolver createSolver( DenseMatrix64F A ) {
        return new AdjLinearSolverQr_D64();
    }
}
