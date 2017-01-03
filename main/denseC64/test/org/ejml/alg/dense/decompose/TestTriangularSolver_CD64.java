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
import org.ejml.data.CDenseMatrix64F;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTriangularSolver_CD64 {

    Random rand = new Random(234);

    @Test
    public void solveU() {
        CDenseMatrix64F U = RandomMatrices_CD64.createRandom(3, 3, -1 ,1 ,rand);
        for( int i = 0; i < U.numRows; i++ ) {
            for( int j = 0; j < i; j++ ) {
                U.set(i,j,0,0);
            }
        }

        CDenseMatrix64F X = RandomMatrices_CD64.createRandom(3, 1, -1 ,1 ,rand);
        CDenseMatrix64F B = new CDenseMatrix64F(3,1);

        CommonOps_CD64.mult(U, X, B);

        TriangularSolver_CD64.solveU(U.data,B.data,3);

        assertTrue(MatrixFeatures_CD64.isIdentical(X, B, UtilEjml.TEST_64F));
    }

    @Test
    public void solveL_diagReal() {
        for( int N = 1; N <= 4; N++ ) {
            CDenseMatrix64F L = createLowerTriangleDiagReal(N);

            CDenseMatrix64F X = RandomMatrices_CD64.createRandom(N, 1, -1, 1, rand);
            CDenseMatrix64F B = new CDenseMatrix64F(N, 1);

            CommonOps_CD64.mult(L, X, B);

            TriangularSolver_CD64.solveL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CD64.isIdentical(X, B, UtilEjml.TEST_64F));
        }
    }

    /**
     * Creates a random complex lower triangular matrix with real diagonal elements
     */
    private CDenseMatrix64F createLowerTriangleDiagReal(int n) {
        CDenseMatrix64F L = RandomMatrices_CD64.createRandom(n, n, -1, 1, rand);
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
            CDenseMatrix64F L = createLowerTriangleDiagReal(N);

            CDenseMatrix64F L_ct = new CDenseMatrix64F(N, N);
            CommonOps_CD64.transposeConjugate(L,L_ct);

            CDenseMatrix64F X = RandomMatrices_CD64.createRandom(N, 1, -1, 1, rand);
            CDenseMatrix64F B = new CDenseMatrix64F(N, 1);

            CommonOps_CD64.mult(L_ct, X, B);

            TriangularSolver_CD64.solveConjTranL_diagReal(L.data, B.data, N);

            assertTrue(MatrixFeatures_CD64.isIdentical(X, B, UtilEjml.TEST_64F));
        }
    }
}