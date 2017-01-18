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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
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
            SMatrixCmpC_F64 L = RandomMatrices_O64.triangleLower(5, 0, nz_size, -1, 1, rand);
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
            SMatrixCmpC_F64 L = RandomMatrices_O64.triangleLower(5, 0, nz_size, -1, 1, rand);
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

    @Test
    public void solve_sparseX_vector() {
        solve_sparseX_vector(true);
        solve_sparseX_vector(false);
    }

    public void solve_sparseX_vector( boolean lower ) {
        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
                int lengthX = rand.nextInt(3)+3;

                SMatrixCmpC_F64 G;
                if( lower)
                    G = RandomMatrices_O64.triangleLower(5, 0, nz_size, -1, 1, rand);
                else
                    G = RandomMatrices_O64.triangleUpper(5, 0, nz_size, -1, 1, rand);
                SMatrixCmpC_F64 b = RandomMatrices_O64.rectangle(5, 1,lengthX, rand);
                DMatrixRow_F64 x = new DMatrixRow_F64(b.numRows,b.numCols);

                int ret = TriangularSolver_O64.solve(G,lower, b,0, x.data, null, null);
                assertTrue(5-lengthX >= ret);

                DMatrixRow_F64 found = x.createLike();
                CommonOps_O64.mult(G, x, found);

                DMatrixRow_F64 expected = ConvertSparseMatrix_F64.convert(b,(DMatrixRow_F64)null);
                assertTrue(MatrixFeatures_R64.isEquals(found, expected, UtilEjml.TEST_F64));
            }
        }
    }

    @Test
    public void solve_sparseX_matrix() {
        solve_sparseX_matrix(true);
        solve_sparseX_matrix(false);
    }

    public void solve_sparseX_matrix( boolean lower ) {
        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
                nz_size = 8;
                int lengthX = rand.nextInt(3)+3;

                SMatrixCmpC_F64 G;
                if( lower)
                    G = RandomMatrices_O64.triangleLower(5, 0, nz_size, -1, 1, rand);
                else
                    G = RandomMatrices_O64.triangleUpper(5, 0, nz_size, -1, 1, rand);
                SMatrixCmpC_F64 b = RandomMatrices_O64.rectangle(5, 2,lengthX*2, rand);
                SMatrixCmpC_F64 x = new SMatrixCmpC_F64(b.numRows,b.numCols,1);

                TriangularSolver_O64.solve(G,lower,b,x, null, null, null);

                SMatrixCmpC_F64 found = x.createLike();
                CommonOps_O64.mult(G, x, found);

                // Don't use a sparse test since the solution might contain 0 values due to cancellations
                EjmlUnitTests.assertEquals(found,b);
            }
        }
    }


    /**
     * Test a simple case where A is diagonal
     */
    @Test
    public void searchNzRowsInB_diag() {
        SMatrixCmpC_F64 A = CommonOps_O64.diag(1,2,3);
        SMatrixCmpC_F64 B = RandomMatrices_O64.rectangle(3,1,3,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        // A is diagonal and B is filled in
        int top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);

        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal and B is empty
        B = new SMatrixCmpC_F64(3,1,3);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(3,top);

        // A is diagonal and B has element 1 not zero
        B.set(1,0,2.0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(2,top);
        assertEquals(1,xi[2]);

        // A is diagonal with one missing and B is full
        A.remove(1,1);
        B = RandomMatrices_O64.rectangle(3,1,3,-1,1,rand);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal with one missing and B is missing the same element
        B.remove(1,0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(1,top);
        assertEquals(2,xi[1]);
        assertEquals(0,xi[2]);

    }

    /**
     * A is filled in triangular
     */
    @Test
    public void searchNzRowsInB_triangle() {
        SMatrixCmpC_F64 A = RandomMatrices_O64.triangleLower(4,0,16, -1,1,rand);
        SMatrixCmpC_F64 B = RandomMatrices_O64.rectangle(4,1,4,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        int top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }

        // Add a hole which should be filled in
        B.remove(1,0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }

        // add a hole on top.  This should not be filled in nor the one below it
        B.remove(0,0);
        top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(2,top);
        for (int i = 0; i < 2; i++) {
            assertEquals(i+2,xi[i]);
        }
    }


    /**
     * hand constructed system and verify that the results are as expected
     */
    @Test
    public void searchNzRowsInB_case0() {
        DMatrixRow_F64 D = UtilEjml.parse_R64(
                "1 0 0 0 0 " +
                        "1 1 0 0 0 "+
                        "0 1 1 0 0 " +
                        "1 0 0 1 0 " +
                        "0 1 0 0 1",5);

        SMatrixCmpC_F64 A = ConvertSparseMatrix_F64.convert(D,(SMatrixCmpC_F64)null);

        SMatrixCmpC_F64 B = RandomMatrices_O64.rectangle(5,1,4,-1,1,rand);

        int xi[] = new int[A.numCols];
        int w[] = new int[B.numRows*2];

        int top = TriangularSolver_O64.searchNzRowsInB(A,B,0,xi,w);
        assertEquals(0,top);
        assertEquals(0,xi[0]); // hand traced through
        assertEquals(3,xi[1]);
        assertEquals(1,xi[2]);
        assertEquals(4,xi[3]);
        assertEquals(2,xi[4]);
    }

}