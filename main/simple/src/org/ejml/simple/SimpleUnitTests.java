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

package org.ejml.simple;

import org.ejml.ops.EjmlUnitTests;

/**
 * Unit testing functions for {@link SimpleMatrix}
 *
 * @author Peter Abeles
 */
public class SimpleUnitTests {
    /**
     * Checks to see if every element in A is countable.  A doesn't have any element with
     * a value of NaN or infinite.
     *
     * @param A Matrix
     */
    public static void assertCountable( SimpleMatrix A ) {
        EjmlUnitTests.assertCountable(A.getMatrix());
    }

    /**
     * <p>
     * Checks to see if A and B have the same shape.
     * </p>
     *
     * @param A Matrix
     * @param B Matrix
     */
    public static void assertShape( SimpleMatrix A , SimpleMatrix B ) {
        EjmlUnitTests.assertShape(A.getMatrix(), B.getMatrix());
    }

    /**
     * <p>
     * Checks to see if each element in the matrix is within tolerance of each other:
     * </p>
     *
     * <p>
     * The two matrices are identical with in tolerance if:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol
     * </p>
     *
     * <p>
     * In addition if an element is NaN or infinite in one matrix it must be the same in the other.
     * </p>
     *
     * @param A Matrix A
     * @param B Matrix B
     * @param tol Tolerance
     */
    public static void assertEqualsUncountable( SimpleMatrix A , SimpleMatrix B , double tol ) {
        EjmlUnitTests.assertEqualsUncountable(A.getMatrix(), B.getMatrix(), tol);
    }

    /**
     * <p>
     * Checks to see if each element in the matrices are within tolerance of each other and countable:
     * </p>
     *
     * <p>
     * The two matrices are identical with in tolerance if:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol
     * </p>
     *
     * <p>
     * The test will fail if any element in either matrix is NaN or infinite.
     * </p>
     *
     * @param A Matrix A
     * @param B Matrix B
     * @param tol Tolerance
     */
    public static void assertEquals( SimpleMatrix A , SimpleMatrix B , double tol ) {
        EjmlUnitTests.assertEquals(A.getMatrix(), B.getMatrix(), tol);
    }
}
