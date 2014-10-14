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

import org.ejml.data.Complex64F;
import org.ejml.data.ComplexPolar64F;
import org.ejml.ops.ComplexMath64F;

/**
 * Demonstration of different operations that can be performed on complex numbers.
 *
 * @author Peter Abeles
 */
public class ExampleComplexMath {

	public static void main( String []args ) {
		Complex64F a = new Complex64F(1,2);
		Complex64F b = new Complex64F(-1,-0.6);
		Complex64F c = new Complex64F();
		ComplexPolar64F polarC = new ComplexPolar64F();

		System.out.println("a = "+a);
		System.out.println("b = "+b);
		System.out.println("------------------");

		ComplexMath64F.plus(a, b, c);
		System.out.println("a + b = "+c);
		ComplexMath64F.minus(a, b, c);
		System.out.println("a - b = "+c);
		ComplexMath64F.multiply(a, b, c);
		System.out.println("a * b = "+c);
		ComplexMath64F.divide(a, b, c);
		System.out.println("a / b = "+c);

		System.out.println("------------------");
		ComplexPolar64F polarA = new ComplexPolar64F();
		ComplexMath64F.convert(a, polarA);
		System.out.println("polar notation of a = "+polarA);
		ComplexMath64F.pow(polarA, 3, polarC);
		System.out.println("a ** 3 = "+polarC);
		ComplexMath64F.convert(polarC, c);
		System.out.println("a ** 3 = "+c);
	}
}
