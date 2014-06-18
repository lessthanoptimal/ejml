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
 * "result = A*B*A' + 2*C - D'"
 *
 * functions "scalar = det(A)"
 *           "[U,S,V] = svd(A)"
 *           "[U,S,V] = eig(A)"
 *
 * @author Peter Abeles
 */
// TODO plus, minus, times, transpose
// TODO support for scalar
public class Equation {
    HashMap<String,Variable> variables = new HashMap<String, Variable>();

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
    public Sequence compile( String equation ) {

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
