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

import org.ejml.data.DenseMatrix64F;

import java.util.HashMap;

/**
 *
 *
 * "result = (A*B' + C)*Q"
 * "result = A*B*A' + 2*C - D'"
 *
 * functions "scalar = det(A)"
 *           "[U,S,V] = svd(A)"
 *           "[U,S,V] = eig(A)"
 *           "B = inv(A)"
 *           "B = pinv(A)"
 *
 *
 * @author Peter Abeles
 */
// TODO plus, minus, times, transpose
// TODO support for scalar
// TODO Recycle temporary variables
public class Equation {
    HashMap<String,Variable> variables = new HashMap<String, Variable>();

    char storage[] = new char[1024];

    /**
     * Adds a new Matrix variable
     * @param variable Matrix which is to be assigned to name
     * @param name The name of the variable
     */
    public void alias( DenseMatrix64F variable , String name ) {
        variables.put(name,new VariableMatrix(variable));
    }

    /**
     * Parses the equation and compiles it into a sequence which can be executed later on
     * @param equation String in simple equation format.
     * @return Sequence of operations on the variables
     */
    // TODO handle ( and ) correctly
    // TODO handle transpose
    // TODO handle functions
    // TODO instead make the output be assigned the value of sequence
    public Sequence compile( String equation ) {
        TokenList tokens = extractTokens(equation);

        if( tokens.size() < 3 )
            throw new RuntimeException("Too few tokens");

        TokenList.Token t0 = tokens.getFirst();
        TokenList.Token t1 = tokens.getFirst();

        if( t1.isVariable() || t1.symbol != '=')
            throw new RuntimeException("Expected = symbol not "+t1);

        // Get the results variable
        if( !t0.isVariable())
            throw new RuntimeException("Expected variable name first.  Not "+t0);

        Variable result = t0.getVariable();

        // the last token should be a variable
        if( !tokens.getLast().isVariable())
            throw new RuntimeException("Equation didn't end in a variable name");

        // now it gets interesting

        Sequence sequence = new Sequence();

        // search for operations based on their priority
        parseOperations(t1.next,new char[]{'*'},tokens,sequence);
        parseOperations(t1.next,new char[]{'+','-'},tokens,sequence);

        if( tokens.size() != 3 )
            throw new RuntimeException("BUG 3 symbols must be left at this point");
        if( !tokens.getLast().isVariable() )
            throw new RuntimeException("BUG the last token must be a variable");

        // copy the results into the output
        sequence.addOperation(Operation.copy((VariableMatrix)tokens.getLast().getVariable(),(VariableMatrix)result));

        return sequence;
    }

    private void parseOperations( TokenList.Token token , char ops[] , TokenList tokens, Sequence sequence ) {

        if( !token.isVariable() )
            throw new RuntimeException("The first token in an equation needs to be a variable");

        boolean hasLeft = false;
        while( token != null ) {
            if( token.isVariable() ) {
                if( hasLeft ) {
                    if( token.previous.isVariable() ) {
                        throw new RuntimeException("Two variables next to each other");
                    }
                    if( isTargetOp(token.previous,ops)) {
                        token = createOp(token.previous.previous,token.previous,token,tokens,sequence);
                    }
                } else {
                    hasLeft = true;
                }
            } else {
                if( !token.previous.isVariable() ) {
                    throw new RuntimeException("Two symbols next to each other");
                }
            }
            token = token.next;
        }
    }

    private TokenList.Token createOp( TokenList.Token tvar0 , TokenList.Token op , TokenList.Token tvar1 ,
                                TokenList tokens , Sequence sequence ) {
        VariableMatrix var0 = (VariableMatrix)tvar0.getVariable();
        VariableMatrix var1 = (VariableMatrix)tvar1.getVariable();

        VariableMatrix output = VariableMatrix.createTemp();

        switch( op.symbol ) {
            case '+':
                sequence.addOperation(Operation.mAdd(var0,var1,output));
                break;

            case '-':
                sequence.addOperation(Operation.mSub(var0,var1,output));
                break;

            case '*':
                sequence.addOperation(Operation.mMult(var0,var1,output));
                break;

            default: throw new RuntimeException("Unknown operation "+op.symbol);
        }

        // replace the symbols with their output
        TokenList.Token t = new TokenList.Token(output);
        tokens.remove(tvar0);
        tokens.remove(tvar1);
        tokens.replace(op,t);
        return t;

    }

    private Variable lookupVariable(String token) {
        Variable result = variables.get(token);
        if( result == null )
            throw new RuntimeException("Unknown variable "+token);
        return result;
    }

    /**
     * Parses the text string to extract tokens
     */
    protected TokenList extractTokens(String equation) {
        TokenList tokens = new TokenList();

        int length = 0;
        boolean word = false;
        for( int i = 0; i < equation.length(); i++ ) {
            char c = equation.charAt(i);
            if( word ) {
                if( isLetter(c) ) {
                    storage[length++] = c;
                } else {
                    // add the variable/function name to token list
                    tokens.add( lookupVariable(new String(storage,0,length)));
                    word = false;
                    // if it's a special character add it.  If whitespace ignore it
                    if( c == '*' || c == '+' || c == '-' || c == '(' || c == ')' || c == '=') {
                        tokens.add(c);
                    }
                }
            } else {
                if( c == '*' || c == '+' || c == '-' || c == '(' || c == ')' || c == '=') {
                    tokens.add(c);
                } else if( c == ' ' || c == '\t' || c =='\n') {
                    continue;// ignore white space
                } else {
                    // start adding to the word
                    word = true;
                    storage[0] = c;
                    length = 1;
                }
            }
        }

        return tokens;
    }

    private static boolean isTargetOp( TokenList.Token token , char[] ops ) {
        char c = token.symbol;
        for (int i = 0; i < ops.length; i++) {
            if( c == ops[i])
                return true;
        }
        return false;
    }

    private static boolean isLetter( char c ) {
        return !(c == '*' || c == '+' || c == '-' || c == '(' || c == ')' || c == '=' || c == ' ' || c == '\t' || c == '\n');
    }

    /**
     * Compiles and performs the provided equation.
     *
     * @param equation String in simple equation format
     */
    public void process( String equation ) {
        compile(equation).perform();
    }
}
