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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.ComplexPolar_F32;
import org.ejml.data.Complex_F32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMath_F32 {
    @Test
    public void conj() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);

        ComplexMath_F32.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_F32);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void plus() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 c = new Complex_F32();

        ComplexMath_F32.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_F32);
        assertEquals(9, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void minus() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 c = new Complex_F32();

        ComplexMath_F32.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_F32);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void multiply() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 c = new Complex_F32();

        ComplexMath_F32.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_F32);
        assertEquals(3, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void divide() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 c = new Complex_F32();

        ComplexMath_F32.divide(a, b, c);

        assertEquals(0.26666666666f, c.real, UtilEjml.TEST_F32);
        assertEquals(-0.466666666666f, c.imaginary, UtilEjml.TEST_F32);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        Complex_F32 a = new Complex_F32(2, 3);
        ComplexPolar_F32 b = new ComplexPolar_F32();
        Complex_F32 c = new Complex_F32();

        ComplexMath_F32.convert(a, b);
        ComplexMath_F32.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_F32);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void mult_polar() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 expected = new Complex_F32();

        ComplexMath_F32.multiply(a, b, expected);

        ComplexPolar_F32 pa = new ComplexPolar_F32(a);
        ComplexPolar_F32 pb = new ComplexPolar_F32(b);
        ComplexPolar_F32 pc = new ComplexPolar_F32();

        ComplexMath_F32.multiply(pa, pb, pc);

        Complex_F32 found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void div_polar() {
        Complex_F32 a = new Complex_F32(2, 3);
        Complex_F32 b = new Complex_F32(-3, 6);
        Complex_F32 expected = new Complex_F32();

        ComplexMath_F32.divide(a, b, expected);

        ComplexPolar_F32 pa = new ComplexPolar_F32(a);
        ComplexPolar_F32 pb = new ComplexPolar_F32(b);
        ComplexPolar_F32 pc = new ComplexPolar_F32();

        ComplexMath_F32.divide(pa, pb, pc);

        Complex_F32 found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }

    @Test
    public void pow() {
        ComplexPolar_F32 a = new ComplexPolar_F32(2, 0.2f);
        ComplexPolar_F32 expected = new ComplexPolar_F32();
        ComplexPolar_F32 found = new ComplexPolar_F32();

        ComplexMath_F32.multiply(a, a, expected);
        ComplexMath_F32.multiply(a, expected, expected);

        ComplexMath_F32.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_F32);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_F32);
    }

    @Test
    public void root_polar() {
        ComplexPolar_F32 expected = new ComplexPolar_F32(2, 0.2f);
        ComplexPolar_F32 root = new ComplexPolar_F32();
        ComplexPolar_F32 found = new ComplexPolar_F32();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath_F32.root(expected, 2, 0, root);

            ComplexMath_F32.multiply(root, root, found);

            Complex_F32 e = expected.toStandard();
            Complex_F32 f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_F32);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void root_standard() {
        Complex_F32 expected = new Complex_F32(2, 0.2f);
        Complex_F32 root = new Complex_F32();
        Complex_F32 found = new Complex_F32();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath_F32.root(expected, 2, 0, root);

            ComplexMath_F32.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_F32);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F32);
        }
    }

    @Test
    public void sqrt_standard() {
        Complex_F32 input = new Complex_F32(2, 0.2f);
        Complex_F32 root = new Complex_F32();
        Complex_F32 found = new Complex_F32();

        ComplexMath_F32.sqrt(input, root);
        ComplexMath_F32.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F32);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F32);

        input = new Complex_F32(2, -0.2f);

        ComplexMath_F32.sqrt(input, root);
        ComplexMath_F32.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F32);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F32);
    }
}
