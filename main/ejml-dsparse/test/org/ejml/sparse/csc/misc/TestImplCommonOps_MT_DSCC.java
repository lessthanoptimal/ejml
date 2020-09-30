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

package org.ejml.sparse.csc.misc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
class TestImplCommonOps_MT_DSCC {
    private final Random rand = new Random(324);

    @Test
    void add() {
        double alpha = 1.5;
        double beta = 2.3;

        for (int numRows : new int[]{2, 4, 6, 10}) {
            for (int numCols : new int[]{2, 4, 6, 10}) {
                DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows, numCols, 7, -1, 1, rand);
                DMatrixSparseCSC b = RandomMatrices_DSCC.rectangle(numRows, numCols, 8, -1, 1, rand);
                DMatrixSparseCSC c = RandomMatrices_DSCC.rectangle(numRows, numCols, 3, -1, 1, rand);
                DMatrixSparseCSC cc = c.copy();

                ImplCommonOps_DSCC.add(alpha, a, beta, b, c, null, null);
                ImplCommonOps_MT_DSCC.add(alpha, a, beta, b, cc, null);
                assertTrue(CommonOps_DSCC.checkStructure(cc));

                assertTrue(MatrixFeatures_DSCC.isEqualsSort(c, cc, UtilEjml.TEST_F64));
            }
        }
    }
}