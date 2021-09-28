/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

package org.ejml.masks;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDMasksSparseStructural extends EjmlStandardJUnit {

    private static Stream<Arguments> maskAndExpectedMaxEntries() {
        DMatrixSparseCSC maskMatrix = RandomMatrices_DSCC.rectangle(10, 10, 20, new Random(42));
        var maskBuilder = new DMaskSparseStructural.Builder(maskMatrix);

        return Stream.of(
                Arguments.of(maskBuilder.withNegated(true).build(), 80),
                Arguments.of(maskBuilder.withNegated(false).build(), 20)
        );
    }

    @Test
    void masks() {
        int dim = 10;
        DMatrixSparseCSC matrix = RandomMatrices_DSCC.rectangle(dim, dim, 50, new Random(42));

        DMaskSparseStructural.Builder builder = new DMaskSparseStructural.Builder(matrix);
        Mask mask = builder.withNegated(false).build();
        Mask negated_mask = builder.withNegated(true).build();

        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                boolean expected = matrix.isAssigned(row, col);
                assertEquals(mask.isSet(row, col), expected);
                assertEquals(negated_mask.isSet(row, col), !expected);
            }
        }
    }

    @Test
    void indexedMask() {
        int dim = 10;
        DMatrixSparseCSC matrix = RandomMatrices_DSCC.rectangle(dim, dim, 50, new Random(42));

        DMaskSparseStructural.Builder builder = new DMaskSparseStructural.Builder(matrix);
        Mask mask = builder.withNegated(false).build();
        Mask negated_mask = builder.withNegated(true).build();

        for (int col = 0; col < dim; col++) {
            mask.setIndexColumn(col);
            for (int row = 0; row < dim; row++) {
                boolean expected = matrix.isAssigned(row, col);
                assertEquals(mask.isSet(row, col), expected);
                assertEquals(negated_mask.isSet(row, col), !expected);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("maskAndExpectedMaxEntries")
    void maxEntries( Mask mask, int expected ) {
        assertEquals(expected, mask.maxMaskedEntries());
    }
}
