/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs_ZDRM {

    Random rand = new Random(234);

    @Test
    public void square() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 4, -1, 1, rand);
        ZMatrixRMaj b = a.copy();

        TransposeAlgs_ZDRM.square(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j, i, expected);
                b.get(i, j, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void squareConjugate() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 4, -1, 1, rand);
        ZMatrixRMaj b = a.copy();

        TransposeAlgs_ZDRM.squareConjugate(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j, i, expected);
                b.get(i, j, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void standard() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 5, -1, 1, rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(5, 4, -1, 1, rand);

        TransposeAlgs_ZDRM.standard(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void standardConjugate() {
        ZMatrixRMaj a = RandomMatrices_ZDRM.rectangle(4, 5, -1, 1, rand);
        ZMatrixRMaj b = RandomMatrices_ZDRM.rectangle(5, 4, -1, 1, rand);

        TransposeAlgs_ZDRM.standardConjugate(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i, j, expected);
                b.get(j, i, found);

                assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
            }
        }
    }
}