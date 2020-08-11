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

package org.ejml.dense.row.decompose;

import org.ejml.UtilEjml;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilDecompositons_CDRM {
    Random rand = new Random(234);

    @Test
    public void checkIdentity_null() {
        CMatrixRMaj A = UtilDecompositons_CDRM.checkIdentity(null,4,3);
        assertTrue(MatrixFeatures_CDRM.isIdentity(A, UtilEjml.TEST_F32));
    }

    @Test
    public void checkIdentity_random() {
        CMatrixRMaj orig = RandomMatrices_CDRM.rectangle(4,3,rand);
        CMatrixRMaj A = UtilDecompositons_CDRM.checkIdentity(orig,4,3);
        assertTrue(MatrixFeatures_CDRM.isIdentity(A, UtilEjml.TEST_F32));
        assertTrue(A==orig);
    }

    @Test
    public void checkZeros_null() {
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZeros(null,4,3);
        assertTrue(MatrixFeatures_CDRM.isZeros(A, UtilEjml.TEST_F32));
    }

    @Test
    public void checkZeros_random() {
        CMatrixRMaj orig = RandomMatrices_CDRM.rectangle(4,3,rand);
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZeros(orig,4,3);
        assertTrue(MatrixFeatures_CDRM.isZeros(A, UtilEjml.TEST_F32));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosLT_null() {
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZerosLT(null,4,3);
        assertTrue(MatrixFeatures_CDRM.isUpperTriangle(A,0, UtilEjml.TEST_F32));
    }

    @Test
    public void checkZerosLT_random() {
        CMatrixRMaj orig = RandomMatrices_CDRM.rectangle(4,3,rand);
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZerosLT(orig,4,3);
        assertTrue(MatrixFeatures_CDRM.isUpperTriangle(A, 0, UtilEjml.TEST_F32));
        assertTrue(A==orig);
    }

    @Test
    public void checkZerosUT_null() {
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZerosUT(null,4,3);
        assertTrue(MatrixFeatures_CDRM.isLowerTriangle(A,0, UtilEjml.TEST_F32));
    }

    @Test
    public void checkZerosUT_random() {
        CMatrixRMaj orig = RandomMatrices_CDRM.rectangle(4,3,rand);
        CMatrixRMaj A = UtilDecompositons_CDRM.checkZerosUT(orig,4,3);
        assertTrue(MatrixFeatures_CDRM.isLowerTriangle(A, 0, UtilEjml.TEST_F32));
        assertTrue(A==orig);
    }
}