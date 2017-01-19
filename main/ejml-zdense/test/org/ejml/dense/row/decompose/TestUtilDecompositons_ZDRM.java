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

package org.ejml.dense.row.decompose;

import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilDecompositons_ZDRM {
    Random rand = new Random(234);

    @Test
    public void checkIdentity_null() {
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkIdentity(null,4,3);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(A, UtilEjml.TEST_F64));
    }

    @Test
    public void checkIdentity_random() {
        ZMatrixRMaj orig = RandomMatrices_ZDRM.createRandom(4,3,rand);
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkIdentity(orig,4,3);
        assertTrue(MatrixFeatures_ZDRM.isIdentity(A, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZeros_null() {
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZeros(null,4,3);
        assertTrue(MatrixFeatures_ZDRM.isZeros(A, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZeros_random() {
        ZMatrixRMaj orig = RandomMatrices_ZDRM.createRandom(4,3,rand);
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZeros(orig,4,3);
        assertTrue(MatrixFeatures_ZDRM.isZeros(A, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosLT_null() {
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZerosLT(null,4,3);
        assertTrue(MatrixFeatures_ZDRM.isUpperTriangle(A,0, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZerosLT_random() {
        ZMatrixRMaj orig = RandomMatrices_ZDRM.createRandom(4,3,rand);
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZerosLT(orig,4,3);
        assertTrue(MatrixFeatures_ZDRM.isUpperTriangle(A, 0, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosUT_null() {
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZerosUT(null,4,3);
        assertTrue(MatrixFeatures_ZDRM.isLowerTriangle(A,0, UtilEjml.TEST_F64));
    }

    @Test
    public void checkZerosUT_random() {
        ZMatrixRMaj orig = RandomMatrices_ZDRM.createRandom(4,3,rand);
        ZMatrixRMaj A = UtilDecompositons_ZDRM.checkZerosUT(orig,4,3);
        assertTrue(MatrixFeatures_ZDRM.isLowerTriangle(A, 0, UtilEjml.TEST_F64));
        assertTrue(A==orig);
    }
}