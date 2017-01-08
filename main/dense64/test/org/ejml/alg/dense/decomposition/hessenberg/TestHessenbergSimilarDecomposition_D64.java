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
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.ops.SpecializedOps_D64;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decomposition.CheckDecompositionInterface_D64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHessenbergSimilarDecomposition_D64 {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        RowMatrix_F64 A = RandomMatrices_D64.createRandom(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(RowMatrix_F64 A) {
        HessenbergSimilarDecomposition_D64 decomp = new HessenbergSimilarDecomposition_D64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        RowMatrix_F64 Q = decomp.getQ(null);
        RowMatrix_F64 H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2e");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2e");
        assertTrue(MatrixFeatures_D64.isOrthogonal(Q, UtilEjml.TEST_F64));

        RowMatrix_F64 temp0 = new RowMatrix_F64(5,5);

        CommonOps_D64.mult(Q,H,temp0);
        CommonOps_D64.multTransB(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2e");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2e");

        assertTrue(!MatrixFeatures_D64.hasUncountable(H));

        assertTrue(MatrixFeatures_D64.isIdentical(A,H,UtilEjml.TEST_F64));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        RowMatrix_F64 A = RandomMatrices_D64.createRandom(4,4,rand);
        RowMatrix_F64 B = A.copy();

        HessenbergSimilarDecomposition_D64 decomp = new HessenbergSimilarDecomposition_D64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(MatrixFeatures_D64.isIdentical(A,B,0));
    }

    /**
     * Give it a matrix that is already a Hessenberg matrix and see if its comes out the same.
     */
//    @Test
//    public void testNoChange() {
//        RowMatrix_F64 A = RandomMatrices.createUpperTriangle(4,1,-1,1,rand);
//
//        HessenbergSimilarDecomposition decomp = new HessenbergSimilarDecomposition(A.numRows);
//
//        assertTrue(decomp.decompose(A));
//
//        RowMatrix_F64 H = decomp.getH(null);
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
        RowMatrix_F64 A = RandomMatrices_D64.createRandom(N,N,rand);
        RowMatrix_F64 B = new RowMatrix_F64(N,N);

        HessenbergSimilarDecomposition_D64 decomp = new HessenbergSimilarDecomposition_D64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        RowMatrix_F64 QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        double gammas[] = decomp.getGammas();

        RowMatrix_F64 u = new RowMatrix_F64(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            RowMatrix_F64 Q = SpecializedOps_D64.createReflector(u,gammas[i]);
            CommonOps_D64.multTransA(Q,A,B);
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
            CommonOps_D64.mult(B,Q,A);

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
        RowMatrix_F64 A = RandomMatrices_D64.createRandom(N,N,rand);

        HessenbergSimilarDecomposition_D64 decomp = new HessenbergSimilarDecomposition_D64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        RowMatrix_F64 QH = decomp.getQH();

        double gammas[] = decomp.getGammas();

        RowMatrix_F64 u = new RowMatrix_F64(N,1);


        RowMatrix_F64 Q = CommonOps_D64.identity(N);
        RowMatrix_F64 temp = new RowMatrix_F64(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[i+1] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j] = QH.get(j,i);
            }

            RowMatrix_F64 Qi = SpecializedOps_D64.createReflector(u,gammas[i]);

            CommonOps_D64.mult(Qi,Q,temp);
            Q.set(temp);
        }
        RowMatrix_F64 expectedH = new RowMatrix_F64(N,N);

        CommonOps_D64.multTransA(Q,A,temp);
        CommonOps_D64.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        RowMatrix_F64 foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(MatrixFeatures_D64.isIdentical(expectedH,foundH,UtilEjml.TEST_F64));

        System.out.println();
    }
}
