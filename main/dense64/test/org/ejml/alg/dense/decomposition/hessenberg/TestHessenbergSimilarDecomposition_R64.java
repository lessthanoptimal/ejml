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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.ops.SpecializedOps_R64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface_R64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHessenbergSimilarDecomposition_R64 {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(DMatrixRow_F64 A) {
        HessenbergSimilarDecomposition_R64 decomp = new HessenbergSimilarDecomposition_R64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        DMatrixRow_F64 Q = decomp.getQ(null);
        DMatrixRow_F64 H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2e");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2e");
        assertTrue(MatrixFeatures_R64.isOrthogonal(Q, UtilEjml.TEST_F64));

        DMatrixRow_F64 temp0 = new DMatrixRow_F64(5,5);

        CommonOps_R64.mult(Q,H,temp0);
        CommonOps_R64.multTransB(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2e");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2e");

        assertTrue(!MatrixFeatures_R64.hasUncountable(H));

        assertTrue(MatrixFeatures_R64.isIdentical(A,H,UtilEjml.TEST_F64));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(4,4,rand);
        DMatrixRow_F64 B = A.copy();

        HessenbergSimilarDecomposition_R64 decomp = new HessenbergSimilarDecomposition_R64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_R64.isIdentical(A,B,0));
    }

    /**
     * Give it a matrix that is already a Hessenberg matrix and see if its comes out the same.
     */
//    @Test
//    public void testNoChange() {
//        DMatrixRow_F64 A = RandomMatrices.createUpperTriangle(4,1,-1,1,rand);
//
//        HessenbergSimilarDecomposition decomp = new HessenbergSimilarDecomposition(A.numRows);
//
//        assertTrue(decomp.decompose(A));
//
//        DMatrixRow_F64 H = decomp.getH(null);
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
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(N,N,rand);
        DMatrixRow_F64 B = new DMatrixRow_F64(N,N);

        HessenbergSimilarDecomposition_R64 decomp = new HessenbergSimilarDecomposition_R64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        DMatrixRow_F64 QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        double gammas[] = decomp.getGammas();

        DMatrixRow_F64 u = new DMatrixRow_F64(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            DMatrixRow_F64 Q = SpecializedOps_R64.createReflector(u,gammas[i]);
            CommonOps_R64.multTransA(Q,A,B);
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
            CommonOps_R64.mult(B,Q,A);

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
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(N,N,rand);

        HessenbergSimilarDecomposition_R64 decomp = new HessenbergSimilarDecomposition_R64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        DMatrixRow_F64 QH = decomp.getQH();

        double gammas[] = decomp.getGammas();

        DMatrixRow_F64 u = new DMatrixRow_F64(N,1);


        DMatrixRow_F64 Q = CommonOps_R64.identity(N);
        DMatrixRow_F64 temp = new DMatrixRow_F64(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            DMatrixRow_F64 Qi = SpecializedOps_R64.createReflector(u,gammas[i]);

            CommonOps_R64.mult(Qi,Q,temp);
            Q.set(temp);
        }
        DMatrixRow_F64 expectedH = new DMatrixRow_F64(N,N);

        CommonOps_R64.multTransA(Q,A,temp);
        CommonOps_R64.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        DMatrixRow_F64 foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_R64.isIdentical(expectedH,foundH,UtilEjml.TEST_F64));

        System.out.println();
    }
}
