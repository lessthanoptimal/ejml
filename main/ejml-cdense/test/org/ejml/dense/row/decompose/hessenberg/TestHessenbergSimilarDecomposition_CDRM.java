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
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_CDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHessenbergSimilarDecomposition_CDRM {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(CMatrixRMaj A) {
        HessenbergSimilarDecomposition_CDRM decomp = new HessenbergSimilarDecomposition_CDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        CMatrixRMaj Q = decomp.getQ(null);
        CMatrixRMaj H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2fe");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2fe");
        assertTrue(MatrixFeatures_CDRM.isUnitary(Q, UtilEjml.TEST_F32));

        CMatrixRMaj temp0 = new CMatrixRMaj(5,5);

        CommonOps_CDRM.mult(Q,H,temp0);
        CommonOps_CDRM.transposeConjugate(Q);
        CommonOps_CDRM.mult(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2fe");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2fe");

        assertTrue(!MatrixFeatures_CDRM.hasUncountable(H));

        assertTrue(MatrixFeatures_CDRM.isIdentical(A,H,UtilEjml.TEST_F32));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,4,rand);
        CMatrixRMaj B = A.copy();

        HessenbergSimilarDecomposition_CDRM decomp = new HessenbergSimilarDecomposition_CDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_CDRM.isIdentical(A,B,0));
    }

    /**
     * Give it a matrix that is already a Hessenberg matrix and see if its comes out the same.
     */
//    @Test
//    public void testNoChange() {
//        FMatrixRMaj A = RandomMatrices.createUpperTriangle(4,1,-1,1,rand);
//
//        HessenbergSimilarDecomposition decomp = new HessenbergSimilarDecomposition(A.numRows);
//
//        assertTrue(decomp.decompose(A));
//
//        FMatrixRMaj H = decomp.getH(null);
//
//        assertTrue(MatrixFeatures.isIdentical(A,H,0));
//    }

    /**
     * This checks to see the gammas and if the householder vectors stored in QH are correct. This
     * is done by extracting the vectors, computing reflectors, and multiplying them by A and seeing
     * if it has the expected response.
     */
    @Test
    public void testHouseholderVectors()
    {
        int N = 5;
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(N,N,rand);
        CMatrixRMaj B = new CMatrixRMaj(N,N);

        HessenbergSimilarDecomposition_CDRM decomp = new HessenbergSimilarDecomposition_CDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        CMatrixRMaj QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        float gammas[] = decomp.getGammas();

        CMatrixRMaj u = new CMatrixRMaj(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
//            System.out.println("------- Column "+i);
            u.zero();
            u.data[(i+1)*2  ] = 1;
            u.data[(i+1)*2+1] = 0;

            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QH.getReal(j,i);
                u.data[j*2+1] = QH.getImag(j,i);
            }

            CMatrixRMaj Q = SpecializedOps_CDRM.createReflector(u,gammas[i]);
            CommonOps_CDRM.multTransA(Q,A,B);
//            System.out.println("----- u ------");
//            UtilEjml.print(u);
//            System.out.println("----- Q ------")
//            Q.print();
//            System.out.println("----- B ------");
//            B.print();

            for( int j = 0; j  < i+2; j++ ) {
                assertTrue(Math.abs(B.getReal(j,i))>UtilEjml.TEST_F32);
                assertTrue(Math.abs(B.getImag(j,i))>UtilEjml.TEST_F32);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,B.getReal(j,i),UtilEjml.TEST_F32);
                assertEquals(0,B.getImag(j,i),UtilEjml.TEST_F32);
            }
            CommonOps_CDRM.mult(B,Q,A);

//            System.out.println("-------------------");
//            A.print();
//            System.out.println("-------------------");
        }
    }

    /**
     * Compute the overall Q matrix from the stored u vectors.  See if the extract H is the same as the expected H.
     */
    @Test
    public void testH() {
        int N = 5;
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(N,N,rand);

        HessenbergSimilarDecomposition_CDRM decomp = new HessenbergSimilarDecomposition_CDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        CMatrixRMaj QH = decomp.getQH();

        float gammas[] = decomp.getGammas();

        CMatrixRMaj u = new CMatrixRMaj(N,1);

        CMatrixRMaj Q = CommonOps_CDRM.identity(N);
        CMatrixRMaj temp = new CMatrixRMaj(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[(i+1)*2] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QH.getReal(j,i);
                u.data[j*2+1] = QH.getImag(j,i);
            }

            CMatrixRMaj Qi = SpecializedOps_CDRM.createReflector(u,gammas[i]);

            CommonOps_CDRM.mult(Qi,Q,temp);
            Q.set(temp);
        }
        CMatrixRMaj expectedH = new CMatrixRMaj(N,N);

        CommonOps_CDRM.multTransA(Q,A,temp);
        CommonOps_CDRM.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        CMatrixRMaj foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_CDRM.isIdentical(expectedH,foundH,UtilEjml.TEST_F32));
    }
}
