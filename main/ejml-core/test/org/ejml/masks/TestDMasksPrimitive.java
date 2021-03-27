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

package org.ejml.masks;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDMasksPrimitive {

    @Test
    void primitiveArray() {
        double[] values = {2, 0, 4, 0, 0, -1};

        DMaskPrimitive.Builder maskBuilder = DMaskFactory.builder(values);
        DMaskPrimitive mask = maskBuilder.withNegated(false).build();
        DMaskPrimitive negated_mask = maskBuilder.withNegated(true).build();
        boolean[] expected = {true, false, true, false, false, true};

        for (int i = 0; i < values.length; i++) {
            assertEquals(mask.isSet(i), expected[i]);
            assertEquals(negated_mask.isSet(i), !expected[i]);
        }
    }

    @Test
    void denseMatrix() {
        int dim = 20;
        DMatrixRMaj matrix = RandomMatrices_DDRM.rectangle(dim, dim, new Random(42));

        DMaskPrimitive.Builder maskBuilder = DMaskFactory.builder(matrix);
        DMaskPrimitive mask = maskBuilder.withNegated(false).build();
        DMaskPrimitive negated_mask = maskBuilder.withNegated(true).build();

        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                boolean expected = (matrix.get(row, col) != 0);
                assertEquals(mask.isSet(row, col), expected);
                assertEquals(negated_mask.isSet(row, col), !expected);
            }
        }
    }

    @Test
    void maxEntries() {
        Mask mask = DMaskFactory.builder(new double[100]).build();
        assertEquals(100, mask.maxMaskedEntries());
    }
}
