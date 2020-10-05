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

package org.ejml.sparse.csc;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestRandomMatrices_DSCC {
    Random rand = new Random(324);

    @Test
    void uniform() {
        int numRows = 6;
        int numCols = 7;

        DMatrixSparseCSC a = RandomMatrices_DSCC.rectangle(numRows, numCols, 10, -1, 1, rand);

        assertEquals(numRows, a.numRows);
        assertEquals(numCols, a.numCols);
        assertEquals(10, a.nz_length);
        assertTrue(CommonOps_DSCC.checkStructure(a));

        int count = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                double value = a.get(row, col);

                if (value == 0)
                    continue;
                if (value > 1 || value < -1)
                    fail("Out of expected range");
                count++;
            }
        }

        assertEquals(10, count);
        assertTrue(CommonOps_DSCC.checkSortedFlag(a));
    }

    /**
     * There was a bug where the rows and columns multiplied together caused an overflow
     */
    @Test
    void rectangle_large() {
        assertThrows(IllegalArgumentException.class, () ->
                RandomMatrices_DSCC.rectangle(1_000_000, 1_000_000, 10, -1, 1, rand));
    }
    
    private static Stream<Arguments> randomMatrixDimensions() {
        int[] rowCounts = {1, 10, 15, 100, 1000};
        int[] colCounts = {1, 10, 15, 100, 1000};
        double[] densities = {1, 0.8, 0.2, 0.01};

        Stream.Builder<Arguments> streamBuilder = Stream.builder();

        for (int rowCount : rowCounts) {
            for (int colCount : colCounts) {
                for (double density : densities) {
                    streamBuilder.accept(Arguments.of(rowCount, colCount, Math.round(density*rowCount)));
                }
            }
        }

        return streamBuilder.build();
    }

    @ParameterizedTest
    @MethodSource("randomMatrixDimensions")
    void generateUniform( int numRows, int numCols, int entriesPerColumn ) {
        DMatrixSparseCSC a = RandomMatrices_DSCC.generateUniform(numRows, numCols, entriesPerColumn, -1, 1, rand);

        assertEquals(entriesPerColumn*numCols, a.nz_length);
        assertTrue(CommonOps_DSCC.checkStructure(a));
    }


    @Test
    void createLowerTriangular() {
        DMatrixSparseCSC L;
        for (int trial = 0; trial < 20; trial++) {
            for (int length : new int[]{0, 2, 6, 12, 20}) {
                L = RandomMatrices_DSCC.triangleLower(6, 0, length, -1, 1, rand);

                assertEquals(Math.max(6, length), L.nz_length);
                assertTrue(CommonOps_DSCC.checkStructure(L));
                assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L, 0, 0.0));

                L = RandomMatrices_DSCC.triangleLower(6, 1, length, -1, 1, rand);
                assertEquals(Math.max(5, length), L.nz_length);
                assertTrue(CommonOps_DSCC.checkStructure(L));
                assertTrue(MatrixFeatures_DSCC.isLowerTriangle(L, 1, 0.0));

                assertFalse(CommonOps_DSCC.checkDuplicateElements(L));
            }
        }
    }

    @Test
    void symmetric() {
        for (int N = 1; N <= 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                int nz = (int)(N*N*0.5*(rand.nextDouble()*0.5 + 0.1) + 0.5);
                nz = Math.max(1,nz);
                DMatrixSparseCSC A = RandomMatrices_DSCC.symmetric(N, nz, -1, 1, rand);

                assertTrue(CommonOps_DSCC.checkStructure(A));

                // Sanity check to see if it's obeying the requested number of non-zero elements
                assertTrue(A.nz_length >= nz && A.nz_length <= 2*nz);

                // Check the matrix properties
                assertTrue(MatrixFeatures_DSCC.isSymmetric(A, UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * There was a bug where the rows and columns multiplied together caused an overflow
     */
    @Test
    void symmetric_large() {
        assertThrows(IllegalArgumentException.class, () ->
                RandomMatrices_DSCC.symmetric(1_000_000, 10, -1, 1, rand));
    }

    @Test
    void symmetricPosDef() {
        double probabilityZero = 0.25;

        for (int N = 1; N <= 10; N++) {
            for (int mc = 0; mc < 30; mc++) {
                DMatrixSparseCSC A = RandomMatrices_DSCC.symmetricPosDef(N, probabilityZero, rand);

                assertTrue(CommonOps_DSCC.checkStructure(A));

                // The upper limit is  bit fuzzy. This really just checks to see if it's exceeded by an extreme amount
                assertTrue(A.nz_length <= (int)Math.ceil(N*N*(1.0-probabilityZero))+N);

                // Extremely crude check to see if the size is above a lower limit. In theory it could be full of
                // zeros and that would still be valid.
                assertTrue(A.nz_length >= N*N*(probabilityZero/5.0));

                assertTrue(MatrixFeatures_DSCC.isPositiveDefinite(A));
            }
        }
    }
}
