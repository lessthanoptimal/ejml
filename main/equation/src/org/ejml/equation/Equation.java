/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.ejml.equation.TokenList.Type;

/**
 * <p>
 * Equation allows the user to manipulate matrices in a more compact symbolic way, similar to Matlab and Octave.
 * Aliases are made to Matrices and scalar values which can then be manipulated by specifying an equation in a string.
 * These equations can either be "pre-compiled" [1] into a sequence of operations or immediately executed.  While the
 * former is more verbose, when dealing with small matrices it significantly faster and runs close to the speed of
 * normal hand written code.
 * </p>
 * <p>
 * Each string represents a single line and must have one and only one assignment '=' operator.  Temporary variables
 * are handled transparently to the user.  Temporary variables are declared at compile time, but resized at runtime.
 * If the inputs are not resized and the code is precompiled, then no new memory will be declared.  When a matrix
 * is assigned the results of an operation it is resized so that it can store the results.
 * </p>
 * <p>
 * The compiler currently produces simplistic code.  For example, if it encounters the following equation "a = b*c' it
 * will not invoke multTransB(b,c,a), but will explicitly transpose c and then call mult().  In the future it
 * will recognize such short cuts.
 * </p>
 *
 * <p>
 * Usage example:
 * <pre>
 * Equation eq = new Equation();
 * eq.alias(x,"x", P,"P", Q,"Q");
 *
 * eq.process("x = F*x");
 * eq.process("P = F*P*F' + Q");
 * </pre>
 * Which will modify the matrices 'x' and 'P'.  Support for sub-matrices and inline matrix construction is also
 * available.
 * <pre>
 * eq.process("x = [2 1 0; 0 1 3;4 5 6]*x");  // create a 3x3 matrix then multiply it by x
 * eq.process("x(1:3,5:9) = [a ; b]*2");      // fill the sub-matrix with the result
 * eq.process("x(:) = a(4:2:20)");            // fill all elements of x with the specified elements in 'a'
 * eq.process("x( 4 3 ) = a");                // fill only the specified number sequences with 'a'
 * eq.process("x = [2:3:25 1 4]");            // create a row matrix from the number sequence
 * </pre>
 * </p>
 *
 * <p>
 * To pre-compile one of the above lines, do the following instead:
 * <pre>
 * Sequence predictX = eq.compile("x = F*x");
 * predictX.perform();
 * </pre>
 * Then you can invoke it as much as you want without the "expensive" compilation step.  If you are dealing with
 * larger matrices (e.g. 100 by 100) then it is likely that the compilation step has an insignificant runtime
 * cost.
 * </p>
 *
 * <p>
 * Variables can also be lazily declared and their type inferred under certain conditions.  For example:
 * <pre>
 * eq.alias(A,"A", B,"B");
 * eq.process("C = A*B");
 * DenseMatrix64F C = eq.lookupMatrix("C");
 * </pre>
 * In this case 'C' was lazily declared.  To access the variable, or any others, you can use one of the lookup*()
 * functions.
 * </p>
 *
 * <p>
 * Sometimes you don't get the results you expect and it can be helpful to print out the tokens and which operations
 * the compiler selected.  To do this set the second parameter to eq.compile() or eq.process() to true:
 * <pre>
 * Code:
 * eq.process("C=2.1*B'*A",true);
 *
 * Output:
 * Parsed tokens:
 * ------------
 * Word:C
 * ASSIGN
 * VarSCALAR
 * TIMES
 * VarMATRIX
 * TRANSPOSE
 * TIMES
 * VarMATRIX
 *
 * Operations:
 * ------------
 * transpose-m
 * multiply-ms
 * multiply-mm
 * copy-mm
 * </pre>
 * </p>
 *
 * <p>
 * <h2>Built in Constants</h2>
 * <pre>
 * pi = Math.PI
 * e  = Math.E
 * </pre>
 * </p>
 *
 * <p>
 * <h2>Supported functions</h2>
 * <pre>
 * eye(N)       Create an identity matrix which is N by N.
 * eye(A)       Create an identity matrix which is A.numRows by A.numCols
 * normF(A)     Frobenius normal of the matrix.
 * det(A)       Determinant of the matrix
 * inv(A)       Inverse of a matrix
 * pinv(A)      Pseudo-inverse of a matrix
 * rref(A)      Reduced row echelon form of A
 * trace(A)     Trace of the matrix
 * zeros(r,c)   Matrix full of zeros with r rows and c columns.
 * ones(r,c)    Matrix full of ones with r rows and c columns.
 * diag(A)      If a vector then returns a square matrix with diagonal elements filled with vector
 * diag(A)      If a matrix then it returns the diagonal elements as a column vector
 * dot(A,B)     Returns the dot product of two vectors as a double.  Does not work on general matrices.
 * solve(A,B)   Returns the solution X from A*X = B.
 * kron(A,B)    Kronecker product
 * abs(A)       Absolute value of A.
 * max(A)       Element with the largest value in A.
 * min(A)       Element with the smallest value in A.
 * pow(a,b)     Computes a to the power of b.  Can also be invoked with "a^b" scalars only.
 * sqrt(a)      Computes the square root of a.
 * sin(a)       Math.sin(a) for scalars only
 * cos(a)       Math.cos(a) for scalars only
 * atan(a)      Math.atan(a) for scalars only
 * atan2(a,b)   Math.atan2(a,b) for scalars only
 * exp(a)       Math.exp(a) for scalars is also an element-wise matrix operator
 * log(a)       Math.log(a) for scalars is also an element-wise matrix operator
 * </pre>
 * </p>
 *
 * <p>
 * <h2>Supported operations</h2>
 * <pre>
 * '*'        multiplication (Matrix-Matrix, Scalar-Matrix, Scalar-Scalar)
 * '+'        addition (Matrix-Matrix, Scalar-Matrix, Scalar-Scalar)
 * '-'        subtraction (Matrix-Matrix, Scalar-Matrix, Scalar-Scalar)
 * '/'        divide (Matrix-Scalar, Scalar-Scalar)
 * '/'        matrix solve "x=b/A" is equivalent to x=solve(A,b) (Matrix-Matrix)
 * '^'        Scalar power.  a^b is a to the power of b.
 * '\'        left-divide.  Same as divide but reversed.  e.g. x=A\b is x=solve(A,b)
 * '.*'       element-wise multiplication (Matrix-Matrix)
 * './'       element-wise division (Matrix-Matrix)
 * '.^'       element-wise power. (scalar-scalar) (matrix-matrix) (scalar-matrix) (matrix-scalar)
 * '''        matrix transpose
 * '='        assignment by value (Matrix-Matrix, Scalar-Scalar)
 * </pre>
 * Order of operations:  [ ' ] precedes [ ^ .^ ] precedes [ *  /  .*  ./ ] precedes [ +  - ]
 * </p>
 *
 * <p>
 * <h2>Specialized submatrix and matrix construction syntax</h2>
 * <pre>
 * Extracts a sub-matrix from A with rows 1 to 10 (inclusive) and column 3.
 *               A(1:10,3)
 * Extracts a sub-matrix from A with rows 2 to numRows-1 (inclusive) and all the columns.
 *               A(2:,:)
 * Will concat A and B along their columns and then concat the result with  C along their rows.
 *                [A,B;C]
 * Defines a 3x2 matrix.
 *            [1 2; 3 4; 4 5]
 * You can also perform operations inside:
 *            [[2 3 4]';[4 5 6]']
 * Will assign B to the sub-matrix in A.
 *             A(1:3,4:8) = B
 * </pre>
 * </p>
 *
 * <h2>Integer Number Sequences</h2>
 * Previous example code has made much use of integer number sequences. There are three different types of integer number
 * sequences 'explicit', 'for', and 'for-range'.
 * <pre>
 * 1) Explicit:
 *    Example: "1 2 4 0"
 *    Example: "1 2,-7,4"     Commas needed to create negative numbers. Otherwise it will be subtraction.
 * 2) for:
 *    Example:  "2:10"        Sequence of "2 3 4 5 6 7 8 9 10"
 *    Example:  "2:2:10"      Sequence of "2 4 6 8 10"
 * 3) for-range:
 *    Example:  "2:"          Sequence of "2 3 ... max"
 *    Example:  "2:2:"        Sequence of "2 4 ... max"
 * 4) combined:
 *    Example:  "1 2 7:10"    Sequence of "1 2 7 8 9 10"
 * </pre>
 *
 * <h2>Macros</h2>
 * Macros are used to insert patterns into the code.  Consider this example:
 * <pre>
 * eq.process("macro ata( a ) = (a'*a)");
 * eq.process("b = ata(c)");
 * </pre>
 * The first line defines a macro named "ata" with one parameter 'a'.  When compiled the equation in the second
 * line is replaced with "b = (a'*a)".  The "(" ")" in the macro isn't strictly necissary in this situation, but
 * is a good practice.  Consider the following.
 * <pre>
 * eq.process("b = ata(c)*r");
 * </pre>
 * Will become "b = (a'*a)*r"  but with out () it will be "b = a'*a*r" which is not the same thing!
 *
 * <p><b>NOTE:</b>In the future macros might be replaced with functions.  Macros are harder for the user to debug, but
 * functions are harder for EJML's developer to implement.</p>
 *
 * <h2>Footnotes:</h2>
 * <pre>
 * [1] It is not compiled into Java byte-code, but into a sequence of operations stored in a List.
 * </pre>
 *
 * @author Peter Abeles
 */
