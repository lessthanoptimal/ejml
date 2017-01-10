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
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.dense.row.CommonOps_CR64;
import org.ejml.dense.row.MatrixFeatures_CR64;
import org.ejml.dense.row.RandomMatrices_CR64;
import org.ejml.ops.ComplexMath_F64;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMatrixMult_CR64 {

    @Test
    public void generalChecks() {

        int numChecked = 0;
        Method methods[] = MatrixMatrixMult_CR64.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if (!name.contains("mult"))
                continue;

//            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = double.class == params[0];
            boolean transA = name.contains("TransA");
            boolean transB = name.contains("TransB");
            if( name.contains("TransAB"))
                transA = transB = true;

            try {
//                System.out.println("add "+add+" alpha "+hasAlpha+" TA "+transA+" TB "+transB+"  "+name);
                check(method,add,hasAlpha,transA,transB);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            numChecked++;
        }

        assertEquals(28,numChecked);
    }

    public static void check( Method method , boolean isAdd , boolean hasAlpha,
                              boolean transA , boolean transB ) throws InvocationTargetException, IllegalAccessException {
        Random rand = new Random(234);

        double realAlpha = 2.3;
        double imgAlpha = 1.3;

        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 4; j++) {
                for (int k = 1; k <= 4; k++) {
                    DMatrixRow_C64 A = transA ? RandomMatrices_CR64.createRandom(j,i,-1,1,rand) :
                            RandomMatrices_CR64.createRandom(i,j,-1,1,rand);
                    DMatrixRow_C64 B = transB ? RandomMatrices_CR64.createRandom(k,j,-1,1,rand) :
                            RandomMatrices_CR64.createRandom(j,k,-1,1,rand);
                    DMatrixRow_C64 C = RandomMatrices_CR64.createRandom(i,k,-1,1,rand);

                    DMatrixRow_C64 AB = multiply(A,B,transA,transB);
                    DMatrixRow_C64 expected = new DMatrixRow_C64(i,k);

                    if( hasAlpha ) {
                        CommonOps_CR64.elementMultiply(AB,realAlpha,imgAlpha,AB);
                    }

                    if( isAdd ) {
                        CommonOps_CR64.add(C,AB,expected);
                    } else {
                        expected.set(AB);
                    }

                    invoke(method,realAlpha,imgAlpha,A,B,C);

                    assertTrue(i+" "+j+" "+k, MatrixFeatures_CR64.isEquals(expected,C, UtilEjml.TEST_F64));
                }
            }
        }
    }

    public static void invoke(Method func,
                              double realAlpha, double imgAlpha,
                              DMatrixRow_C64 a, DMatrixRow_C64 b, DMatrixRow_C64 c)
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

    public static DMatrixRow_C64 multiply(DMatrixRow_C64 A , DMatrixRow_C64 B, boolean transA, boolean transB ) {

        if( transA ) {
            DMatrixRow_C64 A_h = new DMatrixRow_C64(A.numCols, A.numRows);
            CommonOps_CR64.transposeConjugate(A,A_h);
            A = A_h;
        }
        if( transB ) {
            DMatrixRow_C64 B_h = new DMatrixRow_C64(B.numCols, B.numRows);
            CommonOps_CR64.transposeConjugate(B,B_h);
            B = B_h;
        }
        DMatrixRow_C64 C = new DMatrixRow_C64(A.numRows,B.numCols);

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();
        Complex_F64 m = new Complex_F64();

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                Complex_F64 sum = new Complex_F64();

                for (int k = 0; k < A.numCols; k++) {
                    A.get(i,k,a);
                    B.get(k,j,b);

                    ComplexMath_F64.multiply(a,b,m);
                    sum.real += m.real;
                    sum.imaginary += m.imaginary;
                }

                C.set(i,j,sum.real,sum.imaginary);
            }
        }

        return C;
    }
}