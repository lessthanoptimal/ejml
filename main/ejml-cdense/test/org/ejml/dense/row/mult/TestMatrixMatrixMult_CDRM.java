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
import org.ejml.data.Complex_F32;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.ops.ComplexMath_F32;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestMatrixMatrixMult_CDRM {

    @Test
    public void generalChecks() {

        int numChecked = 0;
        Method methods[] = MatrixMatrixMult_CDRM.class.getMethods();

        for( Method method : methods ) {
            String name = method.getName();

            // only look at function which perform matrix multiplications
            if (!name.contains("mult"))
                continue;

//            System.out.println(name);

            Class[] params = method.getParameterTypes();

            boolean add = name.contains("Add");
            boolean hasAlpha = float.class == params[0];
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

        float realAlpha = 2.3f;
        float imgAlpha = 1.3f;

        for (int i = 1; i <= 4; i++) {
            for (int j = 1; j <= 4; j++) {
                for (int k = 1; k <= 4; k++) {
                    CMatrixRMaj A = transA ? RandomMatrices_CDRM.rectangle(j,i,-1,1,rand) :
                            RandomMatrices_CDRM.rectangle(i,j,-1,1,rand);
                    CMatrixRMaj B = transB ? RandomMatrices_CDRM.rectangle(k,j,-1,1,rand) :
                            RandomMatrices_CDRM.rectangle(j,k,-1,1,rand);
                    CMatrixRMaj C = RandomMatrices_CDRM.rectangle(i,k,-1,1,rand);

                    CMatrixRMaj AB = multiply(A,B,transA,transB);
                    CMatrixRMaj expected = new CMatrixRMaj(i,k);

                    if( hasAlpha ) {
                        CommonOps_CDRM.elementMultiply(AB,realAlpha,imgAlpha,AB);
                    }

                    if( isAdd ) {
                        CommonOps_CDRM.add(C,AB,expected);
                    } else {
                        expected.set(AB);
                    }

                    invoke(method,realAlpha,imgAlpha,A,B,C);

                    assertTrue(MatrixFeatures_CDRM.isEquals(expected,C, UtilEjml.TEST_F32),i+" "+j+" "+k);
                }
            }
        }
    }

    public static void invoke(Method func,
                              float realAlpha, float imgAlpha,
                              CMatrixRMaj a, CMatrixRMaj b, CMatrixRMaj c)
            throws IllegalAccessException, InvocationTargetException {
        if( func.getParameterTypes().length == 3 ) {
            func.invoke(null, a, b, c);
        } else {
            if( func.getParameterTypes()[0] == float.class ) {
                if( func.getParameterTypes().length == 5 )
                    func.invoke(null,realAlpha, imgAlpha, a, b, c);
                else
                    func.invoke(null,realAlpha, imgAlpha, a, b, c,null);
            } else {
                func.invoke(null, a, b, c,null);
            }
        }
    }

    public static CMatrixRMaj multiply(CMatrixRMaj A , CMatrixRMaj B, boolean transA, boolean transB ) {

        if( transA ) {
            CMatrixRMaj A_h = new CMatrixRMaj(A.numCols, A.numRows);
            CommonOps_CDRM.transposeConjugate(A,A_h);
            A = A_h;
        }
        if( transB ) {
            CMatrixRMaj B_h = new CMatrixRMaj(B.numCols, B.numRows);
            CommonOps_CDRM.transposeConjugate(B,B_h);
            B = B_h;
        }
        CMatrixRMaj C = new CMatrixRMaj(A.numRows,B.numCols);

        Complex_F32 a = new Complex_F32();
        Complex_F32 b = new Complex_F32();
        Complex_F32 m = new Complex_F32();

        for (int i = 0; i < A.numRows; i++) {
            for (int j = 0; j < B.numCols; j++) {
                Complex_F32 sum = new Complex_F32();

                for (int k = 0; k < A.numCols; k++) {
                    A.get(i,k,a);
                    B.get(k,j,b);

                    ComplexMath_F32.multiply(a,b,m);
                    sum.real += m.real;
                    sum.imaginary += m.imaginary;
                }

                C.set(i,j,sum.real,sum.imaginary);
            }
        }

        return C;
    }
}