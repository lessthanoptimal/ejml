/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.misc;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_DSCC {

    Random rand = new Random(234);

    @Test
    public void solveL_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 1, rand);
            DMatrixRMaj x = b.copy();

            TriangularSolver_DSCC.solveL(L, x.data);

            DMatrixRMaj found = x.createLike();
            CommonOps_DSCC.mult(L, x, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveTranL_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 1, rand);
            DMatrixRMaj x = b.copy();

            TriangularSolver_DSCC.solveTranL(L, x.data);

            DMatrixRMaj found = x.createLike();
            DMatrixSparseCSC L_tran = new DMatrixSparseCSC(5,5,0);
            CommonOps_DSCC.transpose(L,L_tran,null);
            CommonOps_DSCC.mult(L_tran, x, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solveU_denseX() {
        for (int nz_size : new int[]{5, 8, 10, 20}) {
            DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(5, 0, nz_size, -1, 1, rand);
            DMatrixSparseCSC U = new DMatrixSparseCSC(5, 5, L.nz_length);
            CommonOps_DSCC.transpose(L, U, null);

            DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 1, rand);
            DMatrixRMaj x = b.copy();

            TriangularSolver_DSCC.solveU(U, x.data);

            DMatrixRMaj found = x.createLike();
            CommonOps_DSCC.mult(U, x, found);

            assertTrue(MatrixFeatures_DDRM.isIdentical(found, b, UtilEjml.TEST_F64));
        }
    }

    @Test
    public void solve_sparseX_vector() {
        solve_sparseX_vector(true);
        solve_sparseX_vector(false);
    }

    public void solve_sparseX_vector( boolean lower ) {
        for (int trial = 0; trial < 10; trial++) {
            for( int m : new int[]{1,2,5,10,20,50}) {
                int[] w = new int[m*2];
                int maxSize = m*m;
                int nz_size_G = m;
                while( nz_size_G < maxSize ) {
                    DMatrixSparseCSC G = createTriangular(lower,m, nz_size_G);

                    int lengthX = rand.nextInt(m/2+1)+m/2;
                    DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(m, 1,lengthX, rand);
                    DMatrixRMaj x = new DMatrixRMaj(b.numRows,b.numCols);

                    int ret = TriangularSolver_DSCC.solveColB(G,lower, b,0, x.data,null, null, w);
                    assertTrue(m-lengthX >= ret);

                    DMatrixRMaj found = x.createLike();
                    CommonOps_DSCC.mult(G, x, found);

                    DMatrixRMaj expected = DConvertMatrixStruct.convert(b,(DMatrixRMaj)null);
                    assertTrue(MatrixFeatures_DDRM.isEquals(found, expected, UtilEjml.TEST_F64));
                    nz_size_G = (int)(nz_size_G*1.5);
                }
            }
        }
    }

    private DMatrixSparseCSC createTriangular( boolean lower , int rows , int nz_size ) {
        DMatrixSparseCSC T;
        if( lower)
            T = RandomMatrices_DSCC.triangleLower(rows, 0, nz_size, -1, 1, rand);
        else
            T = RandomMatrices_DSCC.triangleUpper(rows, 0, nz_size, -1, 1, rand);

        // make sure the diagonal elements are close to one so that the system is stable
        for (int row = 0; row < rows; row++) {
            T.set(row,row, 1.0 + (double) (rand.nextGaussian() * 0.001) );
        }
        return T;
    }

    @Test
    public void solve_sparseX_matrix_square() {
        // test square matrix
        solve_sparseX_matrix_square(true);
        solve_sparseX_matrix_square(false);
    }

    public void solve_sparseX_matrix_square( boolean lower ) {
        DGrowArray gx = new DGrowArray();
        IGrowArray gxi = new IGrowArray();
        IGrowArray gw = new IGrowArray();

        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
                int lengthX = rand.nextInt(3)+3;

                DMatrixSparseCSC G = createTriangular(lower,5, nz_size);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(5, 2,lengthX*2, rand);
                DMatrixSparseCSC x = new DMatrixSparseCSC(b.numRows,b.numCols,1);

                TriangularSolver_DSCC.solve(G,lower,b,x, null, gx, gxi, gw);
                assertTrue(CommonOps_DSCC.checkStructure(x));

                DMatrixSparseCSC found = x.createLike();
                CommonOps_DSCC.mult(G, x, found);

                // Don't use a sparse test since the solution might contain 0 values due to cancellations
                EjmlUnitTests.assertEquals(found,b);

                //===========================================================
                // now try it with pivots
                int[] p = UtilEjml.shuffled(G.numRows,rand);
                int[] pinv = CommonOps_DSCC.permutationInverse(p,p.length);

                DMatrixSparseCSC Gp = G.createLike();
                CommonOps_DSCC.permute(null,G,p,Gp);
                CommonOps_DSCC.mult(G,x,b);
                x = x.createLike();
                TriangularSolver_DSCC.solve(Gp,lower,b,x,pinv, gx, gxi, gw);
                DMatrixSparseCSC b_found = b.createLike();
                CommonOps_DSCC.mult(G,x,b_found);
                EjmlUnitTests.assertEquals(b_found,b);
            }
        }
    }

    @Test
    public void solve_sparseX_matrixTran_square() {
        solve_sparseX_matrixTran_square(true);
        solve_sparseX_matrixTran_square(false);
    }

    public void solve_sparseX_matrixTran_square( boolean lower ) {
        DGrowArray gx = new DGrowArray();
        IGrowArray gxi = new IGrowArray();
        IGrowArray gw = new IGrowArray();

        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {
//                System.out.println("NZ:"+nz_size+"   trial:"+trial);

                int N = 5, Bcol = 2;

                int B_nz_count = (int)(N*Bcol*(rand.nextDouble()*0.7+0.35)); // bias so it will fill up

                DMatrixSparseCSC G = createTriangular(lower,N, nz_size);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(N, Bcol,B_nz_count, rand);
                DMatrixSparseCSC x = new DMatrixSparseCSC(b.numRows,b.numCols,1);

                DMatrixSparseCSC GT = G.createLike();
                CommonOps_DSCC.transpose(G,GT,gw);

                TriangularSolver_DSCC.solveTran(G,lower,b,x, null, gx, gxi, gw);
                assertTrue(CommonOps_DSCC.checkStructure(x));

                DMatrixSparseCSC found = x.createLike();
                CommonOps_DSCC.mult(GT, x, found);

                // Don't use a sparse test since the solution might contain 0 values due to cancellations
                EjmlUnitTests.assertEquals(found,b);

                //===========================================================
                // now try it with pivots
                int[] p = UtilEjml.shuffled(G.numRows,rand);
                int[] pinv = CommonOps_DSCC.permutationInverse(p,p.length);

                DMatrixSparseCSC Gp = G.createLike();
                CommonOps_DSCC.permute(null,G,p,Gp);
                CommonOps_DSCC.mult(G,x,b);
                x = x.createLike();
                TriangularSolver_DSCC.solve(Gp,lower,b,x,pinv, gx, gxi, gw);
                DMatrixSparseCSC b_found = b.createLike();
                CommonOps_DSCC.mult(G,x,b_found);
                EjmlUnitTests.assertEquals(b_found,b);
            }
        }
    }

    @Test
    public void solve_sparseX_matrix_lower_tall() {
        for (int i = 0; i < 40; i++) {
            solve_sparseX_matrix_lower_tall(3,1, 2);
            solve_sparseX_matrix_lower_tall(1,3, 3);
            solve_sparseX_matrix_lower_tall(6,4,2);
            solve_sparseX_matrix_lower_tall(20,30,1);
        }
    }

    public void solve_sparseX_matrix_lower_tall(int triangle, int tall, int colB) {
        DMatrixSparseCSC L = RandomMatrices_DSCC.triangleLower(triangle, 0, triangle*2, -1, 1, rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(tall,triangle,3,rand);

        DMatrixSparseCSC G = new DMatrixSparseCSC(triangle+tall,triangle);
        CommonOps_DSCC.concatRows(L,B,G);

        DMatrixSparseCSC x_truth = RandomMatrices_DSCC.rectangle(triangle,colB,8,rand);
        B = RandomMatrices_DSCC.rectangle(triangle+tall,colB,8,rand);

        CommonOps_DSCC.mult(G,x_truth,B);

        DMatrixSparseCSC x = x_truth.createLike();

        TriangularSolver_DSCC.solve(G,true,B,x,null, null, null, null);

        assertTrue(CommonOps_DSCC.checkStructure(x));

        DMatrixSparseCSC B_found = B.createLike();
        CommonOps_DSCC.mult(G,x,B_found);

        EjmlUnitTests.assertEquals(B_found,B);
    }

    @Test
    public void solve_sparseX_matrix_upper_tall() {
        for (int i = 0; i < 40; i++) {
            solve_sparseX_matrix_upper_tall(3,1,2);
            solve_sparseX_matrix_upper_tall(1,3,3);
            solve_sparseX_matrix_upper_tall(6,1,2);
            solve_sparseX_matrix_upper_tall(6,4,2);
            solve_sparseX_matrix_upper_tall(20,30,1);
        }
    }

    public void solve_sparseX_matrix_upper_tall(int triangle, int tall, int colB) {
        DMatrixSparseCSC R = RandomMatrices_DSCC.triangleUpper(triangle, 0, triangle*2, -1, 1, rand);
        DMatrixSparseCSC B = new DMatrixSparseCSC(tall,triangle);

        DMatrixSparseCSC G = new DMatrixSparseCSC(triangle+tall,triangle);
        CommonOps_DSCC.concatRows(R,B,G);

        int max_X = triangle*colB;
        DMatrixSparseCSC x_truth = RandomMatrices_DSCC.rectangle(triangle,colB,Math.max(colB,max_X/2+1),rand);

        B = RandomMatrices_DSCC.rectangle(triangle+tall,colB,1,rand);

        CommonOps_DSCC.mult(G,x_truth,B);

        DMatrixSparseCSC x = x_truth.createLike();

        TriangularSolver_DSCC.solve(G,false,B,x,null, null, null, null);
        assertTrue(CommonOps_DSCC.checkStructure(x));

        DMatrixSparseCSC B_found = B.createLike();
        CommonOps_DSCC.mult(G,x,B_found);

        EjmlUnitTests.assertEquals(B_found,B);

        //======================================================================
        // now try it with pivots
        int[] p = UtilEjml.shuffled(R.numRows,rand);
        int[] pinv = CommonOps_DSCC.permutationInverse(p,p.length);

        DMatrixSparseCSC Gp = G.createLike();
        CommonOps_DSCC.permute(null,G,p,Gp);
        CommonOps_DSCC.mult(G,x_truth,B);
        TriangularSolver_DSCC.solve(Gp,false,B,x,pinv, null, null, null);
        B_found = B.createLike();
        CommonOps_DSCC.mult(G,x,B_found);
        EjmlUnitTests.assertEquals(B_found,B);
    }

    @Test
    public void solveColB_pivots_sparseX_vector() {
        solveColB_pivots_sparseX_vector(true);
        solveColB_pivots_sparseX_vector(false);
    }

    public void solveColB_pivots_sparseX_vector( boolean lower ) {
        int m = 5;
        int[] w = new int[m*2];

        for (int trial = 0; trial < 10; trial++) {
            for (int nz_size : new int[]{5, 8, 10, 20}) {

                int[] p = UtilEjml.shuffled(m,rand);
                int[] pinv = CommonOps_DSCC.permutationInverse(p,5);

                int lengthX = rand.nextInt(3)+3;

                DMatrixSparseCSC G = createTriangular(lower,m, nz_size);

                DMatrixSparseCSC Gp = new DMatrixSparseCSC(m,5,0);
                CommonOps_DSCC.permute(null,G,p,Gp);

                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(m, 1,lengthX, rand);
                DMatrixRMaj x = new DMatrixRMaj(b.numRows,b.numCols);

                int ret = TriangularSolver_DSCC.solveColB(Gp,lower, b,0, x.data,pinv, null, w);
                assertTrue(m-lengthX >= ret);

                DMatrixRMaj found = x.createLike();
                CommonOps_DSCC.mult(G, x, found);

                DMatrixRMaj expected = DConvertMatrixStruct.convert(b,(DMatrixRMaj)null);
                assertTrue(MatrixFeatures_DDRM.isEquals(found, expected, UtilEjml.TEST_F64));
            }
        }
    }


    /**
     * Test a simple case where A is diagonal
     */
    @Test
    public void searchNzRowsInB_diag() {
        DMatrixSparseCSC A = CommonOps_DSCC.diag(1,2,3);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(3,1,3,-1,1,rand);

        int[] xi = new int[A.numCols];
        int[] w = new int[B.numRows*2];

        // A is diagonal and B is filled in
        int top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);

        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal and B is empty
        B = new DMatrixSparseCSC(3,1,3);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(3,top);

        // A is diagonal and B has element 1 not zero
        B.set(1,0,2.0);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(2,top);
        assertEquals(1,xi[2]);

        // A is diagonal with one missing and B is full
        A.remove(1,1);
        B = RandomMatrices_DSCC.rectangle(3,1,3,-1,1,rand);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 3; i++) {
            assertEquals(2-i,xi[i]);
        }

        // A is diagonal with one missing and B is missing the same element
        B.remove(1,0);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(1,top);
        assertEquals(2,xi[1]);
        assertEquals(0,xi[2]);
    }

    /**
     * A is filled in triangular
     */
    @Test
    public void searchNzRowsInX_triangle() {
        DMatrixSparseCSC A = RandomMatrices_DSCC.triangleLower(4,0,16, -1,1,rand);
        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(4,1,4,-1,1,rand);

        int[] xi = new int[A.numCols];
        int[] w = new int[A.numCols*2];

        int top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }
        for (int i = 0; i < A.numCols; i++) {
            assertEquals(0,w[i]);
        }

        // Add a hole which should be filled in
        B.remove(1,0);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(0,top);
        for (int i = 0; i < 4; i++) {
            assertEquals(i,xi[i]);
        }
        for (int i = 0; i < A.numCols; i++) {
            assertEquals(0,w[i]);
        }

        // add a hole on top.  This should not be filled in nor the one below it
        B.remove(0,0);
        top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(2,top);
        for (int i = 0; i < 2; i++) {
            assertEquals(i+2,xi[i]);
        }
        for (int i = 0; i < A.numCols; i++) {
            assertEquals(0,w[i]);
        }
    }

    /**
     * hand constructed system and verify that the results are as expected
     */
    @Test
    public void searchNzRowsInX_case0() {
        DMatrixRMaj D = UtilEjml.parse_DDRM(
                     "1 0 0 0 0 " +
                        "1 1 0 0 0 "+
                        "0 1 1 0 0 " +
                        "1 0 0 1 0 " +
                        "0 1 0 0 1",5);

        DMatrixSparseCSC A = DConvertMatrixStruct.convert(D,(DMatrixSparseCSC)null, UtilEjml.EPS);

        DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(5,1,5,-1,1,rand);

        int[] xi = new int[A.numCols];
        int[] w = new int[B.numRows*2];

        int top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
        assertEquals(0,top);
        assertEquals(0,xi[0]); // hand traced through
        assertEquals(3,xi[1]);
        assertEquals(1,xi[2]);
        assertEquals(4,xi[3]);
        assertEquals(2,xi[4]);
    }

    /**
     * Only the upper portion of a tall matrix A determine the non-zero pattern in X
     */
    @Test
    public void searchNzRowsInX_Tall_Lower() {
        for (int trial = 0; trial < 20; trial++) {
            DMatrixSparseCSC A = RandomMatrices_DSCC.triangleLower(5,0,5,-1,1,rand);
            DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(5,2,2,-1,1,rand);

            int[] xi = new int[A.numCols];
            int[] w = new int[A.numCols*2];

            int top = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi,w);
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(0,w[j]);
            }

            DMatrixSparseCSC bottom = RandomMatrices_DSCC.rectangle(4,5,10,-1,1,rand);
            A = CommonOps_DSCC.concatRows(A,bottom,null);

            int[] xi2 = new int[A.numCols];
            int[] w2 = new int[A.numCols*2];

            int top2 = TriangularSolver_DSCC.searchNzRowsInX(A,B,0,null,xi2,w2);
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(0,w[j]);
            }

            assertEquals(top,top2);
            for (int j = 0; j < A.numCols; j++) {
                assertEquals(xi[j],xi2[j]);
            }
        }
    }

    /**
     * All elements in A are filled in.  ata = false
     */
    @Test
    public void eliminationTree_full_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 1 1 1 1 " +
                        "0 1 1 1 1 "+
                        "0 0 1 1 1 " +
                        "0 0 0 1 1 " +
                        "0 0 0 0 1",5);

        int[] parent = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        for (int i = 0; i < A.numCols-1; i++) {
            assertEquals(i+1,parent[i]);
        }
        assertEquals(-1,parent[A.numCols-1]);
    }

    /**
     * All elements in A are empty except the diagonal ones. ata = false
     */
    @Test
    public void eliminationTree_diagonal_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 0 0 0 " +
                        "0 1 0 0 0 "+
                        "0 0 1 0 0 " +
                        "0 0 0 1 0 " +
                        "0 0 0 0 1",5);

        int[] parent = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(-1,parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     */
    @Test
    public void eliminationTree_case0_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 0 0 0 " +
                        "0 1 1 1 0 "+
                        "0 0 1 0 1 " +
                        "0 0 0 1 0 " +
                        "0 0 0 0 1 ",5);

        int[] parent = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int[] expected = new int[]{-1,2,3,4,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     */
    @Test
    public void eliminationTree_case1_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 0 " +
                        "0 1 0 1 0 0 0 " +
                        "0 0 1 0 1 0 0 " +
                        "0 0 0 1 0 0 0 " +
                        "0 0 0 0 1 0 1 " +
                        "0 0 0 0 0 1 1 " +
                        "0 0 0 0 0 0 1 ",7);

        int[] parent = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int[] expected = new int[]{2,3,3,4,5,6,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Hand constructed sparse test case with hand constructed solution. ata = false
     * This is designed to make sure I didn't cheat in the previous test
     */
    @Test
    public void eliminationTree_case2_square() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 " +
                        "0 1 0 1 0 0 " +
                        "0 0 1 0 1 0 " +
                        "0 0 0 1 0 0 " +
                        "0 0 0 0 1 0 " +
                        "0 0 0 0 0 1 " ,6);

        int[] parent = new int[A.numCols];

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

        int[] expected = new int[]{2,3,3,4,5,-1};

        for (int i = 0; i < A.numCols; i++) {
            assertEquals(expected[i],parent[i]);
        }
    }

    /**
     * Test the elimination tree from its definition.  Test it by seeing if for each off diagonal non-zero element
     * A[i,j] there is a path from i to j. i < j.  Hmm that description is actually for the transpose of A
     */
    @Test
    public void eliminationTree_random_square() {
        for (int i = 0; i < 200; i++) {
            // select the matrix size
            int N = rand.nextInt(16)+1;
            // select number of non-zero elements in the matrix. diagonal elements are always filled
            int nz = (int)(((N-1)*(N-1)/2)*(rand.nextDouble()*0.8+0.2))+N;
            DMatrixSparseCSC A = RandomMatrices_DSCC.triangleUpper(N,0,nz,-1,1,rand);

            int[] parent = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

            for (int col = 0; col < A.numCols; col++) {
                int idx0 = A.col_idx[col];
                int idx1 = A.col_idx[col+1];

                // skip over diagonal elements
                for (int j = idx0; j < idx1-1; j++) {
                    int row = A.nz_rows[j];

                    checkPathEliminationTree(row,col,parent);
                }
            }
        }
    }

    private void checkPathEliminationTree(int start , int end , int[] parent) {
        int i = start;
        while( i < end ) {
            i = parent[i];
        }
        assertEquals(end, i);
    }

    /**
     * Test with ATA = true using square matrices. The test is done by explicitly computing
     * ATA and seeing if it yields the same results
     */
    @Test
    public void eliminationTree_ata_square() {
        for (int mc = 0; mc < 200; mc++) {
            int N = rand.nextInt(16)+1;
//            System.out.println("mc = "+mc+"   N = "+N);

            DMatrixSparseCSC A = RandomMatrices_DSCC.triangle(true,N,0.1,0.5,rand);
            DMatrixSparseCSC At = CommonOps_DSCC.transpose(A,null,null);
            DMatrixSparseCSC ATA = CommonOps_DSCC.mult(At,A,null,null,null);

            int[] expected = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(ATA,false,expected,null);
            int[] found = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(A,true,found,null);

            for (int i = 0; i < A.numCols; i++) {
                assertEquals(expected[i],found[i]);
            }
        }
    }

    /**
     * Test case for tall input arrays. ata = true
     */
    @Test
    public void eliminationTree_ata_tall() {
        for (int mc = 0; mc < 200; mc++) {
            int N = rand.nextInt(16)+1;

            DMatrixSparseCSC A = RandomMatrices_DSCC.triangle(true,N,0.1,0.5,rand);
            DMatrixSparseCSC bottom = RandomMatrices_DSCC.rectangle(3,N,8,rand);
            DMatrixSparseCSC tall = CommonOps_DSCC.concatRows(A,bottom,null);

            DMatrixSparseCSC ATA = new DMatrixSparseCSC(N,N,0);
            DMatrixSparseCSC tall_t = CommonOps_DSCC.transpose(tall,null,null);
            CommonOps_DSCC.mult(tall_t,tall,ATA,null,null);

            int[] expected = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(ATA,false,expected,null);
            int[] found = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(tall,true,found,null);

            for (int i = 0; i < A.numCols; i++) {
                assertEquals(expected[i],found[i]);
            }
        }
    }

    /**
     * Test an example from the book
     */
    @Test
    public void postorder_case0() {
        int[] parent =   new int[]{5,2,7,5,7,6,8,9,9,10,-1};

        int N = 11;

        int[] found = new int[N];

        TriangularSolver_DSCC.postorder(parent,N,found,null);

        assertPostorder(parent,found,N);
    }

    /**
     * Uses the definition to see if a list is post-ordered
     */
    private void assertPostorder(int[] parent, int[] order, int N ) {

        int[] mod = new int[N];
        int[] sanity = new int[N];

        // reverse[ original index ] = postorder index
        int[] reverse = new int[N];

        // create a reverse lookup table
        for (int i = 0; i < N; i++) {
            sanity[order[i]]++;
            reverse[order[i]] = i;
        }

        // its a permutation so all elements should be touched once
        for (int i = 0; i < N; i++) {
            assertEquals(1,sanity[i]);
        }

        // apply post ordering to the graph
        for (int i = 0; i < N; i++) {
            if( parent[i] == -1 )
                mod[reverse[i]] = -1;
            else
                mod[reverse[i]] = reverse[parent[i]];
        }

        for (int i = 0; i < N; i++) {
            int n = i;
            while( mod[n] != -1 ) {
                if( mod[n] <= i )
                    fail("found a parent with a lower index. mod["+n+"] = "+mod[n]);
                n = mod[n];
            }
        }


    }

    /**
     * Everything is an island
     */
    @Test
    public void postorder_case1() {
        int[] parent =   new int[]{-1,-1,-1,-1,-1};
        int N = 5;

        int[] found = new int[N];

        TriangularSolver_DSCC.postorder(parent,N,found,null);

        assertPostorder(parent,found,N);
    }

    /**
     * Multiple root nodes
     */
    @Test
    public void postorder_case2() {
        int[] parent =   new int[]{5,2,7,5,7,6,8,-1,-1};

        int N = 9;

        int[] found = new int[N];

        TriangularSolver_DSCC.postorder(parent,N,found,null);

        assertPostorder(parent,found,N);
    }

    /**
     * Hand constructed test case
     */
    @Test
    public void searchNzRowsElim_case0() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                     "1 0 1 1 0 1 0 " +
                        "0 1 0 1 0 0 0 " +
                        "0 0 1 0 1 0 0 " +
                        "0 0 0 1 0 0 0 " +
                        "0 0 0 0 1 0 1 " +
                        "0 0 0 0 0 1 1 " +
                        "0 0 0 0 0 0 1 ",7);

        int[] parent = new int[]{2,3,3,4,5,6,-1};

        int top;
        int[] s = new int[7];
        int[] w = new int[7];

        int[] expected;

        // check each row one at a time
        top = TriangularSolver_DSCC.searchNzRowsElim(A,0,parent,s,w);
        assertEquals(top,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,1,parent,s,w);
        assertEquals(top,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,2,parent,s,w);
        assertEquals(top,A.numCols-1);
        expected = new int[]{0,0,0,0,0,0,0};
        assertSetEquals(expected,s,A.numCols-1,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,3,parent,s,w);
        assertEquals(top,A.numCols-3);
        expected = new int[]{0,0,0,0,0,1,2};
        assertSetEquals(expected,s,A.numCols-3,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,4,parent,s,w);
        assertEquals(top,A.numCols-2);
        expected = new int[]{0,0,0,0,0,2,3};
        assertSetEquals(expected,s,A.numCols-2,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,5,parent,s,w);
        assertEquals(top,A.numCols-4);
        expected = new int[]{0,0,0,0,2,3,4};
        assertSetEquals(expected,s,A.numCols-4,A.numCols);

        top = TriangularSolver_DSCC.searchNzRowsElim(A,6,parent,s,w);
        assertEquals(top,A.numCols-2);
        expected = new int[]{0,0,0,0,0,4,5};
        assertSetEquals(expected,s,A.numCols-2,A.numCols);
    }

    /**
     * Makes sure the same elements are contained in the two list but order doesn't matter
     */
    private static void assertSetEquals(int[] expected, int[] found, int start , int end ) {
        boolean[] matched = new boolean[end];
        for (int i = start; i < end; i++) {
            if( matched[i] )
                fail("matched twice");
            matched[found[i]] = true;
        }

        for (int i = start; i < end; i++) {
            assertTrue(matched[expected[i]]);
        }
    }

    @Test
    public void qualityTriangular() {
        DMatrixSparseCSC T = RandomMatrices_DSCC.triangleUpper(10,0,20,-1,1,rand);

        double found0 = TriangularSolver_DSCC.qualityTriangular(T);

        // see if it's scale invariant
        CommonOps_DSCC.scale(2.0,T,T);
        double found1 = TriangularSolver_DSCC.qualityTriangular(T);

        assertEquals(found0,found1,UtilEjml.TEST_F64);

        // now reduce the matrice's quality
        T.set(3,3,T.get(3,3)*UtilEjml.TEST_F64);
        double found2 = TriangularSolver_DSCC.qualityTriangular(T);

        assertTrue(found2 < found0*UtilEjml.TEST_F64_SQ);
    }

}