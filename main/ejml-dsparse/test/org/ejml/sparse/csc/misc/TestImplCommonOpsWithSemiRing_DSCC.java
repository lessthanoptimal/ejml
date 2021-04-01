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

package org.ejml.sparse.csc.misc;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMaskFactory;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.ejml.TestDMaskUtil.assertMaskedResult;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"UnusedMethod"})
public class TestImplCommonOpsWithSemiRing_DSCC {

    @ParameterizedTest
    @MethodSource("elementWiseAddSemiringSource")
    public void add( DSemiRing semiRing, double[] expected) {
        // == graph unions
        DMatrixSparseCSC a = new DMatrixSparseCSC(3, 3);
        DMatrixSparseCSC b = a.copy();
        DMatrixSparseCSC c = a.copy();

        a.set(1, 1, 2);
        b.set(1, 1, 3);
        b.set(0, 0, 4);

        ImplCommonOpsWithSemiRing_DSCC.add(1, a, 1, b, c, semiRing, null, null, null);

        double[] found = new double[]{c.get(0, 0), c.get(1, 1)};

        assertTrue(c.getNonZeroLength() == 2);
        assertTrue(Arrays.equals(expected, found));
    }

    @Test
    public void maskedAdd() {
        var random = new Random(42);
        var a = RandomMatrices_DSCC.rectangle(10, 10, 30, random);
        var b = RandomMatrices_DSCC.rectangle(10, 10, 30, random);
        var mask = DMaskFactory.builder(RandomMatrices_DSCC.rectangle(10, 10, 30, random), true).build();

        var unmasked = new DMatrixSparseCSC(10, 10, 0);
        var masked = new DMatrixSparseCSC(10, 10, 0);

        ImplCommonOpsWithSemiRing_DSCC.add(1, a, 1, b, unmasked, DSemiRings.PLUS_TIMES, null, null, null);
        ImplCommonOpsWithSemiRing_DSCC.add(1, a, 1, b, masked, DSemiRings.PLUS_TIMES, mask, null, null);


        assertMaskedResult(unmasked, masked, mask);
    }

    @Test
    public void useMaskForResultExpansionInAdd() {
        var random = new Random(42);
        var a = RandomMatrices_DSCC.rectangle(10, 10, 100, random);
        var b = RandomMatrices_DSCC.rectangle(10, 10, 100, random);
        int expectedResultEntries = 50;
        var mask = DMaskFactory.builder(RandomMatrices_DSCC.rectangle(10, 10, expectedResultEntries, random), false).build();

        var result = new DMatrixSparseCSC(10, 10, 1);

        ImplCommonOpsWithSemiRing_DSCC.add(1, a, 1, b, result, DSemiRings.PLUS_TIMES, mask, null, null);

        assertEquals(expectedResultEntries, result.nz_length);
        assertEquals(50, result.nz_rows.length );
        assertEquals(50, result.nz_values.length );
    }

    @Test
    public void useMaskForResultExpansionInMult() {
        var random = new Random(42);
        var a = RandomMatrices_DSCC.rectangle(10, 10, 100, random);
        var b = RandomMatrices_DSCC.rectangle(10, 10, 100, random);
        int expectedResultEntries = 50;
        var mask = DMaskFactory.builder(RandomMatrices_DSCC.rectangle(10, 10, expectedResultEntries, random), false).build();

        var result = new DMatrixSparseCSC(10, 10, 1);

        ImplCommonOpsWithSemiRing_DSCC.elementMult( a, b, result, DSemiRings.PLUS_TIMES, mask, null, null);

        assertEquals(expectedResultEntries, result.nz_length);
        assertEquals(50, result.nz_rows.length );
        assertEquals(50, result.nz_values.length );
    }

    @ParameterizedTest
    @MethodSource("elementWiseMultSemiringSource")
    public void elementWiseMult( DSemiRing semiRing, double[] expected) {
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

        ImplCommonOpsWithSemiRing_DSCC.elementMult(matrix, otherMatrix, result, semiRing, null, null, null);

        assertEquals(2, result.getNonZeroLength());
        assertTrue(expected[0] == result.get(1, 1));
        assertTrue(expected[1] == result.get(1, 2));
    }

    @Test
    public void maskedeWiseMult() {
        var random = new Random(42);
        var a = RandomMatrices_DSCC.rectangle(10, 10, 30, random);
        var b = RandomMatrices_DSCC.rectangle(10, 10, 30, random);
        var mask = DMaskFactory.builder(RandomMatrices_DSCC.rectangle(10, 10, 30, random), true).build();

        var unmasked = new DMatrixSparseCSC(10, 10, 0);
        var masked = new DMatrixSparseCSC(10, 10, 0);

        ImplCommonOpsWithSemiRing_DSCC.elementMult(a, b, unmasked, DSemiRings.PLUS_TIMES, null, null, null);
        ImplCommonOpsWithSemiRing_DSCC.elementMult(a, b, masked, DSemiRings.PLUS_TIMES, mask, null, null);


        assertMaskedResult(unmasked, masked, mask);
    }

    private static Stream<Arguments> elementWiseAddSemiringSource() {
        return Stream.of(
                Arguments.of(DSemiRings.PLUS_TIMES, new double[]{4, 5}),
                Arguments.of(DSemiRings.MIN_MAX, new double[]{4, 2}),
                Arguments.of(DSemiRings.OR_AND, new double[]{1, 1})
        );
    }

    private static Stream<Arguments> elementWiseMultSemiringSource() {
        return Stream.of(
                Arguments.of(DSemiRings.PLUS_TIMES, new double[]{12, -2}),
                Arguments.of(DSemiRings.PLUS_MIN, new double[]{3, -2}),
                Arguments.of(DSemiRings.MIN_MAX, new double[]{4, 1}),
                Arguments.of(DSemiRings.OR_AND, new double[]{1, 1})
        );
    }
}
