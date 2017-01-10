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

package org.ejml.dense.row.misc;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.dense.row.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestTransposeAlgs_CR64 {

    Random rand = new Random(234);

    @Test
    public void square() {
        DMatrixRow_C64 a = RandomMatrices_CR64.createRandom(4,4,-1,1,rand);
        DMatrixRow_C64 b = a.copy();

        TransposeAlgs_CR64.square(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real, UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void squareConjugate() {
        DMatrixRow_C64 a = RandomMatrices_CR64.createRandom(4,4,-1,1,rand);
        DMatrixRow_C64 b = a.copy();

        TransposeAlgs_CR64.squareConjugate(b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void standard() {
        DMatrixRow_C64 a = RandomMatrices_CR64.createRandom(4,5,-1,1,rand);
        DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(5, 4, -1, 1, rand);

        TransposeAlgs_CR64.standard(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    public void standardConjugate() {
        DMatrixRow_C64 a = RandomMatrices_CR64.createRandom(4,5,-1,1,rand);
        DMatrixRow_C64 b = RandomMatrices_CR64.createRandom(5, 4, -1, 1, rand);

        TransposeAlgs_CR64.standardConjugate(a, b);

        Complex_F64 found = new Complex_F64();
        Complex_F64 expected = new Complex_F64();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,UtilEjml.TEST_F64);
                assertEquals(-expected.imaginary,found.imaginary,UtilEjml.TEST_F64);
            }
        }
    }
}