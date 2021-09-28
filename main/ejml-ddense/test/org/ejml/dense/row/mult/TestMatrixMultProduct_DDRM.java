/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMultProduct_DDRM extends EjmlStandardJUnit {
    @Test
    public void outer() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(20, 10, rand);
        DMatrixRMaj found = new DMatrixRMaj(20,20);
        DMatrixRMaj expected = new DMatrixRMaj(20,20);

        MatrixMatrixMult_DDRM.multTransB(A, A, expected);
        MatrixMultProduct_DDRM.outer(A, found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, UtilEjml.TEST_F64));
    }

    @Test
    public void inner_small() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(20, 10, rand);
        DMatrixRMaj found = new DMatrixRMaj(10,10);
        DMatrixRMaj expected = new DMatrixRMaj(10,10);

        MatrixMatrixMult_DDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_DDRM.inner_small(A, found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, UtilEjml.TEST_F64));
    }

    @Test
    public void inner_reorder() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(20,10,rand);
        DMatrixRMaj found = new DMatrixRMaj(10,10);
        DMatrixRMaj expected = new DMatrixRMaj(10,10);

        MatrixMatrixMult_DDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_DDRM.inner_reorder(A, found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, UtilEjml.TEST_F64));
    }

    @Test
    public void inner_reorder_upper() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(20,10,rand);
        DMatrixRMaj found = RandomMatrices_DDRM.rectangle(10,10,rand);
        DMatrixRMaj expected = new DMatrixRMaj(10,10);

        MatrixMatrixMult_DDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_DDRM.inner_reorder_upper(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = i; j < found.numCols; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void inner_reorder_lower() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(20,10,rand);
        DMatrixRMaj found = RandomMatrices_DDRM.rectangle(10,10,rand);
        DMatrixRMaj expected = new DMatrixRMaj(10,10);

        MatrixMatrixMult_DDRM.multTransA_reorder(A,A,expected);
        MatrixMultProduct_DDRM.inner_reorder_lower(A, found);

        // only check the upper triangle
        for( int i = 0; i < found.numRows; i++ ) {
            for( int j = 0; j <= i; j++ ) {
                assertEquals(expected.get(i,j),found.get(i,j),UtilEjml.TEST_F64);
            }
        }
    }
}
