/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestZMatrixRMaj extends GenericTestsZMatrix {
    @Override protected ZMatrix createMatrix( int numRows, int numCols ) {
        return new ZMatrixRMaj(numRows, numCols);
    }
    
    @Test void constructor_darray() {
        var a = new ZMatrixRMaj(new double[][]{{1,2,3,4},{5,6,7,8},{5,6,7,8}});
        var b = new ZMatrixRMaj(3,2,true,1,2,3,4,5,6,7,8,5,6,7,8);

        EjmlUnitTests.assertEquals(a, b, UtilEjml.TEST_F64);
    }

    @Test void constructor_cmatrix() {
        var a = new ZMatrixRMaj(3,4);
        a.set(1,3,9,2);

        var b = new ZMatrixRMaj(a);
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    @Test void constructor_shape() {
        var a = new ZMatrixRMaj(5,7);
        assertEquals(5,a.numRows);
        assertEquals(7,a.numCols);
        assertEquals(5*7*2,a.data.length);

    }

    @Test void getIndex() {
        ZMatrixRMaj a = new ZMatrixRMaj(5,7);

        assertEquals(2*14+6,a.getIndex(2,3));
    }

    @Test void reshape() {
        var a = new ZMatrixRMaj(5,7);

        assertEquals(5*7*2,a.data.length);
        assertEquals(5, a.numRows);
        assertEquals(7,a.numCols);

        // make it larger
        a.reshape(10,6);

        assertEquals(10*6*2,a.data.length);
        assertEquals(10,a.numRows);
        assertEquals(6,a.numCols);

        // make it smaller
        a.reshape(3,2);

        assertTrue(a.data.length > 3*2*2);
        assertEquals(3,a.numRows);
        assertEquals(2,a.numCols);
    }

    @Test void get() {
        var a = new ZMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(c.real,5, UtilEjml.TEST_F64);
        assertEquals(c.imaginary, 6, UtilEjml.TEST_F64);
    }

    @Test void set_rowcolumn() {
        var a = new ZMatrixRMaj(3,4);

        a.set(2, 1, 5, 6);

        assertEquals(5,a.data[2*4*2+2],UtilEjml.TEST_F64);
        assertEquals(6,a.data[2*4*2+3],UtilEjml.TEST_F64);
    }

    @Test void getReal() {
        var a = new ZMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(a.getReal(2, 1), 5, UtilEjml.TEST_F64);
    }

    @Test void setReal() {
        var a = new ZMatrixRMaj(3,4);

        a.setReal(2,1,5);

        assertEquals(5, a.data[2 * 4 * 2 + 2], UtilEjml.TEST_F64);
    }

    @Test void getImaginary() {
        var a = new ZMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(a.getImag(2, 1), 6, UtilEjml.TEST_F64);
    }

    @Test void setImaginary() {
        var a = new ZMatrixRMaj(3,4);

        a.setImag(2, 1, 6);

        assertEquals(6, a.data[2 * 4 * 2 + 3], UtilEjml.TEST_F64);
    }

    @Test void getDataLength() {
        assertEquals(3*4*2,new ZMatrixRMaj(3,4).getDataLength());
    }

    @Test void copy() {
        var a = new ZMatrixRMaj(3,4);
        a.set(1, 3, 9, 2);

        ZMatrixRMaj b = a.copy();
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    @Test void getRowStride() {
        var a = new ZMatrixRMaj(3,4);
        assertEquals(4*2,a.getRowStride());
    }

    @Test void set_array() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(3,4,rand);

        var B = new ZMatrixRMaj(1,1);
        B.setTo(3,4,true,A.data);

        assertTrue(MatrixFeatures_ZDRM.isEquals(A,B));

        var A_tran = new ZMatrixRMaj(4,3);
        CommonOps_ZDRM.transpose(A,A_tran);

        B.setTo(3,4,false,A_tran.data);

        assertTrue(MatrixFeatures_ZDRM.isEquals(A,B));
    }
}
