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
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixVectorMult_FDRM {

    Random rand = new Random(0x7354);

    @Test
    public void checkShapesOfInput() {
        CheckMatrixVectorMultShape_FDRM check = new CheckMatrixVectorMultShape_FDRM(MatrixVectorMult_FDRM.class);
        check.checkAll();
    }

    @Test
    public void mult() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,1,rand);

        MatrixVectorMult_FDRM.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void mult_zero() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,1,rand);

        MatrixVectorMult_FDRM.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void multAdd() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(2,3, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = new FMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_FDRM.multAdd(a,b,c);

        UtilTestMatrix.checkMat(c,7,20);
    }

    @Test
    public void multTransA_small() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(3,2, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,1,rand);

        MatrixVectorMult_FDRM.multTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multTransA_reorder() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(3,2, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(2,1,rand);

        MatrixVectorMult_FDRM.multTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multAddTransA_small() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(3,2, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = new FMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_FDRM.multAddTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void multAddTransA_reorder() {
        float d[] = new float[]{0,1,2,3,4,5};
        FMatrixRMaj a = new FMatrixRMaj(3,2, true, d);
        FMatrixRMaj b = new FMatrixRMaj(3,1, true, d);
        FMatrixRMaj c = new FMatrixRMaj(2,1, true, 2, 6);

        MatrixVectorMult_FDRM.multAddTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void checkZeroRowsColumns() throws InvocationTargetException, IllegalAccessException {
        checkZeros(5,0);
        checkZeros(0,5);
    }

    @Test
    public void innerProduct() {
        FMatrixRMaj a = RandomMatrices_FDRM.rectangle(5,1,rand);
        FMatrixRMaj B = RandomMatrices_FDRM.rectangle(5,5,rand);
        FMatrixRMaj c = RandomMatrices_FDRM.rectangle(5,1,rand);

        FMatrixRMaj tmp = new FMatrixRMaj(5,1);
        CommonOps_FDRM.multTransA(a,B,tmp);
        float expected = VectorVectorMult_FDRM.innerProd(tmp,c);
        float found = MatrixVectorMult_FDRM.innerProduct(a.data,0,B,c.data,0);
        assertEquals(expected,found, UtilEjml.TEST_F32);

        // now have offsets
        int offsetA = 1;
        int offsetC = 2;
        float[] _a = new float[10];
        float[] _c = new float[10];
        System.arraycopy(a.data,0,_a,offsetA,5);
        System.arraycopy(c.data,0,_c,offsetC,5);

        found = MatrixVectorMult_FDRM.innerProduct(_a,offsetA,B,_c,offsetC);
        assertEquals(expected,found, UtilEjml.TEST_F32);
    }

    /**
     * Sees if all the matrix multiplications produce the expected results against the provided
     * known solution.
     */
    private void checkZeros( int rowsA , int colsA  )
            throws InvocationTargetException, IllegalAccessException
    {

        int numChecked = 0;
        Method methods[] = MatrixVectorMult_FDRM.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

//            System.out.println(name);

            FMatrixRMaj a = new FMatrixRMaj(rowsA,colsA);
            FMatrixRMaj b = new FMatrixRMaj(colsA,1);
            FMatrixRMaj c = RandomMatrices_FDRM.rectangle(rowsA,1,rand);

            boolean add = name.contains("multAdd");

            if( name.contains("TransAB")) {
                CommonOps_FDRM.transpose(a);
                CommonOps_FDRM.transpose(b);
            } else if( name.contains("TransA")) {
                CommonOps_FDRM.transpose(a);
            } else if( name.contains("TransB")) {
                CommonOps_FDRM.transpose(b);
            }

            FMatrixRMaj original = c.copy();
            invoke(method,a,b,c);

            if( add ) {
                assertTrue(MatrixFeatures_FDRM.isEquals(original, c));
            } else {
                assertTrue(MatrixFeatures_FDRM.isZeros(c, UtilEjml.TEST_F32));
            }
            numChecked++;
        }

        assertEquals(numChecked,6);
    }

    public static void invoke(Method func,
                              FMatrixRMaj a, FMatrixRMaj b, FMatrixRMaj c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            throw new RuntimeException("WTF?");
        }
    }
}
