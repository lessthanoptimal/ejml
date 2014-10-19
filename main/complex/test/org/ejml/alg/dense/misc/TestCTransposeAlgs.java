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

package org.ejml.alg.dense.misc;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCTransposeAlgs {

    Random rand = new Random(234);

    @Test
    public void square() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CTransposeAlgs.square(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void squareConjugate() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(4,4,-1,1,rand);
        CDenseMatrix64F b = a.copy();

        CTransposeAlgs.squareConjugate(b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a.get(j,i,expected);
                b.get(i,j,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(-expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void standard() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(5, 4, -1, 1, rand);

        CTransposeAlgs.standard(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(expected.imaginary,found.imaginary,1e-8);
            }
        }
    }

    @Test
    public void standardConjugate() {
        CDenseMatrix64F a = CRandomMatrices.createRandom(4,5,-1,1,rand);
        CDenseMatrix64F b = CRandomMatrices.createRandom(5, 4, -1, 1, rand);

        CTransposeAlgs.standardConjugate(a, b);

        Complex64F found = new Complex64F();
        Complex64F expected = new Complex64F();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                a.get(i,j,expected);
                b.get(j,i,found);

                assertEquals(expected.real,found.real,1e-8);
                assertEquals(-expected.imaginary,found.imaginary,1e-8);
            }
        }
    }
}