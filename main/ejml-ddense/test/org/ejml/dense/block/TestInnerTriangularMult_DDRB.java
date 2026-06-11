/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestInnerTriangularMult_DDRB extends EjmlStandardJUnit {
    // Automatic unit test which reads in all methods and parses their operation
    @Test void all() {
        Method[] methods = InnerTriangularMult_DDRB.class.getMethods();

        int count = 0;
        for (Method m : methods) {
            if (!Modifier.isStatic(m.getModifiers()))
                continue;
            String name = m.getName();

            boolean leftMult = name.startsWith("lmult");
            boolean add = name.contains("Add");
            boolean unitDiag = name.contains("Unit");
            boolean lower = name.contains("Low");
            boolean tranT = name.contains("TransT");

            trmm(m, leftMult, add, unitDiag, lower, tranT);
            count++;
        }

        assertEquals(11, count);
    }

    private void trmm( Method method,
                       boolean leftMult, boolean add, boolean unitDiag, boolean lower,
                       boolean tranT ) {

        int offsetT = 3;
        int offsetB = 4;
        int offsetC = 5;

        // t is the square m-by-m triangle, n is the other dimension; sweep both small, dense and strided.
        for (int m = 1; m <= 6; m++) {
            for (int n = 1; n <= 6; n++) {
                for (int strided = 0; strided <= 1; strided++) {
                    int padT = strided == 0 ? 0 : 1;
                    int padB = strided == 0 ? 0 : 2;
                    int padC = strided == 0 ? 0 : 3;

                    // b is m by n for lmult, n by m for rmult
                    int rowsB = leftMult ? m : n;
                    int colsB = leftMult ? n : m;
                    int rowsC = leftMult ? m : n;
                    int colsC = leftMult ? n : m;

                    // Random matrices with implicit zeros and ones not zero or one
                    DMatrixRMaj T = RandomMatrices_DDRM.rectangle(m, m, -1, 1, rand);
                    DMatrixRMaj B = RandomMatrices_DDRM.rectangle(rowsB, colsB, -1, 1, rand);
                    DMatrixRMaj C = RandomMatrices_DDRM.rectangle(rowsC, colsC, -1, 1, rand);

                    // Lay each matrix into an array with an offset and a row stride wider than its width,
                    // junk-filling the gaps so a wrong stride reads/writes the wrong slot and fails.
                    int strideT = m + padT;
                    int strideB = colsB + padB;
                    int strideC = colsC + padC;
                    double[] arrayT = embed(T, strideT, offsetT);
                    double[] arrayB = embed(B, strideB, offsetB);
                    double[] arrayC = embed(C, strideC, offsetC);

                    // Make the ground truth matrices match the implicit structure of the op
                    SpecializedOps_DDRM.fillTriangle(T, lower, 0, 0.0);
                    if (unitDiag) {
                        for (int i = 0; i < m; i++) {
                            T.set(i, i, 1.0);
                        }
                    }
                    if (tranT) {
                        CommonOps_DDRM.transpose(T);
                    }

                    if (leftMult) {
                        if (add)
                            CommonOps_DDRM.multAdd(T, B, C);
                        else
                            CommonOps_DDRM.mult(T, B, C);
                    } else {
                        if (add)
                            CommonOps_DDRM.multAdd(B, T, C);
                        else
                            CommonOps_DDRM.mult(B, T, C);
                    }

                    try {
                        method.invoke(null, arrayT, arrayB, arrayC, m, n,
                                strideT, strideB, strideC, offsetT, offsetB, offsetC);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }

                    for (int row = 0; row < C.getNumRows(); row++) {
                        for (int col = 0; col < C.getNumCols(); col++) {
                            assertEquals(C.get(row, col), arrayC[offsetC + row*strideC + col], UtilEjml.TESTP_F64);
                        }
                    }
                }
            }
        }
    }

    /// Copies a dense matrix into a freshly allocated array at the given row stride and offset, filling
    /// every other slot (the offset prefix and the stride gaps) with NaN so any stray access fails.
    private static double[] embed( DMatrixRMaj M, int stride, int offset ) {
        double[] array = new double[offset + M.numRows*stride];
        java.util.Arrays.fill(array, Double.NaN);
        for (int row = 0; row < M.numRows; row++) {
            for (int col = 0; col < M.numCols; col++) {
                array[offset + row*stride + col] = M.get(row, col);
            }
        }
        return array;
    }
}