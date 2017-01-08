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
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRowMatrix_C64 {

    Random rand = new Random(234);

    @Test
    public void constructor_darray() {
        RowMatrix_C64 a = new RowMatrix_C64(new double[][]{{1,2,3,4},{5,6,7,8},{5,6,7,8}});
        RowMatrix_C64 b = new RowMatrix_C64(3,2,true,1,2,3,4,5,6,7,8,5,6,7,8);

        EjmlUnitTests.assertEquals(a, b, UtilEjml.TEST_F64);
    }

    @Test
    public void constructor_cmatrix() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);
        a.set(1,3,9,2);

        RowMatrix_C64 b = new RowMatrix_C64(a);
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void constructor_shape() {
        RowMatrix_C64 a = new RowMatrix_C64(5,7);
        assertEquals(5,a.numRows);
        assertEquals(7,a.numCols);
        assertEquals(5*7*2,a.data.length);

    }

    @Test
    public void getIndex() {
        RowMatrix_C64 a = new RowMatrix_C64(5,7);

        assertEquals(2*14+6,a.getIndex(2,3));
    }

    @Test
    public void reshape() {
        RowMatrix_C64 a = new RowMatrix_C64(5,7);

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
        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(c.real,5, UtilEjml.TEST_F64);
        assertEquals(c.imaginary, 6, UtilEjml.TEST_F64);
    }

    @Test
    public void set_rowcolumn() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.set(2, 1, 5, 6);

        assertEquals(5,a.data[2*4*2+2],UtilEjml.TEST_F64);
        assertEquals(6,a.data[2*4*2+3],UtilEjml.TEST_F64);
    }

    @Test
    public void getReal() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(a.getReal(2, 1), 5, UtilEjml.TEST_F64);
    }

    @Test
    public void setReal() {

        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.setReal(2,1,5);

        assertEquals(5, a.data[2 * 4 * 2 + 2], UtilEjml.TEST_F64);
    }

    @Test
    public void getImaginary() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.data[2*4*2 + 2] = 5;
        a.data[2*4*2 + 3] = 6;

        Complex_F64 c = new Complex_F64();
        a.get(2,1,c);

        assertEquals(a.getImag(2, 1), 6, UtilEjml.TEST_F64);
    }

    @Test
    public void setImaginary() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);

        a.setImag(2, 1, 6);

        assertEquals(6, a.data[2 * 4 * 2 + 3], UtilEjml.TEST_F64);
    }

    @Test
    public void getDataLength() {
        assertEquals(3*4*2,new RowMatrix_C64(3,4).getDataLength());
    }

    @Test
    public void copy() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);
        a.set(1, 3, 9, 2);

        RowMatrix_C64 b = a.copy();
        for (int i = 0; i < a.getDataLength(); i++) {
            assertEquals(a.data[i],b.data[i],UtilEjml.TEST_F64);
        }
    }

    @Test
    public void getRowStride() {
        RowMatrix_C64 a = new RowMatrix_C64(3,4);
        assertEquals(4*2,a.getRowStride());
    }

    @Test
    public void set_array() {
        RowMatrix_C64 A = RandomMatrices_CR64.createRandom(3,4,rand);

        RowMatrix_C64 B = new RowMatrix_C64(1,1);
        B.set(3,4,true,A.data);

        assertTrue(MatrixFeatures_CR64.isEquals(A,B));

        RowMatrix_C64 A_tran = new RowMatrix_C64(4,3);
        CommonOps_CR64.transpose(A,A_tran);

        B.set(3,4,false,A_tran.data);

        assertTrue(MatrixFeatures_CR64.isEquals(A,B));
    }
}
