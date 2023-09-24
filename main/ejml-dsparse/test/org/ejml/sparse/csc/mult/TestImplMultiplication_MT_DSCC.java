/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;
import pabeles.concurrency.GrowArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
class TestImplMultiplication_MT_DSCC extends EjmlStandardJUnit {
    private final GrowArray<DGrowArray> workArrays = new GrowArray<>(DGrowArray::new);
    private final GrowArray<Workspace_MT_DSCC> workSpaceMT = new GrowArray<>(Workspace_MT_DSCC::new);

    @Test void mult_s_s() {
        for (int i = 0; i < 50; i++) {
            mult_s_s(5, 5, 5);
            mult_s_s(10, 5, 5);
            mult_s_s(5, 10, 5);
            mult_s_s(5, 5, 10);
        }

        // See comment in mult_s_s. This triggered a bug
        mult_s_s(10, 10, 0);
    }


    private void mult_s_s( int rowsA, int colsA, int colsB ) {
        int nz_a = RandomMatrices_DSCC.nonzero(rowsA, colsA, 0.05, 0.7, rand);
        int nz_b = RandomMatrices_DSCC.nonzero(colsA, colsB, 0.05, 0.7, rand);
        int nz_c = RandomMatrices_DSCC.nonzero(rowsA, colsB, 0.05, 0.7, rand);

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(rowsA, colsA, nz_a, -1, 1, rand);
        DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(colsA, colsB, nz_b, -1, 1, rand);
        DMatrixSparseCSC expected = RandomMatrices_DSCC.rectangle(rowsA, colsB, nz_c, -1, 1, rand);
        DMatrixSparseCSC found = expected.copy();

        // Make sure the work space is cleaned up. There was a bug where if b has zero columns stitching would
        // throw an exception if this wasn't empty
        workSpaceMT.grow();

        ImplMultiplication_DSCC.mult(a, b, expected, null, null);
        ImplMultiplication_MT_DSCC.mult(a, b, found, workSpaceMT);
        assertTrue(CommonOps_DSCC.checkStructure(found));

        assertTrue(MatrixFeatures_DSCC.isEqualsSort(expected, found, UtilEjml.TEST_F64));
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

        GrowArray<DGrowArray> work = new GrowArray<>(DGrowArray::new);

        if (add) {
            ImplMultiplication_MT_DSCC.multAdd(a, b, c, work);
            CommonOps_DDRM.multAdd(dense_a, b, expected_c);
        } else {
            ImplMultiplication_MT_DSCC.mult(a, b, c, work);
            CommonOps_DDRM.mult(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }

    @Test void multTransA_s_d() {
        multTransA_s_d(5, 5, 5);
        multTransA_s_d(10, 5, 5);
        multTransA_s_d(5, 10, 5);
        multTransA_s_d(5, 5, 10);
    }

    private void multTransA_s_d( int rowsA, int colsA, int colsB ) {
        int nz_a = RandomMatrices_DSCC.nonzero(rowsA, colsA, 0.05, 0.7, rand);

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(colsA, rowsA, nz_a, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(colsA, colsB, -1, 1, rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(rowsA, colsB, -1, 1, rand);
        DMatrixRMaj found = expected.copy();

        ImplMultiplication_DSCC.multTransA(a, b, expected, workArrays.grow());
        ImplMultiplication_MT_DSCC.multTransA(a, b, found, workArrays);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found, UtilEjml.TEST_F64));
    }

    @Test void multAddTransA_s_d() {
        multAddTransA_s_d(5, 5, 5);
        multAddTransA_s_d(10, 5, 5);
        multAddTransA_s_d(5, 10, 5);
        multAddTransA_s_d(5, 5, 10);
    }

    private void multAddTransA_s_d( int rowsA, int colsA, int colsB ) {
        int nz_a = RandomMatrices_DSCC.nonzero(rowsA, colsA, 0.05, 0.7, rand);

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(colsA, rowsA, nz_a, -1, 1, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(colsA, colsB, -1, 1, rand);
        DMatrixRMaj expected = RandomMatrices_DDRM.rectangle(rowsA, colsB, -1, 1, rand);
        DMatrixRMaj found = expected.copy();

        ImplMultiplication_DSCC.multAddTransA(a, b, expected, workArrays.grow());
        ImplMultiplication_MT_DSCC.multAddTransA(a, b, found, workArrays);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found, UtilEjml.TEST_F64));
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

        GrowArray<DGrowArray> work = new GrowArray<>(DGrowArray::new);

        if (add) {
            ImplMultiplication_MT_DSCC.multAddTransB(a, b, c, work);
            CommonOps_DDRM.multAddTransB(dense_a, b, expected_c);
        } else {
            ImplMultiplication_MT_DSCC.multTransB(a, b, c, false, work);
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
            ImplMultiplication_MT_DSCC.multAddTransAB(a, b, c);
            CommonOps_DDRM.multAddTransAB(dense_a, b, expected_c);
        } else {
            ImplMultiplication_MT_DSCC.multTransAB(a, b, c);
            CommonOps_DDRM.multTransAB(dense_a, b, expected_c);
        }

        for (int row = 0; row < c.numRows; row++) {
            for (int col = 0; col < c.numCols; col++) {
                assertEquals(expected_c.get(row, col), c.get(row, col), UtilEjml.TEST_F64, row + " " + col);
            }
        }
    }
}