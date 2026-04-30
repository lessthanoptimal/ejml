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

package org.ejml.ops;

import org.ejml.EjmlStandardJUnit;
import org.ejml.MapPrintFormat;
import org.ejml.MatrixPrintFormat;
import org.ejml.UtilEjml;
import org.ejml.data.ComplexPolar_F64;
import org.ejml.data.Complex_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestComplexMath_F64 extends EjmlStandardJUnit {
    @Test void conj() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);

        ComplexMath_F64.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_F64);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_F64);
    }

    @Test void plus() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
        var c = new Complex_F64();

        ComplexMath_F64.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_F64);
        assertEquals(9, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test void minus() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
        var c = new Complex_F64();

        ComplexMath_F64.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_F64);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test void multiply() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
        var c = new Complex_F64();

        ComplexMath_F64.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_F64);
        assertEquals(3, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test void divide() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
        var c = new Complex_F64();

        ComplexMath_F64.divide(a, b, c);

        assertEquals(0.26666666666, c.real, UtilEjml.TEST_F64);
        assertEquals(-0.466666666666, c.imaginary, UtilEjml.TEST_F64);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test void convert() {
        var a = new Complex_F64(2, 3);
        ComplexPolar_F64 b = new ComplexPolar_F64();
        var c = new Complex_F64();

        ComplexMath_F64.convert(a, b);
        ComplexMath_F64.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_F64);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_F64);
    }

    @Test void mult_polar() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
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

    @Test void div_polar() {
        var a = new Complex_F64(2, 3);
        var b = new Complex_F64(-3, 6);
        Complex_F64 expected = new Complex_F64();

        ComplexMath_F64.divide(a, b, expected);

        var pa = new ComplexPolar_F64(a);
        var pb = new ComplexPolar_F64(b);
        var pc = new ComplexPolar_F64();

        ComplexMath_F64.divide(pa, pb, pc);

        Complex_F64 found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
    }

    @Test void pow() {
        var a = new ComplexPolar_F64(2, 0.2);
        var expected = new ComplexPolar_F64();
        var found = new ComplexPolar_F64();

        ComplexMath_F64.multiply(a, a, expected);
        ComplexMath_F64.multiply(a, expected, expected);

        ComplexMath_F64.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_F64);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_F64);
    }

    @Test void root_polar() {
        var expected = new ComplexPolar_F64(2, 0.2);
        var root = new ComplexPolar_F64();
        var found = new ComplexPolar_F64();

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

    @Test void root_standard() {
        var expected = new Complex_F64(2, 0.2);
        var root = new Complex_F64();
        var found = new Complex_F64();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath_F64.root(expected, 2, 0, root);

            ComplexMath_F64.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_F64);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_F64);
        }
    }

    @Test void sqrt_standard() {
        var input = new Complex_F64(2, 0.2);
        var root = new Complex_F64();
        var found = new Complex_F64();

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

    @Test void format_Matrix() {
        var a = new Complex_F64(2, 0.1234);
        String found = a.format(new MatrixPrintFormat().withPrecision(2));
        assertEquals("{2, 0.12}", found);
    }

    @Test void formatMap() {
        var a = new Complex_F64(2, 0.1234);
        String found = a.format(new MapPrintFormat().withPrecision(2));
        assertEquals("{real: 2, imaginary: 0.12}", found);
    }
}
