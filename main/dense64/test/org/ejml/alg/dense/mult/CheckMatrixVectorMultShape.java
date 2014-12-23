/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixDimensionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Checks to see if the input to a matrix vector mutiply is accepted or rejected correctly depending
 * on the shape in the input matrices.  Java reflections is used to grab all functions
 * with "mult" in its name and then it determins if any of matrices are transposed.
 *
 * @author Peter Abeles
 */
public class CheckMatrixVectorMultShape {

    Class theClass;

    public CheckMatrixVectorMultShape( Class theClass ) {
        this.theClass = theClass;
    }

    /**
     * Perform all shape input checks.
     *
     * @throws Throwable
     */
    public void checkAll()
    {
        int numChecked = 0;
        Method methods[] = theClass.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplcation
            if( !name.contains("mult"))
                continue;

            boolean transA = false;

            if( name.contains("TransA")) {
                transA = true;
            }

            try {
                checkPositive(method, transA);
                checkNegative(method,transA);
            } catch( Throwable e ) {
                System.out.println("Failed on function: "+name);
                e.printStackTrace();
                fail("An exception was thrown");
            }
            numChecked++;
        }

        // make sure some functions were checked!
        assertTrue(numChecked!=0);
    }

    /**
     * See if the function can be called with matrices of the correct size
     */
    private void checkPositive(Method func, boolean transA) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        DenseMatrix64F A,B;
        DenseMatrix64F C = new DenseMatrix64F(2,1);

        if( transA ) {
            A = new DenseMatrix64F(4,2);
        } else {
            A = new DenseMatrix64F(2,4);
        }

        // should work for B as a column or row vector
        B = new DenseMatrix64F(4,1);
        func.invoke(null,A,B,C);
        B = new DenseMatrix64F(1,4);
        func.invoke(null,A,B,C);
    }

    /**
     * See if the function throws an exception when it is given bad inputs
     */
    private void checkNegative(Method func, boolean transA) throws NoSuchMethodException, IllegalAccessException {
        DenseMatrix64F A,B;
        DenseMatrix64F C = new DenseMatrix64F(2,1);

        if( transA ) {
            A = new DenseMatrix64F(2,4);
        } else {
            A = new DenseMatrix64F(4,2);
        }

        // see if it catched B not being a vector
        B = new DenseMatrix64F(4,2);
        invokeExpectFail(func, A, B, C);
        // B is not compatible with A
        B = new DenseMatrix64F(3,1);
        invokeExpectFail(func, A, B, C);
        // C is a row vector
        B = new DenseMatrix64F(4,1);
        C = new DenseMatrix64F(1,2);
        invokeExpectFail(func, A, B, C);
        // C is not a vector
        C = new DenseMatrix64F(2,2);
        invokeExpectFail(func, A, B, C);
        // C is not compatible with A
        C = new DenseMatrix64F(3,1);
        invokeExpectFail(func, A, B, C);

    }

    private void invokeExpectFail(Method func, DenseMatrix64F a, DenseMatrix64F b, DenseMatrix64F c) throws IllegalAccessException {
        try {
            func.invoke(null, b, a, c);
        } catch( InvocationTargetException e ) {
            assertTrue(e.getCause().getClass() == MatrixDimensionException.class );
        }
    }

}