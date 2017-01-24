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

package org.ejml.dense.row.decompose;

import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_ZDRM {

    Random rand = new Random(234);

    @Test
    public void solveU() {
        ZMatrixRMaj U = RandomMatrices_ZDRM.rectangle(3, 3, -1 ,1 ,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0,0);
            }
        }

        ZMatrixRMaj X = RandomMatrices_ZDRM.rectangle(3, 1, -1 ,1 ,rand);
        ZMatrixRMaj B = new ZMatrixRMaj(3,1);

        CommonOps_ZDRM.mult(U, X, B);

        TriangularSolver_ZDRM.solveU(U.data,B.data,3);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(X, B, UtilEjml.TEST_F64));
    }

    @Test
    public void solveL_diagReal() {
        for( int N = 1; N <= 4; N++ ) {
            ZMatrixRMaj L = createLowerTriangleDiagReal(N);

            ZMatrixRMaj X = RandomMatrices_ZDRM.rectangle(N, 1, -1, 1, rand);
            ZMatrixRMaj B = new ZMatrixRMaj(N, 1);

            CommonOps_ZDRM.mult(L, X, B);

            TriangularSolver_ZDRM.solveL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_ZDRM.isIdentical(X, B, UtilEjml.TEST_F64));
        }
    }

    /**
     * Creates a random complex lower triangular matrix with real diagonal elements
     */
    private ZMatrixRMaj createLowerTriangleDiagReal(int n) {
        ZMatrixRMaj L = RandomMatrices_ZDRM.rectangle(n, n, -1, 1, rand);
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
            ZMatrixRMaj L = createLowerTriangleDiagReal(N);

            ZMatrixRMaj L_ct = new ZMatrixRMaj(N, N);
            CommonOps_ZDRM.transposeConjugate(L,L_ct);

            ZMatrixRMaj X = RandomMatrices_ZDRM.rectangle(N, 1, -1, 1, rand);
            ZMatrixRMaj B = new ZMatrixRMaj(N, 1);

            CommonOps_ZDRM.mult(L_ct, X, B);

            TriangularSolver_ZDRM.solveConjTranL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_ZDRM.isIdentical(X, B, UtilEjml.TEST_F64));
        }
    }
}