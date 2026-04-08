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

package org.ejml.equation;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestIntegerSequence extends EjmlStandardJUnit {
    @Test void explicit() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(6));
        var c = new TokenList.Token(new VariableInteger(-3));

        a.next = a;
        compare(new IntegerSequence.Explicit(a), 4);
        a.next = b;
        compare(new IntegerSequence.Explicit(a, b), 4, 6);
        b.next = c;
        compare(new IntegerSequence.Explicit(a, c), 4, 6, -3);
    }

    @Test void checkFor_two() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(7));

        compare(new IntegerSequence.For(a, null, b), 4, 5, 6, 7);
    }

    // Check the length for ranges
    @Test void checkFor_two_length() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(2));
        var c = new TokenList.Token(new VariableInteger(1));

        var case0 = new IntegerSequence.For(b, null,a);
        case0.initialize(10);
        var case1 = new IntegerSequence.For(b, null,b);
        case1.initialize(10);
        var case2 = new IntegerSequence.For(b, null,c);
        case2.initialize(10);

        assertEquals(3, case0.length);
        assertEquals(1, case1.length);
        assertEquals(0, case2.length);

        // Give it an invalid range
        try {
            new IntegerSequence.For(a, null,c).initialize(10);
            fail("Should have thrown an exception");
        } catch (RuntimeException ignore) {
        }
    }

    @Test void checkFor_three() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(2));
        var c = new TokenList.Token(new VariableInteger(12));

        compare(new IntegerSequence.For(a, b, c), 4, 6, 8, 10, 12);
    }

    @Test void range_zero() {
        compare(new IntegerSequence.Range(null, null), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test void range_one() {
        var a = new TokenList.Token(new VariableInteger(4));

        compare(new IntegerSequence.Range(a, null), 4, 5, 6, 7, 8, 9, 10);
    }

    @Test void range_two() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(2));
        compare(new IntegerSequence.Range(a, b), 4, 6, 8, 10);
    }

    @Test void combined() {
        var a = new TokenList.Token(new VariableInteger(4));
        var b = new TokenList.Token(new VariableInteger(7));

        var varA = new VariableIntegerSequence(new IntegerSequence.For(a, null, b));
        var varB = new VariableInteger(7);

        var tokenA = new TokenList.Token(varA);
        var tokenB = new TokenList.Token(varB);
        tokenA.next = tokenB;

        compare(new IntegerSequence.Combined(tokenA, tokenB), 4, 5, 6, 7, 7);
    }

    private void compare( IntegerSequence sequence, int... expected ) {
        sequence.initialize(10);
        for (int i = 0; i < expected.length; i++) {
            assertTrue(sequence.hasNext());
            assertEquals(expected[i], sequence.next());
        }
        assertFalse(sequence.hasNext());
    }
}
