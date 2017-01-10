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

package org.ejml.dense.row.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.dense.row.CommonOps_CR64;
import org.ejml.dense.row.MatrixFeatures_CR64;
import org.ejml.dense.row.RandomMatrices_CR64;
import org.ejml.dense.row.SpecializedOps_CR64;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F64;
import org.junit.Test;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_CR64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_CR64 extends StandardTridiagonalTests_CR64 {

    @Override
    protected TridiagonalSimilarDecomposition_F64<DMatrixRow_C64> createDecomposition() {
        return new TridiagonalDecompositionHouseholder_CR64();
    }

        /**
     * This checks to see the gammas and if the householder vectors stored in QH are correct. This
     * is done by extracting the vectors, computing reflectors, and multiplying them by A and seeing
     * if it has the expected response.
     */
    @Test
    public void testHouseholderVectors()
    {
        int N = 5;
        DMatrixRow_C64 A = RandomMatrices_CR64.createHermitian(N,-1,1,rand);
        DMatrixRow_C64 B = new DMatrixRow_C64(N,N);

//        System.out.println("A");
//        A.print();
//        System.out.println("-------------------");

        TridiagonalDecompositionHouseholder_CR64 decomp = new TridiagonalDecompositionHouseholder_CR64();

        assertTrue(safeDecomposition(decomp,A));

        DMatrixRow_C64 QT = decomp.getQT();

        double gammas[] = decomp.getGammas();

        DMatrixRow_C64 u = new DMatrixRow_C64(N,1);

        for( int i = 0; i < N-1; i++ ) {
//            System.out.println("------- Column "+i);
            u.zero();

            u.data[(i+1)*2  ] = 1;
            u.data[(i+1)*2+1] = 0;

            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QT.getReal(i,j);
                u.data[j*2+1] = QT.getImag(i,j); // the reflector stored in the row will be the conjugate
            }

            DMatrixRow_C64 Q = SpecializedOps_CR64.createReflector(u,gammas[i]);
            CommonOps_CR64.mult(Q,A,B);
            CommonOps_CR64.mult(B,Q,A);

            // sanity check
            assertTrue(MatrixFeatures_CR64.isHermitian(A,UtilEjml.TEST_F64));

            for( int j = i; j  < i+2; j++ ) {
                assertTrue(Math.abs(A.getReal(j,i))> UtilEjml.TEST_F64);
                if( j != i)
                    assertTrue(Math.abs(A.getImag(j,i))> UtilEjml.TEST_F64);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(j,i),UtilEjml.TEST_F64);
                assertEquals(0,A.getImag(j,i),UtilEjml.TEST_F64);
            }

            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(i,j),UtilEjml.TEST_F64);
                assertEquals(0,A.getImag(i,j),UtilEjml.TEST_F64);
            }
        }
    }
}