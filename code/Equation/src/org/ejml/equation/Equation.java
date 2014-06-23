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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.ejml.equation.TokenList.Type;

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
// TODO Recycle temporary variables
public class Equation {
    HashMap<String,Variable> variables = new HashMap<String, Variable>();

    char storage[] = new char[1024];

    ManagerFunctions functions = new ManagerFunctions();

    /**
     * Adds a new Matrix variable
     * @param variable Matrix which is to be assigned to name
     * @param name The name of the variable
     */
    public void alias( DenseMatrix64F variable , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");
        variables.put(name,new VariableMatrix(variable));
    }

    /**
     * Adds a new floating point variable
     * @param value Value of the number
     * @param name Name in code
     */
    public void alias( double value , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");
        variables.put(name,new VariableDouble(value));
    }

    /**
     * Parses the equation and compiles it into a sequence which can be executed later on
     * @param equation String in simple equation format.
     * @return Sequence of operations on the variables
     */
    // TODO handle transpose
    // TODO tokenize integers
    // TODO tokenize floats
    // TODO handle two character (element-wise) operators
    // TODO handle functions with 2+ inputs
    // TODO reference sub-matrices
    public Sequence compile( String equation ) {
        ManagerTempVariables managerTemp = new ManagerTempVariables();
        functions.setManagerTemp(managerTemp);

        TokenList tokens = extractTokens(equation);

        if( tokens.size() < 3 )
            throw new RuntimeException("Too few tokens");

        TokenList.Token t0 = tokens.getFirst();
        TokenList.Token t1 = t0.next;

        if( t1.getType() != Type.SYMBOL || t1.symbol != '=')
            throw new RuntimeException("Expected '=' symbol not "+t1);

        // Get the results variable
        if( t0.getType() != Type.VARIABLE)
            throw new RuntimeException("Expected variable name first.  Not "+t0);

        Variable result = t0.getVariable();

        // now it gets interesting
        Sequence sequence = new Sequence();

        TokenList tokensRight = tokens.extractSubList(t1.next,tokens.last);
        handleParentheses( tokensRight ,sequence);

        // see if it needs to be parsed more
        if( tokensRight.size() != 1 )
            throw new RuntimeException("BUG");
        if( tokensRight.getLast().getType() != Type.VARIABLE )
            throw new RuntimeException("BUG the last token must be a variable");

        // copy the results into the output
        sequence.addOperation(Operation.copy(tokensRight.getFirst().getVariable(),result));

        return sequence;
    }

    /**
     * Searches for pairs of parentheses and processes blocks inside of them.  Embedded parentheses are handled
     * with no problem.  On output only a single token should be in tokens.
     * @param tokens List of parsed tokens
     * @param sequence Sequence of operators
     */
    protected void handleParentheses( TokenList tokens, Sequence sequence ) {
        List<TokenList.Token> left = new ArrayList<TokenList.Token>();
        List<TokenList.Token> right = new ArrayList<TokenList.Token>();

        // find all of them
        TokenList.Token t = tokens.first;
        while( t != null ) {
            if( t.getType() == Type.SYMBOL ) {
                if( t.getSymbol() == '(' )
                    left.add(t);
                else if( t.getSymbol() == ')' )
                    right.add(t);
            }
            t = t.next;

            if( left.size() >= 1 && left.size() == right.size() ) {
                // handle the code inside the one or more embedded parentheses

                // process from last to first
                for (int i = left.size()-1; i >= 0; i--) {
                    TokenList.Token a = left.get(i);
                    TokenList.Token b = right.get(left.size()-1-i);

                    // remember the element before so the new one can be inserted afterwards
                    TokenList.Token before = a.previous;

                    TokenList sublist = tokens.extractSubList(a,b);
                    // remove parentheses
                    sublist.remove(sublist.first);
                    sublist.remove(sublist.last);

                    TokenList.Token output = parseBlockNoParentheses(sublist,sequence);
                    // if its a function before () then the () indicates its an input to a function
                    if( before != null && before.getType() == Type.FUNCTION ) {
                        if( output == null )
                            throw new RuntimeException("Empty function input parameters");
                        else {
                            createFunction(before,output,tokens,sequence);
                        }
                    } else {
                        // if null then it was empty inside
                        if (output != null)
                            tokens.insert(before, output);
                    }
                }

                // reset and look for the next set
                left.clear();
                right.clear();
            }
        }

        if( !left.isEmpty() || !right.isEmpty() )
            throw new RuntimeException("Dangling parentheses");

        if( tokens.size() > 1 ) {
            parseBlockNoParentheses(tokens,sequence);
        }
    }

