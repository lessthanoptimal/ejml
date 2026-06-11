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

package org.ejml.simple;

import org.ejml.EjmlStandardJUnit;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.data.MatrixType;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSimpleSVD extends EjmlStandardJUnit {
    // Verifies that the input is not modified
    @Test void inputModified() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(5, 5, rand);
        DMatrixRMaj original = mat.copy();

        // It will decompose. If not properly shielded it could modify the input
        new SimpleSVD<>(mat, true) {
            @Override
            protected SingularValueDecomposition<DMatrixRMaj> createSvd( int numRows, int numCols, MatrixType type, boolean compact ) {
                return new SingularValueDecomposition<>() {
                    @Override public int numberOfSingularValues() {return 0;}
                    @Override public boolean isCompact() { return false;}
                    @Override public DMatrixRMaj getU( @Nullable DMatrixRMaj U, boolean transposed ) {return new DMatrixRMaj();}
                    @Override public DMatrixRMaj getV( @Nullable DMatrixRMaj V, boolean transposed ) {return new DMatrixRMaj(); }
                    @Override public DMatrixRMaj getW( @Nullable DMatrixRMaj W ) {return new DMatrixRMaj();}
                    @Override public int numRows() {return 0;}
                    @Override public int numCols() {return 0;}
                    @Override public boolean inputModified() {return true;}
                    @Override public boolean decompose( DMatrixRMaj orig ) {
                        orig.set(0,0, 100);
                        return true;
                    }
                };
            }

            @Override protected void extractDecomposition() {}
        };

        assertTrue(MatrixFeatures_DDRM.isIdentical(mat, original, 0.0));
    }

    @Test void rank_case0() {
        for (int dimen = 1; dimen < 10; dimen++) {
            assertEquals(dimen, SimpleMatrix.identity(dimen).svd().rank());
            assertEquals(dimen, SimpleMatrix.identity(dimen).svd(true).rank());
        }
    }

    @Test void rank_case1() {
        double[] values = new double[]{10.0, 3.0, 20.0, 6.0, 30.0, 9.0};
        assertEquals(1, new SimpleMatrix(3, 2, true, values).svd().rank());
        assertEquals(1, new SimpleMatrix(3, 2, true, values).svd(true).rank());
    }

    @Test void singularValues_Full() {
        double[] found = SimpleMatrix.random(10, 10).svd(false).getSingularValues();
        assertEquals(10, found.length);

        found = SimpleMatrix.random(10, 5).svd(false).getSingularValues();
        assertEquals(5, found.length);

        found = SimpleMatrix.random(5, 10).svd(false).getSingularValues();
        assertEquals(5, found.length);
    }

    @Test void singularValues_Compact() {
        double[] found = SimpleMatrix.random(10, 10).svd(true).getSingularValues();
        assertEquals(10, found.length);

        found = SimpleMatrix.random(10, 5).svd(true).getSingularValues();
        assertEquals(5, found.length);

        found = SimpleMatrix.random(5, 10).svd(true).getSingularValues();
        assertEquals(5, found.length);
    }
}