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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized place to create new instances of operations and functions.  Must call
 * {@link #setManagerTemp} before any other functions.
 *
 * @author Peter Abeles
 */
public class ManagerFunctions {

    // List of functions which take in N inputs
    Map<String,Input1> input1 = new HashMap<String,Input1>();
    Map<String,InputN> inputN = new HashMap<String,InputN>();

    // Reference to temporary variable manager
    protected ManagerTempVariables managerTemp;

    public ManagerFunctions() {
        addBuiltIn();
    }

    /**
     * Returns true if the string matches the name of a function
     */
    public boolean isFunctionName( String s ) {
        if( input1.containsKey(s))
            return true;
        if( inputN.containsKey(s))
            return true;

        return false;
    }

    /**
     * Create a new instance of single input functions
     * @param name function name
     * @param var0 Input variable
     * @return Resulting operation
     */
    public Operation.Info create( String name , Variable var0 ) {
        Input1 func = input1.get(name);
        if( func == null )
            return null;
        return func.create(var0);
    }

    /**
     * Create a new instance of single input functions
     * @param name function name
     * @param vars Input variables
     * @return Resulting operation
     */
    public Operation.Info create( String name , List<Variable> vars ) {
        InputN func = inputN.get(name);
        if( func == null )
            return null;
        return func.create(vars);
    }

    /**
     * Create a new instance of a single input function from an operator character
     * @param op Which operation
     * @param input Input variable
     * @return Resulting operation
     */
    public Operation.Info create( char op , Variable input ) {
        switch( op ) {
            case '\'':
                return Operation.transpose(input, managerTemp);

            default:
                throw new RuntimeException("Unknown operation " + op);
        }
    }

    /**
     * Create a new instance of a two input function from an operator character
     * @param op Which operation
     * @param left Input variable on left
     * @param right Input variable on right
     * @return Resulting operation
     */
    public Operation.Info create( Symbol op , Variable left , Variable right ) {
        switch( op ) {
            case PLUS:
                return Operation.add(left, right, managerTemp);

            case MINUS:
                return Operation.subtract(left, right, managerTemp);

            case TIMES:
                return Operation.multiply(left, right, managerTemp);

            case RDIVIDE:
                return Operation.divide(left, right, managerTemp);

            case LDIVIDE:
                return Operation.divide(right, left, managerTemp);

            case POWER:
                return Operation.pow(left, right, managerTemp);

            case ELEMENT_DIVIDE:
                return Operation.elementDivision(left, right, managerTemp);

            case ELEMENT_TIMES:
                return Operation.elementMult(left, right, managerTemp);

            default:
                throw new RuntimeException("Unknown operation " + op);
        }
    }

    /**
     *
     * @param managerTemp
     */

    public void setManagerTemp(ManagerTempVariables managerTemp) {
        this.managerTemp = managerTemp;
    }

    /**
     * Adds a function, with a single input, to the list
     * @param name Name of function
     * @param function Function factory
     */
    public void add( String name , Input1 function ) {
       input1.put(name, function);
    }

    /**
     * Adds a function, with a two inputs, to the list
     * @param name Name of function
     * @param function Function factory
     */
    public void add( String name , InputN function ) {
        inputN.put(name,function);
    }

    /**
     * Adds built in functions
     */
    private void addBuiltIn( ) {
        input1.put("inv",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.inv(A,managerTemp);
            }
        });
        input1.put("pinv",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.pinv(A, managerTemp);
            }
        });
        input1.put("eye",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.eye(A, managerTemp);
            }
        });
        input1.put("det",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.det(A, managerTemp);
            }
        });
        input1.put("normF",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.normF(A, managerTemp);
            }
        });
        input1.put("trace",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.trace(A, managerTemp);
            }
        });
        input1.put("diag",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.diag(A, managerTemp);
            }
        });
        input1.put("max",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.max(A, managerTemp);
            }
        });
        input1.put("abs",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.abs(A, managerTemp);
            }
        });
        input1.put("sin",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.sin(A, managerTemp);
            }
        });
        input1.put("cos",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.cos(A, managerTemp);
            }
        });
        input1.put("atan",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.atan(A, managerTemp);
            }
        });
        input1.put("exp",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.exp(A, managerTemp);
            }
        });
        input1.put("log",new Input1() {
            @Override
            public Operation.Info create(Variable A) {
                return Operation.log(A, managerTemp);
            }
        });

        inputN.put("zeros",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.zeros(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("ones",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.ones(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("kron",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.kron(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("dot",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.dot(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("pow",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.pow(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("solve",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                if( inputs.size() != 2 ) throw new RuntimeException("Two inputs expected");
                return Operation.solve(inputs.get(0), inputs.get(1), managerTemp);
            }
        });

        inputN.put("extract",new InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs) {
                return Operation.extract(inputs, managerTemp);
            }
        });
    }

    public ManagerTempVariables getManagerTemp() {
        return managerTemp;
    }

    /**
     * Creates new instances of functions from a single variable
     */
    public static interface Input1 {
        Operation.Info create( Variable A );
    }

    /**
     * Creates a new instance of functions from two variables
     */
    public static interface InputN {
        Operation.Info create( List<Variable> inputs );
    }
}
