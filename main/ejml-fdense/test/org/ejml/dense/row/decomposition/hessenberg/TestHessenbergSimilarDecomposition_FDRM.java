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

package org.ejml.dense.row.decomposition.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.SpecializedOps_FDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHessenbergSimilarDecomposition_FDRM {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(FMatrixRMaj A) {
        HessenbergSimilarDecomposition_FDRM decomp = new HessenbergSimilarDecomposition_FDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        FMatrixRMaj Q = decomp.getQ(null);
        FMatrixRMaj H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2fe");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2fe");
        assertTrue(MatrixFeatures_FDRM.isOrthogonal(Q, UtilEjml.TEST_F32));

        FMatrixRMaj temp0 = new FMatrixRMaj(5,5);

        CommonOps_FDRM.mult(Q,H,temp0);
        CommonOps_FDRM.multTransB(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2fe");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2fe");

        assertTrue(!MatrixFeatures_FDRM.hasUncountable(H));

        assertTrue(MatrixFeatures_FDRM.isIdentical(A,H,UtilEjml.TEST_F32));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,4,rand);
        FMatrixRMaj B = A.copy();

        HessenbergSimilarDecomposition_FDRM decomp = new HessenbergSimilarDecomposition_FDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_FDRM.isIdentical(A,B,0));
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
     * is done by extracting the vectors, computing reflectors, and multipling them by A and seeing
     * if it has the expected response.
     */
    @Test
    public void testHouseholderVectors()
    {
        int N = 5;
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N,N,rand);
        FMatrixRMaj B = new FMatrixRMaj(N,N);

        HessenbergSimilarDecomposition_FDRM decomp = new HessenbergSimilarDecomposition_FDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        FMatrixRMaj QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        float gammas[] = decomp.getGammas();

        FMatrixRMaj u = new FMatrixRMaj(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            FMatrixRMaj Q = SpecializedOps_FDRM.createReflector(u,gammas[i]);
            CommonOps_FDRM.multTransA(Q,A,B);
//            System.out.println("----- u ------");
//            UtilEjml.print(u);
//            System.out.println("----- Q ------");
//            UtilEjml.print(Q);
//            System.out.println("----- B ------");
//            UtilEjml.print(B);

            for( int j = 0; j < i+2; j++ ) {
                assertTrue(Math.abs(B.get(j,i))>UtilEjml.TEST_F32);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,B.get(j,i),UtilEjml.TEST_F32);
            }
            CommonOps_FDRM.mult(B,Q,A);

//            System.out.println("-------------------");
//            UtilEjml.print(A);
//            System.out.println("-------------------");
        }
    }

    /**
     * Compute the overall Q matrix from the stored u vectors.  See if the extract H is the same as the expected H.
     */
    @Test
    public void testH() {
        int N = 5;
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(N,N,rand);

        HessenbergSimilarDecomposition_FDRM decomp = new HessenbergSimilarDecomposition_FDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        FMatrixRMaj QH = decomp.getQH();

        float gammas[] = decomp.getGammas();

        FMatrixRMaj u = new FMatrixRMaj(N,1);


        FMatrixRMaj Q = CommonOps_FDRM.identity(N);
        FMatrixRMaj temp = new FMatrixRMaj(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            FMatrixRMaj Qi = SpecializedOps_FDRM.createReflector(u,gammas[i]);

            CommonOps_FDRM.mult(Qi,Q,temp);
            Q.set(temp);
        }
        FMatrixRMaj expectedH = new FMatrixRMaj(N,N);

        CommonOps_FDRM.multTransA(Q,A,temp);
        CommonOps_FDRM.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        FMatrixRMaj foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_FDRM.isIdentical(expectedH,foundH,UtilEjml.TEST_F32));

        System.out.println();
    }
}