// TODO Change parsing so that operations specify a pattern.
// TODO Recycle temporary variables
// TODO intelligently handle identity matrices
public class Equation {
    HashMap<String,Variable> variables = new HashMap<String, Variable>();
    HashMap<String,Macro> macros = new HashMap<String, Macro>();

    // storage for a single word in the tokenizer
    char storage[] = new char[1024];

    ManagerFunctions functions = new ManagerFunctions();

    public Equation() {
        alias(Math.PI,"pi");
        alias(Math.E,"e");
    }

    /**
     * Adds a new Matrix variable.  If one already has the same name it is written over.
     *
     * While more verbose for multiple variables, this function doesn't require new memory be declared
     * each time it's called.
     *
     * @param variable Matrix which is to be assigned to name
     * @param name The name of the variable
     */
    public void alias( DenseMatrix64F variable , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");
        VariableMatrix old = (VariableMatrix)variables.get(name);
        if( old == null ) {
            variables.put(name, new VariableMatrix(variable));
        }else {
            old.matrix = variable;
        }
    }

    public void alias( SimpleMatrix variable , String name ) {
        alias(variable.getMatrix(),name);
    }

    /**
     * Adds a new floating point variable. If one already has the same name it is written over.
     * @param value Value of the number
     * @param name Name in code
     */
    public void alias( double value , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");

        VariableDouble old = (VariableDouble)variables.get(name);
        if( old == null ) {
            variables.put(name, new VariableDouble(value));
        }else {
            old.value = value;
        }
    }

    /**
     * Adds a new integer variable. If one already has the same name it is written over.
     * @param value Value of the number
     * @param name Name in code
     */
    public void alias( int value , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");

        VariableInteger old = (VariableInteger)variables.get(name);
        if( old == null ) {
            variables.put(name, new VariableInteger(value));
        }else {
            old.value = value;
        }
    }

    private void alias( IntegerSequence sequence , String name ) {
        if( isReserved(name))
            throw new RuntimeException("Reserved word or contains a reserved character");

        VariableIntegerSequence old = (VariableIntegerSequence)variables.get(name);
        if( old == null ) {
            variables.put(name, new VariableIntegerSequence(sequence));
        }else {
            old.sequence = sequence;
        }
    }

    /**
     * Creates multiple aliases at once.
     */
    public void alias( Object ...args ) {
        if( args.length % 2 == 1 )
            throw new RuntimeException("Even number of arguments expected");

        for (int i = 0; i < args.length; i += 2) {
            if( args[i].getClass() == Integer.class ) {
                alias(((Integer)args[i]).intValue(),(String)args[i+1]);
            } else if( args[i].getClass() == Double.class ) {
                alias(((Double)args[i]).doubleValue(),(String)args[i+1]);
            } else if( args[i].getClass() == DenseMatrix64F.class ) {
                alias((DenseMatrix64F)args[i],(String)args[i+1]);
            } else if( args[i].getClass() == SimpleMatrix.class ) {
                alias((SimpleMatrix)args[i],(String)args[i+1]);
            } else {
                throw new RuntimeException("Unknown value type "+args[i]);
            }
        }
    }

