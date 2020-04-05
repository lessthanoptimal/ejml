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
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.dense.row.SpecializedOps_ZDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decompose.CheckDecompositionInterface_ZDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHessenbergSimilarDecomposition_ZDRM {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(ZMatrixRMaj A) {
        HessenbergSimilarDecomposition_ZDRM decomp = new HessenbergSimilarDecomposition_ZDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        ZMatrixRMaj Q = decomp.getQ(null);
        ZMatrixRMaj H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2e");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2e");
        assertTrue(MatrixFeatures_ZDRM.isUnitary(Q, UtilEjml.TEST_F64));

        ZMatrixRMaj temp0 = new ZMatrixRMaj(5,5);

        CommonOps_ZDRM.mult(Q,H,temp0);
        CommonOps_ZDRM.transposeConjugate(Q);
        CommonOps_ZDRM.mult(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2e");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2e");

        assertTrue(!MatrixFeatures_ZDRM.hasUncountable(H));

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A,H,UtilEjml.TEST_F64));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(4,4,rand);
        ZMatrixRMaj B = A.copy();

        HessenbergSimilarDecomposition_ZDRM decomp = new HessenbergSimilarDecomposition_ZDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A,B,0));
    }

    /**
     * Give it a matrix that is already a Hessenberg matrix and see if its comes out the same.
     */
//    @Test
//    public void testNoChange() {
//        DMatrixRMaj A = RandomMatrices.createUpperTriangle(4,1,-1,1,rand);
//
//        HessenbergSimilarDecomposition decomp = new HessenbergSimilarDecomposition(A.numRows);
//
//        assertTrue(decomp.decompose(A));
//
//        DMatrixRMaj H = decomp.getH(null);
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
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(N,N,rand);
        ZMatrixRMaj B = new ZMatrixRMaj(N,N);

        HessenbergSimilarDecomposition_ZDRM decomp = new HessenbergSimilarDecomposition_ZDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        ZMatrixRMaj QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        double gammas[] = decomp.getGammas();

        ZMatrixRMaj u = new ZMatrixRMaj(N,1);

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

            ZMatrixRMaj Q = SpecializedOps_ZDRM.createReflector(u,gammas[i]);
            CommonOps_ZDRM.multTransA(Q,A,B);
//            System.out.println("----- u ------");
//            UtilEjml.print(u);
//            System.out.println("----- Q ------")
//            Q.print();
//            System.out.println("----- B ------");
//            B.print();

            for( int j = 0; j  < i+2; j++ ) {
                assertTrue(Math.abs(B.getReal(j,i))>UtilEjml.TEST_F64);
                assertTrue(Math.abs(B.getImag(j,i))>UtilEjml.TEST_F64);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,B.getReal(j,i),UtilEjml.TEST_F64);
                assertEquals(0,B.getImag(j,i),UtilEjml.TEST_F64);
            }
            CommonOps_ZDRM.mult(B,Q,A);

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
        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(N,N,rand);

        HessenbergSimilarDecomposition_ZDRM decomp = new HessenbergSimilarDecomposition_ZDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        ZMatrixRMaj QH = decomp.getQH();

        double gammas[] = decomp.getGammas();

        ZMatrixRMaj u = new ZMatrixRMaj(N,1);

        ZMatrixRMaj Q = CommonOps_ZDRM.identity(N);
        ZMatrixRMaj temp = new ZMatrixRMaj(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[(i+1)*2] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QH.getReal(j,i);
                u.data[j*2+1] = QH.getImag(j,i);
            }

            ZMatrixRMaj Qi = SpecializedOps_ZDRM.createReflector(u,gammas[i]);

            CommonOps_ZDRM.mult(Qi,Q,temp);
            Q.set(temp);
        }
        ZMatrixRMaj expectedH = new ZMatrixRMaj(N,N);

        CommonOps_ZDRM.multTransA(Q,A,temp);
        CommonOps_ZDRM.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        ZMatrixRMaj foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(expectedH,foundH,UtilEjml.TEST_F64));
    }
}
