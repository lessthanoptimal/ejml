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

package org.ejml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMatrixPrintFormat extends EjmlStandardJUnit {
    @Test void row() {
        var array = new double[]{0, 1.1345, 100, 2, 1.30494954342, 9498481, -3};

        var alg = new MatrixPrintFormat().fsetPrecision(2);
        var builder = new StringBuilder();
        alg.row(builder, array.length - 1, ( i ) -> array[i + 1]);

        String found = builder.toString();
        assertEquals(1 + (9 + 2)*6 - 2 + 1, found.length());
        assertEquals("{1.1345   , 100      , 2        , 1.3049495, 9498481  , -3       }", found);

        alg.aligned = false;
        builder.delete(0, builder.length());
        alg.row(builder, array.length - 1, ( i ) -> array[i + 1]);
        assertEquals("{1.13, 100, 2, 1.3, 9498481, -3}", builder.toString());
    }
}
