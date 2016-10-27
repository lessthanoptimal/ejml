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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilDecompositons_D64 {

    Random rand = new Random(234);

    @Test
    public void checkIdentity_null() {
        DenseMatrix64F A = UtilDecompositons_D64.checkIdentity(null,4,3);
        assertTrue(MatrixFeatures.isIdentity(A, 1e-8));
    }

    @Test
    public void checkIdentity_random() {
        DenseMatrix64F orig = RandomMatrices.createRandom(4,3,rand);
        DenseMatrix64F A = UtilDecompositons_D64.checkIdentity(orig,4,3);
        assertTrue(MatrixFeatures.isIdentity(A, 1e-8));
        assertTrue(A==orig);
    }

    @Test
    public void checkZeros_null() {
        DenseMatrix64F A = UtilDecompositons_D64.checkZeros(null,4,3);
        assertTrue(MatrixFeatures.isZeros(A, 1e-8));
    }

    @Test
    public void checkZeros_random() {
        DenseMatrix64F orig = RandomMatrices.createRandom(4,3,rand);
        DenseMatrix64F A = UtilDecompositons_D64.checkZeros(orig,4,3);
        assertTrue(MatrixFeatures.isZeros(A, 1e-8));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosLT_null() {
        DenseMatrix64F A = UtilDecompositons_D64.checkZerosLT(null,4,3);
        assertTrue(MatrixFeatures.isUpperTriangle(A,0, 1e-8));
    }

    @Test
    public void checkZerosLT_random() {
        DenseMatrix64F orig = RandomMatrices.createRandom(4,3,rand);
        DenseMatrix64F A = UtilDecompositons_D64.checkZerosLT(orig,4,3);
        assertTrue(MatrixFeatures.isUpperTriangle(A, 0, 1e-8));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosUT_null() {
        DenseMatrix64F A = UtilDecompositons_D64.checkZerosUT(null,4,3);
        assertTrue(MatrixFeatures.isLowerTriangle(A,0, 1e-8));
    }

    @Test
    public void checkZerosUT_random() {
        DenseMatrix64F orig = RandomMatrices.createRandom(4,3,rand);
        DenseMatrix64F A = UtilDecompositons_D64.checkZerosUT(orig,4,3);
        assertTrue(MatrixFeatures.isLowerTriangle(A, 0, 1e-8));
        assertTrue(A==orig);
    }
}