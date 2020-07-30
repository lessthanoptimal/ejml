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

package org.ejml.sparse.csc.mult;

import org.ejml.MatrixDimensionException;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.ejml.UtilEjml.hasNullableArgument;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Checks to see if the input to a matrix mutiply is accepted or rejected correctly depending
 * on the shape in the input matrices.  Java reflections is used to grab all functions
 * with "mult" in its name and then it determins if any of matrices are transposed.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("rawtypes")
public class CheckMatrixMultShape_DSCC {

    // TODO merge with CheckMatrixMultShape_DDRM - Need to mess with project dependencies to do that

    Class theClass;

    public CheckMatrixMultShape_DSCC(Class theClass ) {
        this.theClass = theClass;
    }

    /**
     * Perform all shape input checks.
     */
    public void checkAll()
    {
        int numChecked = 0;
        Method[] methods = theClass.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplcation
            if( !name.contains("mult") || name.contains("Element") ||
                    name.contains("Inner") || name.contains("Outer") )
                continue;
            if( name.equals("multRows") || name.equals("multCols") || name.equals("multColumns") || name.equals("multRowsCols"))
                continue;

            boolean transA = false;
            boolean transB = false;

            if( name.contains("TransAB")) {
                transA = true;
                transB = true;
            } else if( name.contains("TransA")) {
                transA = true;
            } else if( name.contains("TransB")) {
                transB = true;
            }

            try {
                checkPositive(method, transA, transB);
                checkNegative(method, transA, transB);
            } catch( Throwable e ) {
                System.out.println("Failed on "+name);
                e.printStackTrace();
                fail("An exception was thrown");
            }
            numChecked++;
        }

