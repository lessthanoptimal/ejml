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

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.ComplexMath64F;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCMatrixMatrixMult {

    @Test
    public void generalChecks() {

        int numChecked = 0;
        Method methods[] = CMatrixMatrixMult.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if (!name.contains("mult"))
                continue;

//            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = double.class == params[0];

            try {
                check(method,add,hasAlpha);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }

        assertEquals(8,numChecked);
    }

    public static void check( Method method , boolean isAdd , boolean hasAlpha ) throws InvocationTargetException, IllegalAccessException {
        Random rand = new Random(234);

        double realAlpha = 2.3;
        double imgAlpha = 1.3;

        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 4; j++) {
                for (int k = 1; k <= 4; k++) {
                    CDenseMatrix64F A = CRandomMatrices.createRandom(i,j,-1,1,rand);
                    CDenseMatrix64F B = CRandomMatrices.createRandom(j,k,-1,1,rand);
                    CDenseMatrix64F C = CRandomMatrices.createRandom(i,k,-1,1,rand);

                    CDenseMatrix64F AB = multiply(A,B);
                    CDenseMatrix64F expected = new CDenseMatrix64F(i,k);

                    if( hasAlpha ) {
                        CCommonOps.elementMultiply(AB,realAlpha,imgAlpha,AB);
                    }

                    if( isAdd ) {
                        CCommonOps.add(C,AB,expected);
                    } else {
                        expected.set(AB);
                    }

                    invoke(method,realAlpha,imgAlpha,A,B,C);

                    assertTrue(i+" "+j+" "+k,CMatrixFeatures.isEquals(expected,C,1e-8));
                }
            }
        }
    }

    public static void invoke(Method func,
                              double realAlpha, double imgAlpha,
                              CDenseMatrix64F a, CDenseMatrix64F b, CDenseMatrix64F c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            if( func.getParameterTypes()[0] == double.class ) {
                if( func.getParameterTypes().length == 5 )
                    func.invoke(null,realAlpha, imgAlpha, a, b, c);
                else
                    func.invoke(null,realAlpha, imgAlpha, a, b, c,null);
            } else {
                func.invoke(null, a, b, c,null);
            }
        }
    }

    public static CDenseMatrix64F multiply( CDenseMatrix64F A , CDenseMatrix64F B ) {
        CDenseMatrix64F C = new CDenseMatrix64F(A.numRows,B.numCols);

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        Complex64F m = new Complex64F();

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                Complex64F sum = new Complex64F();

                for (int k = 0; k < A.numCols; k++) {
                    A.get(i,k,a);
                    B.get(k,j,b);

                    ComplexMath64F.multiply(a,b,m);
                    sum.real += m.real;
                    sum.imaginary += m.imaginary;
                }

                C.set(i,j,sum.real,sum.imaginary);
            }
        }

        return C;
    }
}