    public Sequence compile( String equation ) {
        return compile(equation,false);
    }

    /**
     * Parses the equation and compiles it into a sequence which can be executed later on
     * @param equation String in simple equation format.
     * @param debug if true it will print out debugging information
     * @return Sequence of operations on the variables
     */
    public Sequence compile( String equation , boolean debug ) {

        ManagerTempVariables managerTemp = new ManagerTempVariables();
        functions.setManagerTemp(managerTemp);

        Sequence sequence = new Sequence();
        TokenList tokens = extractTokens(equation,managerTemp);

        if( tokens.size() < 3 )
            throw new RuntimeException("Too few tokens");

        TokenList.Token t0 = tokens.getFirst();

        if( t0.word != null && t0.word.compareToIgnoreCase("macro") == 0 ) {
            parseMacro(tokens,sequence);
        } else {
            insertFunctionsAndVariables(tokens);
            insertMacros(tokens);
            if (debug) {
                System.out.println("Parsed tokens:\n------------");
                tokens.print();
                System.out.println();
            }

            // Get the results variable
            if (t0.getType() != Type.VARIABLE && t0.getType() != Type.WORD)
                throw new ParseError("Expected variable name first.  Not " + t0);

            // see if it is assign or a range
            List<Variable> range = parseAssignRange(sequence, tokens, t0);

            TokenList.Token t1 = t0.next;
            if (t1.getType() != Type.SYMBOL || t1.getSymbol() != Symbol.ASSIGN)
                throw new ParseError("Expected assignment operator next");

            // Parse the right side of the equation
            TokenList tokensRight = tokens.extractSubList(t1.next, tokens.last);
            checkForUnknownVariables(tokensRight);
            handleParentheses(tokensRight, sequence);

            // see if it needs to be parsed more
            if (tokensRight.size() != 1)
                throw new RuntimeException("BUG");
            if (tokensRight.getLast().getType() != Type.VARIABLE)
                throw new RuntimeException("BUG the last token must be a variable");

            // copy the results into the output
            Variable variableRight = tokensRight.getFirst().getVariable();
            if (range == null) {
                // no range, so copy results into the entire output matrix
                Variable output = createVariableInferred(t0, variableRight);
                sequence.addOperation(Operation.copy(variableRight, output));
            } else {
                // a sub-matrix range is specified.  Copy into that inner part
                if (t0.getType() == Type.WORD) {
                    throw new ParseError("Can't do lazy variable initialization with submatrices. " + t0.getWord());
                }
                sequence.addOperation(Operation.copy(variableRight, t0.getVariable(), range));
            }

            if (debug) {
                System.out.println("Operations:\n------------");
                for (int i = 0; i < sequence.operations.size(); i++) {
                    System.out.println(sequence.operations.get(i).name());
                }
            }
        }

        return sequence;
    }

    /**
     * Parse a macro defintion.
     *
     * "macro NAME( var0 , var1 ) = 5+var0+var1'
     */
    private void parseMacro( TokenList tokens , Sequence sequence ) {
        Macro macro = new Macro();

        TokenList.Token t = tokens.getFirst().next;

        if( t.word == null ) {
            throw new ParseError("Expected the macro's name after "+tokens.getFirst().word);
        }
        List<TokenList.Token> variableTokens = new ArrayList<TokenList.Token>();

        macro.name = t.word;
        t = t.next;
        t = parseMacroInput(variableTokens, t);
        for( TokenList.Token a : variableTokens ) {
            if( a.word == null) throw new ParseError("expected word in macro header");
            macro.inputs.add(a.word);
        }
        t = t.next;
        if( t == null || t.getSymbol() != Symbol.ASSIGN)
            throw new ParseError("Expected assignment");
        t = t.next;
        macro.tokens = new TokenList(t,tokens.last);

        sequence.addOperation(macro.createOperation(macros));
    }


    private TokenList.Token parseMacroInput(List<TokenList.Token> variables, TokenList.Token t) {
        if( t.getSymbol() != Symbol.PAREN_LEFT ) {
            throw new ParseError("Expected (");
        }
        t = t.next;
        boolean expectWord = true;
        while( t != null && t.getSymbol() != Symbol.PAREN_RIGHT ) {
            if( expectWord ) {
                variables.add(t);
                expectWord = false;
            } else {
                if( t.getSymbol() != Symbol.COMMA )
                    throw new ParseError("Expected comma");
                expectWord = true;
            }

            t = t.next;
        }
        if( t == null )
            throw new ParseError("Token sequence ended unexpectedly");
        return t;
    }

    /**
     * Examines the list of variables for any unknown variables and throws an exception if one is found
     */
    private void checkForUnknownVariables(TokenList tokens) {
        TokenList.Token t = tokens.getFirst();
        while( t != null ) {
            if( t.getType() == Type.WORD )
                throw new ParseError("Unknown variable on right side. "+t.getWord());
            t = t.next;
        }
    }

    /**
     * Infer the type of and create a new output variable using the results from the right side of the equation.
     * If the type is already known just return that.
     */
    private Variable createVariableInferred(TokenList.Token t0, Variable variableRight) {
        Variable result;

        if( t0.getType() == Type.WORD ) {
            switch( variableRight.getType()) {
                case MATRIX:
                    alias(new DenseMatrix64F(1,1),t0.getWord());
                    break;

                case SCALAR:
                    if( variableRight instanceof VariableInteger) {
                        alias(0,t0.getWord());
                    } else {
                        alias(1.0,t0.getWord());
                    }
                    break;

                case INTEGER_SEQUENCE:
                    alias((IntegerSequence)null,t0.getWord());
                    break;

                default:
                    throw new RuntimeException("Type not supported for assignment: "+variableRight.getType());
            }

            result = variables.get(t0.getWord());
        } else {
            result = t0.getVariable();
        }
        return result;
    }

