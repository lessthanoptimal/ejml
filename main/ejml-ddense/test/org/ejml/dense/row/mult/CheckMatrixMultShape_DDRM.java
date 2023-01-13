/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.MatrixDimensionException;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks to see if the input to a matrix mutiply is accepted or rejected correctly depending
 * on the shape in the input matrices. Java reflections is used to grab all functions
 * with "mult" in its name and then it determins if any of matrices are transposed.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("rawtypes")
public class CheckMatrixMultShape_DDRM extends EjmlStandardJUnit {

    // TODO merge with CheckMatrixMultShape_DSCC - Need to mess with project dependencies to do that

    Class theClass;

    public CheckMatrixMultShape_DDRM( Class theClass ) {
        this.theClass = theClass;
    }

    /**
     * Perform all shape input checks.
     */
    public void checkAll() {
        int numChecked = 0;
        Method[] methods = theClass.getMethods();

        for (Method method : methods) {
            String name = method.getName();

            // only look at function which perform matrix multiplcation
            if (!name.contains("mult") || name.contains("Element") ||
                    name.contains("Inner") || name.contains("Outer"))
                continue;
            if (name.equals("multRows") || name.equals("multCols"))
                continue;

            boolean transA = false;
            boolean transB = false;

            if (name.contains("TransAB")) {
                transA = true;
                transB = true;
            } else if (name.contains("TransA")) {
                transA = true;
            } else if (name.contains("TransB")) {
                transB = true;
            }

            try {
                checkPositive(method, transA, transB);
                checkNegative(method, transA, transB);
            } catch (Throwable e) {
                System.out.println("Failed on " + name);
                e.printStackTrace();
                fail("An exception was thrown");
            }
            numChecked++;
        }

        // make sure some functions were checked!
        assertTrue(numChecked != 0);
    }

    /**
     * Iterate through a variety of different sizes and shapes of matrices.
     */
    private void checkPositive( Method func, boolean transA, boolean transB )
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 4; j++) {
                for (int k = 1; k <= 4; k++) {
                    checkPositive(func, transA, transB, i, j, k);
                }
            }
        }
    }

    /**
     * See if the function can be called with matrices of the correct size
     */
    private void checkPositive( Method func, boolean transA, boolean transB,
                                int m, int n, int o ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        DMatrixRMaj A, B;
        DMatrixRMaj C = new DMatrixRMaj(m, o);

        if (transA) {
            A = new DMatrixRMaj(n, m);
        } else {
            A = new DMatrixRMaj(m, n);
        }
        if (transB) {
            B = new DMatrixRMaj(o, n);
        } else {
            B = new DMatrixRMaj(n, o);
        }

        TestMatrixMatrixMult_DDRM.invoke(func, 2.0, A, B, C);

        if (UtilEjml.hasNullableArgument(func)) {
            DMatrixRMaj ret = TestMatrixMatrixMult_DDRM.invoke(func, 2.0, A, B, null);
            assertNotNull(ret);
            assertEquals(ret.numRows, C.numRows);
            assertEquals(ret.numCols, C.numCols);
        }
    }

    /**
     * See if the function throws an exception when it is given bad inputs
     */
    private void checkNegative( Method func, boolean transA, boolean transB ) throws IllegalAccessException {

        // don't reshape if it adds since C is also an input
        boolean reshape = !func.getName().contains("Add");

//        System.out.println("func = "+func);
        // correct = 2,4,4,3,2,3
        //           i,j,j,k,i,k

        // mis matched i
        if (reshape)
            checkReshapeC(func, 2, 4, 4, 3, 6, 3, transA, transB);
        else
            checkNegative(func, 2, 4, 5, 3, 2, 3, transA, transB);
        // missmatched j
        checkNegative(func, 2, 4, 5, 3, 2, 3, transA, transB);
        // miss matched k
        if (reshape)
            checkReshapeC(func, 2, 4, 4, 7, 2, 3, transA, transB);
        else
            checkNegative(func, 2, 4, 4, 7, 2, 3, transA, transB);
    }

    /**
     * See if the function throws an exception when it is given bad inputs
     */
    private void checkNegative( Method func,
                                int m_a, int n_a, int m_b, int n_b, int m_c, int n_c,
                                boolean transA, boolean transB ) throws IllegalAccessException {
        DMatrixRMaj A, B;
        DMatrixRMaj C = new DMatrixRMaj(m_c, n_c);

        if (transA) {
            A = new DMatrixRMaj(n_a, m_a);
        } else {
            A = new DMatrixRMaj(m_a, n_a);
        }
        if (transB) {
            B = new DMatrixRMaj(n_b, m_b);
        } else {
            B = new DMatrixRMaj(m_b, n_b);
        }

        try {
            TestMatrixMatrixMult_DDRM.invoke(func, 2.0, A, B, C);
            fail("An exception should have been thrown.");
        } catch (InvocationTargetException e) {
            assertSame(e.getCause().getClass(), MatrixDimensionException.class);
        }
    }

    /**
     * The C matrix will have the incorrect size, see if it's reshaped correctly
     */
    private void checkReshapeC( Method func,
                                int m_a, int n_a, int m_b, int n_b, int m_c, int n_c,
                                boolean transA, boolean transB ) throws IllegalAccessException {
        DMatrixRMaj A, B;
        DMatrixRMaj C = new DMatrixRMaj(m_c, n_c);

        if (transA) {
            A = new DMatrixRMaj(n_a, m_a);
        } else {
            A = new DMatrixRMaj(m_a, n_a);
        }
        if (transB) {
            B = new DMatrixRMaj(n_b, m_b);
        } else {
            B = new DMatrixRMaj(m_b, n_b);
        }

        try {
            TestMatrixMatrixMult_DDRM.invoke(func, 2.0, A, B, C);
            assertEquals(m_a, C.numRows);
            assertEquals(n_b, C.numCols);
        } catch (InvocationTargetException e) {
            fail("there should be no exception!");
        }
    }
}