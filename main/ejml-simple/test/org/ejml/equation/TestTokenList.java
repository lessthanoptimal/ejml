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

package org.ejml.equation;

import org.junit.jupiter.api.Test;

import static org.ejml.equation.TokenList.Token;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public class TestTokenList {

    @Test
    public void push() {
        TokenList list = new TokenList();

        assertNull(list.getFirst());
        assertNull(list.getLast());
        assertEquals(0,list.size());

        list.add(Symbol.MINUS);
        Token a = list.getFirst();
        assertNotNull(a);
        assertSame(a, list.getLast());
        assertEquals(1,list.size());


        list.add(Symbol.PAREN_LEFT);
        Token b = list.getLast();
        assertNotSame(a, b);
        assertNotSame(b, list.getFirst());
        assertEquals(2,list.size());

        list.add(Symbol.PAREN_RIGHT);
        Token c = list.getLast();
        assertNotSame(a, c);
        assertNotSame(b, c);
        assertNotSame(c, list.getFirst());
        assertEquals(3,list.size());
    }

    @Test
    public void insert() {
        TokenList list = new TokenList();

        list.insert(null,new Token(Symbol.MINUS));
        assertEquals(1, list.size());
        assertTrue(list.first == list.last && list.first != null);

        list.insert(null,new Token(Symbol.PLUS));
        assertEquals(2, list.size());
        assertEquals(Symbol.PLUS,list.first.getSymbol());
        assertEquals(Symbol.MINUS, list.first.next.getSymbol());

        list.insert(list.last,new Token(Symbol.TIMES));
        assertEquals(3, list.size());
        assertEquals(Symbol.PLUS,list.first.getSymbol());
        assertEquals(Symbol.MINUS,list.first.next.getSymbol());
        assertEquals(Symbol.TIMES,list.last.getSymbol());

        list.insert(list.first.next,new Token(Symbol.ASSIGN));
        assertEquals(4, list.size());
        assertEquals(Symbol.MINUS, list.first.next.getSymbol());
        assertEquals(Symbol.ASSIGN, list.first.next.next.getSymbol());
        assertEquals(Symbol.TIMES, list.first.next.next.next.getSymbol());
    }

    @Test
    public void remove() {
        TokenList list = new TokenList();
        Token A,B,C;

        list.add(Symbol.MINUS);
        list.remove(list.first);
        assertEquals(0,list.size);
        assertNull(list.getFirst());
        assertNull(list.getLast());

        A = list.add(Symbol.MINUS);
        list.add(Symbol.PLUS);
        list.remove(list.last);
        assertEquals(1, list.size);
        assertSame(A, list.getFirst());
        assertSame(A, list.getLast());
        assertNull(A.next);
        assertNull(A.previous);
        B = list.add(Symbol.PLUS);
        list.remove(list.first);
        assertEquals(1, list.size);
        assertSame(B, list.getFirst());
        assertSame(B, list.getLast());
        assertNull(B.next);
        assertNull(B.previous);

        list.remove(list.first);
        A = list.add(Symbol.MINUS);
            list.add(Symbol.PLUS);
        C = list.add(Symbol.TIMES);
        list.remove(list.first.next);
        assertEquals(2, list.size);
        assertSame(A, list.getFirst());
        assertSame(C, list.getLast());
        assertSame(A.next, C);
        assertSame(A, C.previous);
    }

    @Test
    public void replace() {
        TokenList list = new TokenList();
        Token A,B,C,D;

        A = list.add(Symbol.MINUS);
        B = new Token(Symbol.PLUS);
        list.replace(A, B);
        assertEquals(1,list.size);
        assertSame(B, list.getFirst());
        assertSame(B, list.getLast());

        list.remove(B);

        A = list.add(Symbol.MINUS);
        B = list.add(Symbol.PLUS);
        C = new Token(Symbol.TIMES);

        list.replace(A, C);
        assertEquals(2, list.size);
        assertSame(C, list.getFirst());
        assertSame(B, list.getLast());
        assertNull(C.previous);
        assertSame(C.next, B);
        assertSame(B.previous, C);
        assertNull(B.next);

        list = new TokenList();
        A=list.add(Symbol.MINUS);
        B=list.add(Symbol.PLUS);
        C=list.add(Symbol.TIMES);
        D=new Token(Symbol.RDIVIDE);

        list.replace(B,D);
        assertEquals(3, list.size);
        assertSame(A, list.getFirst());
        assertSame(C, list.getLast());
        assertSame(A.next, D);
        assertSame(A, D.previous);
        assertSame(C.previous, D);
        assertSame(C, D.next);
    }

    @Test
    public void extractSubList() {

        // Check when the input list has a size of 1
        TokenList list = new TokenList();
        list.add(Symbol.MINUS);

        TokenList found = list.extractSubList(list.first,list.first);
        assertEquals(0, list.size);
        assertEquals(1, found.size);
        assertTrue(null == list.first && null == list.last);
        assertTrue(found.first==found.last && found.first != null);

        // Remove entire thing
        list = new TokenList();
        list.add(Symbol.MINUS);
        list.add(Symbol.PLUS);
        list.add(Symbol.RDIVIDE);
        found = list.extractSubList(list.first,list.last);
        assertEquals(0,list.size);
        assertEquals(3,found.size);
        assertEquals(Symbol.MINUS,found.first.getSymbol());
        assertEquals(Symbol.RDIVIDE,found.last.getSymbol());

        // Remove stuff in the middle
        list = new TokenList();
        list.add(Symbol.MINUS);
        list.add(Symbol.PLUS);
        list.add(Symbol.RDIVIDE);
        list.add(Symbol.TIMES);
        found = list.extractSubList(list.first.next,list.last.previous);
        assertEquals(2,list.size);
        assertEquals(2,found.size);
        assertEquals(Symbol.MINUS,list.first.getSymbol());
        assertEquals(Symbol.TIMES,list.last.getSymbol());
        assertEquals(Symbol.PLUS,found.first.getSymbol());
        assertEquals(Symbol.RDIVIDE,found.last.getSymbol());
    }

    @Test
    public void Token_getType() {
        assertSame(new Token(new VariableMatrix(null)).getType(), TokenList.Type.VARIABLE);
        assertSame(new Token(Symbol.PLUS).getType(), TokenList.Type.SYMBOL);
        assertSame(new Token(new Function("foo")).getType(), TokenList.Type.FUNCTION);
    }
}