    /**
     * See if a range for assignment is specified.  If so return the range, otherwise return null
     *
     * Example of assign range:
     *    a(0:3,4:5) = blah
     */
    private List<Variable> parseAssignRange(Sequence sequence, TokenList tokens, TokenList.Token t0) {
        List<Variable> range;
        TokenList.Token t1 = t0.next;
        if( t1.getType() == Type.SYMBOL ) {
            if( t1.symbol == Symbol.ASSIGN ) {
                range = null; // copy into the entire matrix
            } else if( t1.symbol == Symbol.PAREN_LEFT ) {
                // find the right parentheses
                TokenList.Token t2 = t1.next;
                while( t2 != null && t2.symbol != Symbol.PAREN_RIGHT ) {
                    t2 = t2.next;
                }

                if( t2 == null )
                    throw new ParseError("Could not find closing )");

                TokenList.Token n = t2.next;
                TokenList sublist = tokens.extractSubList(t1.next,t2.previous);
                // need to remove paren also
                tokens.remove(t1);
                tokens.remove(t2);

                List<TokenList.Token> inputs = parseParameterCommaBlock(sublist, sequence);
                if (inputs.isEmpty())
                    throw new ParseError("Empty function input parameters");

                range = new ArrayList<Variable>();
                addSubMatrixVariables(inputs, range);
                if( range.size() != 1 && range.size() != 2 ) {
                    throw new ParseError("Unexpected number of range variables.  1 or 2 expected");
                }

                t1 = n;
                if( t1 == null || t1.symbol != Symbol.ASSIGN )
                    throw new ParseError("Expected assign after sub-matrix");
            } else {
                throw new ParseError("Expected assignment '=' or submatrix of variable being assigned '('");
            }
        } else {
            throw new ParseError("Expecting symbol after first variable");
        }
        return range;
    }

    /**
     * Searches for pairs of parentheses and processes blocks inside of them.  Embedded parentheses are handled
     * with no problem.  On output only a single token should be in tokens.
     * @param tokens List of parsed tokens
     * @param sequence Sequence of operators
     */
    protected void handleParentheses( TokenList tokens, Sequence sequence ) {
        // have a list to handle embedded parentheses, e.g. (((((a)))))
        List<TokenList.Token> left = new ArrayList<TokenList.Token>();

        // find all of them
        TokenList.Token t = tokens.first;
        while( t != null ) {
            TokenList.Token next = t.next;
            if( t.getType() == Type.SYMBOL ) {
                if( t.getSymbol() == Symbol.PAREN_LEFT  )
                    left.add(t);
                else if( t.getSymbol() == Symbol.PAREN_RIGHT ) {
                    if( left.isEmpty() )
                        throw new ParseError(") found with no matching (");

                    TokenList.Token a = left.remove(left.size()-1);

                    // remember the element before so the new one can be inserted afterwards
                    TokenList.Token before = a.previous;

                    TokenList sublist = tokens.extractSubList(a,t);
                    // remove parentheses
                    sublist.remove(sublist.first);
                    sublist.remove(sublist.last);

                    // if its a function before () then the () indicates its an input to a function
                    if( before != null && before.getType() == Type.FUNCTION ) {
                        List<TokenList.Token> inputs = parseParameterCommaBlock(sublist, sequence);
                        if (inputs.isEmpty())
                            throw new ParseError("Empty function input parameters");
                        else {
                            createFunction(before, inputs, tokens, sequence);
                        }
                    } else if( before != null && before.getType() == Type.VARIABLE &&
                            before.getVariable().getType() == VariableType.MATRIX ) {
                        // if it's a variable then that says it's a sub-matrix
                        TokenList.Token extract = parseSubmatrixToExtract(before,sublist, sequence);
                        // put in the extract operation
                        tokens.insert(before,extract);
                        tokens.remove(before);
                    } else {
                        // if null then it was empty inside
                        TokenList.Token output = parseBlockNoParentheses(sublist,sequence, false);
                        if (output != null)
                            tokens.insert(before, output);
                    }
                }
            }
            t = next;
        }

        if( !left.isEmpty())
            throw new ParseError("Dangling ( parentheses");

        if( tokens.size() > 1 ) {
            parseBlockNoParentheses(tokens, sequence, false);
        }
    }

    /**
     * Searches for commas in the set of tokens.  Used for inputs to functions
     *
     * @return List of output tokens between the commas
     */
    protected List<TokenList.Token> parseParameterCommaBlock( TokenList tokens, Sequence sequence ) {
        // find all the comma tokens
        List<TokenList.Token> commas = new ArrayList<TokenList.Token>();
        TokenList.Token token = tokens.first;

        while( token != null ) {
            if( token.getType() == Type.SYMBOL && token.getSymbol() == Symbol.COMMA ) {
                commas.add(token);
            }
            token = token.next;
        }

        List<TokenList.Token> output = new ArrayList<TokenList.Token>();
        if( commas.isEmpty() ) {
            output.add(parseBlockNoParentheses(tokens, sequence, false));
        } else {
            TokenList.Token before = tokens.first;
            for (int i = 0; i < commas.size(); i++) {
                TokenList.Token after = commas.get(i);
                if( before == after )
                    throw new ParseError("No empty function inputs allowed!");
                TokenList.Token tmp = after.next;
                TokenList sublist = tokens.extractSubList(before,after);
                sublist.remove(after);// remove the comma
                output.add(parseBlockNoParentheses(sublist, sequence, false));
                before = tmp;
            }

            // if the last character is a comma then after.next above will be null and thus before is null
            if( before == null )
                throw new ParseError("No empty function inputs allowed!");

            TokenList.Token after = tokens.last;
            TokenList sublist = tokens.extractSubList(before, after);
            output.add(parseBlockNoParentheses(sublist, sequence, false));
        }

        return output;
    }

