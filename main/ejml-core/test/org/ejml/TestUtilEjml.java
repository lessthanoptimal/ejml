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

package org.ejml;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
@SuppressWarnings("FloatingPointLiteralPrecision")
public class TestUtilEjml {

    Random rand = new Random(23423);

    @Test
    void max_array() {
        double[] a = new double[]{-1, 2, 3, 4, 5, 6, 3, 4, 5, 7, 8, 2, 3, -5, -6};

        assertEquals(UtilEjml.max(a, 0, a.length), 8);
        assertEquals(UtilEjml.max(a, 6, 3), 5);
    }

    /**
     * Provide it a couple of different strings to parse.  Then compare
     * the results against the expect answer
     */
    @Test
    void parse_DDRM() {
        String a = "-0.779094   1.682750\n" +
                "   1.304014  -1.880739\n";

        DMatrixRMaj m = UtilEjml.parse_DDRM(a, 2);

        assertEquals(2, m.numCols);
        assertEquals(2, m.numRows);
        assertEquals(-0.779094, m.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(1.682750, m.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(1.304014, m.get(1, 0), UtilEjml.TEST_F64);
        assertEquals(-1.880739, m.get(1, 1), UtilEjml.TEST_F64);

        // give it a matrix with a space in the first element, see if that screws it up
        a = " -0.779094   1.682750  5\n" +
                "   1.304014  -1.880739  8\n";

        m = UtilEjml.parse_DDRM(a, 3);
        assertEquals(3, m.numCols);
        assertEquals(2, m.numRows);
        assertEquals(-0.779094, m.get(0, 0), UtilEjml.TEST_F64);
    }

    @Test
    void parse_DSCC() {
        String a = "-0.779094   1.682750\n" +
                "   0  -1.880739\n";

        DMatrixSparseCSC m = UtilEjml.parse_DSCC(a, 2);

        assertEquals(3, m.nz_length);

        assertEquals(2, m.numCols);
        assertEquals(2, m.numRows);
        assertEquals(-0.779094, m.get(0, 0), UtilEjml.TEST_F64);
        assertEquals(1.682750, m.get(0, 1), UtilEjml.TEST_F64);
        assertEquals(0, m.get(1, 0), UtilEjml.TEST_F64);
        assertEquals(-1.880739, m.get(1, 1), UtilEjml.TEST_F64);
    }

    @Test
    void checkTooLarge() {
        UtilEjml.checkTooLarge(0, 0);
        UtilEjml.checkTooLarge(1000, 100);

        try {
            UtilEjml.checkTooLarge(Integer.MAX_VALUE, 600);
            fail("Exception should have been thrown");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    void checkTooLargeComplex() {
        UtilEjml.checkTooLargeComplex(0, 0);
        UtilEjml.checkTooLargeComplex(1000, 100);
        UtilEjml.checkTooLargeComplex(Integer.MAX_VALUE/2, 1);

        try {
            UtilEjml.checkTooLargeComplex(Integer.MAX_VALUE/2, 2);
            fail("Exception should have been thrown");
        } catch (IllegalArgumentException ignore) {}

        try {
            UtilEjml.checkTooLargeComplex(Integer.MAX_VALUE, 600);
            fail("Exception should have been thrown");
        } catch (IllegalArgumentException ignore) {}
    }

    @Test
    void shuffle() {
        int[] m = new int[200];
        for (int i = 0; i < m.length; i++) {
            m[i] = i;
        }
        int N = m.length - 5;
        UtilEjml.shuffle(m, N, 0, 40, rand);

        // end should be untouched
        for (int i = N; i < m.length; i++) {
            assertEquals(i, m[i]);
        }

        // the order should be drastically changed
        int numOrdered = 0;
        for (int i = 0; i < 40; i++) {
            if (m[i] == i) {
                numOrdered++;
            }
        }
        assertTrue(numOrdered < 10);
    }

    @Test
    void fancyStringF() {
        DecimalFormat format = new DecimalFormat("#");
        assertEquals("-0         ", UtilEjml.fancyStringF(-0.0, format, 11, 4));
        assertEquals(" 0         ", UtilEjml.fancyStringF(0.0, format, 11, 4));
        assertEquals("-1         ", UtilEjml.fancyStringF(-1, format, 11, 4));
        assertEquals(" 1         ", UtilEjml.fancyStringF(1, format, 11, 4));
        assertEquals("-12        ", UtilEjml.fancyStringF(-12, format, 11, 4));
        assertEquals(" 12        ", UtilEjml.fancyStringF(12, format, 11, 4));
        assertEquals("-1.1234    ", UtilEjml.fancyStringF(-1.1234, format, 11, 4));
        assertEquals(" 1.1234    ", UtilEjml.fancyStringF(1.1234, format, 11, 4));
        assertEquals("-1234.1234 ", UtilEjml.fancyStringF(-1234.1234, format, 11, 4));
        assertEquals(" 1234.1234 ", UtilEjml.fancyStringF(1234.1234, format, 11, 4));
        assertEquals("-1234.12343", UtilEjml.fancyStringF(-1234.123433, format, 11, 4)); // check rounding here
        assertEquals(" 1234.12343", UtilEjml.fancyStringF(1234.123433, format, 11, 4));
        assertEquals("-1234.12346", UtilEjml.fancyStringF(-1234.123456, format, 11, 4)); // no rounding needed
        assertEquals(" 1234.12346", UtilEjml.fancyStringF(1234.123456, format, 11, 4));
        assertEquals("-123456.123", UtilEjml.fancyStringF(-123456.123456, format, 11, 4));
        assertEquals(" 123456.123", UtilEjml.fancyStringF(123456.123456, format, 11, 4));
        assertEquals("-1.2346E+10", UtilEjml.fancyStringF(-12345678901.123456, format, 11, 4));
        assertEquals(" 1.2346E+10", UtilEjml.fancyStringF(12345678901.123456, format, 11, 4));
        assertEquals("-.1234     ", UtilEjml.fancyStringF(-0.1234, format, 11, 4));
        assertEquals(" .1234     ", UtilEjml.fancyStringF(0.1234, format, 11, 4));
        assertEquals("-.12345678 ", UtilEjml.fancyStringF(-0.12345678, format, 11, 4));
        assertEquals(" .12345678 ", UtilEjml.fancyStringF(0.12345678, format, 11, 4));
        assertEquals("-.123456789", UtilEjml.fancyStringF(-0.12345678901, format, 11, 4));
        assertEquals(" .123456789", UtilEjml.fancyStringF(0.12345678901, format, 11, 4));
        assertEquals("-.0000123  ", UtilEjml.fancyStringF(-0.0000123, format, 11, 4));
        assertEquals(" .0000123  ", UtilEjml.fancyStringF(0.0000123, format, 11, 4));
        assertEquals("-.000012345", UtilEjml.fancyStringF(-0.0000123451, format, 11, 4));
        assertEquals(" .000012345", UtilEjml.fancyStringF(0.0000123451, format, 11, 4));
        assertEquals("-.000012346", UtilEjml.fancyStringF(-0.0000123456, format, 11, 4));
        assertEquals(" .000012346", UtilEjml.fancyStringF(0.0000123456, format, 11, 4));
        assertEquals("-.000001235", UtilEjml.fancyStringF(-0.000001234567, format, 11, 3)); // see if reduction of significant digits changes output
        assertEquals(" .000001235", UtilEjml.fancyStringF(0.000001234567, format, 11, 3));
        assertEquals("-1.2345E-06", UtilEjml.fancyStringF(-0.0000012345, format, 11, 4));
        assertEquals(" 1.2345E-06", UtilEjml.fancyStringF(0.0000012345, format, 11, 4));
        assertEquals("-1.2346E-06", UtilEjml.fancyStringF(-0.00000123456, format, 11, 4));
        assertEquals(" 1.2346E-06", UtilEjml.fancyStringF(0.00000123456, format, 11, 4));
        assertEquals("-1.235E-07 ", UtilEjml.fancyStringF(-0.0000001234567, format, 11, 3));
        assertEquals(" 1.235E-07 ", UtilEjml.fancyStringF(0.0000001234567, format, 11, 3));
        assertEquals("-1.2346E-06", UtilEjml.fancyStringF(-0.000001234567, format, 11, 4));
        assertEquals(" 1.2346E-06", UtilEjml.fancyStringF(0.000001234567, format, 11, 4));
        assertEquals("-1.2346E-07", UtilEjml.fancyStringF(-0.0000001234567, format, 11, 4));
        assertEquals(" 1.2346E-07", UtilEjml.fancyStringF(0.0000001234567, format, 11, 4));
        assertEquals("-1.235E-102", UtilEjml.fancyStringF(-1.234567E-102, format, 11, 4));
        assertEquals(" 1.235E-102", UtilEjml.fancyStringF(1.234567E-102, format, 11, 4));
        assertEquals("-1.235E+102", UtilEjml.fancyStringF(-1.234567E102, format, 11, 4));
        assertEquals(" 1.235E+102", UtilEjml.fancyStringF(1.234567E102, format, 11, 4));
    }
}
