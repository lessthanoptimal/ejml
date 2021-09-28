/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.junit.jupiter.api.Test;

import static org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM.safeDecomposition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHessenbergSimilarDecomposition_DDRM extends EjmlStandardJUnit {
    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(DMatrixRMaj A) {
        HessenbergSimilarDecomposition_DDRM decomp = new HessenbergSimilarDecomposition_DDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        DMatrixRMaj Q = decomp.getQ(null);
        DMatrixRMaj H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2e");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2e");
        assertTrue(MatrixFeatures_DDRM.isOrthogonal(Q, UtilEjml.TEST_F64));

        DMatrixRMaj temp0 = new DMatrixRMaj(5,5);

        CommonOps_DDRM.mult(Q,H,temp0);
        CommonOps_DDRM.multTransB(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2e");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2e");

        assertTrue(!MatrixFeatures_DDRM.hasUncountable(H));

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,H,UtilEjml.TEST_F64));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,4,rand);
        DMatrixRMaj B = A.copy();

        HessenbergSimilarDecomposition_DDRM decomp = new HessenbergSimilarDecomposition_DDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,B,0));
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
     * is done by extracting the vectors, computing reflectors, and multipling them by A and seeing
     * if it has the expected response.
     */
    @Test
    public void testHouseholderVectors()
    {
        int N = 5;
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N,N,rand);
        DMatrixRMaj B = new DMatrixRMaj(N,N);

        HessenbergSimilarDecomposition_DDRM decomp = new HessenbergSimilarDecomposition_DDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        DMatrixRMaj QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        double[] gammas = decomp.getGammas();

        DMatrixRMaj u = new DMatrixRMaj(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            DMatrixRMaj Q = SpecializedOps_DDRM.createReflector(u,gammas[i]);
            CommonOps_DDRM.multTransA(Q,A,B);
//            System.out.println("----- u ------");
//            UtilEjml.print(u);
//            System.out.println("----- Q ------");
//            UtilEjml.print(Q);
//            System.out.println("----- B ------");
//            UtilEjml.print(B);

            for( int j = 0; j < i+2; j++ ) {
                assertTrue(Math.abs(B.get(j,i))>UtilEjml.TEST_F64);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,B.get(j,i),UtilEjml.TEST_F64);
            }
            CommonOps_DDRM.mult(B,Q,A);

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
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(N,N,rand);

        HessenbergSimilarDecomposition_DDRM decomp = new HessenbergSimilarDecomposition_DDRM(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        DMatrixRMaj QH = decomp.getQH();

        double[] gammas = decomp.getGammas();

        DMatrixRMaj u = new DMatrixRMaj(N,1);


        DMatrixRMaj Q = CommonOps_DDRM.identity(N);
        DMatrixRMaj temp = new DMatrixRMaj(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            DMatrixRMaj Qi = SpecializedOps_DDRM.createReflector(u,gammas[i]);

            CommonOps_DDRM.mult(Qi,Q,temp);
            Q.setTo(temp);
        }
        DMatrixRMaj expectedH = new DMatrixRMaj(N,N);

        CommonOps_DDRM.multTransA(Q,A,temp);
        CommonOps_DDRM.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        DMatrixRMaj foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_DDRM.isIdentical(expectedH,foundH,UtilEjml.TEST_F64));
    }
}
