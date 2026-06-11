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
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.data.MatrixType;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSimpleEVD extends EjmlStandardJUnit {
    // Verifies that the input is not modified
    @Test void inputModified() {
        DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(5, 5, rand);
        DMatrixRMaj original = mat.copy();

        // Create a mock EVD that we know will modify the input
        new SimpleEVD<>(mat) {
            @Override protected EigenDecomposition createEigen( MatrixType type ) {
                return new EigenDecomposition_F64() {
                    @Override public Complex_F64 getEigenvalue( int index ) {return null; }
                    @Override public int getNumberOfEigenvalues() { return 0; }
                    @Override public Matrix getEigenVector( int index ) {return null;}
                    @Override public boolean inputModified() {return true;}
                    @Override public boolean decompose( Matrix orig ) {
                        ((DMatrixRMaj)orig).set(0,0, 100);
                        return true;
                    }
                };
            }
        };

        assertTrue(MatrixFeatures_DDRM.isIdentical(mat, original, 0.0));
    }
}
