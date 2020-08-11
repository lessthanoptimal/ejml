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

package org.ejml.dense.row.mult;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMultProduct_FDRM {

    Random rand = new Random(2345);

    @Test
    public void outer() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(20, 10, rand);
        FMatrixRMaj found = new FMatrixRMaj(20,20);
        FMatrixRMaj expected = new FMatrixRMaj(20,20);

        MatrixMatrixMult_FDRM.multTransB(A, A, expected);
        MatrixMultProduct_FDRM.outer(A, found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found, UtilEjml.TEST_F32));
    }

    @Test
    public void inner_small() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(20, 10, rand);
        FMatrixRMaj found = new FMatrixRMaj(10,10);
        FMatrixRMaj expected = new FMatrixRMaj(10,10);

        MatrixMatrixMult_FDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_FDRM.inner_small(A, found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found, UtilEjml.TEST_F32));
    }

    @Test
    public void inner_reorder() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(20,10,rand);
        FMatrixRMaj found = new FMatrixRMaj(10,10);
        FMatrixRMaj expected = new FMatrixRMaj(10,10);

        MatrixMatrixMult_FDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_FDRM.inner_reorder(A, found);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected, found, UtilEjml.TEST_F32));
    }

    @Test
    public void inner_reorder_upper() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(20,10,rand);
        FMatrixRMaj found = RandomMatrices_FDRM.rectangle(10,10,rand);
        FMatrixRMaj expected = new FMatrixRMaj(10,10);

        MatrixMatrixMult_FDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_FDRM.inner_reorder_upper(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = i; j < found.numCols; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }

    @Test
    public void inner_reorder_lower() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(20,10,rand);
        FMatrixRMaj found = RandomMatrices_FDRM.rectangle(10,10,rand);
        FMatrixRMaj expected = new FMatrixRMaj(10,10);

        MatrixMatrixMult_FDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_FDRM.inner_reorder_lower(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = 0; j <= i; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),UtilEjml.TEST_F32);
            }
        }
    }
}
