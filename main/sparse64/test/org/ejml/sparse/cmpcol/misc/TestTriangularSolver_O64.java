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

package org.ejml.sparse.cmpcol.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.sparse.cmpcol.CommonOps_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_O64 {

    Random rand = new Random(234);

    @Test
    public void solveL_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            SMatrixCmpC_F64 L = RandomMatrices_O64.createLowerTriangular(5, 0, nz_size, -1, 1, rand);
            DMatrixRow_F64 b = RandomMatrices_R64.createRandom(5, 1, rand);
            DMatrixRow_F64 x = b.copy();

            TriangularSolver_O64.solveL(L, x.data);

            DMatrixRow_F64 found = x.createLike();
            CommonOps_O64.mult(L, x, found);

            assertTrue(MatrixFeatures_R64.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveU_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            SMatrixCmpC_F64 L = RandomMatrices_O64.createLowerTriangular(5, 0, nz_size, -1, 1, rand);
            SMatrixCmpC_F64 U = new SMatrixCmpC_F64(5, 5, L.nz_length);
            CommonOps_O64.transpose(L, U, null);

            DMatrixRow_F64 b = RandomMatrices_R64.createRandom(5, 1, rand);
            DMatrixRow_F64 x = b.copy();

            TriangularSolver_O64.solveU(U, x.data);

            DMatrixRow_F64 found = x.createLike();
            CommonOps_O64.mult(U, x, found);

            assertTrue(MatrixFeatures_R64.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    /**
     * Test a simple case where A is diagonal
     */
    @Test
    public void searchNzRowsInB_diag() {
        SMatrixCmpC_F64 A = CommonOps_O64.diag(1,2,3);
        SMatrixCmpC_F64 B = RandomMatrices_O64.uniform(3,1,3,-1,1,rand);

        int xi[] = new int[A.numCols];

        // A is diagonal and B is filled in
        int top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,null);

        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal and B is empty
        B = new SMatrixCmpC_F64(3,1,3);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,null);
        assertEquals(3,top);

        // A is diagonal and B has element 1 not zero
        B.set(1,0,2.0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,null);
        assertEquals(2,top);
        assertEquals(1,xi[2]);

        // A is diagonal with one missing and B is full
        A.remove(1,1);
        B = RandomMatrices_O64.uniform(3,1,3,-1,1,rand);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,null);
        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(i,xi[i]);
        }

        // A is diagonal with one missing and B is missing the same element
        B.remove(1,0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,null);
        assertEquals(1,top);
        assertEquals(2,xi[1]);
        assertEquals(0,xi[2]);

    }

    /**
     * A is triangular
     */
    @Test
    public void searchNzRowsInB_triangle() {

    }


    /**
     * hand constructed system and verify that the results are as expected
     */
    @Test
    public void searchNzRowsInB_case0() {

    }

}