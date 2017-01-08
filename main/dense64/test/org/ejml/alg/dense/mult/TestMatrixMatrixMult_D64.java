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

package org.ejml.alg.dense.mult;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixMatrixMult_D64 {
    Random rand = new Random(121342);

    /**
     * Checks to see that it only accepts input matrices that have compatible shapes
     */
    @Test
    public void checkShapesOfInput() {
        CheckMatrixMultShape_D64 check = new CheckMatrixMultShape_D64(MatrixMatrixMult_D64.class);
        check.checkAll();
    }

    @Test
    public void checkZeroRowsColumns() throws InvocationTargetException, IllegalAccessException {
        checkZeros(5,0,0,6);
        checkZeros(0,5,5,0);
    }

    /**
     * Checks to see if the input 'c' matrix is not 'a' or 'b'
     */
    @Test
    public void checkInputInstance() throws IllegalAccessException {
        Method methods[] = MatrixMatrixMult_D64.class.getMethods();
        for( Method method : methods ) {
            String name = method.getName();

            if( !name.contains("mult") )
                continue;


            // make sure it checks that the c matrix is not a or b
            try {
                RowMatrix_F64 a = new RowMatrix_F64(2,2);
                RowMatrix_F64 b = new RowMatrix_F64(2,2);
                invoke(method,2.0,a,b,a);
                fail("An exception should have been thrown");
            } catch( InvocationTargetException e ) {
                assertTrue(e.getTargetException() instanceof IllegalArgumentException );
            }

            try {
                RowMatrix_F64 a = new RowMatrix_F64(2,2);
                RowMatrix_F64 b = new RowMatrix_F64(2,2);
                invoke(method,2.0,a,b,b);
                fail("An exception should have been thrown");
            } catch( InvocationTargetException e ) {
                assertTrue(e.getTargetException() instanceof IllegalArgumentException );
            }
        }
    }

    /**
     * Use java reflections to get a list of all the functions.  From the name extract what
     * the function is supposed to do.  then compute the expected results.
     *
     * Correctness is tested against a known case.
     */
    @Test
    public void checkAllAgainstKnown() throws InvocationTargetException, IllegalAccessException {
        double d[] = new double[]{0,1,2,3,4,5,6,7,8,9,10,11,12};
        RowMatrix_F64 a_orig = new RowMatrix_F64(2,3, true, d);
        RowMatrix_F64 b_orig = new RowMatrix_F64(3,4, true, d);
        RowMatrix_F64 c_orig = RandomMatrices_D64.createRandom(2,4,rand);

        RowMatrix_F64 r_orig = new RowMatrix_F64(2,4, true, 20, 23, 26, 29, 56, 68, 80, 92);

        checkResults(a_orig,b_orig,c_orig,r_orig);
    }

    /**
     * Creates a bunch of random matrices and computes the expected results using mult().
     *
     * The known case is needed since this test case tests against other algorithms in
     * this library, which could in theory be wrong.
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void checkAgainstRandomDiffShapes() throws InvocationTargetException, IllegalAccessException {

        for( int i = 1; i <= 4; i++ ) {
            for( int j = 1; j <= 4; j++ ) {
                for( int k = 1; k <= 4; k++ ) {
                    RowMatrix_F64 a_orig = RandomMatrices_D64.createRandom(i,j, rand);
                    RowMatrix_F64 b_orig = RandomMatrices_D64.createRandom(j,k, rand);
                    RowMatrix_F64 c_orig = RandomMatrices_D64.createRandom(i,k, rand);

                    RowMatrix_F64 r_orig = RandomMatrices_D64.createRandom(i,k,rand);

                    MatrixMatrixMult_D64.mult_small(a_orig,b_orig,r_orig);

                    checkResults(a_orig,b_orig,c_orig,r_orig);
                }
            }
        }
    }

    /**
     * Sees if all the matrix multiplications produce the expected results against the provided
     * known solution.
     */
    private void checkResults( RowMatrix_F64 a_orig ,
                               RowMatrix_F64 b_orig ,
                               RowMatrix_F64 c_orig ,
                               RowMatrix_F64 r_orig )
            throws InvocationTargetException, IllegalAccessException
    {
        double alpha = 2.5;

        int numChecked = 0;
        Method methods[] = MatrixMatrixMult_D64.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();
//            System.out.println(name);

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

            RowMatrix_F64 a = a_orig.copy();
            RowMatrix_F64 b = b_orig.copy();
            RowMatrix_F64 c = c_orig.copy();

            boolean add = name.contains("multAdd");
            boolean hasAlpha = method.getGenericParameterTypes()[0] == double.class;

            if( name.contains("TransAB")) {
                transpose(a);
                transpose(b);
            } else if( name.contains("TransA")) {
                transpose(a);
            } else if( name.contains("TransB")) {
                transpose(b);
            }

            RowMatrix_F64 expected = r_orig.copy();
            double []expectedData = expected.data;

            if( hasAlpha ) {
                for( int i = 0; i < expectedData.length; i++ ) {
                    expectedData[i] *= alpha;
                }
            }
            if( add ) {
                for( int i = 0; i < expectedData.length; i++ ) {
                    expectedData[i] += c_orig.get(i);
                }
            }

            invoke(method,alpha,a,b,c);

            EjmlUnitTests.assertEquals(expected,c,UtilEjml.TEST_F64);
            numChecked++;
        }

        assertEquals(numChecked,32);
    }

    /**
     * Sees if all the matrix multiplications produce the expected results against the provided
     * known solution.
     */
    private void checkZeros( int rowsA , int colsA , int rowsB , int colsB )
            throws InvocationTargetException, IllegalAccessException
    {

        double alpha = 2.5;

        int numChecked = 0;
        Method methods[] = MatrixMatrixMult_D64.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

//            System.out.println(name);

            RowMatrix_F64 a = new RowMatrix_F64(rowsA,colsA);
            RowMatrix_F64 b = new RowMatrix_F64(rowsB,colsB);
            RowMatrix_F64 c = RandomMatrices_D64.createRandom(rowsA,colsB,rand);

            boolean add = name.contains("multAdd");

            if( name.contains("TransAB")) {
                transpose(a);
                transpose(b);
            } else if( name.contains("TransA")) {
                transpose(a);
            } else if( name.contains("TransB")) {
                transpose(b);
            }

            RowMatrix_F64 original = c.copy();
            invoke(method,alpha,a,b,c);

            if( add ) {
                assertTrue(MatrixFeatures_D64.isEquals(original, c));
            } else {
                assertTrue(MatrixFeatures_D64.isZeros(c, UtilEjml.TEST_F64));
            }
            numChecked++;
        }

        assertEquals(numChecked,32);
    }

    private void transpose( RowMatrix_F64 a ) {
        RowMatrix_F64 b = new RowMatrix_F64(a.numCols,a.numRows);
        CommonOps_D64.transpose(a,b);
        a.set(b);
    }

    public static void invoke(Method func,
                              double alpha,
                              RowMatrix_F64 a, RowMatrix_F64 b, RowMatrix_F64 c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            if( func.getParameterTypes()[0] == double.class ) {
                if( func.getParameterTypes().length == 4 )
                    func.invoke(null,alpha, a, b, c);
                else
                    func.invoke(null,alpha, a, b, c,null);
            } else {
                func.invoke(null, a, b, c,null);
            }
        }
    }
}
