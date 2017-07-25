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

package org.ejml.sparse.csc.linsol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.FillInPermutation;
import org.ejml.sparse.LinearSolverSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Generic tests for linear solvers
 *
 * @author Peter Abeles
 */
public abstract class GenericLinearSolverSparseTests_DSCC {

    protected Random rand = new Random(234);

    protected FillInPermutation permutationTests[] =
            new FillInPermutation[]{FillInPermutation.NONE,FillInPermutation.SOMETHING};

    public abstract LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> createSolver(FillInPermutation permutation);

    /**
     * Create a random matrix. The exact shape is determined by the implementation but all allowable shapes for this
     * size should be randomized
     */
    public abstract DMatrixSparseCSC createA(int size);

    public DMatrixRMaj create( int rows , int cols ) {
        return RandomMatrices_DDRM.rectangle(rows,cols,rand);
    }

    @Test
    public void randomSolveable() {

        for( FillInPermutation perm : permutationTests ) {
            LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(perm);

            for (int N : new int[]{1, 2, 5, 10, 20}) {
                for (int mc = 0; mc < 30; mc++) {
                    DMatrixSparseCSC A = createA(N);
                    DMatrixRMaj X = create(A.numCols, 3);
                    DMatrixRMaj foundX = create(A.numCols, 3);
                    DMatrixRMaj B = new DMatrixRMaj(A.numRows, 3);

                    // compute the solution
                    CommonOps_DSCC.mult(A, X, B);

                    assertTrue(solver.setA(A));
                    solver.solve(B, foundX);

                    // TODO uneasy about needing the 10x when permutation is added. Should be literally identical
                    EjmlUnitTests.assertEqualsR(X, foundX, 10*UtilEjml.TEST_F64);
                }
            }
        }
    }

    /**
     * Give it a matrix that's all zeros and see if it fails.
     */
    @Test
    public void handleAllZeros() {
        fail("Implement");
    }

    /**
     * Provides wide or tall matrices and see if it throws an exception
     */
    @Test
    public void checkShapeOfA() {
        fail("Implement");
    }

    @Test
    public void modifiesA() {
        fail("Implement");
    }

    @Test
    public void modifiesB() {
        fail("Implement");
    }

    @Test
    public void quality() {
        fail("Implement"); // todo see row major for ideas here
    }
}
