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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
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
}