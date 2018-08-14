/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.ComplexPolar_F64;
import org.ejml.data.Complex_F64;
import org.ejml.ops.ComplexMath_F64;

/**
 * Demonstration of different operations that can be performed on complex numbers.
 *
 * @author Peter Abeles
 */
public class ExampleComplexMath {

    public static void main( String []args ) {
        Complex_F64 a = new Complex_F64(1,2);
        Complex_F64 b = new Complex_F64(-1,-0.6);
        Complex_F64 c = new Complex_F64();
        ComplexPolar_F64 polarC = new ComplexPolar_F64();

        System.out.println("a = "+a);
        System.out.println("b = "+b);
        System.out.println("------------------");

        ComplexMath_F64.plus(a, b, c);
        System.out.println("a + b = "+c);
        ComplexMath_F64.minus(a, b, c);
        System.out.println("a - b = "+c);
        ComplexMath_F64.multiply(a, b, c);
        System.out.println("a * b = "+c);
        ComplexMath_F64.divide(a, b, c);
        System.out.println("a / b = "+c);

        System.out.println("------------------");
        ComplexPolar_F64 polarA = new ComplexPolar_F64();
        ComplexMath_F64.convert(a, polarA);
        System.out.println("polar notation of a = "+polarA);
        ComplexMath_F64.pow(polarA, 3, polarC);
        System.out.println("a ** 3 = "+polarC);
        ComplexMath_F64.convert(polarC, c);
        System.out.println("a ** 3 = "+c);
    }
}
