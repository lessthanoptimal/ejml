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

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.equation.*;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import java.util.List;
import java.util.Random;

/**
 * Demonstration on how to create and use a custom function in Equation.  A custom function must implement
 * ManagerFunctions.Input1 or ManagerFunctions.InputN, depending on the number of inputs it takes.
 *
 * @author Peter Abeles
 */
public class EquationCustomFunction {

    public static void main(String[] args) {
        Random rand = new Random(234);

        Equation eq = new Equation();
        eq.getFunctions().add("multTransA",createMultTransA());

        SimpleMatrix A = new SimpleMatrix(1,1); // will be resized
        SimpleMatrix B = SimpleMatrix.random(3,4,-1,1,rand);
        SimpleMatrix C = SimpleMatrix.random(3, 4, -1, 1, rand);

        eq.alias(A,"A",B,"B",C,"C");

        eq.process("A=multTransA(B,C)");

        System.out.println("Found");
        System.out.println(A);
        System.out.println("Expected");
        B.transpose().mult(C).print();
    }

    /**
     * Create the function.  Be sure to handle all possible input types and combinations correctly and provide
     * meaningful error messages.  The output matrix should be resized to fit the inputs.
     */
    public static ManagerFunctions.InputN createMultTransA() {
        return new ManagerFunctions.InputN() {
            @Override
            public Operation.Info create(List<Variable> inputs, ManagerTempVariables manager ) {
                if( inputs.size() != 2 )
                    throw new RuntimeException("Two inputs required");

                final Variable varA = inputs.get(0);
                final Variable varB = inputs.get(1);

                Operation.Info ret = new Operation.Info();

                if( varA instanceof VariableMatrix && varB instanceof VariableMatrix ) {
                    final VariableMatrix output = manager.createMatrix();
                    ret.output = output;
                    ret.op = new Operation("multTransA-mm") {
                        @Override
                        public void process() {
                            DenseMatrix64F mA = ((VariableMatrix)varA).matrix;
                            DenseMatrix64F mB = ((VariableMatrix)varB).matrix;
                            output.matrix.reshape(mA.numCols,mB.numCols);

                            CommonOps.multTransA(mA,mB,output.matrix);
                        }
                    };
                } else {
                    throw new IllegalArgumentException("Expected both inputs to be a matrix");
                }

                return ret;
            }
        };
    }
}
