/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple.ops;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.Matrix;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class BaseSimpleOperationsChecks<T extends Matrix> extends EjmlStandardJUnit {
    public abstract SimpleOperations<T> createOps();

    public abstract T randomRect( int numRows, int numCols );

    @Test void getRow() {
        SimpleMatrix A = SimpleMatrix.wrap(randomRect(5, 10));

        int row = 0;
        int col0 = 1;
        int col1 = A.getNumCols();

        // complex matrices have two instead of one values per element
        int stride = A.getType().isReal() ? 1 : 2;

        for (int trial = 0; trial < 10; trial++) {
            /**/double[] found = createOps().getRow(A.getMatrix(), row, col0, col1);

            for (int col = col0; col < col1; col++) {
                assertEquals(A.getReal(row, col), found[stride*(col - col0)]);
            }
        }
    }

    @Test void getColumn() {
        SimpleMatrix A = SimpleMatrix.wrap(randomRect(5, 10));

        int col = 0;
        int row0 = 1;
        int row1 = A.getNumRows();

        // complex matrices have two instead of one values per element
        int stride = A.getType().isReal() ? 1 : 2;

        for (int trial = 0; trial < 10; trial++) {
            /**/double[] found = createOps().getColumn(A.getMatrix(), col, row0, row1);

            for (int row = row0; row < row1; row++) {
                assertEquals(A.getReal(row, col), found[stride*(row - row0)]);
            }
        }
    }
}