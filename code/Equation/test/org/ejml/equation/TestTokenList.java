/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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
public class TestTokenList {

    @Test
    public void push() {
        TokenList list = new TokenList();

        assertTrue(null==list.getFirst());
        assertTrue(null==list.getLast());
        assertEquals(0,list.size());

        list.add('a');
        TokenList.Token a = list.getFirst();
        assertTrue(null!=a);
        assertTrue(a==list.getLast());
        assertEquals(1,list.size());


        list.add('b');
        TokenList.Token b = list.getLast();
        assertTrue(a!=b);
        assertTrue(b!=list.getFirst());
        assertEquals(2,list.size());

        list.add('c');
        TokenList.Token c = list.getLast();
        assertTrue(a!=c);
        assertTrue(b!=c);
        assertTrue(c!=list.getFirst());
        assertEquals(3,list.size());
    }

    @Test
    public void remove() {
        TokenList list = new TokenList();
        TokenList.Token A,B,C;

        list.add('a');
        list.remove(list.first);
        assertEquals(0,list.size);
        assertTrue(null==list.getFirst());
        assertTrue(null==list.getLast());

        A = list.add('a');
        list.add('b');
        list.remove(list.last);
        assertEquals(1, list.size);
        assertTrue(A == list.getFirst());
        assertTrue(A==list.getLast());
        assertTrue(A.next==null);
        assertTrue(A.previous==null);
        B = list.add('b');
        list.remove(list.first);
        assertEquals(1, list.size);
        assertTrue(B == list.getFirst());
        assertTrue(B==list.getLast());
        assertTrue(B.next==null);
        assertTrue(B.previous==null);


        list.remove(list.first);
        A = list.add('a');
        B = list.add('b');
        C = list.add('c');
        list.remove(list.first.next);
        assertEquals(2, list.size);
        assertTrue(A == list.getFirst());
        assertTrue(C==list.getLast());
        assertTrue(A.next==C);
        assertTrue(A==C.previous);

    }

    @Test
    public void replace() {
        TokenList list = new TokenList();
        TokenList.Token A,B,C,D;

        A = list.add('a');
        B = new TokenList.Token('b');
        list.replace(A, B);
        assertEquals(1,list.size);
        assertTrue(B==list.getFirst());
        assertTrue(B == list.getLast());

        list.remove(B);

        A = list.add('a');
        B = list.add('b');
        C = new TokenList.Token('c');

        list.replace(A, C);
        assertEquals(2, list.size);
        assertTrue(C==list.getFirst());
        assertTrue(B==list.getLast());
        assertTrue(C.previous==null);
        assertTrue(C.next==B);
        assertTrue(B.previous==C);
        assertTrue(B.next==null);

        list = new TokenList();
        A=list.add('a');
        B=list.add('b');
        C=list.add('c');
        D=new TokenList.Token('d');

        list.replace(B,D);
        assertEquals(3, list.size);
        assertTrue(A == list.getFirst());
        assertTrue(C==list.getLast());
        assertTrue(A.next==D);
        assertTrue(A==D.previous);
        assertTrue(C.previous==D);
        assertTrue(C==D.next);
    }

    @Test
    public void Token_isVariable() {
        assertTrue(new TokenList.Token(new VariableMatrix(null)).isVariable());
        assertFalse(new TokenList.Token('a').isVariable());
    }
}
