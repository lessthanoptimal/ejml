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

import org.ejml.EjmlUnitTests;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"UnusedMethod"})
public class TestMatrixMatrixMultWithSemiRing_DSCC {
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
    @MethodSource("sparseVectorMatrixMultSources")
    void mult_v_A(String desc, DSemiRing semiRing, double[] expected) {
        // graphblas == following outgoing edges of source nodes
        DMatrixSparseCSC vector = new DMatrixSparseCSC(1, 7);
        vector.set(0, 3, 0.5);
        vector.set(0, 5, 0.6);

        DMatrixSparseCSC found = CommonOpsWithSemiRing_DSCC.mult(vector, inputMatrix, null, semiRing);

        assertEquals(expected[0], found.get(0, 0));
        assertEquals(expected[1], found.get(0, 2));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("sparseMatrixSources")
    void elementMult(String desc, DMatrixSparseCSC matrix, DMatrixSparseCSC otherMatrix) {
        DSemiRing semiRing = DSemiRings.PLUS_TIMES;

        DMatrixSparseCSC found = CommonOpsWithSemiRing_DSCC.elementMult(matrix, otherMatrix, null, semiRing, null, null);
        DMatrixSparseCSC expected = CommonOps_DSCC.elementMult(matrix, otherMatrix, null, null, null);

        EjmlUnitTests.assertEquals(expected, found);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sparseMatrixSources")
    void add(String desc, DMatrixSparseCSC matrix, DMatrixSparseCSC otherMatrix) {
        DSemiRing semiRing = DSemiRings.PLUS_TIMES;

        DMatrixSparseCSC found = CommonOpsWithSemiRing_DSCC.add(1, matrix, 1, otherMatrix, null, semiRing, null, null);
        DMatrixSparseCSC expected = CommonOps_DSCC.add(1, matrix, 1, otherMatrix, null, null, null);

        EjmlUnitTests.assertEquals(expected, found);
    }

    private static Stream<Arguments> sparseVectorMatrixMultSources() {
        return Stream.of(
                // expected entries for (0, 0) and (0, 2)
                Arguments.of("PLUS, TIMES", DSemiRings.PLUS_TIMES, new double[]{0.1, 0.5}),
                Arguments.of("OR, AND", DSemiRings.OR_AND, new double[]{1, 1}),
                Arguments.of("MIN, PLUS", DSemiRings.MIN_PLUS, new double[]{0.7, 0.9}),
                Arguments.of("MAX, PLUS", DSemiRings.MAX_PLUS, new double[]{0.7, 1.1}),
                Arguments.of("MIN, TIMES", DSemiRings.MIN_TIMES, new double[]{0.1, 0.2}),
                Arguments.of("MAX, MIN", DSemiRings.MAX_MIN, new double[]{0.2, 0.5})
        );
    }

    private static Stream<Arguments> sparseMatrixSources() {
        Random rand = new Random(42);
        Random otherRandom = new Random(1337);
        DMatrixSparseCSC sparseMatrix = RandomMatrices_DSCC.rectangle(10, 10, 15, rand);
        DMatrixSparseCSC denseMatrix = RandomMatrices_DSCC.rectangle(10, 10, 90, rand);

        return Stream.of(
                Arguments.of("Both really sparse", sparseMatrix, RandomMatrices_DSCC.rectangle(10, 10, 15, otherRandom)),
                Arguments.of("Sparse, denseCSC", sparseMatrix, denseMatrix),
                Arguments.of("dense, sparse", denseMatrix, sparseMatrix),
                Arguments.of("Both denseCSC", denseMatrix, RandomMatrices_DSCC.rectangle(10, 10, 90, otherRandom))
        );
    }
}
