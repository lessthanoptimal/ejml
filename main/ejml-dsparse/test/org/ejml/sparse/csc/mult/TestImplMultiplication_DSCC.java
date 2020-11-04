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

package org.ejml.sparse.csc.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestImplMultiplication_DSCC {

    Random rand = new Random(234);
    DGrowArray workArray = new DGrowArray();

    @Test void mult_s_s() {
        for (int i = 0; i < 50; i++) {
            mult_s_s(5, 5, 5);
            mult_s_s(10, 5, 5);
            mult_s_s(5, 10, 5);
            mult_s_s(5, 5, 10);
        }
    }

    private void mult_s_s( int rowsA, int colsA, int colsB ) {
        int nz_a = RandomMatrices_DSCC.nonzero(rowsA, colsA, 0.05, 0.7, rand);
        int nz_b = RandomMatrices_DSCC.nonzero(colsA, colsB, 0.05, 0.7, rand);
        int nz_c = RandomMatrices_DSCC.nonzero(rowsA, colsB, 0.05, 0.7, rand);

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(rowsA, colsA, nz_a, -1, 1, rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(colsA, colsB, nz_b, -1, 1, rand);
        DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(rowsA, colsB, nz_c, -1, 1, rand);

        ImplMultiplication_DSCC.mult(a, b, c, null, null);
        assertTrue(CommonOps_DSCC.checkStructure(c));

        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);
        DMatrixRMaj dense_b = DConvertMatrixStruct.convert(b, (DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_DDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(dense_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    /**
     * Makes sure the size of the output matrix is adjusted as needed
     */
    @Test void mult_s_s_grow() {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4, 6, 17, -1, 1, rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(6, 5, 15, -1, 1, rand);
        DMatrixSparseCSC c = new DMatrixSparseCSC(4, 5, 0);

        ImplMultiplication_DSCC.mult(a, b, c, null, null);
        assertTrue(CommonOps_DSCC.checkStructure(c));

        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);
        DMatrixRMaj dense_b = DConvertMatrixStruct.convert(b, (DMatrixRMaj)null);
        DMatrixRMaj dense_c = new DMatrixRMaj(dense_a.numRows, dense_b.numCols);

        CommonOps_DDRM.mult(dense_a, dense_b, dense_c);

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(dense_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void mult_s_d() {
        for (int i = 0; i < 10; i++) {
            mult_s_d(24, false);
            mult_s_d(15, false);
            mult_s_d(4, false);
            mult_s_d(24, true);
            mult_s_d(15, true);
            mult_s_d(4, true);
        }
    }

    private void mult_s_d( int elementsA, boolean add ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4, 6, elementsA, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(6, 5, -1, 1, rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(4, 5, -1, 1, rand);
        DMatrixRMaj expected_c = c.copy();
        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);

        if (add) {
            ImplMultiplication_DSCC.multAdd(a, b, c);
            CommonOps_DDRM.multAdd(dense_a, b, expected_c);
        } else {
            ImplMultiplication_DSCC.mult(a, b, c);
            CommonOps_DDRM.mult(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void multTransA_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransA_s_d(24, false);
            multTransA_s_d(15, false);
            multTransA_s_d(4, false);

            multTransA_s_d(24, true);
            multTransA_s_d(15, true);
            multTransA_s_d(4, true);
        }
    }

    private void multTransA_s_d( int elementsA, boolean add ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(6, 4, elementsA, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(6, 5, -1, 1, rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(4, 5, -1, 1, rand);
        DMatrixRMaj expected_c = c.copy();
        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);

        if (add) {
            ImplMultiplication_DSCC.multAddTransA(a, b, c, workArray);
            CommonOps_DDRM.multAddTransA(dense_a, b, expected_c);
        } else {
            ImplMultiplication_DSCC.multTransA(a, b, c, workArray);
            CommonOps_DDRM.multTransA(dense_a, b, expected_c);
        }
        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void multTransB_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransB_s_d(24, false);
            multTransB_s_d(15, false);
            multTransB_s_d(4, false);

            multTransB_s_d(24, true);
            multTransB_s_d(15, true);
            multTransB_s_d(4, true);
        }
    }

    private void multTransB_s_d( int elementsA, boolean add ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(4, 6, elementsA, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 6, -1, 1, rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(4, 5, -1, 1, rand);
        DMatrixRMaj expected_c = c.copy();
        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);

        if (add) {
            ImplMultiplication_DSCC.multAddTransB(a, b, c, workArray);
            CommonOps_DDRM.multAddTransB(dense_a, b, expected_c);
        } else {
            ImplMultiplication_DSCC.multTransB(a, b, c, workArray);
            CommonOps_DDRM.multTransB(dense_a, b, expected_c);
        }
        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void multTransAB_s_d() {
        for (int i = 0; i < 10; i++) {
            multTransAB_s_d(24, false);
            multTransAB_s_d(15, false);
            multTransAB_s_d(4, false);

            multTransAB_s_d(24, true);
            multTransAB_s_d(15, true);
            multTransAB_s_d(4, true);
        }
    }

    private void multTransAB_s_d( int elementsA, boolean add ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(6, 4, elementsA, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(5, 6, -1, 1, rand);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(4, 5, -1, 1, rand);
        DMatrixRMaj expected_c = c.copy();
        DMatrixRMaj dense_a = DConvertMatrixStruct.convert(a, (DMatrixRMaj)null);

        if (add) {
            ImplMultiplication_DSCC.multAddTransAB(a, b, c);
            CommonOps_DDRM.multAddTransAB(dense_a, b, expected_c);
        } else {
            ImplMultiplication_DSCC.multTransAB(a, b, c);
            CommonOps_DDRM.multTransAB(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void addRowsInAInToC() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
                "1 0 1 0 0 " +
                        "1 0 0 1 0 " +
                        "0 0 1 0 0 " +
                        "0 0 0 1 0 " +
                        "1 1 0 0 1", 5);

        int[] w = new int[5];

        DMatrixSparseCSC B = new DMatrixSparseCSC(5, 5, 25);
        B.nz_length = 0;

        // nothing should be added here since w is full of 0 and colC = 0
        ImplMultiplication_DSCC.addRowsInAInToC(A, 0, B, 0, w);
        assertEquals(0, B.col_idx[1]);

        // colA shoul dnow be added to colB
        ImplMultiplication_DSCC.addRowsInAInToC(A, 0, B, 1, w);
        B.numCols = 2;// needed to be set correctly for structure unit test
        assertTrue(CommonOps_DSCC.checkStructure(B));
        assertEquals(3, B.col_idx[2]);
        int expected[] = new int[]{0, 1, 4};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], B.nz_rows[i]);
            assertEquals(1, w[expected[i]]);
        }
    }

    @Test void dotInnerColumns() {
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();

        for (int mc = 0; mc < 50; mc++) {
            int A_nz = RandomMatrices_DSCC.nonzero(8, 4, 0.1, 1.0, rand);
            int B_nz = RandomMatrices_DSCC.nonzero(8, 6, 0.1, 1.0, rand);

            DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(8, 4, A_nz, rand);
            DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(8, 6, B_nz, rand);

            int colA = rand.nextInt(4);
            int colB = rand.nextInt(6);

            double found = ImplMultiplication_DSCC.dotInnerColumns(A, colA, B, colB, gw, gx);

            double expected = 0;
            for (int i = 0; i < 8; i++) {
                expected += A.get(i, colA)*B.get(i, colB);
            }

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }
}