    /**
     * Converts a submatrix into an extract matrix operation.
     * @param variableTarget The variable in which the submatrix is extracted from
     */
    protected TokenList.Token parseSubmatrixToExtract(TokenList.Token variableTarget,
                                                      TokenList tokens, Sequence sequence) {


        List<TokenList.Token> inputs = parseParameterCommaBlock(tokens, sequence);

        List<Variable> variables = new ArrayList<Variable>();

        // for the operation, the first variable must be the matrix which is being manipulated
        variables.add(variableTarget.getVariable());

        addSubMatrixVariables(inputs, variables);
        if( variables.size() != 2 && variables.size() != 3 ) {
            throw new ParseError("Unexpected number of variables.  1 or 2 expected");
        }

        // first parameter is the matrix it will be extracted from.  rest specify range
        Operation.Info info;

        // only one variable means its referencing elements
        // two variables means its referencing a sub matrix
        if( inputs.size() == 1 ) {
            Variable varA = variables.get(1);
            if( varA.getType() == VariableType.SCALAR ) {
                info = functions.create("extractScalar", variables);
            } else {
                info = functions.create("extract", variables);
            }
        } else if( inputs.size() == 2 ) {
            Variable varA = variables.get(1);
            Variable varB = variables.get(2);

            if( varA.getType() == VariableType.SCALAR && varB.getType() == VariableType.SCALAR) {
                info = functions.create("extractScalar", variables);
            } else {
                info = functions.create("extract", variables);
            }
        } else {
            throw new ParseError("Expected 2 inputs to sub-matrix");
        }

        sequence.addOperation(info.op);

        return new TokenList.Token(info.output);
    }

    /**
     * Goes through the token lists and adds all the variables which can be used to define a sub-matrix.  If anything
     * else is found an excpetion is thrown
     */
    private void addSubMatrixVariables(List<TokenList.Token> inputs, List<Variable> variables) {
        for (int i = 0; i < inputs.size(); i++) {
            TokenList.Token t = inputs.get(i);
            if( t.getType() != Type.VARIABLE )
                throw new ParseError("Expected variables only in sub-matrix input, not "+t.getType());
            Variable v = t.getVariable();
            if( v.getType() == VariableType.INTEGER_SEQUENCE || isVariableInteger(t) ) {
                variables.add(v);
            } else {
                throw new ParseError("Expected an integer, integer sequence, or array range to define a submatrix");
            }
        }
    }

    /**
     * Parses a code block with no parentheses and no commas.  After it is done there should be a single token left,
     * which is returned.
     */
    protected TokenList.Token parseBlockNoParentheses(TokenList tokens, Sequence sequence, boolean insideMatrixConstructor) {

        // search for matrix bracket operations
        if( !insideMatrixConstructor ) {
            parseBracketCreateMatrix(tokens, sequence);
        }

        // First create sequences from anything involving a colon
        parseSequencesWithColons(tokens);

        // process operators depending on their priority
        parseNegOp(tokens, sequence);
        parseOperationsL(tokens, sequence);
        parseOperationsLR(new Symbol[]{Symbol.POWER, Symbol.ELEMENT_POWER}, tokens, sequence);
        parseOperationsLR(new Symbol[]{Symbol.TIMES, Symbol.RDIVIDE, Symbol.LDIVIDE, Symbol.ELEMENT_TIMES, Symbol.ELEMENT_DIVIDE}, tokens, sequence);
        parseOperationsLR(new Symbol[]{Symbol.PLUS, Symbol.MINUS}, tokens, sequence);

        // Commas are used in integer sequences.  Can be used to force to compiler to treat - as negative not
        // minus.  They can now be removed since they have served their purpose
        stripCommas(tokens);

        // now construct rest of the lists and combine them together
        parseIntegerLists(tokens);
        parseCombineIntegerLists(tokens);

        if( !insideMatrixConstructor ) {
            if (tokens.size() > 1)
                throw new RuntimeException("BUG in parser.  There should only be a single token left");

            return tokens.first;
        } else {
            return null;
        }
    }

    /**
     * Removes all commas from the token list
     */
    private void stripCommas(TokenList tokens) {
        TokenList.Token t = tokens.getFirst();

        while( t != null ) {
            TokenList.Token next = t.next;
            if( t.getSymbol() == Symbol.COMMA ) {
                tokens.remove(t);
            }
            t = next;
        }
    }

    /**
     * Searches for descriptions of integer sequences and array ranges that have a colon character in them
     *
     * Examples of integer sequences:
     * 1:6
     * 2:4:20
     * :
     *
     * Examples of array range
     * 2:
     * 2:4:
     */
    protected void parseSequencesWithColons(TokenList tokens ) {
        TokenList.Token t = tokens.getFirst();
        if( t == null )
            return;

        int state = 0;

        TokenList.Token start = null;
        TokenList.Token middle = null;
        TokenList.Token prev = t;

        boolean last = false;
        while( true ) {
            if( state == 0 ) {
                if( isVariableInteger(t) && (t.next != null && t.next.getSymbol() == Symbol.COLON) ) {
                    start = t;
                    state = 1;
                    t = t.next;
                } else if( t != null && t.getSymbol() == Symbol.COLON ) {
                    // If it starts with a colon then it must be 'all'  or a type-o
                    IntegerSequence range = new IntegerSequence.Range(null,null);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(range);
                    TokenList.Token n = new TokenList.Token(varSequence);
                    tokens.insert(t.previous, n);
                    tokens.remove(t);
                    t = n;
                }
            } else if( state == 1 ) {
                // var : ?
                if (isVariableInteger(t)) {
                    state = 2;
                } else {
                    // array range
                    IntegerSequence range = new IntegerSequence.Range(start,null);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(range);
                    replaceSequence(tokens, varSequence, start, prev);
                    state = 0;
                }
            } else if ( state == 2 ) {
                // var:var ?
                if( t != null && t.getSymbol() == Symbol.COLON ) {
                    middle = prev;
                    state = 3;
                } else {
                    // create for sequence with start and stop elements only
                    IntegerSequence sequence = new IntegerSequence.For(start,null,prev);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(sequence);
                    replaceSequence(tokens, varSequence, start, prev );
                    if( t != null )
                        t = t.previous;
                    state = 0;
                }
            } else if ( state == 3 ) {
                // var:var: ?
                if( isVariableInteger(t) ) {
                    // create 'for' sequence with three variables
                    IntegerSequence sequence = new IntegerSequence.For(start,middle,t);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(sequence);
                    t = replaceSequence(tokens, varSequence, start, t);
                } else {
                    // array range with 2 elements
                    IntegerSequence range = new IntegerSequence.Range(start,middle);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(range);
                    replaceSequence(tokens, varSequence, start, prev);
                }
                state = 0;
            }

            if( last ) {
                break;
            } else if( t.next == null ) {
                // handle the case where it is the last token in the sequence
                last = true;
            }
            prev = t;
            t = t.next;
        }
    }

