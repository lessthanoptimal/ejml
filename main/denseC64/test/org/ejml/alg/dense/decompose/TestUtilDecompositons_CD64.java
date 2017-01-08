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

package org.ejml.alg.dense.decompose;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_C64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilDecompositons_CD64 {
    Random rand = new Random(234);

    @Test
    public void checkIdentity_null() {
        RowMatrix_C64 A = UtilDecompositons_CD64.checkIdentity(null,4,3);
        assertTrue(MatrixFeatures_CD64.isIdentity(A, UtilEjml.TEST_F64));
    }

    @Test
    public void checkIdentity_random() {
        RowMatrix_C64 orig = RandomMatrices_CD64.createRandom(4,3,rand);
        RowMatrix_C64 A = UtilDecompositons_CD64.checkIdentity(orig,4,3);
        assertTrue(MatrixFeatures_CD64.isIdentity(A, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZeros_null() {
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZeros(null,4,3);
        assertTrue(MatrixFeatures_CD64.isZeros(A, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZeros_random() {
        RowMatrix_C64 orig = RandomMatrices_CD64.createRandom(4,3,rand);
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZeros(orig,4,3);
        assertTrue(MatrixFeatures_CD64.isZeros(A, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosLT_null() {
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZerosLT(null,4,3);
        assertTrue(MatrixFeatures_CD64.isUpperTriangle(A,0, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZerosLT_random() {
        RowMatrix_C64 orig = RandomMatrices_CD64.createRandom(4,3,rand);
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZerosLT(orig,4,3);
        assertTrue(MatrixFeatures_CD64.isUpperTriangle(A, 0, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosUT_null() {
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZerosUT(null,4,3);
        assertTrue(MatrixFeatures_CD64.isLowerTriangle(A,0, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZerosUT_random() {
        RowMatrix_C64 orig = RandomMatrices_CD64.createRandom(4,3,rand);
        RowMatrix_C64 A = UtilDecompositons_CD64.checkZerosUT(orig,4,3);
        assertTrue(MatrixFeatures_CD64.isLowerTriangle(A, 0, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }
}