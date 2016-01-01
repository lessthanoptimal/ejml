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

/**
 * Linked-list of tokens parsed from the equations string.
 *
 * @author Peter Abeles
 */
class TokenList {

    Token first;
    Token last;
    int size = 0;

    public TokenList() {
    }

    /**
     * Creates a list from the two given tokens.  These tokens are assumed to form a linked list starting at 'first'
     * and ending at 'last'
     * @param first First element in the new list
     * @param last Last element in the new list
     */
    public TokenList(Token first, Token last) {
        this.first = first;
        this.last = last;

        Token t = first;
        while( t != null ) {
            size++;
            t = t.next;
        }
    }

    /**
     * Adds a function to the end of the token list
     * @param function Function which is to be added
     * @return The new Token created around function
     */
    public Token add( Function function ) {
        Token t = new Token(function);
        push( t );
        return t;
    }

    /**
     * Adds a variable to the end of the token list
     * @param variable Variable which is to be added
     * @return The new Token created around variable
     */
    public Token add( Variable variable ) {
        Token t = new Token(variable);
        push( t );
        return t;
    }

    /**
     * Adds a symbol to the end of the token list
     * @param symbol Symbol which is to be added
     * @return The new Token created around symbol
     */
    public Token add( Symbol symbol ) {
        Token t = new Token(symbol);
        push( t );
        return t;
    }

    /**
     * Adds a word to the end of the token list
     * @param word word which is to be added
     * @return The new Token created around symbol
     */
    public Token add( String word ) {
        Token t = new Token(word);
        push( t );
        return t;
    }

    /**
     * Adds a new Token to the end of the linked list
     */
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

    /**
     * Removes the token from the list
     * @param token Token which is to be removed
     */
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

    /**
     * Removes 'original' and places 'target' at the same location
     */
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

    /**
     * Inserts the LokenList immediately following the 'before' token
     */
    public void insertAfter(Token before, TokenList list ) {
        Token after = before.next;

        before.next = list.first;
        list.first.previous = before;
        if( after == null ) {
            last = list.last;
        } else {
            after.previous = list.last;
            list.last.next = after;
        }
        size += list.size;
    }

    /**
     * Prints the list of tokens
     */
    public String toString() {
        String ret = "";
        Token t = first;
        while( t != null ) {
            ret += t +" ";
            t = t.next;
        }
        return ret;
    }

    /**
     * First token in the list
     */
    public Token getFirst() {
        return first;
    }

    /**
     * Last token in the list
     */
    public Token getLast() {
        return last;
    }

    /**
     * Number of tokens in the list
     */
    public int size() {
        return size;
    }

    /**
     * The token class contains a reference to parsed data (e.g. function, variable, or symbol) and reference
     * to list elements before and after it.
     */
    public static class Token {
        /**
         * Next element in the list.  If null then it's at the end of the list
         */
        public Token next;
        /**
         * Previous element in the list.  If null then it's the first element in the list
         */
        public Token previous;

        public Function function;
        public Variable variable;
        public Symbol symbol;
        public String word;

        public Token(Function function) {
            this.function = function;
        }

        public Token(Variable variable) {
            this.variable = variable;
        }

        public Token(Symbol symbol) {
            this.symbol = symbol;
        }

        public Token(String word) {
            this.word = word;
        }

        public Token() {
        }

        public Type getType() {
            if( function != null )
                return Type.FUNCTION;
            else if( variable != null )
                return Type.VARIABLE;
            else if( word != null )
                return Type.WORD;
            else
                return Type.SYMBOL;
        }

        public Variable getVariable() {
            return variable;
        }

        public Function getFunction() {
            return function;
        }

        public Symbol getSymbol() {
            return symbol;
        }

        public String getWord() {
            return word;
        }

        /**
         * If a scalar variable it returns its type, otherwise null
         */
        public VariableScalar.Type getScalarType() {
            if( variable != null )
                if( variable.getType() == VariableType.SCALAR ) {
                    return ((VariableScalar)variable).getScalarType();
                }
            return null;
        }

        public String toString() {
            switch( getType() ) {
                case FUNCTION:
                    return "Func:"+function.getName();
                case SYMBOL:
                    return ""+symbol;
                case VARIABLE:
                    return variable.toString();
                case WORD:
                    return "Word:"+word;
            }
            throw new RuntimeException("Unknown type");
        }

        public Token copy() {
            Token t = new Token();
            t.word = word;
            t.function = function;
            t.symbol = symbol;
            t.variable = variable;

            return t;
        }
    }

    public void print() {
        Token t = first;
        while( t != null ) {
            System.out.println(t);
            t = t.next;
        }
    }

    /**
     * Specifies the type of data stored in a Token.
     */
    public static enum Type
    {
        FUNCTION,
        VARIABLE,
        SYMBOL,
        WORD
    }
}