    /**
     * Searches for a sequence of integers
     *
     * example:
     * 1 2 3 4 6 7 -3
     */
    protected void parseIntegerLists(TokenList tokens) {
        TokenList.Token t = tokens.getFirst();
        if( t == null || t.next == null )
            return;

        int state = 0;

        TokenList.Token start = null;
        TokenList.Token prev = t;

        boolean last = false;
        while( true ) {
            if( state == 0 ) {
                if( isVariableInteger(t) ) {
                    start = t;
                    state = 1;
                }
            } else if( state == 1 ) {
                // var ?
                if( isVariableInteger(t)) {                 // see if its explicit number sequence
                    state = 2;
                } else {  // just scalar integer, skip
                    state = 0;
                }
            } else if ( state == 2 ) {
                // var var ....
                if( !isVariableInteger(t) ) {
                    // create explicit list sequence
                    IntegerSequence sequence = new IntegerSequence.Explicit(start,prev);
                    VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(sequence);
                    replaceSequence(tokens, varSequence, start, prev);
                    state = 0;
                }
            }

            if( last ) {
                break;
            } else if( t.next == null ) {
                // handle the case where it is the last token in the sequence
                last = true;
            }
            prev = t;
            t = t.next;
        }
    }

    /**
     * Looks for sequences of integer lists and combine them into one big sequence
     */
    protected void parseCombineIntegerLists(TokenList tokens) {
        TokenList.Token t = tokens.getFirst();
        if( t == null || t.next == null )
            return;

        int numFound = 0;

        TokenList.Token start = null;
        TokenList.Token end = null;

        while( t != null ) {
            if( t.getType() == Type.VARIABLE && (isVariableInteger(t) ||
                    t.getVariable().getType() == VariableType.INTEGER_SEQUENCE )) {
                if( numFound == 0 ) {
                    numFound = 1;
                    start = end = t;
                } else {
                    numFound++;
                    end = t;
                }
            } else if( numFound > 1 ) {
                IntegerSequence sequence = new IntegerSequence.Combined(start,end);
                VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(sequence);
                replaceSequence(tokens, varSequence, start, end);
                numFound = 0;
            } else {
                numFound = 0;
            }
            t = t.next;
        }

        if( numFound > 1 ) {
            IntegerSequence sequence = new IntegerSequence.Combined(start,end);
            VariableIntegerSequence varSequence = functions.getManagerTemp().createIntegerSequence(sequence);
            replaceSequence(tokens, varSequence, start, end);
        }
    }

    private TokenList.Token replaceSequence(TokenList tokens, Variable target, TokenList.Token start, TokenList.Token end) {
        TokenList.Token tmp = new TokenList.Token(target);
        tokens.insert(start.previous, tmp);
        tokens.extractSubList(start, end);
        return tmp;
    }

    /**
     * Checks to see if the token is an integer scalar
     *
     * @return true if integer or false if not
     */
    private static boolean isVariableInteger(TokenList.Token t) {
        if( t == null )
            return false;

        return t.getScalarType() == VariableScalar.Type.INTEGER;
    }

    /**
     * Searches for brackets which are only used to construct new matrices by concatenating
     * 1 or more matrices together
     */
    protected void parseBracketCreateMatrix(TokenList tokens, Sequence sequence) {
        List<TokenList.Token> left = new ArrayList<TokenList.Token>();

        TokenList.Token t = tokens.getFirst();

        while( t != null ) {
            TokenList.Token next = t.next;
            if( t.getSymbol() == Symbol.BRACKET_LEFT ) {
                left.add(t);
            } else if( t.getSymbol() == Symbol.BRACKET_RIGHT ) {
                if( left.isEmpty() )
                    throw new RuntimeException("No matching left bracket for right");

                TokenList.Token start = left.remove(left.size() - 1);

                // Compute everything inside the [ ], this will leave a
                // series of variables and semi-colons hopefully
                TokenList bracketLet = tokens.extractSubList(start.next,t.previous);
                parseBlockNoParentheses(bracketLet, sequence, true);
                MatrixConstructor constructor = constructMatrix(bracketLet);

                // define the matrix op and inject into token list
                Operation.Info info = Operation.matrixConstructor(constructor);
                sequence.addOperation(info.op);

                tokens.insert(start.previous, new TokenList.Token(info.output));

                // remove the brackets
                tokens.remove(start);
                tokens.remove(t);
            }

            t = next;
        }

        if( !left.isEmpty() )
            throw new RuntimeException("Dangling [");
    }

    private MatrixConstructor constructMatrix(TokenList bracketLet) {
        // Go through the bracket and construct the matrix
        MatrixConstructor constructor = new MatrixConstructor(functions.getManagerTemp());

        TokenList.Token n = bracketLet.first;

        while( n != null ) {
            if( n.getType() == Type.VARIABLE ) {
                constructor.addToRow(n.getVariable());
            } else if( n.getType() == Type.SYMBOL ) {
                if( n.getSymbol() == Symbol.SEMICOLON ) {
                    constructor.endRow();
                }
            } else {
                throw new ParseError("Expected variable or symbol only");
            }
            n = n.next;
        }
        constructor.endRow();
        return constructor;
    }

    /**
     * Searches for cases where a minus sign means negative operator.  That happens when there is a minus
     * sign with a variable to its right and no variable to its left
     *
     * Example:
     *  a = - b * c
     */
    protected void parseNegOp(TokenList tokens, Sequence sequence) {
        if( tokens.size == 0 )
            return;

        TokenList.Token token = tokens.first;

        while( token != null ) {
            TokenList.Token next = token.next;
            escape:
            if( token.getSymbol() == Symbol.MINUS ) {
                if( token.previous != null && token.previous.getType() != Type.SYMBOL)
                    break escape;
                if( token.next == null || token.next.getType() == Type.SYMBOL)
                    break escape;

                if( token.next.getType() != Type.VARIABLE )
                    throw new RuntimeException("Crap bug rethink this function");

                // create the operation
                Operation.Info info = Operation.neg(token.next.getVariable(),functions.getManagerTemp());
                // add the operation to the sequence
                sequence.addOperation(info.op);
                // update the token list
                TokenList.Token t = new TokenList.Token(info.output);
                tokens.insert(token.next,t);
                tokens.remove(token.next);
                tokens.remove(token);
                next = t;
            }
            token = next;
        }
    }