    /**
     * Parses a code block with no parentheses.  After it is done there should be a single token left, which
     * is returned.
     */
    protected TokenList.Token parseBlockNoParentheses(TokenList tokens, Sequence sequence ) {
        // process operators depending on their priority
        parseOperations(new char[]{'*','/'},tokens,sequence);
        parseOperations(new char[]{'+','-'},tokens,sequence);

        if( tokens.size() > 1 )
            throw new RuntimeException("BUG in parser.  There should only be a single token left");

        return tokens.first;
    }

    /**
     * Parses all tokens after input 'token' and adds operations to sequence.
     *
     * @param ops List of operations which should be parsed
     * @param tokens List of all the tokens
     * @param sequence List of operation sequence
     */
    protected void parseOperations( char ops[] , TokenList tokens, Sequence sequence) {

        if( tokens.size == 0 )
            return;

        TokenList.Token token = tokens.first;

        if( token.getType() != Type.VARIABLE )
            throw new RuntimeException("The first token in an equation needs to be a variable and not "+token);

        boolean hasLeft = false;
        while( token != null ) {
            if( token.getType() == Type.FUNCTION ) {
                throw new RuntimeException("Function encountered with no parentheses");
            } else if( token.getType() == Type.VARIABLE ) {
                if( hasLeft ) {
                    if( token.previous.getType() == Type.VARIABLE ) {
                        throw new RuntimeException("Two variables next to each other");
                    }
                    if( isTargetOp(token.previous,ops)) {
                        token = createOp(token.previous.previous,token.previous,token,tokens,sequence);
                    }
                } else {
                    hasLeft = true;
                }
            } else {
                if( token.previous.getType() == Type.SYMBOL ) {
                    throw new RuntimeException("Two symbols next to each other");
                }
            }
            token = token.next;
        }
    }

    /**
     * Adds a new operation to the list from the operation and two variables.  The inputs are removed
     * from the token list and replaced by their output.
     */
    protected TokenList.Token createOp( TokenList.Token left , TokenList.Token op , TokenList.Token right ,
                                      TokenList tokens , Sequence sequence )
    {
        Operation.Info info = functions.create(op.symbol, left.getVariable(), right.getVariable());

        sequence.addOperation(info.op);

        // replace the symbols with their output
        TokenList.Token t = new TokenList.Token(info.output);
        tokens.remove(left);
        tokens.remove(right);
        tokens.replace(op,t);
        return t;

    }

    /**
     * Adds a new operation to the list from the operation and two variables.  The inputs are removed
     * from the token list and replaced by their output.
     */
    protected TokenList.Token createFunction( TokenList.Token name , TokenList.Token input , TokenList tokens , Sequence sequence )
    {
        Operation.Info info = functions.create(name.getFunction().getName(),input.getVariable());

        sequence.addOperation(info.op);

        // just a sanity check and reminder/comment
        if( name.next == input )
            throw new RuntimeException("BUG:  I assumed that input had not been added to the list yet");

        // replace the symbols with the function's output
        TokenList.Token t = new TokenList.Token(info.output);
        tokens.replace(name, t);
        return t;

    }

    /**
     * Looks up a variable given its name.  If none is found then return null.
     */
    protected <T extends Variable> T lookupVariable(String token) {
        Variable result = variables.get(token);
        return (T)result;
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
                    String name = new String(storage,0,length);
                    Variable v = lookupVariable(name);
                    if( v == null ) {
                        if( functions.isFunctionName(name)) {
                            tokens.add(new Function(name));
                        } else {
                            throw new RuntimeException("word '"+name+"' is neither a variable or function");
                        }
                    } else {
                        tokens.add(v);
                    }

                    word = false;
                    // if it's a special character add it.  If whitespace ignore it
                    if( isOperator(c) ) {
                        tokens.add(c);
                    }
                }
            } else {
                if( isOperator(c) ) {
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
        if( word ) {
            tokens.add( lookupVariable(new String(storage,0,length)));
        }

        return tokens;
    }

    protected static boolean isTargetOp( TokenList.Token token , char[] ops ) {
        char c = token.symbol;
        for (int i = 0; i < ops.length; i++) {
            if( c == ops[i])
                return true;
        }
        return false;
    }

    protected static boolean isOperator( char c ) {
        return c == '*' || c == '/' || c == '+' || c == '-' || c == '(' || c == ')' || c == '=' || c == '\'';
    }

    protected static boolean isLetter( char c ) {
        return !(isOperator(c) || c == ' ' || c == '\t' || c == '\n');
    }

    protected static boolean isReserved( String name ) {
        for( String s : Operation.functionNames ) {
            if( name.equals(s))
                return true;
        }
        for (int i = 0; i < name.length(); i++) {
            if( !isLetter(name.charAt(i)) )
                return true;
        }
        return false;
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
