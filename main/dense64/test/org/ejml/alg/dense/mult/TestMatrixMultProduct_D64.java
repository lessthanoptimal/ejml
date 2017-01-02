/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMultProduct_D64 {

    Random rand = new Random(2345);

    @Test
    public void outer() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(20, 10, rand);
        DenseMatrix64F found = new DenseMatrix64F(20,20);
        DenseMatrix64F expected = new DenseMatrix64F(20,20);

        MatrixMatrixMult_D64.multTransB(A, A, expected);
        MatrixMultProduct_D64.outer(A, found);

        assertTrue(MatrixFeatures_D64.isIdentical(expected, found, UtilEjml.TEST_64F));
    }

    @Test
    public void inner_small() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(20, 10, rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult_D64.multTransA_reorder(A,A,expected);
        MatrixMultProduct_D64.inner_small(A, found);

        assertTrue(MatrixFeatures_D64.isIdentical(expected, found, UtilEjml.TEST_64F));
    }

    @Test
    public void inner_reorder() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(20,10,rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult_D64.multTransA_reorder(A,A,expected);
        MatrixMultProduct_D64.inner_reorder(A, found);

        assertTrue(MatrixFeatures_D64.isIdentical(expected, found, UtilEjml.TEST_64F));
    }

    @Test
    public void inner_reorder_upper() {
        DenseMatrix64F A = RandomMatrices_D64.createRandom(20,10,rand);
        DenseMatrix64F found = new DenseMatrix64F(10,10);
        DenseMatrix64F expected = new DenseMatrix64F(10,10);

        MatrixMatrixMult_D64.multTransA_reorder(A,A,expected);
        MatrixMultProduct_D64.inner_reorder_upper(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = i; j < found.numCols; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),UtilEjml.TEST_64F);
            }
        }
    }
}
