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

package org.ejml.sparse.csc;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.Mask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MaskTestUtil {
    public static void assertMaskedResult( DMatrixSparseCSC unmaskedResult, DMatrixSparseCSC maskedResult, Mask mask ) {
        for (int col = 0; col < unmaskedResult.numCols; col++) {
            for (int row = 0; row < unmaskedResult.numRows; row++) {
                if (mask == null || mask.isSet(row, col)) {
                    // entry should be computed
                    assertEquals(unmaskedResult.isAssigned(row, col), maskedResult.isAssigned(row, col));
                    assertEquals(unmaskedResult.get(row, col), maskedResult.get(row, col));
                } else {
                    // entry should not be computed as not set in mask
                    assertFalse(maskedResult.isAssigned(row, col));
                }
            }
        }
    }
}
