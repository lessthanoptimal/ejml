/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.ZComplex;
import org.ejml.data.ZComplexPolar;
import org.ejml.ops.ComplexMathZ;

/**
 * Demonstration of different operations that can be performed on complex numbers.
 *
 * @author Peter Abeles
 */
public class ExampleComplexMath {

    public static void main( String []args ) {
        ZComplex a = new ZComplex(1,2);
        ZComplex b = new ZComplex(-1,-0.6);
        ZComplex c = new ZComplex();
        ZComplexPolar polarC = new ZComplexPolar();

        System.out.println("a = "+a);
        System.out.println("b = "+b);
        System.out.println("------------------");

        ComplexMathZ.plus(a, b, c);
        System.out.println("a + b = "+c);
        ComplexMathZ.minus(a, b, c);
        System.out.println("a - b = "+c);
        ComplexMathZ.multiply(a, b, c);
        System.out.println("a * b = "+c);
        ComplexMathZ.divide(a, b, c);
        System.out.println("a / b = "+c);

        System.out.println("------------------");
        ZComplexPolar polarA = new ZComplexPolar();
        ComplexMathZ.convert(a, polarA);
        System.out.println("polar notation of a = "+polarA);
        ComplexMathZ.pow(polarA, 3, polarC);
        System.out.println("a ** 3 = "+polarC);
        ComplexMathZ.convert(polarC, c);
        System.out.println("a ** 3 = "+c);
    }
}
