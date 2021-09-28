/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.ComplexPolar_F64;
import org.ejml.data.Complex_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMath_F64 extends EjmlStandardJUnit {
    @Test
    public void conj() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);

        ComplexMath_F64.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_F64);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void plus() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 c = new Complex_F64();

        ComplexMath_F64.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_F64);
        assertEquals(9, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void minus() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 c = new Complex_F64();

        ComplexMath_F64.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_F64);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void multiply() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 c = new Complex_F64();

        ComplexMath_F64.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_F64);
        assertEquals(3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void divide() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 c = new Complex_F64();

        ComplexMath_F64.divide(a, b, c);

        assertEquals(0.26666666666, c.real, UtilEjml.TEST_F64);
        assertEquals(-0.466666666666, c.imaginary, UtilEjml.TEST_F64);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        Complex_F64 a = new Complex_F64(2, 3);
        ComplexPolar_F64 b = new ComplexPolar_F64();
        Complex_F64 c = new Complex_F64();

        ComplexMath_F64.convert(a, b);
        ComplexMath_F64.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_F64);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void mult_polar() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 expected = new Complex_F64();

        ComplexMath_F64.multiply(a, b, expected);

        ComplexPolar_F64 pa = new ComplexPolar_F64(a);
        ComplexPolar_F64 pb = new ComplexPolar_F64(b);
        ComplexPolar_F64 pc = new ComplexPolar_F64();

        ComplexMath_F64.multiply(pa, pb, pc);

        Complex_F64 found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void div_polar() {
        Complex_F64 a = new Complex_F64(2, 3);
        Complex_F64 b = new Complex_F64(-3, 6);
        Complex_F64 expected = new Complex_F64();

        ComplexMath_F64.divide(a, b, expected);

        ComplexPolar_F64 pa = new ComplexPolar_F64(a);
        ComplexPolar_F64 pb = new ComplexPolar_F64(b);
        ComplexPolar_F64 pc = new ComplexPolar_F64();

        ComplexMath_F64.divide(pa, pb, pc);

        Complex_F64 found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test
    public void pow() {
        ComplexPolar_F64 a = new ComplexPolar_F64(2, 0.2);
        ComplexPolar_F64 expected = new ComplexPolar_F64();
        ComplexPolar_F64 found = new ComplexPolar_F64();

        ComplexMath_F64.multiply(a, a, expected);
        ComplexMath_F64.multiply(a, expected, expected);

        ComplexMath_F64.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_F64);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_F64);
    }

    @Test
    public void root_polar() {
        ComplexPolar_F64 expected = new ComplexPolar_F64(2, 0.2);
        ComplexPolar_F64 root = new ComplexPolar_F64();
        ComplexPolar_F64 found = new ComplexPolar_F64();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath_F64.root(expected, 2, 0, root);

            ComplexMath_F64.multiply(root, root, found);

            Complex_F64 e = expected.toStandard();
            Complex_F64 f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_F64);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void root_standard() {
        Complex_F64 expected = new Complex_F64(2, 0.2);
        Complex_F64 root = new Complex_F64();
        Complex_F64 found = new Complex_F64();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath_F64.root(expected, 2, 0, root);

            ComplexMath_F64.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
        }
    }

    @Test
    public void sqrt_standard() {
        Complex_F64 input = new Complex_F64(2, 0.2);
        Complex_F64 root = new Complex_F64();
        Complex_F64 found = new Complex_F64();

        ComplexMath_F64.sqrt(input, root);
        ComplexMath_F64.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F64);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F64);

        input = new Complex_F64(2, -0.2);

        ComplexMath_F64.sqrt(input, root);
        ComplexMath_F64.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_F64);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }
}
