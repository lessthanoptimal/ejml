/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.mult;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestMatrixVectorMult_D64 {

    Random rand = new Random(0x7354);

    @Test
    public void checkShapesOfInput() {
        CheckMatrixVectorMultShape_D64 check = new CheckMatrixVectorMultShape_D64(MatrixVectorMult_D64.class);
        check.checkAll();
    }

    @Test
    public void mult() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,1,rand);

        MatrixVectorMult_D64.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void mult_zero() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,1,rand);

        MatrixVectorMult_D64.mult(a,b,c);

        UtilTestMatrix.checkMat(c,5,14);
    }

    @Test
    public void multAdd() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(2,3, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult_D64.multAdd(a,b,c);

        UtilTestMatrix.checkMat(c,7,20);
    }

    @Test
    public void multTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,1,rand);

        MatrixVectorMult_D64.multTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = RandomMatrices_D64.createRandom(2,1,rand);

        MatrixVectorMult_D64.multTransA_reorder(a,b,c);

        UtilTestMatrix.checkMat(c,10,13);
    }

    @Test
    public void multAddTransA_small() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult_D64.multAddTransA_small(a,b,c);

        UtilTestMatrix.checkMat(c,12,19);
    }

    @Test
    public void multAddTransA_reorder() {
        double d[] = new double[]{0,1,2,3,4,5};
        DenseMatrix64F a = new DenseMatrix64F(3,2, true, d);
        DenseMatrix64F b = new DenseMatrix64F(3,1, true, d);
        DenseMatrix64F c = new DenseMatrix64F(2,1, true, 2, 6);

        MatrixVectorMult_D64.multAddTransA_reorder(a,b,c);

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
        Method methods[] = MatrixVectorMult_D64.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

//            System.out.println(name);

            DenseMatrix64F a = new DenseMatrix64F(rowsA,colsA);
            DenseMatrix64F b = new DenseMatrix64F(colsA,1);
            DenseMatrix64F c = RandomMatrices_D64.createRandom(rowsA,1,rand);

            boolean add = name.contains("multAdd");

            if( name.contains("TransAB")) {
                CommonOps_D64.transpose(a);
                CommonOps_D64.transpose(b);
            } else if( name.contains("TransA")) {
                CommonOps_D64.transpose(a);
            } else if( name.contains("TransB")) {
                CommonOps_D64.transpose(b);
            }

            DenseMatrix64F original = c.copy();
            invoke(method,a,b,c);

            if( add ) {
                assertTrue(MatrixFeatures_D64.isEquals(original, c));
            } else {
                assertTrue(MatrixFeatures_D64.isZeros(c, UtilEjml.TEST_64F));
            }
            numChecked++;
        }

        assertEquals(numChecked,6);
    }

    public static void invoke(Method func,
                              DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            throw new RuntimeException("WTF?");
        }
    }
}
