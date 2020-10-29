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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
class TestCommonOps_MT_DSCC {

    private final Random rand = new Random(234);

    @Test
    public void mult_s_s_shapes() {
        // multiple trials to test more sparse structures
        for (int trial = 0; trial < 50; trial++) {
            check_s_s_mult(
                    RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_DSCC.rectangle(5, 4, 7, rand), false);

            check_s_s_mult(
                    RandomMatrices_DSCC.rectangle(5, 7, 5, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_DSCC.rectangle(5, 5, 7, rand), true);
            check_s_s_mult(
                    RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_DSCC.rectangle(5, 5, 7, rand), false);
            check_s_s_mult(
                    RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand), false);
            check_s_s_mult(
                    RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand),
                    RandomMatrices_DSCC.rectangle(6, 4, 7, rand), false);
        }
    }

    private void check_s_s_mult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, boolean exception ) {
        DMatrixSparseCSC expected = C.createLike();

        try {
            CommonOps_MT_DSCC.mult(A, B, C, null);
            assertTrue(CommonOps_DSCC.checkStructure(C));

            if (exception)
                fail("exception expected");

            CommonOps_DSCC.mult(A, B, expected);

            assertTrue(MatrixFeatures_DSCC.isEqualsSort(expected, C, UtilEjml.TEST_F64));
        } catch (RuntimeException e) {
            if (!exception)
                fail("no exception expected. " + e.getMessage());
        }
    }

    @Test
    public void add_shapes() {
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5*6, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 20, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 16, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 0, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 5, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 5, 5, rand), false);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(4, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand), true);
        check_add(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DSCC.rectangle(4, 6, 5, rand), false);
    }

    private void check_add( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, boolean exception ) {
        double alpha = 1.5;
        double beta = -0.6;
        try {
            CommonOps_MT_DSCC.add(alpha, A, beta, B, C, null);
            assertTrue(CommonOps_DSCC.checkStructure(C));

            if (exception)
                fail("exception expected");

            DMatrixSparseCSC expected = C.createLike();

            CommonOps_DSCC.add(alpha, A, beta, B, expected, null, null);

            assertTrue(MatrixFeatures_DSCC.isEqualsSort(expected, C, UtilEjml.TEST_F64));
        } catch (RuntimeException ignore) {
            if (!exception)
                fail("no exception expected");
        }
    }

    @Test
    public void mult_s_d_shapes() {
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(5, 4, rand), false);

        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(7, 4, rand),
                RandomMatrices_DDRM.rectangle(5, 4, rand), true);

        // Matrix C is resized
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(5, 5, rand), false);
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand), false);
        check_s_d_mult(
                RandomMatrices_DSCC.rectangle(5, 6, 5, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand),
                RandomMatrices_DDRM.rectangle(6, 4, rand), false);
    }

    private void check_s_d_mult( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, boolean exception ) {
        DMatrixRMaj denseA = DConvertMatrixStruct.convert(A, (DMatrixRMaj)null);
        DMatrixRMaj expected = C.copy();

        DMatrixSparseCSC A_t = CommonOps_DSCC.transpose(A, null, null);
        DMatrixRMaj B_t = CommonOps_DDRM.transpose(B, null);
        DMatrixRMaj denseA_t = CommonOps_DDRM.transpose(denseA, null);

        for (int i = 0; i < 2; i++) {
            boolean transA = i == 1;
            for (int j = 0; j < 2; j++) {
                boolean transB = j == 1;
                for (int k = 0; k < 2; k++) {
                    boolean add = k == 1;
                    try {
                        if (add) {
                            if (transA) {
                                if (transB) {
                                    continue;
//                                    CommonOps_DSCC.multAddTransAB(A_t, B_t, C);
//                                    CommonOps_DDRM.multAddTransAB(denseA_t, B_t, expected);
                                } else {
                                    CommonOps_DSCC.multAddTransA(A_t, B, C);
                                    CommonOps_DDRM.multAddTransA(denseA_t, B, expected);
                                }
                            } else if (transB) {
                                continue;
//                                CommonOps_DSCC.multAddTransB(A, B_t, C);
//                                CommonOps_DDRM.multAddTransB(denseA, B_t, expected);
                            } else {
                                continue;
//                                CommonOps_DSCC.multAdd(A, B, C);
//                                CommonOps_DDRM.multAdd(denseA, B, expected);
                            }
                        } else {
                            if (transA) {
                                if (transB) {
                                    continue;
//                                    CommonOps_DSCC.multTransAB(A_t, B_t, C);
//                                    CommonOps_DDRM.multTransAB(denseA_t, B_t, expected);
                                } else {
                                    CommonOps_DSCC.multTransA(A_t, B, C);
                                    CommonOps_DDRM.multTransA(denseA_t, B, expected);
                                }
                            } else if (transB) {
                                continue;
//                                CommonOps_DSCC.multTransB(A, B_t, C);
//                                CommonOps_DDRM.multTransB(denseA, B_t, expected);
                            } else {
                                continue;
//                                CommonOps_DSCC.mult(A, B, C);
//                                CommonOps_DDRM.mult(denseA, B, expected);
                            }
                        }

                        if (exception)
                            fail("exception expected");

                        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, C, UtilEjml.TEST_F64));
                    } catch (RuntimeException ignore) {
                        if (!exception)
                            fail("no exception expected");
                    }
                }
            }
        }
    }
}