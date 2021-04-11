/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.masks.DMaskFactory;
import org.ejml.masks.Mask;
import org.ejml.masks.MaskBuilder;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.ejml.TestDMaskUtil.assertMaskedResult;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("UnusedMethod")
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
        inputMatrix.set(3, 0, 0.2);
        inputMatrix.set(3, 2, 0.4);
        inputMatrix.set(4, 5, 1);
        inputMatrix.set(5, 2, 0.5);
        inputMatrix.set(6, 2, 1);
        inputMatrix.set(6, 3, 1);
        inputMatrix.set(6, 4, 1);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vectorMatrixMultSources")
    void mult_v_A( String desc, DSemiRing semiRing, double[] expected ) {
        // graphblas == following outgoing edges of source nodes
        double[] v = new double[7];
        Arrays.fill(v, semiRing.add.id);
        v[3] = 0.5;
        v[5] = 0.6;

        double[] found = new double[7];

        MatrixVectorMultWithSemiRing_DSCC.mult(v, inputMatrix, found, semiRing, null);

        assertTrue(Arrays.equals(found, expected));
    }

    @ParameterizedTest
    @MethodSource("maskedInputSources")
    void mult_v_A_masked( double[] vector, Mask mask ) {
        var semiRing = DSemiRings.OR_AND;

        double[] found = new double[7];
        double[] foundMasked = new double[7];
        MatrixVectorMultWithSemiRing_DSCC.mult(vector, inputMatrix, found, semiRing, null);

        MatrixVectorMultWithSemiRing_DSCC.mult(vector, inputMatrix, foundMasked, semiRing, mask);

        double[] expected = {1, 1, 1, 1, 0, 0, 0};
        assertArrayEquals(found, expected);
        assertMaskedResult(found, foundMasked, mask);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matrixVectorMultSources")
    void mult_A_v( String desc, DSemiRing semiRing, double[] expected ) {
        // graphblas == following incoming edges of source nodes
        double[] v = new double[7];
        Arrays.fill(v, semiRing.add.id);
        v[3] = 0.5;
        v[4] = 0.6;

        double[] found = new double[7];

        MatrixVectorMultWithSemiRing_DSCC.mult(inputMatrix, v, found, semiRing, null);

        assertTrue(Arrays.equals(found, expected));
    }

    @ParameterizedTest
    @MethodSource("maskedInputSources")
    void mult_A_v_masked( double[] vector, Mask mask ) {
        var semiRing = DSemiRings.OR_AND;

        double[] found = new double[7];
        double[] foundMasked = new double[7];

        MatrixVectorMultWithSemiRing_DSCC.mult(inputMatrix, vector, found, semiRing, null);
        MatrixVectorMultWithSemiRing_DSCC.mult(inputMatrix, vector, foundMasked, semiRing, mask);

        double[] expected = {1, 0, 0, 1, 0, 0, 1};
        assertArrayEquals(found, expected);
        assertMaskedResult(found, foundMasked, mask);
    }

    private static Stream<Arguments> vectorMatrixMultSources() {
        return Stream.of(
                Arguments.of("Plus, Times", DSemiRings.PLUS_TIMES, new double[]{0.1, 0, 0.5, 0, 0, 0, 0}),
                Arguments.of("OR, AND", DSemiRings.OR_AND, new double[]{1, 0, 1, 0, 0, 0, 0}),
                Arguments.of("MIN, PLUS", DSemiRings.MIN_PLUS,
                        new double[]{0.7, Double.MAX_VALUE, 0.9, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}),
                Arguments.of("MIN, TIMES", DSemiRings.MIN_TIMES,
                        new double[]{0.1, Double.MAX_VALUE, 0.2, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}),
                Arguments.of("MAX, MIN", DSemiRings.MAX_MIN,
                        new double[]{0.2, -Double.MAX_VALUE, 0.5, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE})
        );
    }

    private static Stream<Arguments> matrixVectorMultSources() {
        return Stream.of(
                Arguments.of("PLUS, TIMES", DSemiRings.PLUS_TIMES, new double[]{0.5, 0.6, 0, 0, 0, 0, 1.1}),
                Arguments.of("OR, AND", DSemiRings.OR_AND, new double[]{1, 1, 0, 0, 0, 0, 1}),
                Arguments.of("MIN, PLUS", DSemiRings.MIN_PLUS,
                        new double[]{1.5, 1.6, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 1.5})
        );
    }

    private static Stream<Arguments> maskedInputSources() {
        int vectorLength = 7;
        double[] v = new double[vectorLength];
        v[0] = 0.5;
        v[3] = 0.6;

        DMatrixSparseCSC sparseVector = new DMatrixSparseCSC(vectorLength, 1);
        sparseVector.set(0, 0, 0.5);
        sparseVector.set(3, 0, 0.6);

        Stream<MaskBuilder> maskBuilders = Stream.of(
                DMaskFactory.builder(v),
                DMaskFactory.builder(sparseVector, true),
                DMaskFactory.builder(sparseVector, false)
        );

        return maskBuilders.map(builder -> Arguments.of(v, builder.withNegated(true).build()));
    }
}