    /**
     * Parses operations where the input comes from variables to its left only.  Hard coded to only look
     * for transpose for now
     *
     * @param tokens List of all the tokens
     * @param sequence List of operation sequence
     */
    protected void parseOperationsL(TokenList tokens, Sequence sequence) {

        if( tokens.size == 0 )
            return;

        TokenList.Token token = tokens.first;

        if( token.getType() != Type.VARIABLE )
            throw new ParseError("The first token in an equation needs to be a variable and not "+token);

        while( token != null ) {
            if( token.getType() == Type.FUNCTION ) {
                throw new ParseError("Function encountered with no parentheses");
            } else if( token.getType() == Type.SYMBOL && token.getSymbol() == Symbol.TRANSPOSE) {
                if( token.previous.getType() == Type.VARIABLE )
                    token = insertTranspose(token.previous,tokens,sequence);
                else
                    throw new ParseError("Expected variable before transpose");
            }
            token = token.next;
        }
    }

    /**
     * Parses operations where the input comes from variables to its left and right
     *
     * @param ops List of operations which should be parsed
     * @param tokens List of all the tokens
     * @param sequence List of operation sequence
     */
    protected void parseOperationsLR(Symbol ops[], TokenList tokens, Sequence sequence) {

        if( tokens.size == 0 )
            return;

        TokenList.Token token = tokens.first;

        if( token.getType() != Type.VARIABLE )
            throw new ParseError("The first token in an equation needs to be a variable and not "+token);

        boolean hasLeft = false;
        while( token != null ) {
            if( token.getType() == Type.FUNCTION ) {
                throw new ParseError("Function encountered with no parentheses");
            } else if( token.getType() == Type.VARIABLE ) {
                if( hasLeft ) {
                    if( isTargetOp(token.previous,ops)) {
                        token = createOp(token.previous.previous,token.previous,token,tokens,sequence);
                    }
                } else {
                    hasLeft = true;
                }
            } else {
                if( token.previous.getType() == Type.SYMBOL ) {
                    throw new ParseError("Two symbols next to each other. "+token.previous+" and "+token);
                }
            }
            token = token.next;
        }
    }

