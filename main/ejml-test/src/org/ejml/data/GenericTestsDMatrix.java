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

package org.ejml.data;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class GenericTestsDMatrix extends EjmlStandardJUnit {
    protected abstract DMatrix createMatrix( int numRows, int numCols );

    public void allTests() {
        testGetNumRows();
        testGetNumCols();
        testSetAndGet_2D();
        testSetAndGet_2D_unsafe();
        negativeSizedMatrix();
        zeroByZeroMatrix();
    }

    /**
     * An exception should be thrown is you try to set the rows or columns to zero
     */
    public void negativeSizedMatrix() {
        // set each axis to negative independently to ensure they are both checked
        try {
            createMatrix(-1, 2);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignored) {
            // it should throw an exception. Now stop bothering me error prone!
        }

        try {
            createMatrix(2, -1);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignored) {
            // it should throw an exception. Now stop bothering me error prone!
        }

        // Turns out it was only throwing an exception because the array size is negative when rows*cols
        try {
            createMatrix(-1, -1);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException ignored) {
            // it should throw an exception. Now stop bothering me error prone!
        }
    }

    /**
     * 0x0 matrix should be allowed.
     */
    public void zeroByZeroMatrix() {
        DMatrix mat = createMatrix(0, 0);
        assertEquals(0, mat.getNumRows());
        assertEquals(0, mat.getNumCols());
    }

    public void testGetNumRows() {
        DMatrix mat = createMatrix(2, 3);

        assertEquals(2, mat.getNumRows());
    }

    public void testGetNumCols() {
        DMatrix mat = createMatrix(2, 3);

        assertEquals(3, mat.getNumCols());
    }

    public void testSetAndGet_2D() {
        // test a variety of different shapes. Added rigor needed
        // to properly test block matrix.
        checkSetAndGet(10, 12);
        checkSetAndGet(12, 10);
        checkSetAndGet(10, 10);
        checkSetAndGet(19, 5);
        checkSetAndGet(5, 19);
        checkSetAndGet(19, 19);
    }

    private void checkSetAndGet( int m, int n ) {
        DMatrix mat = createMatrix(m, n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                mat.set(i, j, i*m + j);
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double found = mat.get(i, j);

                assertEquals(i*m + j, found, UtilEjml.TEST_F64);
            }
        }
    }

    public void testSetAndGet_2D_unsafe() {
        // test a variety of different shapes. Added rigor needed
        // to properly test block matrix.
        checkSetAndGet_unsafe(10, 12);
        checkSetAndGet_unsafe(12, 10);
        checkSetAndGet_unsafe(10, 10);
        checkSetAndGet_unsafe(19, 5);
        checkSetAndGet_unsafe(5, 19);
        checkSetAndGet_unsafe(19, 19);
    }

    private void checkSetAndGet_unsafe( int m, int n ) {
        DMatrix mat = createMatrix(m, n);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                mat.unsafe_set(i, j, i*m + j);
            }
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double found = mat.unsafe_get(i, j);

                assertEquals(i*m + j, found, UtilEjml.TEST_F64);
            }
        }
    }
}
