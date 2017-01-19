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
import org.ejml.data.ZComplex;
import org.ejml.data.ZComplexPolar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMathZ {
    @Test
    public void conj() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);

        ComplexMathZ.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_F64);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void plus() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex c = new ZComplex();

        ComplexMathZ.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_F64);
        assertEquals(9, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void minus() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex c = new ZComplex();

        ComplexMathZ.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_F64);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void multiply() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex c = new ZComplex();

        ComplexMathZ.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_F64);
        assertEquals(3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void divide() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex c = new ZComplex();

        ComplexMathZ.divide(a, b, c);

        assertEquals(0.26666666666, c.real, UtilEjml.TEST_F64);
        assertEquals(-0.466666666666, c.imaginary, UtilEjml.TEST_F64);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        ZComplex a = new ZComplex(2, 3);
        ZComplexPolar b = new ZComplexPolar();
        ZComplex c = new ZComplex();

        ComplexMathZ.convert(a, b);
        ComplexMathZ.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_F64);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void mult_polar() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex expected = new ZComplex();

        ComplexMathZ.multiply(a, b, expected);

        ZComplexPolar pa = new ZComplexPolar(a);
        ZComplexPolar pb = new ZComplexPolar(b);
        ZComplexPolar pc = new ZComplexPolar();

        ComplexMathZ.multiply(pa, pb, pc);

        ZComplex found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void div_polar() {
        ZComplex a = new ZComplex(2, 3);
        ZComplex b = new ZComplex(-3, 6);
        ZComplex expected = new ZComplex();

        ComplexMathZ.divide(a, b, expected);

        ZComplexPolar pa = new ZComplexPolar(a);
        ZComplexPolar pb = new ZComplexPolar(b);
        ZComplexPolar pc = new ZComplexPolar();

        ComplexMathZ.divide(pa, pb, pc);

        ZComplex found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void pow() {
        ZComplexPolar a = new ZComplexPolar(2, 0.2);
        ZComplexPolar expected = new ZComplexPolar();
        ZComplexPolar found = new ZComplexPolar();

        ComplexMathZ.multiply(a, a, expected);
        ComplexMathZ.multiply(a, expected, expected);

        ComplexMathZ.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_F64);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_F64);
    }

    @Test
    public void root_polar() {
        ZComplexPolar expected = new ZComplexPolar(2, 0.2);
        ZComplexPolar root = new ZComplexPolar();
        ZComplexPolar found = new ZComplexPolar();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMathZ.root(expected, 2, 0, root);

            ComplexMathZ.multiply(root, root, found);

            ZComplex e = expected.toStandard();
            ZComplex f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_F64);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void root_standard() {
        ZComplex expected = new ZComplex(2, 0.2);
        ZComplex root = new ZComplex();
        ZComplex found = new ZComplex();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMathZ.root(expected, 2, 0, root);

            ComplexMathZ.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void sqrt_standard() {
        ZComplex input = new ZComplex(2, 0.2);
        ZComplex root = new ZComplex();
        ZComplex found = new ZComplex();

        ComplexMathZ.sqrt(input, root);
        ComplexMathZ.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F64);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F64);

        input = new ZComplex(2, -0.2);

        ComplexMathZ.sqrt(input, root);
        ComplexMathZ.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F64);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }
}