    /**
     * Adds a new operation to the list from the operation and two variables.  The inputs are removed
     * from the token list and replaced by their output.
     */
    protected TokenList.Token insertTranspose( TokenList.Token variable ,
                                               TokenList tokens , Sequence sequence )
    {
        Operation.Info info = functions.create('\'',variable.getVariable());

        sequence.addOperation(info.op);

        // replace the symbols with their output
        TokenList.Token t = new TokenList.Token(info.output);
        // remove the transpose symbol
        tokens.remove(variable.next);
        // replace the variable with its transposed version
        tokens.replace(variable,t);
        return t;

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
    protected TokenList.Token createFunction( TokenList.Token name , List<TokenList.Token> inputs , TokenList tokens , Sequence sequence )
    {
        Operation.Info info;
        if( inputs.size() == 1 )
            info = functions.create(name.getFunction().getName(),inputs.get(0).getVariable());
        else {
            List<Variable> vars = new ArrayList<Variable>();
            for (int i = 0; i < inputs.size(); i++) {
                vars.add(inputs.get(i).getVariable());
            }
            info = functions.create(name.getFunction().getName(), vars );
        }

        sequence.addOperation(info.op);

        // replace the symbols with the function's output
        TokenList.Token t = new TokenList.Token(info.output);
        tokens.replace(name, t);
        return t;

    }

    /**
     * Looks up a variable given its name.  If none is found then return null.
     */
    public <T extends Variable> T lookupVariable(String token) {
        Variable result = variables.get(token);
        return (T)result;
    }

    public Macro lookupMacro(String token) {
        return macros.get(token);
    }

    public DenseMatrix64F lookupMatrix(String token) {
        return ((VariableMatrix)variables.get(token)).matrix;
    }

    public int lookupInteger(String token) {
        return ((VariableInteger)variables.get(token)).value;
    }

    public double lookupDouble(String token) {
        Variable v = variables.get(token);

        if( v instanceof VariableMatrix ) {
            DenseMatrix64F m = ((VariableMatrix)v).matrix;
            if( m.numCols == 1 && m.numRows == 1 ) {
                return m.get(0,0);
            } else {
                throw new RuntimeException("Can only return 1x1 real matrices as doubles");
            }
        }
        return ((VariableScalar)variables.get(token)).getDouble();
    }

    /**
     * Parses the text string to extract tokens.
     */
    protected TokenList extractTokens(String equation , ManagerTempVariables managerTemp ) {
        // add a space to make sure everything is parsed when its done
        equation += " ";

        TokenList tokens = new TokenList();

        int length = 0;
        boolean again; // process the same character twice
        TokenType type = TokenType.UNKNOWN;
        for( int i = 0; i < equation.length(); i++ ) {
            again = false;
            char c = equation.charAt(i);
            if( type == TokenType.WORD ) {
                if (isLetter(c)) {
                    storage[length++] = c;
                } else {
                    // add the variable/function name to token list
                    String name = new String(storage, 0, length);
                    tokens.add(name);
                    type = TokenType.UNKNOWN;
                    again = true; // process unexpected character a second time
                }
            } else if( type == TokenType.INTEGER ) { // Handle integer numbers.  Until proven to be a float
                if( c == '.' ) {
                    type = TokenType.FLOAT;
                    storage[length++] = c;
                } else if( c == 'e' || c == 'E' ) {
                    type = TokenType.FLOAT_EXP;
                    storage[length++] = c;
                } else if( Character.isDigit(c) ) {
                    storage[length++] = c;
                } else if( isSymbol(c) || Character.isWhitespace(c) ) {
                    int value = Integer.parseInt( new String(storage, 0, length));
                    tokens.add(managerTemp.createInteger(value));
                    type = TokenType.UNKNOWN;
                    again = true; // process unexpected character a second time
                } else {
                    throw new ParseError("Unexpected character at the end of an integer "+c);
                }
            } else if( type == TokenType.FLOAT ) { // Handle floating point numbers
                if( c == '.') {
                    throw new ParseError("Unexpected '.' in a float");
                } else if( c == 'e' || c == 'E' ) {
                    storage[length++] = c;
                    type = TokenType.FLOAT_EXP;
                } else if( Character.isDigit(c) ) {
                    storage[length++] = c;
                } else if( isSymbol(c) || Character.isWhitespace(c) ) {
                    double value = Double.parseDouble( new String(storage, 0, length));
                    tokens.add(managerTemp.createDouble(value));
                    type = TokenType.UNKNOWN;
                    again = true; // process unexpected character a second time
                } else {
                    throw new ParseError("Unexpected character at the end of an float "+c);
                }
            } else if( type == TokenType.FLOAT_EXP ) { // Handle floating point numbers in exponential format
                boolean end = false;
                if( c == '-' ) {
                    char p = storage[length-1];
                    if( p == 'e' || p == 'E') {
                        storage[length++] = c;
                    } else {
                        end = true;
                    }
                } else if( Character.isDigit(c) ) {
                    storage[length++] = c;
                } else if( isSymbol(c) || Character.isWhitespace(c) ) {
                    end = true;
                } else {
                    throw new ParseError("Unexpected character at the end of an float "+c);
                }

                if( end ) {
                    double value = Double.parseDouble( new String(storage, 0, length));
                    tokens.add(managerTemp.createDouble(value));
                    type = TokenType.UNKNOWN;
                    again = true; // process the current character again since it was unexpected
                }
            } else {
                if( isSymbol(c) ) {
                    boolean special = false;
                    if( c == '-' ) {
                        // need to handle minus symbols carefully since it can be part of a number of a minus operator
                        // if next to a number it should be negative sign, unless there is no operator to its left
                        // then its a minus sign.
                        if( i+1 < equation.length() && Character.isDigit(equation.charAt(i+1)) &&
                                (tokens.last == null || isOperatorLR(tokens.last.getSymbol()))) {
                            type = TokenType.INTEGER;
                            storage[0] = c;
                            length = 1;
                            special = true;
                        }
                    }
                    if( !special ) {
                        TokenList.Token t = tokens.add(Symbol.lookup(c));
                        if (t.previous != null && t.previous.getType() == Type.SYMBOL) {
                            // there should only be two symbols in a row if its an element-wise operation
                            if (t.previous.getSymbol() == Symbol.PERIOD) {
                                tokens.remove(t.previous);
                                tokens.remove(t);
                                tokens.add(Symbol.lookupElementWise(c));
                            }
                        }
                    }
                } else if( Character.isWhitespace(c) ) {
                    continue;// ignore white space
                } else {
                    // start adding to the word
                    if( Character.isDigit(c) ) {
                        type = TokenType.INTEGER;
                    } else {
                        type = TokenType.WORD;
                    }
                    storage[0] = c;
                    length = 1;
                }
            }
            // see if it should process the same character again
            if( again )
                i--;
        }

        return tokens;
    }

    /**
     * Search for WORDS in the token list.  Then see if the WORD is a function or a variable.  If so replace
     * the work with the function/variable
     */
    void insertFunctionsAndVariables(TokenList tokens ) {
        TokenList.Token t = tokens.getFirst();
        while( t != null ) {
            if( t.getType() == Type.WORD ) {
                Variable v = lookupVariable(t.word);
                if (v != null) {
                    t.variable = v;
                    t.word = null;
                } else if (functions.isFunctionName(t.word)) {
                    t.function = (new Function(t.word));
                    t.word = null;
                }
            }
            t = t.next;
        }
    }

    /**
     * Checks to see if a WORD matches the name of a macro.  if it does it applies the macro at that location
     */
    void insertMacros(TokenList tokens ) {
        TokenList.Token t = tokens.getFirst();
        while( t != null ) {
            if( t.getType() == Type.WORD ) {
                Macro v = lookupMacro(t.word);
                if (v != null) {
                    TokenList.Token before = t.previous;
                    List<TokenList.Token> inputs = new ArrayList<TokenList.Token>();
                    t = parseMacroInput(inputs,t.next);

                    TokenList sniplet = v.execute(inputs);
                    tokens.extractSubList(before.next,t);
                    tokens.insertAfter(before,sniplet);
                    t = sniplet.last;
                }
            }
            t = t.next;
        }
    }

    protected enum TokenType
    {
        WORD,
        INTEGER,
        FLOAT,
        FLOAT_EXP,
        UNKNOWN
    }

    /**
     * Checks to see if the token is in the list of allowed character operations.  Used to apply order of operations
     * @param token Token being checked
     * @param ops List of allowed character operations
     * @return true for it being in the list and false for it not being in the list
     */
    protected static boolean isTargetOp( TokenList.Token token , Symbol[] ops ) {
        Symbol c = token.symbol;
        for (int i = 0; i < ops.length; i++) {
            if( c == ops[i])
                return true;
        }
        return false;
    }

    protected static boolean isSymbol(char c) {
        return c == '*' || c == '/' || c == '+' || c == '-' || c == '(' || c == ')' || c == '[' || c == ']' ||
               c == '=' || c == '\'' || c == '.' || c == ',' || c == ':' || c == ';' || c == '\\' || c == '^';
    }

    /**
     * Operators which affect the variables to its left and right
     */
    protected static boolean isOperatorLR( Symbol s ) {
        if( s == null )
            return false;

        switch( s ) {
            case ELEMENT_DIVIDE:
            case ELEMENT_TIMES:
            case ELEMENT_POWER:
            case RDIVIDE:
            case LDIVIDE:
            case TIMES:
            case POWER:
            case PLUS:
            case MINUS:
            case ASSIGN:
                return true;
        }
        return false;
    }

    /**
     * Returns true if the character is a valid letter for use in a variable name
     */
    protected static boolean isLetter( char c ) {
        return !(isSymbol(c) || Character.isWhitespace(c));
    }

    /**
     * Returns true if the specified name is NOT allowed.  It isn't allowed if it matches a built in operator
     * or if it contains a restricted character.
     */
    protected boolean isReserved( String name ) {
        if( functions.isFunctionName(name))
            return true;

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

    /**
     * Compiles and performs the provided equation.
     *
     * @param equation String in simple equation format
     */
    public void process( String equation , boolean debug ) {
        compile(equation,debug).perform();
    }

    /**
     * Returns the functions manager
     */
    public ManagerFunctions getFunctions() {
        return functions;
    }
}
