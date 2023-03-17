/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMatrixFeatures extends EjmlStandardJUnit {
    @Test void isVector() {
        var a = new DMatrixRMaj(4, 4);

        assertFalse(MatrixFeatures.isVector(a));

        a.reshape(3, 1, false);
        assertTrue(MatrixFeatures.isVector(a));

        a.reshape(1, 3, false);
        assertTrue(MatrixFeatures.isVector(a));
    }

    @Test void isSquare() {
        var a = new DMatrixRMaj(5, 4);

        assertFalse(MatrixFeatures.isSquare(a));

        a.reshape(4, 4, false);
        assertTrue(MatrixFeatures.isSquare(a));
    }
}
