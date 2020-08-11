/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_CDRM {

    Random rand = new Random(234);

    @Test
    public void solveU() {
        CMatrixRMaj U = RandomMatrices_CDRM.rectangle(3, 3, -1 ,1 ,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0,0);
            }
        }

        CMatrixRMaj X = RandomMatrices_CDRM.rectangle(3, 1, -1 ,1 ,rand);
        CMatrixRMaj B = new CMatrixRMaj(3,1);

        CommonOps_CDRM.mult(U, X, B);

        TriangularSolver_CDRM.solveU(U.data,B.data,3);

        assertTrue(MatrixFeatures_CDRM.isIdentical(X, B, UtilEjml.TEST_F32));
    }

    @Test
    public void solveL_diagReal() {
        for( int N = 1; N <= 4; N++ ) {
            CMatrixRMaj L = createLowerTriangleDiagReal(N);

            CMatrixRMaj X = RandomMatrices_CDRM.rectangle(N, 1, -1, 1, rand);
            CMatrixRMaj B = new CMatrixRMaj(N, 1);

            CommonOps_CDRM.mult(L, X, B);

            TriangularSolver_CDRM.solveL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CDRM.isIdentical(X, B, UtilEjml.TEST_F32));
        }
    }

    /**
     * Creates a random complex lower triangular matrix with real diagonal elements
     */
    private CMatrixRMaj createLowerTriangleDiagReal(int n) {
        CMatrixRMaj L = RandomMatrices_CDRM.rectangle(n, n, -1, 1, rand);
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
            CMatrixRMaj L = createLowerTriangleDiagReal(N);

            CMatrixRMaj L_ct = new CMatrixRMaj(N, N);
            CommonOps_CDRM.transposeConjugate(L,L_ct);

            CMatrixRMaj X = RandomMatrices_CDRM.rectangle(N, 1, -1, 1, rand);
            CMatrixRMaj B = new CMatrixRMaj(N, 1);

            CommonOps_CDRM.mult(L_ct, X, B);

            TriangularSolver_CDRM.solveConjTranL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CDRM.isIdentical(X, B, UtilEjml.TEST_F32));
        }
    }
}