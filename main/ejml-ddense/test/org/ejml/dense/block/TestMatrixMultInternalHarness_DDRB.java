/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.block.MatrixMultInternalHarness_DDRB.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMatrixMultInternalHarness_DDRB extends EjmlStandardJUnit {
    @Test void pickOuterAxis() {
        assertEquals(I, MatrixMultInternalHarness_DDRB.pickOuterAxis(5, 4, 3));
        assertEquals(J, MatrixMultInternalHarness_DDRB.pickOuterAxis(4, 5, 3));

        // K is never selected right now
        assertEquals(J, MatrixMultInternalHarness_DDRB.pickOuterAxis(3, 4, 5));
        assertEquals(I, MatrixMultInternalHarness_DDRB.pickOuterAxis(4, 3, 5));
    }

    @Test void pickMiddleAxis() {
        assertEquals(J, MatrixMultInternalHarness_DDRB.pickMiddleAxis(I));
        assertEquals(I, MatrixMultInternalHarness_DDRB.pickMiddleAxis(J));
        assertEquals(I, MatrixMultInternalHarness_DDRB.pickMiddleAxis(K));
    }

    @Test void sizeOf() {
        assertEquals(5, MatrixMultInternalHarness_DDRB.
                sizeOf(I, 5, 4, 3));
        assertEquals(4, MatrixMultInternalHarness_DDRB.
                sizeOf(J, 5, 4, 3));
        assertEquals(3, MatrixMultInternalHarness_DDRB.
                sizeOf(K, 5, 4, 3));
    }

    @Test void axisValue() {
        assertEquals(5, MatrixMultInternalHarness_DDRB.
                axisValue(I, 5, 4, 3, I, J, K));
        assertEquals(4, MatrixMultInternalHarness_DDRB.
                axisValue(J, 5, 4, 3, I, J, K));
        assertEquals(3, MatrixMultInternalHarness_DDRB.
                axisValue(K, 5, 4, 3, I, J, K));
    }
}
