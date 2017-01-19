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

package org.ejml.data;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCMatrixRMaj {

    Random rand = new Random(234);

    @Test
    public void constructor_darray() {
        CMatrixRMaj a = new CMatrixRMaj(new float[][]{{1,2,3,4},{5,6,7,8},{5,6,7,8}});
        CMatrixRMaj b = new CMatrixRMaj(3,2,true,1,2,3,4,5,6,7,8,5,6,7,8);

        EjmlUnitTests.assertEquals(a, b, UtilEjml.TEST_F32);
    }

    @Test
    public void constructor_cmatrix() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);
        a.set(1,3,9,2);

        CMatrixRMaj b = new CMatrixRMaj(a);
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void constructor_shape() {
        CMatrixRMaj a = new CMatrixRMaj(5,7);
        assertEquals(5,a.numRows);
        assertEquals(7,a.numCols);
        assertEquals(5*7*2,a.data.length);

    }

    @Test
    public void getIndex() {
        CMatrixRMaj a = new CMatrixRMaj(5,7);

        assertEquals(2*14+6,a.getIndex(2,3));
    }

    @Test
    public void reshape() {
        CMatrixRMaj a = new CMatrixRMaj(5,7);

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

    @Test
    public void get() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F32 c = new Complex_F32();
        a.get(2,1,c);

        assertEquals(c.real,5, UtilEjml.TEST_F32);
        assertEquals(c.imaginary, 6, UtilEjml.TEST_F32);
    }

    @Test
    public void set_rowcolumn() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.set(2, 1, 5, 6);

        assertEquals(5,a.data[2*4*2+2],UtilEjml.TEST_F32);
        assertEquals(6,a.data[2*4*2+3],UtilEjml.TEST_F32);
    }

    @Test
    public void getReal() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F32 c = new Complex_F32();
        a.get(2,1,c);

        assertEquals(a.getReal(2, 1), 5, UtilEjml.TEST_F32);
    }

    @Test
    public void setReal() {

        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.setReal(2,1,5);

        assertEquals(5, a.data[2 * 4 * 2 + 2], UtilEjml.TEST_F32);
    }

    @Test
    public void getImaginary() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F32 c = new Complex_F32();
        a.get(2,1,c);

        assertEquals(a.getImag(2, 1), 6, UtilEjml.TEST_F32);
    }

    @Test
    public void setImaginary() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);

        a.setImag(2, 1, 6);

        assertEquals(6, a.data[2 * 4 * 2 + 3], UtilEjml.TEST_F32);
    }

    @Test
    public void getDataLength() {
        assertEquals(3*4*2,new CMatrixRMaj(3,4).getDataLength());
    }

    @Test
    public void copy() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);
        a.set(1, 3, 9, 2);

        CMatrixRMaj b = a.copy();
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F32);
        }
    }

    @Test
    public void getRowStride() {
        CMatrixRMaj a = new CMatrixRMaj(3,4);
        assertEquals(4*2,a.getRowStride());
    }

    @Test
    public void set_array() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(3,4,rand);

        CMatrixRMaj B = new CMatrixRMaj(1,1);
        B.set(3,4,true,A.data);

        assertTrue(MatrixFeatures_CDRM.isEquals(A,B));

        CMatrixRMaj A_tran = new CMatrixRMaj(4,3);
        CommonOps_CDRM.transpose(A,A_tran);

        B.set(3,4,false,A_tran.data);

        assertTrue(MatrixFeatures_CDRM.isEquals(A,B));
    }
}