        // make sure some functions were checked!
        assertTrue(numChecked!=0);
    }

    /**
     * Iterate through a variety of different sizes and shapes of matrices.
     */
    private void checkPositive( Method func, boolean transA, boolean transB )
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for( int i = 1; i <= 4; i++ ) {
            for( int j = 1; j <= 4; j++ ) {
                for( int k = 1; k <= 4; k++ ) {
                    checkPositive(func,transA,transB,i,j,k);
                }
            }
        }
    }

    /**
     * See if the function can be called with matrices of the correct size
     */
    private void checkPositive(Method func, boolean transA, boolean transB ,
                               int m , int n , int o ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        DMatrix A,B;
        DMatrix C = createMatrix(getMatrixType(func,2),m,o);

        if( transA ) {
            A = createMatrix(getMatrixType(func,0),n,m);
        } else {
            A = createMatrix(getMatrixType(func,0),m,n);
        }
        if( transB ) {
            B = createMatrix(getMatrixType(func,1),o,n);
        } else {
            B = createMatrix(getMatrixType(func,1),n,o);
        }

        invoke(func, 2.0, A, B, C);

        if( hasNullableArgument(func) ) {
            DMatrix ret = invoke(func, 2.0, A, B, null);
            assertNotNull(ret);
            assertEquals(ret.getNumRows(),C.getNumRows());
            assertEquals(ret.getNumCols(),C.getNumCols());
        }
    }

    /**
     * See if the function throws an exception when it is given bad inputs
     */
    private void checkNegative(Method func, boolean transA, boolean transB) throws NoSuchMethodException, IllegalAccessException {
        // don't reshape if it adds since C is also an input
        boolean reshape = !func.getName().contains("Add");

//        System.out.println("func = "+func);
        // correct = 2,4,4,3,2,3
        //           i,j,j,k,i,k

        // mis matched i
        if( reshape )
            checkReshapeC(func,2,4,4,3,6,3,transA,transB);
        else
            checkNegative(func,2,4,5,3,2,3,transA,transB);
        // missmatched j
        checkNegative(func,2,4,5,3,2,3,transA,transB);
        // miss matched k
        if( reshape )
            checkReshapeC(func,2,4,4,7,2,3,transA,transB);
        else
            checkNegative(func,2,4,4,7,2,3,transA,transB);
    }

    /**
     * See if the function throws an exception when it is given bad inputs
     */
    private void checkNegative(Method func,
                               int m_a , int n_a , int m_b , int n_b , int m_c , int n_c ,
                               boolean transA, boolean transB) throws IllegalAccessException {
        DMatrix A,B;
        DMatrix C = createMatrix(getMatrixType(func,2),m_c,n_c);

        if( transA ) {
            A = createMatrix(getMatrixType(func,0),n_a,m_a);
        } else {
            A = createMatrix(getMatrixType(func,0),m_a,n_a);
        }
        if( transB ) {
            B = createMatrix(getMatrixType(func,1),n_b,m_b);
        } else {
            B = createMatrix(getMatrixType(func,1),m_b,n_b);
        }
        try {
            invoke(func, 2.0, A, B, C);
            fail("An exception should have been thrown. name="+func.getName());
        } catch( InvocationTargetException e ) {
            assertSame(e.getCause().getClass(), MatrixDimensionException.class);
        }
    }

    /**
     * The C matrix will have the incorrect size, see if it's reshaped correctly
     */
    private void checkReshapeC(Method func,
                               int m_a , int n_a , int m_b , int n_b , int m_c , int n_c ,
                               boolean transA, boolean transB) throws IllegalAccessException {
        DMatrix A,B;
        DMatrix C = createMatrix(getMatrixType(func,2),m_c,n_c);

        if( transA ) {
            A = createMatrix(getMatrixType(func,0),n_a,m_a);
        } else {
            A = createMatrix(getMatrixType(func,0),m_a,n_a);
        }
        if( transB ) {
            B = createMatrix(getMatrixType(func,1),n_b,m_b);
        } else {
            B = createMatrix(getMatrixType(func,1),m_b,n_b);
        }

        try {
            invoke(func, 2.0, A, B, C);
            assertEquals(m_a,C.getNumRows());
            assertEquals(n_b,C.getNumCols());
        } catch( InvocationTargetException e ) {
            e.printStackTrace();
            fail("there should be no exception! ("+A.getNumRows()+"x"+A.getNumCols()+") ("+B.getNumRows()+"x"+B.getNumCols()+")");
        }
    }

    public static Class getMatrixType( Method func , int index ) {
        Class<?>[] parameters = func.getParameterTypes();
        if( parameters[0] == double.class ) {
            return parameters[index+1];
        } else {
            return parameters[index];
        }
    }

    public static DMatrix invoke(Method func,
                                 double alpha,
                                 DMatrix a, DMatrix b, DMatrix c)
            throws IllegalAccessException, InvocationTargetException {

        Object[] arguments = new Object[func.getParameterTypes().length];
        Object ret;
        if( func.getParameterTypes().length == 3 ) {
            arguments[0] = a;
            arguments[1] = b;
            arguments[2] = c;
        } else {
            if( func.getParameterTypes()[0] == double.class ) {
                arguments[0] = alpha;
                arguments[1] = a;
                arguments[2] = b;
                arguments[3] = c;
            } else {
                arguments[0] = a;
                arguments[1] = b;
                arguments[2] = c;
            }
        }

        try {
            ret = func.invoke(null, arguments);
        } catch( IllegalArgumentException e ) {
            for( Class pc : func.getParameterTypes() ) {
                System.out.println(pc.getName());
            }
            throw e;
        }

        if( ret instanceof DMatrix )
            return (DMatrix)ret;
        return null;
    }

    public static DMatrix createMatrix( Class type , int rows , int cols ) {
        if( type == DMatrixSparseCSC.class )
            return new DMatrixSparseCSC(rows,cols);
        else if( type == DMatrixRMaj.class )
            return new DMatrixRMaj(rows,cols);
        throw new RuntimeException("Unknown matrix type: "+type.getSimpleName());
    }
}