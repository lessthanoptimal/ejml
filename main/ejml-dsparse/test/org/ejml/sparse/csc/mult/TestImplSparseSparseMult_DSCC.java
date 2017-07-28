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

package org.ejml.sparse.csc.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.ConvertDMatrixSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplSparseSparseMult_DSCC {

    Random rand = new Random(234);

    @Test
    public void mult_s_s() {
        for (int i = 0; i < 10; i++) {
            mult_s_s(24,30,20);
            mult_s_s(15,15,20);
            mult_s_s(15,15,5);
            mult_s_s(4,5,0);
        }
    }

    private void mult_s_s(int elementsA , int elementsB , int elementsC ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4,6,elementsA,-1,1,rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(6,5,elementsB,-1,1,rand);
        DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(4,5,elementsC,-1,1,rand);

        ImplSparseSparseMult_DSCC.mult(a,b,c, null, null);
        assertTrue(CommonOps_DSCC.checkSortedFlag(c));

        DMatrixRMaj dense_a = ConvertDMatrixSparse.convert(a,(DMatrixRMaj)null);
        DMatrixRMaj dense_b = ConvertDMatrixSparse.convert(b,(DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_DDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    /**
     * Makes sure the size of the output matrix is adjusted as needed
     */
    @Test
    public void mult_s_s_grow() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4,6,17,-1,1,rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(6,5,15,-1,1,rand);
        DMatrixSparseCSC c = new DMatrixSparseCSC(4,5,0);

        ImplSparseSparseMult_DSCC.mult(a,b,c,null,null);

        assertTrue(CommonOps_DSCC.checkSortedFlag(c));

        DMatrixRMaj dense_a = ConvertDMatrixSparse.convert(a,(DMatrixRMaj)null);
        DMatrixRMaj dense_b = ConvertDMatrixSparse.convert(b,(DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_DDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void multTransA_s_s() {
        for (int i = 0; i < 10; i++) {
            multTransA_s_s(24,30,20);
            multTransA_s_s(15,15,20);
            multTransA_s_s(15,15,5);
            multTransA_s_s(4,5,0);
        }
    }

    private void multTransA_s_s(int elementsA , int elementsB , int elementsC ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(6,4,elementsA,-1,1,rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(6,5,elementsB,-1,1,rand);
        DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(4,5,elementsC,-1,1,rand);

        ImplSparseSparseMult_DSCC.multTransA(a,b,c);
        assertTrue(CommonOps_DSCC.checkStructure(c));

        DMatrixRMaj dense_a = ConvertDMatrixSparse.convert(a,(DMatrixRMaj)null);
        DMatrixRMaj dense_b = ConvertDMatrixSparse.convert(b,(DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numCols, dense_b.numCols);

        CommonOps_DDRM.multTransA(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void multTransB_s_s() {
        for (int i = 0; i < 10; i++) {
            multTransB_s_s(24,30,20);
            multTransB_s_s(15,15,20);
            multTransB_s_s(15,15,5);
            multTransB_s_s(4,5,0);
        }
    }

    private void multTransB_s_s(int elementsA , int elementsB , int elementsC ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4,6,elementsA,-1,1,rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(5,6,elementsB,-1,1,rand);
        DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(4,5,elementsC,-1,1,rand);

        ImplSparseSparseMult_DSCC.multTransB(a,b,c);
        assertTrue(CommonOps_DSCC.checkStructure(c));

        DMatrixRMaj dense_a = ConvertDMatrixSparse.convert(a,(DMatrixRMaj)null);
        DMatrixRMaj dense_b = ConvertDMatrixSparse.convert(b,(DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numRows, dense_b.numRows);

        CommonOps_DDRM.multTransB(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void mult_s_d() {
        for (int i = 0; i < 10; i++) {
            mult_s_d(24);
            mult_s_d(15);
            mult_s_d(4);
        }
    }

    private void mult_s_d(int elementsA) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4,6,elementsA,-1,1,rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(6,5,-1,1,rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(4,5,-1,1,rand);

        ImplSparseSparseMult_DSCC.mult(a,b,c);

        DMatrixRMaj dense_a = ConvertDMatrixSparse.convert(a,(DMatrixRMaj)null);
        DMatrixRMaj expected_c = RandomMatrices_DDRM.rectangle(4,5,-1,1,rand);

        CommonOps_DDRM.mult(dense_a, b, expected_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(row+" "+col,expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void addRowsInAInToC() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                      "1 0 1 0 0 " +
                        "1 0 0 1 0 "+
                        "0 0 1 0 0 " +
                        "0 0 0 1 0 " +
                        "1 1 0 0 1",5);

        int w[] = new int[5];

        DMatrixSparseCSC B = new DMatrixSparseCSC(5,5,25);
        B.nz_length = 0;

        // nothing should be added here since w is full of 0 and colC = 0
        ImplSparseSparseMult_DSCC.addRowsInAInToC(A,0,B,0,w);
        assertEquals(0,B.col_idx[1]);

        // colA shoul dnow be added to colB
        ImplSparseSparseMult_DSCC.addRowsInAInToC(A,0,B,1,w);
        assertEquals(3,B.col_idx[1]);
        int expected[] = new int[]{0,1,4};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i],B.nz_rows[i]);
            assertEquals(1,w[expected[i]]);
        }
    }

    @Test
    public void dotInnerColumns() {
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();

        for (int mc = 0; mc < 50; mc++) {
            int A_nz = RandomMatrices_DSCC.nonzero(8,4,0.1,1.0,rand);
            int B_nz = RandomMatrices_DSCC.nonzero(8,6,0.1,1.0,rand);

            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(8,4,A_nz,rand);
            DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(8,6,B_nz,rand);

            int colA = rand.nextInt(4);
            int colB = rand.nextInt(6);

            double found = ImplSparseSparseMult_DSCC.dotInnerColumns(A,colA,B,colB,gw,gx);

            double expected = 0;
            for (int i = 0; i < 8; i++) {
                expected += A.get(i,colA)*B.get(i,colB);
            }

            assertEquals(expected,found,UtilEjml.TEST_F64);
        }
    }
}