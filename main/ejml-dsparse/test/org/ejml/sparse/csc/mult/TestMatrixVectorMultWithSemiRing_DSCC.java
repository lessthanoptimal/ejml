/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.mult;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DoubleSemiRing;
import org.ejml.ops.PreDefinedDoubleSemiRings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMatrixVectorMultWithSemiRing_DSCC {
    DMatrixSparseCSC inputMatrix;

    @BeforeEach
    public void setUp() {
        // based on example in http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf
        inputMatrix = new DMatrixSparseCSC(7, 7, 12);
        inputMatrix.set(0, 1, 1);
        inputMatrix.set(0, 3, 1);
        inputMatrix.set(1, 4, 1);
        inputMatrix.set(1, 6, 1);
        inputMatrix.set(2, 5, 1);
        inputMatrix.set(3, 0, .2);
        inputMatrix.set(3, 2, .4);
        inputMatrix.set(4, 5, 1);
        inputMatrix.set(5, 2, .5);
        inputMatrix.set(6, 2, 1);
        inputMatrix.set(6, 3, 1);
        inputMatrix.set(6, 4, 1);

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vectorMatrixMultSources")
    public void mult_v_A(String desc, DoubleSemiRing semiRing, double[] expected) {
        // graphblas == following outgoing edges of source nodes
        double v[] = new double[7];
        Arrays.fill(v, semiRing.add.id);
        v[3] = 0.5;
        v[5] = 0.6;

        System.out.println("input vector = " + Arrays.toString(v));

        //input.print();

        double found[] = new double[7];

        MatrixVectorMultWithSemiRing_DSCC.mult(v, inputMatrix, found, semiRing);

        System.out.println("found = " + Arrays.toString(found));
        System.out.println("expected = " + Arrays.toString(expected));
        assertTrue(Arrays.equals(found, expected));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matrixVectorMultSources")
    public void mult_A_v(String desc, DoubleSemiRing semiRing, double[] expected) {
        // graphblas == following incoming edges of source nodes
        double v[] = new double[7];
        Arrays.fill(v, semiRing.add.id);
        v[3] = 0.5;
        v[4] = 0.6;

        System.out.println("input vector = " + Arrays.toString(v));

        //input.print();

        double found[] = new double[7];

        MatrixVectorMultWithSemiRing_DSCC.mult(inputMatrix, v, found, semiRing);

        System.out.println("found = " + Arrays.toString(found));
        System.out.println("expected = " + Arrays.toString(expected));
        assertTrue(Arrays.equals(found, expected));
    }

    private static Stream<Arguments> vectorMatrixMultSources() {
        return Stream.of(
                Arguments.of("Plus, Times", PreDefinedDoubleSemiRings.PLUS_TIMES, new double[]{0.1, 0, 0.5, 0, 0, 0, 0}),
                Arguments.of("OR, AND", PreDefinedDoubleSemiRings.OR_AND, new double[]{1, 0, 1, 0, 0, 0, 0}),
                Arguments.of("MIN, PLUS", PreDefinedDoubleSemiRings.MIN_PLUS,
                        new double[]{0.7, Double.MAX_VALUE, 0.9, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}),
// This only works on sparse input vectors (here resulting in 1.0 instead of Double.MIN_Value as max(1.0, Double.MIN_VALUE) = 1.0)
//                Arguments.of("MAX, PLUS", PreDefinedDoubleSemiRings.MAX_PLUS,
//                        new double[]{0.7, Double.MIN_VALUE, 1.1, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE}),
                Arguments.of("MIN, TIMES", PreDefinedDoubleSemiRings.MIN_TIMES,
                        new double[]{0.1, Double.MAX_VALUE, 0.2, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}),
                Arguments.of("MAX, MIN", PreDefinedDoubleSemiRings.MAX_MIN,
                        new double[]{0.2, Double.MIN_VALUE, 0.5, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE})
        );
    }

    private static Stream<Arguments> matrixVectorMultSources() {
        return Stream.of(
                Arguments.of("Plus, Times", PreDefinedDoubleSemiRings.PLUS_TIMES, new double[]{.5, .6, 0, 0, 0, 0, 1.1}),
                Arguments.of("OR, AND", PreDefinedDoubleSemiRings.OR_AND, new double[]{1, 1, 0, 0, 0, 0, 1}),
                Arguments.of("MIN, PLUS", PreDefinedDoubleSemiRings.MIN_PLUS,
                        new double[]{1.5, 1.6, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 1.5})
        );
    }
}