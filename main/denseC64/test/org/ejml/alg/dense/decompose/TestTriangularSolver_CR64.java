/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_C64;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_CR64 {

    Random rand = new Random(234);

    @Test
    public void solveU() {
        RowMatrix_C64 U = RandomMatrices_CR64.createRandom(3, 3, -1 ,1 ,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0,0);
            }
        }

        RowMatrix_C64 X = RandomMatrices_CR64.createRandom(3, 1, -1 ,1 ,rand);
        RowMatrix_C64 B = new RowMatrix_C64(3,1);

        CommonOps_CR64.mult(U, X, B);

        TriangularSolver_CR64.solveU(U.data,B.data,3);

        assertTrue(MatrixFeatures_CR64.isIdentical(X, B, UtilEjml.TEST_F64));
    }

    @Test
    public void solveL_diagReal() {
        for( int N = 1; N <= 4; N++ ) {
            RowMatrix_C64 L = createLowerTriangleDiagReal(N);

            RowMatrix_C64 X = RandomMatrices_CR64.createRandom(N, 1, -1, 1, rand);
            RowMatrix_C64 B = new RowMatrix_C64(N, 1);

            CommonOps_CR64.mult(L, X, B);

            TriangularSolver_CR64.solveL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CR64.isIdentical(X, B, UtilEjml.TEST_F64));
        }
    }

    /**
     * Creates a random complex lower triangular matrix with real diagonal elements
     */
    private RowMatrix_C64 createLowerTriangleDiagReal(int n) {
        RowMatrix_C64 L = RandomMatrices_CR64.createRandom(n, n, -1, 1, rand);
        for (int i = 0; i < L.numRows; i++) {
            for (int j = i + 1; j < L.numCols; j++) {
                L.set(i, j, 0, 0);
            }
        }
        for (int i = 0; i < L.numRows; i++) {
            L.data[(i*L.numRows+i)*2+1] = 0;
        }
        return L;
    }

    @Test
    public void solveConjTranL_diagReal() {
        for( int N = 1; N <= 4; N++ ) {
            RowMatrix_C64 L = createLowerTriangleDiagReal(N);

            RowMatrix_C64 L_ct = new RowMatrix_C64(N, N);
            CommonOps_CR64.transposeConjugate(L,L_ct);

            RowMatrix_C64 X = RandomMatrices_CR64.createRandom(N, 1, -1, 1, rand);
            RowMatrix_C64 B = new RowMatrix_C64(N, 1);

            CommonOps_CR64.mult(L_ct, X, B);

            TriangularSolver_CR64.solveConjTranL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CR64.isIdentical(X, B, UtilEjml.TEST_F64));
        }
    }
}