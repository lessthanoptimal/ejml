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

    public TokenList() {
    }

    public TokenList(Token first, Token last) {
        this.first = first;
        this.last = last;

        Token t = first;
        while( t != null ) {
            size++;
            t = t.next;
        }
    }

    public Token add( Function variable ) {
        Token t = new Token(variable);
        push( t );
        return t;
    }

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
            token.previous = null;
            token.next = null;
        } else {
            last.next = token;
            token.previous = last;
            token.next = null;
            last = token;
        }
    }

    /**
     * Inserts 'token' after 'where'.  if where is null then it is inserted to the beginning of the list.
     * @param where Where 'token' should be inserted after.  if null the put at it at the beginning
     * @param token The token that is to be inserted
     */
    public void insert( Token where , Token token ) {
        if( where == null ) {
            // put at the front of the list
            if( size == 0 )
                push(token);
            else {
                first.previous = token;
                token.previous = null;
                token.next = first;
                first = token;
                size++;
            }
        } else if( where == last || null == last ) {
            push(token);
        } else {
            token.next = where.next;
            token.previous = where;
            where.next.previous = token;
            where.next = token;
            size++;
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

    /**
     * Removes elements from begin to end from the list, inclusive.  Returns a new list which
     * is composed of the removed elements
     * @param begin
     * @param end
     * @return
     */
    public TokenList extractSubList( Token begin , Token end ) {
        if( begin == end ) {
            remove(begin);
            return new TokenList(begin,begin);
        } else {
            if( first == begin ) {
                first = end.next;
            }
            if( last == end ) {
                last = begin.previous;
            }
            if( begin.previous != null ) {
                begin.previous.next = end.next;
            }
            if( end.next != null ) {
                end.next.previous = begin.previous;
            }
            begin.previous = null;
            end.next = null;

            TokenList ret = new TokenList(begin,end);
            size -= ret.size();
            return ret;
        }
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

        public Function function;
        public Variable variable;
        public char symbol;

        public Token(Function function) {
            this.function = function;
        }

        public Token(Variable variable) {
            this.variable = variable;
        }

        public Token(char symbol) {
            this.symbol = symbol;
        }

        public Type getType() {
            if( function != null )
                return Type.FUNCTION;
            else if( variable != null )
                return Type.VARIABLE;
            else
                return Type.SYMBOL;
        }

        public Variable getVariable() {
            return variable;
        }

        public Function getFunction() {
            return function;
        }

        public char getSymbol() {
            return symbol;
        }

        public String toString() {
            switch( getType() ) {
                case FUNCTION:
                    return "Func:"+function.getName();
                case SYMBOL:
                    return ""+symbol;
                case VARIABLE:
                    return variable.toString();
            }
            throw new RuntimeException("Unknown type");
        }
    }

    public static enum Type
    {
        FUNCTION,
        VARIABLE,
        SYMBOL
    }
}
