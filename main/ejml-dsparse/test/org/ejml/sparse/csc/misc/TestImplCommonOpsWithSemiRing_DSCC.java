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

package org.ejml.sparse.csc.misc;

import org.apache.commons.math3.util.Pair;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparse;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DoubleSemiRing;
import org.ejml.ops.PreDefinedDoubleSemiRings;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestImplCommonOpsWithSemiRing_DSCC {

    @ParameterizedTest
    @MethodSource("elementWiseAddSource")
    public void add(DoubleSemiRing semiRing, double[] expected) {
        // == graph unions
        DMatrixSparseCSC a = new DMatrixSparseCSC(3, 3);
        DMatrixSparseCSC b = a.copy();
        DMatrixSparseCSC c = a.copy();

        a.set(1, 1, 2);

        b.set(1, 1, 3);
        b.set(0, 0, 4);

        ImplCommonOpsWithSemiRing_DSCC.add(1, a, 1, b, c, semiRing, null, null);

        double[] found = new double[]{c.get(0, 0), c.get(1, 1)};
        assertTrue(c.getNumElements() == 2);

        assertTrue(Arrays.equals(expected, found));
    }

    @ParameterizedTest
    @MethodSource("elementWiseMultSource")
    public void testelementWiseMult(DoubleSemiRing semiRing, double[] expected) {
        // == graph intersection
        DMatrixSparseCSC matrix = new DMatrixSparseCSC(3, 3, 4);
        matrix.set(1, 1, 4);
        matrix.set(1, 2, -2);

        DMatrixSparseCSC otherMatrix = matrix.copy();
        otherMatrix.set(1, 1, 3);
        otherMatrix.set(1, 2, 1);


        matrix.set(0, 2, 1);
        otherMatrix.set(2, 0, 1);

        DMatrixSparseCSC result = new DMatrixSparseCSC(3, 3, 0);

        ImplCommonOpsWithSemiRing_DSCC.elementMult(matrix, otherMatrix, result, semiRing, null, null);

        assertEquals(2, result.getNumElements());
        assertTrue(expected[0] == result.get(1, 1));
        assertTrue(expected[1] == result.get(1, 2));
    }

    private static Stream<Arguments> elementWiseAddSource() {
        return Stream.of(
                Arguments.of(PreDefinedDoubleSemiRings.PLUS_TIMES, new double[]{4, 5}),
                Arguments.of(PreDefinedDoubleSemiRings.MIN_MAX, new double[]{4, 2}),
                Arguments.of(PreDefinedDoubleSemiRings.OR_AND, new double[]{1, 1})
        );
    }

    private static Stream<Arguments> elementWiseMultSource() {
        return Stream.of(
                Arguments.of(PreDefinedDoubleSemiRings.PLUS_TIMES, new double[]{12, -2}),
                Arguments.of(PreDefinedDoubleSemiRings.PLUS_MIN, new double[]{3, -2}),
                Arguments.of(PreDefinedDoubleSemiRings.MIN_MAX, new double[]{4, 1}),
                Arguments.of(PreDefinedDoubleSemiRings.OR_AND, new double[]{1, 1})
        );
    }
}
