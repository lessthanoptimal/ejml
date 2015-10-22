/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestIntegerSequence {
    @Test
    public void explicit() {

        TokenList.Token a = new TokenList.Token(new VariableInteger(4));
        TokenList.Token b = new TokenList.Token(new VariableInteger(6));
        TokenList.Token c = new TokenList.Token(new VariableInteger(-3));

        a.next = a;
        compare(new IntegerSequence.Explicit(a), 4);
        a.next = b;
        compare(new IntegerSequence.Explicit(a,b), 4, 6);
        b.next = c;
        compare(new IntegerSequence.Explicit(a,c), 4, 6 , -3);
    }

    @Test
    public void checkFor_two() {
        TokenList.Token a = new TokenList.Token(new VariableInteger(4));
        TokenList.Token b = new TokenList.Token(new VariableInteger(7));

        compare(new IntegerSequence.For(a,null,b), 4,5,6,7);
    }

    @Test
    public void checkFor_three() {
        TokenList.Token a = new TokenList.Token(new VariableInteger(4));
        TokenList.Token b = new TokenList.Token(new VariableInteger(2));
        TokenList.Token c = new TokenList.Token(new VariableInteger(12));

        compare(new IntegerSequence.For(a,b,c), 4,6,8,10,12);
    }

    @Test
    public void range_zero() {
        compare(new IntegerSequence.Range(null,null), 0,1,2,3,4,5,6,7,8,9,10);
    }

    @Test
    public void range_one() {
        TokenList.Token a = new TokenList.Token(new VariableInteger(4));

        compare(new IntegerSequence.Range(a,null), 4,5,6,7,8,9,10);
    }

    @Test
    public void range_two() {
        TokenList.Token a = new TokenList.Token(new VariableInteger(4));
        TokenList.Token b = new TokenList.Token(new VariableInteger(2));
        compare(new IntegerSequence.Range(a,b), 4,6,8,10);
    }

    @Test
    public void combined() {
        TokenList.Token a = new TokenList.Token(new VariableInteger(4));
        TokenList.Token b = new TokenList.Token(new VariableInteger(7));

        VariableIntegerSequence varA = new VariableIntegerSequence(new IntegerSequence.For(a,null,b));
        VariableScalar varB = new VariableInteger(7);

        TokenList.Token tokenA = new TokenList.Token(varA);
        TokenList.Token tokenB = new TokenList.Token(varB);
        tokenA.next = tokenB;

        compare(new IntegerSequence.Combined(tokenA,tokenB), 4, 5, 6, 7,7);
    }


    private void compare( IntegerSequence sequence , int ...expected ) {
        sequence.initialize(10);
        for (int i = 0; i < expected.length; i++) {
            assertTrue(sequence.hasNext());
            assertEquals( expected[i], sequence.next() );
        }
        assertFalse(sequence.hasNext());
    }
}
