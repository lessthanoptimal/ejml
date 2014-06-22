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

/**
 * @author Peter Abeles
 */
public class TokenList {

    Token first;
    Token last;
    int size = 0;

    public Token add( Variable variable ) {
        Token t = new Token(variable);
        push( t );
        return t;
    }

    public Token add( char symbol ) {
        Token t = new Token(symbol);
        push( t );
        return t;
    }


    public void push( Token token ) {
        size++;
        if( first == null ) {
            first = token;
            last = token;
        } else {
            last.next = token;
            token.previous = last;
            last = token;
        }
    }

    public void remove( Token token ) {
        if( token == first ) {
            first = first.next;
        }
        if( token == last ) {
            last = last.previous;
        }
        if( token.next != null ) {
            token.next.previous = token.previous;
        }
        if( token.previous != null ) {
            token.previous.next = token.next;
        }

        token.next = token.previous = null;
        size--;
    }

    public void replace( Token original , Token target  ) {
        if( first == original )
            first = target;
        if( last == original )
            last = target;

        target.next = original.next;
        target.previous = original.previous;

        if( original.next != null )
            original.next.previous = target;
        if( original.previous != null )
            original.previous.next = target;

        original.next = original.previous = null;
    }

    public String toString() {
        String ret = "";
        Token t = first;
        while( t != null ) {
            ret += t +" ";
            t = t.next;
        }
        return ret;
    }

    public Token getFirst() {
        return first;
    }

    public Token getLast() {
        return last;
    }

    public int size() {
        return size;
    }

    public static class Token {
        public Token next;
        public Token previous;

        public Variable variable;
        public char symbol;

        public Token(Variable variable) {
            this.variable = variable;
        }

        public Token(char symbol) {
            this.symbol = symbol;
        }

        public boolean isVariable() {
            return variable != null;
        }

        public Variable getVariable() {
            return variable;
        }

        public char getSymbol() {
            return symbol;
        }

        public String toString() {
            if( variable == null ) {
                return ""+symbol;
            } else {
                return variable.toString();
            }
        }
    }
}
