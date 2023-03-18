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

import org.ejml.CheckMultiThreadAgainstSingleThread;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.ejml.dense.row.mult.TestMatrixMatrixMult_DDRM.invoke;

public class TestMatrixMatrixMult_MT_DDRM extends CheckMultiThreadAgainstSingleThread {
    public TestMatrixMatrixMult_MT_DDRM() {
        super(MatrixMatrixMult_DDRM.class, MatrixMatrixMult_MT_DDRM.class, 24);
    }

    /**
     * This test focuses on if the shape of non-square matrices is handled correctly. Standard tests use square matrices
     * since there are fewer edge case and randomize the size.
     */
    @Test void checkNonSquare() {
        Method[] methods = MatrixMatrixMult_MT_DDRM.class.getMethods();

        int sideA = 100;
        int sideB = 3;
        int sideC = 2;

        DMatrixRMaj a = RandomMatrices_DDRM.rectangle(sideA, sideB, rand);
        DMatrixRMaj b = RandomMatrices_DDRM.rectangle(sideB, sideC, rand);
        DMatrixRMaj c = new DMatrixRMaj(sideA, sideC);

        DMatrixRMaj expected = CommonOps_DDRM.mult(a, b, null);

        for (Method method : methods) {
            String name = method.getName();

            if (!name.contains("mult"))
                continue;

            // make sure it checks that the c matrix is not a or b
            try {
                a.reshape(sideA, sideB);
                b.reshape(sideB, sideC);
                if (name.contains("TransAB")) {
                    a.reshape(sideB, sideA);
                    b.reshape(sideC, sideB);
                } else if (name.contains("TransA")) {
                    a.reshape(sideB, sideA);
                } else if (name.contains("TransB")) {
                    b.reshape(sideC, sideB);
                }
                // Fill with non zero value to make sure add is being tested
                CommonOps_DDRM.fill(c, 1);
                invoke(method, 2.0, a, b, c);

                // Compare to the non-threaded code
                CommonOps_DDRM.fill(expected, 1);
                Method testMethod = MatrixMatrixMult_DDRM.class.getMethod(name, method.getParameterTypes());
                invoke(testMethod, 2.0, a, b, expected);

                EjmlUnitTests.assertEquals(expected, c, UtilEjml.TEST_F64);
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
