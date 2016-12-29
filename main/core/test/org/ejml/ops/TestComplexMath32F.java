/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.Complex32F;
import org.ejml.data.ComplexPolar32F;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestComplexMath32F {
    @Test
    public void conj() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);

        ComplexMath32F.conj(a, b);

        assertEquals(a.real, b.real, UtilEjml.TEST_32F);
        assertEquals(-a.imaginary, b.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void plus() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F c = new Complex32F();

        ComplexMath32F.plus(a, b, c);

        assertEquals(-1, c.real, UtilEjml.TEST_32F);
        assertEquals(9, c.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void minus() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F c = new Complex32F();

        ComplexMath32F.minus(a, b, c);

        assertEquals(5, c.real, UtilEjml.TEST_32F);
        assertEquals(-3, c.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void multiply() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F c = new Complex32F();

        ComplexMath32F.multiply(a, b, c);

        assertEquals(-24, c.real, UtilEjml.TEST_32F);
        assertEquals(3, c.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void divide() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F c = new Complex32F();

        ComplexMath32F.divide(a, b, c);

        assertEquals(0.26666666666f, c.real, UtilEjml.TEST_32F);
        assertEquals(-0.466666666666f, c.imaginary, UtilEjml.TEST_32F);
    }

    /**
     * Test conversion to and from polar form by doing just that and see if it gets the original answer again
     */
    @Test
    public void convert() {
        Complex32F a = new Complex32F(2, 3);
        ComplexPolar32F b = new ComplexPolar32F();
        Complex32F c = new Complex32F();

        ComplexMath32F.convert(a, b);
        ComplexMath32F.convert(b, c);

        assertEquals(a.real, c.real, UtilEjml.TEST_32F);
        assertEquals(a.imaginary, c.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void mult_polar() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F expected = new Complex32F();

        ComplexMath32F.multiply(a, b, expected);

        ComplexPolar32F pa = new ComplexPolar32F(a);
        ComplexPolar32F pb = new ComplexPolar32F(b);
        ComplexPolar32F pc = new ComplexPolar32F();

        ComplexMath32F.multiply(pa, pb, pc);

        Complex32F found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_32F);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void div_polar() {
        Complex32F a = new Complex32F(2, 3);
        Complex32F b = new Complex32F(-3, 6);
        Complex32F expected = new Complex32F();

        ComplexMath32F.divide(a, b, expected);

        ComplexPolar32F pa = new ComplexPolar32F(a);
        ComplexPolar32F pb = new ComplexPolar32F(b);
        ComplexPolar32F pc = new ComplexPolar32F();

        ComplexMath32F.divide(pa, pb, pc);

        Complex32F found = pc.toStandard();

        assertEquals(expected.real, found.real, UtilEjml.TEST_32F);
        assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_32F);
    }

    @Test
    public void pow() {
        ComplexPolar32F a = new ComplexPolar32F(2, 0.2f);
        ComplexPolar32F expected = new ComplexPolar32F();
        ComplexPolar32F found = new ComplexPolar32F();

        ComplexMath32F.multiply(a, a, expected);
        ComplexMath32F.multiply(a, expected, expected);

        ComplexMath32F.pow(a, 3, found);

        assertEquals(expected.r, found.r, UtilEjml.TEST_32F);
        assertEquals(expected.theta, found.theta, UtilEjml.TEST_32F);
    }

    @Test
    public void root_polar() {
        ComplexPolar32F expected = new ComplexPolar32F(2, 0.2f);
        ComplexPolar32F root = new ComplexPolar32F();
        ComplexPolar32F found = new ComplexPolar32F();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath32F.root(expected, 2, 0, root);

            ComplexMath32F.multiply(root, root, found);

            Complex32F e = expected.toStandard();
            Complex32F f = found.toStandard();

            assertEquals(e.real, f.real, UtilEjml.TEST_32F);
            assertEquals(e.imaginary, f.imaginary, UtilEjml.TEST_32F);
        }
    }

    @Test
    public void root_standard() {
        Complex32F expected = new Complex32F(2, 0.2f);
        Complex32F root = new Complex32F();
        Complex32F found = new Complex32F();

        // compute the square root of a complex number then see if the
        // roots equal the output
        for (int i = 0; i < 2; i++) {
            ComplexMath32F.root(expected, 2, 0, root);

            ComplexMath32F.multiply(root, root, found);

            assertEquals(expected.real, found.real, UtilEjml.TEST_32F);
            assertEquals(expected.imaginary, found.imaginary, UtilEjml.TEST_32F);
        }
    }

    @Test
    public void sqrt_standard() {
        Complex32F input = new Complex32F(2, 0.2f);
        Complex32F root = new Complex32F();
        Complex32F found = new Complex32F();

        ComplexMath32F.sqrt(input, root);
        ComplexMath32F.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_32F);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_32F);

        input = new Complex32F(2, -0.2f);

        ComplexMath32F.sqrt(input, root);
        ComplexMath32F.multiply(root, root, found);

        assertEquals(input.real, found.real, UtilEjml.TEST_32F);
        assertEquals(input.imaginary, found.imaginary, UtilEjml.TEST_32F);
    }
}
