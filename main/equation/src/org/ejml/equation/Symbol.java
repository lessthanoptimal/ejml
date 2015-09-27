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
 * Types of low level operators which can be applied in the code
 *
 * @author Peter Abeles
 */
public enum Symbol {
    PLUS,
    MINUS,
    TIMES,
    LDIVIDE,
    RDIVIDE,
    POWER,
    PERIOD,
    ELEMENT_TIMES,
    ELEMENT_DIVIDE,
    ELEMENT_POWER,
    ASSIGN,
    PAREN_LEFT,
    PAREN_RIGHT,
    BRACKET_LEFT,
    BRACKET_RIGHT,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQ,
    LESS_THAN_EQ,
    COMMA,
    TRANSPOSE,
    COLON,
    SEMICOLON;

    public static Symbol lookup( char c ) {
        switch( c ) {
            case '.': return PERIOD;
            case ',': return COMMA;
            case '\'': return TRANSPOSE;
            case '+': return PLUS;
            case '-': return MINUS;
            case '*': return TIMES;
            case '\\': return LDIVIDE;
            case '/': return RDIVIDE;
            case '^': return POWER;
            case '=': return ASSIGN;
            case '(': return PAREN_LEFT;
            case ')': return PAREN_RIGHT;
            case '[': return BRACKET_LEFT;
            case ']': return BRACKET_RIGHT;
            case '>': return GREATER_THAN;
            case '<': return LESS_THAN;
            case ':': return COLON;
            case ';': return SEMICOLON;
        }
        throw new RuntimeException("Unknown type "+c);
    }

    public static Symbol lookupElementWise( char c ) {
        switch( c ) {
            case '*': return ELEMENT_TIMES;
            case '/': return ELEMENT_DIVIDE;
            case '^': return ELEMENT_POWER;
        }
        throw new RuntimeException("Unknown element-wise type "+c);
    }
}
