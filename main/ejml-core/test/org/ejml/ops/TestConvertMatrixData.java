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

package org.ejml.ops;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.*;
import org.ejml.dense.fixed.MatrixFeatures_DDF2;
import org.ejml.dense.fixed.MatrixFeatures_DDF3;
import org.ejml.dense.fixed.MatrixFeatures_DDF4;
import org.ejml.dense.row.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConvertMatrixData extends EjmlStandardJUnit {
    @Test void DenseMatrix() {
        DMatrixRMaj A = new DMatrixRMaj(2,2,true,1,2,3,4);
        FMatrixRMaj B = new FMatrixRMaj(2,2);
        DMatrixRMaj C = new DMatrixRMaj(2,2);

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test void DMatrixFixed2x2() {
        DMatrix2x2 A = new DMatrix2x2(1,2,3,4);
        FMatrix2x2 B = new FMatrix2x2();
        DMatrix2x2 C = new DMatrix2x2();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_DDF2.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test void DMatrixFixed3x3() {
        DMatrix3x3 A = new DMatrix3x3(1,2,3,4,5,6,7,8,9);
        FMatrix3x3 B = new FMatrix3x3();
        DMatrix3x3 C = new DMatrix3x3();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_DDF3.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test void DMatrixFixed4x4() {
        DMatrix4x4 A = new DMatrix4x4(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6);
        FMatrix4x4 B = new FMatrix4x4();
        DMatrix4x4 C = new DMatrix4x4();

        ConvertMatrixData.convert(A,B);
        ConvertMatrixData.convert(B,C);

        assertTrue(MatrixFeatures_DDF4.isIdentical(A,C, UtilEjml.TEST_F64));
    }

    @Test void BDRM_DDRM() {
        BMatrixRMaj A = CommonOps_DDRM.elementLessThan(RandomMatrices_DDRM.rectangle(5,10, rand), 0.5, null);
        DMatrixRMaj B = new DMatrixRMaj(A.numRows, A.numCols);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                double expected = A.get(row, col) ? 1.0 : 0.0;
                assertEquals(expected, B.get(row, col));
            }
        }
    }

    @Test void BDRM_FDRM() {
        BMatrixRMaj A = CommonOps_FDRM.elementLessThan(RandomMatrices_FDRM.rectangle(5,10, rand), 0.5f, null);
        FMatrixRMaj B = new FMatrixRMaj(A.numRows, A.numCols);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                float expected = A.get(row, col) ? 1.0f : 0.0f;
                assertEquals(expected, B.get(row, col));
            }
        }
    }

    @Test void BDRM_DSCC() {
        BMatrixRMaj A = CommonOps_DDRM.elementLessThan(RandomMatrices_DDRM.rectangle(5,10, rand), 0.5, null);
        DMatrixSparseCSC B = new DMatrixSparseCSC(A.numRows, A.numCols);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                double expected = A.get(row, col) ? 1.0 : 0.0;
                assertEquals(expected, B.get(row, col));
            }
        }
    }

    @Test void BDRM_FSCC() {
        BMatrixRMaj A = CommonOps_DDRM.elementLessThan(RandomMatrices_DDRM.rectangle(5,10, rand), 0.5, null);
        FMatrixSparseCSC B = new FMatrixSparseCSC(A.numRows, A.numCols);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                double expected = A.get(row, col) ? 1.0 : 0.0;
                assertEquals(expected, B.get(row, col));
            }
        }
    }

    @Test void DDRM_ZDRM() {
        DMatrixRMaj A = new DMatrixRMaj(2,2,true,1,2,3,4);
        ZMatrixRMaj B = new ZMatrixRMaj(2,2);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col),B.getReal(row,col),UtilEjml.TEST_F64);
                assertEquals(0,B.getImag(row,col),UtilEjml.TEST_F64);
            }
        }
    }

    @Test void DDRM_CDRM() {
        DMatrixRMaj A = new DMatrixRMaj(2,2,true,1,2,3,4);
        CMatrixRMaj B = new CMatrixRMaj(2,2);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col),B.getReal(row,col),UtilEjml.TEST_F64);
                assertEquals(0,B.getImag(row,col),UtilEjml.TEST_F64);
            }
        }
    }

    @Test void FDRM_ZDRM() {
        FMatrixRMaj A = new FMatrixRMaj(2,2,true,1,2,3,4);
        ZMatrixRMaj B = new ZMatrixRMaj(2,2);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col),B.getReal(row,col),UtilEjml.TEST_F32);
                assertEquals(0,B.getImag(row,col),UtilEjml.TEST_F32);
            }
        }
    }

    @Test void FDRM_CDRM() {
        FMatrixRMaj A = new FMatrixRMaj(2,2,true,1,2,3,4);
        CMatrixRMaj B = new CMatrixRMaj(2,2);

        ConvertMatrixData.convert(A,B);
        for (int row = 0; row < A.numRows; row++) {
            for (int col = 0; col < A.numCols; col++) {
                assertEquals(A.get(row,col),B.getReal(row,col),UtilEjml.TEST_F32);
                assertEquals(0,B.getImag(row,col),UtilEjml.TEST_F32);
            }
        }
    }

}
