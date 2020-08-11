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

package org.ejml.sparse.csc.mult;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.ops.ConvertFMatrixStruct;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplSparseSparseMult_FSCC {

    Random rand = new Random(234);

    @Test
    public void mult_s_s() {
        for (int i = 0; i < 50; i++) {
            mult_s_s(5,5,5);
            mult_s_s(10,5,5);
            mult_s_s(5,10,5);
            mult_s_s(5,5,10);
        }
    }

    private void mult_s_s( int rowsA , int colsA , int colsB ) {
        int nz_a = RandomMatrices_FSCC.nonzero(rowsA,colsA,0.05f,0.7f,rand);
        int nz_b = RandomMatrices_FSCC.nonzero(colsA,colsB,0.05f,0.7f,rand);
        int nz_c = RandomMatrices_FSCC.nonzero(rowsA,colsB,0.05f,0.7f,rand);

        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(rowsA,colsA,nz_a,-1,1,rand);
        FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(colsA,colsB,nz_b,-1,1,rand);
        FMatrixSparseCSC c = RandomMatrices_FSCC.rectangle(rowsA,colsB,nz_c,-1,1,rand);

        ImplSparseSparseMult_FSCC.mult(a,b,c, null, null);
        assertTrue(CommonOps_FSCC.checkStructure(c));

        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);
        FMatrixRMaj dense_b = ConvertFMatrixStruct.convert(b,(FMatrixRMaj)null);
        FMatrixRMaj dense_c = new FMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_FDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    /**
     * Makes sure the size of the output matrix is adjusted as needed
     */
    @Test
    public void mult_s_s_grow() {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(4,6,17,-1,1,rand);
        FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(6,5,15,-1,1,rand);
        FMatrixSparseCSC c = new FMatrixSparseCSC(4,5,0);

        ImplSparseSparseMult_FSCC.mult(a,b,c,null,null);
        assertTrue(CommonOps_FSCC.checkStructure(c));

        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);
        FMatrixRMaj dense_b = ConvertFMatrixStruct.convert(b,(FMatrixRMaj)null);
        FMatrixRMaj dense_c = new FMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_FDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(dense_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    @Test
    public void multTransA_s_s() {
        for (int i = 0; i < 30; i++) {
            multTransA_s_s(5,6,4);
            multTransA_s_s(6,1,7);
            multTransA_s_s(1,8,1);
        }
    }

    private void multTransA_s_s(int rowsA , int rowsB , int colsB ) {
        int a_nz = RandomMatrices_FSCC.nonzero(rowsB,rowsA,0.05f,0.8f,rand);
        int b_nz = RandomMatrices_FSCC.nonzero(rowsB,colsB,0.05f,0.8f,rand);

        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(rowsB,rowsA,a_nz,-1,1,rand);
        FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(rowsB,colsB,b_nz,-1,1,rand);
        FMatrixSparseCSC c = new FMatrixSparseCSC(rowsA,colsB,0);

        ImplSparseSparseMult_FSCC.multTransA(a,b,c,null,null);
        assertTrue(CommonOps_FSCC.checkStructure(c));

        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);
        FMatrixRMaj dense_b = ConvertFMatrixStruct.convert(b,(FMatrixRMaj)null);
        FMatrixRMaj dense_c = new FMatrixRMaj(rowsA, colsB);

        CommonOps_FDRM.multTransA(dense_a, dense_b, dense_c);

        EjmlUnitTests.assertEquals(dense_c,c,UtilEjml.TEST_F32);
    }

    @Test
    public void multTransB_s_s() {
        for (int i = 0; i < 30; i++) {
            multTransB_s_s(5,6,4);
            multTransB_s_s(6,1,7);
            multTransB_s_s(1,8,1);
        }
    }

    private void multTransB_s_s(int rowsA , int rowsB , int colsB) {
        int a_nz = RandomMatrices_FSCC.nonzero(rowsA, rowsB, 0.05f, 0.8f, rand);
        int b_nz = RandomMatrices_FSCC.nonzero(colsB, rowsB, 0.05f, 0.8f, rand);

        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(rowsA, rowsB, a_nz, -1, 1, rand);
        FMatrixSparseCSC b = RandomMatrices_FSCC.rectangle(colsB, rowsB, b_nz, -1, 1, rand);
        FMatrixSparseCSC c = new FMatrixSparseCSC(rowsA, colsB, 0);

        ImplSparseSparseMult_FSCC.multTransB(a, b, c, null, null);
        assertTrue(CommonOps_FSCC.checkStructure(c));

        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a, (FMatrixRMaj) null);
        FMatrixRMaj dense_b = ConvertFMatrixStruct.convert(b, (FMatrixRMaj) null);
        FMatrixRMaj dense_c = new FMatrixRMaj(rowsA, colsB);

        CommonOps_FDRM.multTransB(dense_a, dense_b, dense_c);

        EjmlUnitTests.assertEquals(dense_c,c,UtilEjml.TEST_F32);
    }

    @Test
    public void mult_s_d() {
        for (int i = 0; i < 10; i++) {
            mult_s_d(24,false);
            mult_s_d(15,false);
            mult_s_d(4,false);
            mult_s_d(24,true);
            mult_s_d(15,true);
            mult_s_d(4,true);
        }
    }

    private void mult_s_d(int elementsA, boolean add ) {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(4,6,elementsA,-1,1,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(6,5,-1,1,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(4,5,-1,1,rand);
        FMatrixRMaj expected_c = c.copy();
        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);

        if( add ) {
            ImplSparseSparseMult_FSCC.multAdd(a, b, c);
            CommonOps_FDRM.multAdd(dense_a, b, expected_c);
        } else {
            ImplSparseSparseMult_FSCC.mult(a, b, c);
            CommonOps_FDRM.mult(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    @Test
    public void multTransA_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransA_s_d(24,false);
            multTransA_s_d(15,false);
            multTransA_s_d(4,false);

            multTransA_s_d(24,true);
            multTransA_s_d(15,true);
            multTransA_s_d(4,true);
        }
    }

    private void multTransA_s_d(int elementsA, boolean add) {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(6,4,elementsA,-1,1,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(6,5,-1,1,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(4,5,-1,1,rand);
        FMatrixRMaj expected_c = c.copy();
        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);

        if( add ) {
            ImplSparseSparseMult_FSCC.multAddTransA(a, b, c);
            CommonOps_FDRM.multAddTransA(dense_a, b, expected_c);
        } else {
            ImplSparseSparseMult_FSCC.multTransA(a, b, c);
            CommonOps_FDRM.multTransA(dense_a, b, expected_c);
        }
        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    @Test
    public void multTransB_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransB_s_d(24,false);
            multTransB_s_d(15,false);
            multTransB_s_d(4,false);

            multTransB_s_d(24,true);
            multTransB_s_d(15,true);
            multTransB_s_d(4,true);
        }
    }

    private void multTransB_s_d(int elementsA , boolean add ) {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(4,6,elementsA,-1,1,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,6,-1,1,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(4,5,-1,1,rand);
        FMatrixRMaj expected_c = c.copy();
        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);

        if( add ) {
            ImplSparseSparseMult_FSCC.multAddTransB(a, b, c);
            CommonOps_FDRM.multAddTransB(dense_a, b, expected_c);
        } else {
            ImplSparseSparseMult_FSCC.multTransB(a, b, c);
            CommonOps_FDRM.multTransB(dense_a, b, expected_c);
        }
        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    @Test
    public void multTransAB_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransAB_s_d(24,false);
            multTransAB_s_d(15,false);
            multTransAB_s_d(4,false);

            multTransAB_s_d(24,true);
            multTransAB_s_d(15,true);
            multTransAB_s_d(4,true);
        }
    }

    private void multTransAB_s_d(int elementsA, boolean add ) {
        FMatrixSparseCSC a = RandomMatrices_FSCC.rectangle(6,4,elementsA,-1,1,rand);
        FMatrixRMaj b = RandomMatrices_FDRM.rectangle(5,6,-1,1,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(4,5,-1,1,rand);
        FMatrixRMaj expected_c = c.copy();
        FMatrixRMaj dense_a = ConvertFMatrixStruct.convert(a,(FMatrixRMaj)null);

        if( add ) {
            ImplSparseSparseMult_FSCC.multAddTransAB(a, b, c);
            CommonOps_FDRM.multAddTransAB(dense_a, b, expected_c);
        } else {
            ImplSparseSparseMult_FSCC.multTransAB(a, b, c);
            CommonOps_FDRM.multTransAB(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row,col), c.get(row,col), UtilEjml.TEST_F32,row+" "+col);
            }
        }
    }

    @Test
    public void addRowsInAInToC() {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                      "1 0 1 0 0 " +
                        "1 0 0 1 0 "+
                        "0 0 1 0 0 " +
                        "0 0 0 1 0 " +
                        "1 1 0 0 1",5);

        int w[] = new int[5];

        FMatrixSparseCSC B = new FMatrixSparseCSC(5,5,25);
        B.nz_length = 0;

        // nothing should be added here since w is full of 0 and colC = 0
        ImplSparseSparseMult_FSCC.addRowsInAInToC(A,0,B,0,w);
        assertEquals(0,B.col_idx[1]);

        // colA shoul dnow be added to colB
        ImplSparseSparseMult_FSCC.addRowsInAInToC(A,0,B,1,w);
        B.numCols = 2;// needed to be set correctly for structure unit test
        assertTrue(CommonOps_FSCC.checkStructure(B));
        assertEquals(3,B.col_idx[2]);
        int expected[] = new int[]{0,1,4};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i],B.nz_rows[i]);
            assertEquals(1,w[expected[i]]);
        }
    }

    @Test
    public void dotInnerColumns() {
        IGrowArray gw = new IGrowArray();
        FGrowArray gx = new FGrowArray();

        for (int mc = 0; mc < 50; mc++) {
            int A_nz = RandomMatrices_FSCC.nonzero(8,4,0.1f,1.0f,rand);
            int B_nz = RandomMatrices_FSCC.nonzero(8,6,0.1f,1.0f,rand);

            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(8,4,A_nz,rand);
            FMatrixSparseCSC B = RandomMatrices_FSCC.rectangle(8,6,B_nz,rand);

            int colA = rand.nextInt(4);
            int colB = rand.nextInt(6);

            float found = ImplSparseSparseMult_FSCC.dotInnerColumns(A,colA,B,colB,gw,gx);

            float expected = 0;
            for (int i = 0; i < 8; i++) {
                expected += A.get(i,colA)*B.get(i,colB);
            }

            assertEquals(expected,found,UtilEjml.TEST_F32);
        }
    }

    @Test
    public void innerProductLower() {
        IGrowArray gw = new IGrowArray();
        FGrowArray gx = new FGrowArray();

        for (int mc = 0; mc < 50; mc++) {
            int numRows = rand.nextInt(10)+1;
            int numCols = rand.nextInt(10)+1;

            int A_nz = RandomMatrices_FSCC.nonzero(numRows,numCols,0.1f,0.9f,rand);
            FMatrixSparseCSC A = RandomMatrices_FSCC.rectangle(numRows,numCols,A_nz,rand);
            FMatrixSparseCSC B = new FMatrixSparseCSC(numCols,numCols);

            FMatrixRMaj A_dense = ConvertFMatrixStruct.convert(A,(FMatrixRMaj)null);
            FMatrixRMaj B_dense = new FMatrixRMaj(numCols,numCols);

            ImplSparseSparseMult_FSCC.innerProductLower(A,B,gw,gx);
            assertTrue(CommonOps_FSCC.checkStructure(B));
            CommonOps_FDRM.multTransA(A_dense,A_dense,B_dense);

//            B.print();
//            B_dense.print();

            for (int row = 0; row < B.numRows; row++) {
                for (int col = 0; col < B.numCols; col++) {
                    if( col > row ) {
                        assertEquals(0,B.get(row,col), UtilEjml.TEST_F32);
                    } else {
                        assertEquals(B_dense.get(row,col),B.get(row,col), UtilEjml.TEST_F32);
                    }
                }
            }
        }
    }
}