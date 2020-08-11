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

package org.ejml.dense.row.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.dense.row.SpecializedOps_CDRM;
import org.ejml.interfaces.decomposition.TridiagonalSimilarDecomposition_F32;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_CDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTridiagonalDecompositionHouseholder_CDRM extends StandardTridiagonalTests_CDRM {

    @Override
    protected TridiagonalSimilarDecomposition_F32<CMatrixRMaj> createDecomposition() {
        return new TridiagonalDecompositionHouseholder_CDRM();
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
        CMatrixRMaj A = RandomMatrices_CDRM.hermitian(N,-1,1,rand);
        CMatrixRMaj B = new CMatrixRMaj(N,N);

//        System.out.println("A");
//        A.print();
//        System.out.println("-------------------");

        TridiagonalDecompositionHouseholder_CDRM decomp = new TridiagonalDecompositionHouseholder_CDRM();

        assertTrue(safeDecomposition(decomp,A));

        CMatrixRMaj QT = decomp.getQT();

        float gammas[] = decomp.getGammas();

        CMatrixRMaj u = new CMatrixRMaj(N,1);

        for( int i = 0; i < N-1; i++ ) {
//            System.out.println("------- Column "+i);
            u.zero();

            u.data[(i+1)*2  ] = 1;
            u.data[(i+1)*2+1] = 0;

            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QT.getReal(i,j);
                u.data[j*2+1] = QT.getImag(i,j); // the reflector stored in the row will be the conjugate
            }

            CMatrixRMaj Q = SpecializedOps_CDRM.createReflector(u,gammas[i]);
            CommonOps_CDRM.mult(Q,A,B);
            CommonOps_CDRM.mult(B,Q,A);

            // sanity check
            assertTrue(MatrixFeatures_CDRM.isHermitian(A,UtilEjml.TEST_F32));

            for( int j = i; j  < i+2; j++ ) {
                assertTrue(Math.abs(A.getReal(j,i))> UtilEjml.TEST_F32);
                if( j != i)
                    assertTrue(Math.abs(A.getImag(j,i))> UtilEjml.TEST_F32);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(j,i),UtilEjml.TEST_F32);
                assertEquals(0,A.getImag(j,i),UtilEjml.TEST_F32);
            }

            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,A.getReal(i,j),UtilEjml.TEST_F32);
                assertEquals(0,A.getImag(i,j),UtilEjml.TEST_F32);
            }
        }
    }
}