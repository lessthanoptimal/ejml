/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Generic tests for linear solvers
 *
 * @author Peter Abeles
 */
// TODO Add a test that makes sure identify permutation produces identical results to no permutation
public abstract class GenericLinearSolverSparseTests_DSCC {

    protected Random rand = new Random(234);

    protected FillReducing permutationTests[] = new FillReducing[]
            {FillReducing.NONE, FillReducing.IDENTITY};

    // used to adjust tolerance threshold
    protected double equalityTolerance = UtilEjml.TEST_F64;

    protected boolean canHandleTall = true;
    protected boolean canHandleWide = true;
    protected boolean canDecomposeZeros = true;
    protected boolean canLockStructure = true;

    public abstract LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> createSolver(FillReducing permutation);

    /**
     * Create a random matrix. The exact shape is determined by the implementation but all allowable shapes for this
     * size should be randomized
     */
    public abstract DMatrixSparseCSC createA(int size);

    public DMatrixRMaj create( int rows , int cols ) {
        return RandomMatrices_DDRM.rectangle(rows,cols,rand);
    }

    public DMatrixSparseCSC createSparse( int rows , int cols ) {
        return RandomMatrices_DSCC.rectangle(rows,cols,rows*cols,rand);
    }

    @Test
    public void randomSolveable() {

        for( FillReducing perm : permutationTests ) {
//            System.out.println("perm = "+perm);

            LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(perm);

            for (int N : new int[]{1, 2, 5, 10, 20}) {
                for (int mc = 0; mc < 30; mc++) {
//                    System.out.println("-=-=-=-=-=-=-=-=      "+N+" mc "+mc);
                    DMatrixSparseCSC A = createA(N);
                    DMatrixSparseCSC A_cpy = A.copy();
                    DMatrixRMaj X = create(A.numCols, 3);
                    DMatrixRMaj foundX = create(A.numCols, 3);
                    DMatrixRMaj B = new DMatrixRMaj(A.numRows, 3);

                    // compute the solution
                    CommonOps_DSCC.mult(A, X, B);
                    DMatrixRMaj B_cpy = B.copy();

                    assertTrue(solver.setA(A));
                    solver.solve(B, foundX);

                    EjmlUnitTests.assertRelativeEquals(X, foundX, equalityTolerance);

                    if( !solver.modifiesA() ) {
                        EjmlUnitTests.assertEquals(A, A_cpy, equalityTolerance);
                    }
                    if( !solver.modifiesB() ) {
                        EjmlUnitTests.assertEquals(B, B_cpy, equalityTolerance);
                    }
                }
            }
        }
    }

    @Test
    public void randomSolveable_Sparse() {

        for( FillReducing perm : permutationTests ) {
            System.out.println("perm = "+perm);

            LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(perm);

            for (int N : new int[]{1, 2, 5, 10, 20}) {
                for (int mc = 0; mc < 30; mc++) {
                    System.out.println("-=-=-=-=-=-=-=-=      "+N+" mc "+mc);
                    DMatrixSparseCSC A = createA(N);
                    DMatrixSparseCSC A_cpy = A.copy();
                    DMatrixSparseCSC X = createSparse(A.numCols, 3);
                    DMatrixSparseCSC foundX = createSparse(A.numCols, 3);
                    DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows, 3,1);

                    // compute the solution
                    CommonOps_DSCC.mult(A, X, B);
                    DMatrixSparseCSC B_cpy = B.copy();

                    assertTrue(solver.setA(A));
                    solver.solveSparse(B, foundX);

                    EjmlUnitTests.assertRelativeEquals(X, foundX, equalityTolerance);

                    // should never be modified
                    EjmlUnitTests.assertEquals(A, A_cpy, equalityTolerance);
                    EjmlUnitTests.assertEquals(B, B_cpy, equalityTolerance);
                }
            }
        }
    }

    /**
     * Give it a matrix that's all zeros and see if it fails.
     */
    @Test
    public void handleAllZeros() {
        DMatrixSparseCSC A = new DMatrixSparseCSC(10,10,0);

        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(FillReducing.NONE);

        assertTrue(canDecomposeZeros == solver.setA(A));
    }

    /**
     * Provides wide or tall matrices and see if it throws an exception
     */
    @Test
    public void checkFailByShape_Tall() {
        if( canHandleTall ) {
            return;
        }
        try {
            LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(FillReducing.NONE);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(10,5,50,rand);
            solver.setA(A);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException ignore ){}
    }

    @Test
    public void checkFailByShape_Wide() {
        if( canHandleWide ) {
            return;
        }
        try {
            LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(FillReducing.NONE);
            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(5,10,50,rand);
            solver.setA(A);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException ignore ){}
    }

    @Test
    public void quality() {
        DMatrixSparseCSC A_good = CommonOps_DSCC.diag(4,3,2,1);
        DMatrixSparseCSC A_bad = CommonOps_DSCC.diag(4, 3, 2, 0.1);

        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = createSolver(FillReducing.NONE);

        assertTrue(solver.setA(A_good));
        double q_good;
        try {
            q_good = (double)solver.quality();
        } catch( IllegalArgumentException e ) {
            // quality is not supported
            return;
        }

        assertTrue(solver.setA(A_bad));
        double q_bad = (double)solver.quality();

        assertTrue(q_bad < q_good);
    }

    @Test
    public void ifCanNotLockThrowException() {
        LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> d = createSolver(FillReducing.NONE);
        if( canLockStructure ) {
            d.setStructureLocked(true);
        } else {
            try {
                d.setStructureLocked(true);
                fail("RuntimeException should have been thrown");
            } catch (RuntimeException ignore) {
            }
        }
    }

    @Test
    public void lockingDoesNotChangeSolution() {
        if( !canLockStructure )
            return;

        LinearSolverSparse<DMatrixSparseCSC,DMatrixRMaj> d = createSolver(FillReducing.NONE);

        DMatrixSparseCSC A = createA(10);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(A.numRows,5,rand);
        DMatrixRMaj X0 = new DMatrixRMaj(A.numCols,5);
        DMatrixRMaj X1 = new DMatrixRMaj(A.numCols,5);

        assertTrue(d.setA((DMatrixSparseCSC)A.copy()));
        d.solve(B.copy(),X0);

        assertFalse(d.isStructureLocked());
        d.setStructureLocked(true);
        assertTrue(d.isStructureLocked());

        assertTrue(d.setA(A));
        d.solve(B,X1);

        EjmlUnitTests.assertEquals(X0,X1,UtilEjml.TEST_F64);
    }
}
