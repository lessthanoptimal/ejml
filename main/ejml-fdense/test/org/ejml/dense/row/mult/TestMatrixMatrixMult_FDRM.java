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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public class TestMatrixMatrixMult_FDRM {
    Random rand = new Random(121342);

    /**
     * Checks to see that it only accepts input matrices that have compatible shapes
     */
    @Test
    public void checkShapesOfInput() {
        CheckMatrixMultShape_FDRM check = new CheckMatrixMultShape_FDRM(MatrixMatrixMult_FDRM.class);
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
        Method[] methods = MatrixMatrixMult_FDRM.class.getMethods();
        for( Method method : methods ) {
            String name = method.getName();

            if( !name.contains("mult") )
                continue;


            // make sure it checks that the c matrix is not a or b
            try {
                FMatrixRMaj a = new FMatrixRMaj(2,2);
                FMatrixRMaj b = new FMatrixRMaj(2,2);
                invoke(method,2.0f,a,b,a);
                fail("An exception should have been thrown");
            } catch( InvocationTargetException e ) {
                assertTrue(e.getTargetException() instanceof IllegalArgumentException );
            }

            try {
                FMatrixRMaj a = new FMatrixRMaj(2,2);
                FMatrixRMaj b = new FMatrixRMaj(2,2);
                invoke(method,2.0f,a,b,b);
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
        float[] d = new float[]{0,1,2,3,4,5,6,7,8,9,10,11,12};
        FMatrixRMaj a_orig = new FMatrixRMaj(2,3, true, d);
        FMatrixRMaj b_orig = new FMatrixRMaj(3,4, true, d);
        FMatrixRMaj c_orig = RandomMatrices_FDRM.rectangle(2,4,rand);

        FMatrixRMaj r_orig = new FMatrixRMaj(2,4, true, 20, 23, 26, 29, 56, 68, 80, 92);

        checkResults(a_orig,b_orig,c_orig,r_orig);
    }

    /**
     * Creates a bunch of random matrices and computes the expected results using mult().
     *
     * The known case is needed since this test case tests against other algorithms in
     * this library, which could in theory be wrong.
     */
    @Test
    public void checkAgainstRandomDiffShapes() throws InvocationTargetException, IllegalAccessException {

        for( int i = 1; i <= 4; i++ ) {
            for( int j = 1; j <= 4; j++ ) {
                for( int k = 1; k <= 4; k++ ) {
                    FMatrixRMaj a_orig = RandomMatrices_FDRM.rectangle(i,j, rand);
                    FMatrixRMaj b_orig = RandomMatrices_FDRM.rectangle(j,k, rand);
                    FMatrixRMaj c_orig = RandomMatrices_FDRM.rectangle(i,k, rand);

                    FMatrixRMaj r_orig = RandomMatrices_FDRM.rectangle(i,k,rand);

                    MatrixMatrixMult_FDRM.mult_small(a_orig,b_orig,r_orig);

                    checkResults(a_orig,b_orig,c_orig,r_orig);
                }
            }
        }
    }

    /**
     * Sees if all the matrix multiplications produce the expected results against the provided
     * known solution.
     */
    private void checkResults( FMatrixRMaj a_orig ,
                               FMatrixRMaj b_orig ,
                               FMatrixRMaj c_orig ,
                               FMatrixRMaj r_orig )
            throws InvocationTargetException, IllegalAccessException
    {
        float alpha = 2.5f;

        int numChecked = 0;
        Method[] methods = MatrixMatrixMult_FDRM.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();
//            System.out.println(name);

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

            FMatrixRMaj a = a_orig.copy();
            FMatrixRMaj b = b_orig.copy();
            FMatrixRMaj c = c_orig.copy();

            boolean add = name.contains("multAdd");
            boolean hasAlpha = method.getGenericParameterTypes()[0] == float.class;

            if( name.contains("TransAB")) {
                transpose(a);
                transpose(b);
            } else if( name.contains("TransA")) {
                transpose(a);
            } else if( name.contains("TransB")) {
                transpose(b);
            }

            FMatrixRMaj expected = r_orig.copy();
            float []expectedData = expected.data;

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

            EjmlUnitTests.assertEquals(expected,c,UtilEjml.TEST_F32);
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

        float alpha = 2.5f;

        int numChecked = 0;
        Method[] methods = MatrixMatrixMult_FDRM.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if( !name.contains("mult") )
                continue;

//            System.out.println(name);

            FMatrixRMaj a = new FMatrixRMaj(rowsA,colsA);
            FMatrixRMaj b = new FMatrixRMaj(rowsB,colsB);
            FMatrixRMaj c = RandomMatrices_FDRM.rectangle(rowsA,colsB,rand);

            boolean add = name.contains("multAdd");

            if( name.contains("TransAB")) {
                transpose(a);
                transpose(b);
            } else if( name.contains("TransA")) {
                transpose(a);
            } else if( name.contains("TransB")) {
                transpose(b);
            }

            FMatrixRMaj original = c.copy();
            invoke(method,alpha,a,b,c);

            if( add ) {
                assertTrue(MatrixFeatures_FDRM.isEquals(original, c));
            } else {
                assertTrue(MatrixFeatures_FDRM.isZeros(c, UtilEjml.TEST_F32));
            }
            numChecked++;
        }

        assertEquals(numChecked,32);
    }

    private void transpose( FMatrixRMaj a ) {
        FMatrixRMaj b = new FMatrixRMaj(a.numCols,a.numRows);
        CommonOps_FDRM.transpose(a,b);
        a.set(b);
    }

    public static FMatrixRMaj invoke(Method func,
                              float alpha,
                              FMatrixRMaj a, FMatrixRMaj b, FMatrixRMaj c)
            throws IllegalAccessException, InvocationTargetException {

        Object ret;
        if( func.getParameterTypes().length == 3 ) {
            ret = func.invoke(null, a, b, c);
        } else {
            if( func.getParameterTypes()[0] == float.class ) {
                if( func.getParameterTypes().length == 4 )
                    ret = func.invoke(null,alpha, a, b, c);
                else
                    ret = func.invoke(null,alpha, a, b, c,null);
            } else {
                ret = func.invoke(null, a, b, c,null);
            }
        }

        if( ret instanceof FMatrixRMaj )
            return (FMatrixRMaj)ret;
        return null;
    }
}
