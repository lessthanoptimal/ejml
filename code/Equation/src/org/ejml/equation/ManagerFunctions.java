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
import java.util.Map;

/**
 * @author Peter Abeles
 */
public class ManagerFunctions {

    // List of functions which take in N inputs
    Map<String,Input1> input1 = new HashMap<String,Input1>();
    Map<String,Input2> input2 = new HashMap<String,Input2>();

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
        if( input2.containsKey(s))
            return true;

        return false;
    }

    public Operation.Info create( String name , Variable var0 ) {
        Input1 func = input1.get(name);
        if( func == null )
            return null;
        return func.create(var0);
    }

    public Operation.Info create( char op , Variable left , Variable right ) {
        switch( op ) {
            case '+':
                return Operation.mAdd(left, right, managerTemp);

            case '-':
                return Operation.mSub(left, right, managerTemp);

            case '*':
                return Operation.mMult(left, right, managerTemp);

            case '/':
                return Operation.mDiv(left, right, managerTemp);

            default:
                throw new RuntimeException("Unknown operation " + op);
        }
    }

    public void setManagerTemp(ManagerTempVariables managerTemp) {
        this.managerTemp = managerTemp;
    }

    public void add( String name , Input1 function ) {
       input1.put(name, function);
    }

    public void add( String name , Input2 function ) {
        input2.put(name,function);
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
    }

    public static interface Input1 {
        Operation.Info create( Variable A );
    }

    public static interface Input2 {
        Operation.Info create( Variable A , Variable B );
    }
}
