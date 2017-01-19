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

package org.ejml.dense.row.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixVectorMult_DDRM {

    Random rand = new Random(0x7354);

    @Test
    public void checkShapesOfInput() {
        CheckMatrixVectorMultShape_DDRM check = new CheckMatrixVectorMultShape_DDRM(MatrixVectorMult_DDRM.class);
        check.checkAll();
    }

    @Test
    public void mult() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,1,rand);

        MatrixVectorMult_DDRM.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void mult_zero() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,1,rand);

        MatrixVectorMult_DDRM.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void multAdd() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(2,3, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = new DMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_DDRM.multAdd(a,b,c);

        UtilTestMatrix.checkMat(c,7,20);
    }

    @Test
    public void multTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(3,2, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,1,rand);

        MatrixVectorMult_DDRM.multTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(3,2, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = RandomMatrices_DDRM.rectangle(2,1,rand);

        MatrixVectorMult_DDRM.multTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multAddTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(3,2, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = new DMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_DDRM.multAddTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void multAddTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DMatrixRMaj a = new DMatrixRMaj(3,2, true, d);
        DMatrixRMaj b = new DMatrixRMaj(3,1, true, d);
        DMatrixRMaj c = new DMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_DDRM.multAddTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void checkZeroRowsColumns() throws InvocationTargetException, IllegalAccessException {
        checkZeros(5,0);
        checkZeros(0,5);
    }

    /**
     * Sees if all the matrix multiplications produce the expected results against the provided
     * known solution.
     */
    private void checkZeros( int rowsA , int colsA  )
            throws InvocationTargetException, IllegalAccessException
    {

        int numChecked = 0;
        Method methods[] = MatrixVectorMult_DDRM.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

//            System.out.println(name);

            DMatrixRMaj a = new DMatrixRMaj(rowsA,colsA);
            DMatrixRMaj b = new DMatrixRMaj(colsA,1);
            DMatrixRMaj c = RandomMatrices_DDRM.rectangle(rowsA,1,rand);

            boolean add = name.contains("multAdd");

            if( name.contains("TransAB")) {
                CommonOps_DDRM.transpose(a);
                CommonOps_DDRM.transpose(b);
            } else if( name.contains("TransA")) {
                CommonOps_DDRM.transpose(a);
            } else if( name.contains("TransB")) {
                CommonOps_DDRM.transpose(b);
            }

            DMatrixRMaj original = c.copy();
            invoke(method,a,b,c);

            if( add ) {
                assertTrue(MatrixFeatures_DDRM.isEquals(original, c));
            } else {
                assertTrue(MatrixFeatures_DDRM.isZeros(c, UtilEjml.TEST_F64));
            }
            numChecked++;
        }

        assertEquals(numChecked,6);
    }

    public static void invoke(Method func,
                              DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            throw new RuntimeException("WTF?");
        }
    }
}
