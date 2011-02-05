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

package org.ejml.alg.dense.linsol.qr;

import org.ejml.alg.dense.linsol.AdjustableLinearSolver;
import org.ejml.alg.dense.linsol.GenericLinearSolverChecks;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.mult.SubmatrixOps;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestAdjLinearSolverQr extends GenericLinearSolverChecks {


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
        AdjustableLinearSolver adjSolver = new AdjLinearSolverQr();

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
        AdjustableLinearSolver adjSolver = new AdjLinearSolverQr();

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
        return new AdjLinearSolverQr();
    }
}
