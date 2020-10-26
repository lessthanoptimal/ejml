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
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
class TestImplSparseSparseMult_MT_DSCC {
    Random rand = new Random(234);

    @Test
    public void mult_s_s() {
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
        DMatrixSparseCSC expected = RandomMatrices_DSCC.rectangle(rowsA, colsB, nz_c, -1, 1, rand);
        DMatrixSparseCSC found = expected.copy();

        ImplSparseSparseMult_DSCC.mult(a, b, expected, null, null);
        ImplSparseSparseMult_MT_DSCC.mult(a, b, found, null);
        assertTrue(CommonOps_DSCC.checkStructure(found));

        assertTrue(MatrixFeatures_DSCC.isEqualsSort(expected, found, UtilEjml.TEST_F64));
    }

    @Test
    public void multTransA_s_d() {
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

        ImplSparseSparseMult_DSCC.multTransA(a, b, expected);
        ImplSparseSparseMult_MT_DSCC.multTransA(a, b, found);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found, UtilEjml.TEST_F64));
    }

    @Test
    public void multAddTransA_s_d() {
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

        ImplSparseSparseMult_DSCC.multAddTransA(a, b, expected);
        ImplSparseSparseMult_MT_DSCC.multAddTransA(a, b, found);

        assertTrue(MatrixFeatures_DDRM.isEquals(expected, found, UtilEjml.TEST_F64));
    }
}