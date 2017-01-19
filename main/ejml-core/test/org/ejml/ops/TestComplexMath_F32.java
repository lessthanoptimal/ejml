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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.CComplex;
import org.ejml.data.CComplexPolar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMath_F32 {
    @Test
    public void conj() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);

        ComplexMathC.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_F32);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void plus() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex c = new CComplex();

        ComplexMathC.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_F32);
        assertEquals(9, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void minus() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex c = new CComplex();

        ComplexMathC.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_F32);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void multiply() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex c = new CComplex();

        ComplexMathC.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_F32);
        assertEquals(3, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void divide() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex c = new CComplex();

        ComplexMathC.divide(a, b, c);

        assertEquals(0.26666666666f, c.real, UtilEjml.TEST_F32);
        assertEquals(-0.466666666666f, c.imaginary, UtilEjml.TEST_F32);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        CComplex a = new CComplex(2, 3);
        CComplexPolar b = new CComplexPolar();
        CComplex c = new CComplex();

        ComplexMathC.convert(a, b);
        ComplexMathC.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_F32);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void mult_polar() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex expected = new CComplex();

        ComplexMathC.multiply(a, b, expected);

        CComplexPolar pa = new CComplexPolar(a);
        CComplexPolar pb = new CComplexPolar(b);
        CComplexPolar pc = new CComplexPolar();

        ComplexMathC.multiply(pa, pb, pc);

        CComplex found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void div_polar() {
        CComplex a = new CComplex(2, 3);
        CComplex b = new CComplex(-3, 6);
        CComplex expected = new CComplex();

        ComplexMathC.divide(a, b, expected);

        CComplexPolar pa = new CComplexPolar(a);
        CComplexPolar pb = new CComplexPolar(b);
        CComplexPolar pc = new CComplexPolar();

        ComplexMathC.divide(pa, pb, pc);

        CComplex found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void pow() {
        CComplexPolar a = new CComplexPolar(2, 0.2f);
        CComplexPolar expected = new CComplexPolar();
        CComplexPolar found = new CComplexPolar();

        ComplexMathC.multiply(a, a, expected);
        ComplexMathC.multiply(a, expected, expected);

        ComplexMathC.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_F32);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_F32);
    }

    @Test
    public void root_polar() {
        CComplexPolar expected = new CComplexPolar(2, 0.2f);
        CComplexPolar root = new CComplexPolar();
        CComplexPolar found = new CComplexPolar();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMathC.root(expected, 2, 0, root);

            ComplexMathC.multiply(root, root, found);

            CComplex e = expected.toStandard();
            CComplex f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_F32);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void root_standard() {
        CComplex expected = new CComplex(2, 0.2f);
        CComplex root = new CComplex();
        CComplex found = new CComplex();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMathC.root(expected, 2, 0, root);

            ComplexMathC.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void sqrt_standard() {
        CComplex input = new CComplex(2, 0.2f);
        CComplex root = new CComplex();
        CComplex found = new CComplex();

        ComplexMathC.sqrt(input, root);
        ComplexMathC.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F32);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F32);

        input = new CComplex(2, -0.2f);

        ComplexMathC.sqrt(input, root);
        ComplexMathC.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F32);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }
}
