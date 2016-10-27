/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.hessenberg;

import org.ejml.UtilEjml;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.CSpecializedOps;
import org.junit.Test;

import java.util.Random;

import static org.ejml.alg.dense.decompose.CheckDecompositionInterface_CD64.safeDecomposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHessenbergSimilarDecomposition_CD64 {

    Random rand = new Random(5745784);

    /**
     * Decomposes the matrix, extracts H and Q, then sees if it can recompute A using similar matrix stuff.
     */
    @Test
    public void testItAllTogether() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(5,5,rand);

        checkItAll(A);
    }

    private void checkItAll(CDenseMatrix64F A) {
        HessenbergSimilarDecomposition_CD64 decomp = new HessenbergSimilarDecomposition_CD64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        CDenseMatrix64F Q = decomp.getQ(null);
        CDenseMatrix64F H = decomp.getH(null);
//        System.out.println("-------- H ---------");
//        UtilEjml.print(H,"%8.2e");
//        System.out.println("-------- Q ---------");
//        UtilEjml.print(Q,"%8.2e");
        assertTrue(CMatrixFeatures.isUnitary(Q, UtilEjml.TOLERANCE));

        CDenseMatrix64F temp0 = new CDenseMatrix64F(5,5);

        CCommonOps.mult(Q,H,temp0);
        CCommonOps.transposeConjugate(Q);
        CCommonOps.mult(temp0,Q,H);

//        System.out.println("------- A ----------");
//        UtilEjml.print(A,"%8.2e");
//        System.out.println("----- Found A ------");
//        UtilEjml.print(H,"%8.2e");

        assertTrue(!CMatrixFeatures.hasUncountable(H));

        assertTrue(CMatrixFeatures.isIdentical(A,H,UtilEjml.TOLERANCE));
    }

    /**
     * Make sure it doesn't change the input
     */
    @Test
    public void testInputUnmodified() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(4,4,rand);
        CDenseMatrix64F B = A.copy();

        HessenbergSimilarDecomposition_CD64 decomp = new HessenbergSimilarDecomposition_CD64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        assertTrue(CMatrixFeatures.isIdentical(A,B,0));
    }

    /**
     * Give it a matrix that is already a Hessenberg matrix and see if its comes out the same.
     */
//    @Test
//    public void testNoChange() {
//        DenseMatrix64F A = RandomMatrices.createUpperTriangle(4,1,-1,1,rand);
//
//        HessenbergSimilarDecomposition decomp = new HessenbergSimilarDecomposition(A.numRows);
//
//        assertTrue(decomp.decompose(A));
//
//        DenseMatrix64F H = decomp.getH(null);
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
        CDenseMatrix64F A = CRandomMatrices.createRandom(N,N,rand);
        CDenseMatrix64F B = new CDenseMatrix64F(N,N);

        HessenbergSimilarDecomposition_CD64 decomp = new HessenbergSimilarDecomposition_CD64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));
        
        CDenseMatrix64F QH = decomp.getQH();
//        System.out.println("------------ QH -----------");
//        UtilEjml.print(QH);

        double gammas[] = decomp.getGammas();

        CDenseMatrix64F u = new CDenseMatrix64F(N,1);

//        UtilEjml.print(A);
//        System.out.println("-------------------");

        for( int i = 0; i < N-1; i++ ) {
//            System.out.println("------- Column "+i);
            u.zero();
            u.data[(i+1)*2  ] = 1;
            u.data[(i+1)*2+1] = 0;

            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QH.getReal(j,i);
                u.data[j*2+1] = QH.getImaginary(j,i);
            }

            CDenseMatrix64F Q = CSpecializedOps.createReflector(u,gammas[i]);
            CCommonOps.multTransA(Q,A,B);
//            System.out.println("----- u ------");
//            UtilEjml.print(u);
//            System.out.println("----- Q ------")
//            Q.print();
//            System.out.println("----- B ------");
//            B.print();

            for( int j = 0; j  < i+2; j++ ) {
                assertTrue(Math.abs(B.getReal(j,i))>UtilEjml.TOLERANCE);
                assertTrue(Math.abs(B.getImaginary(j,i))>UtilEjml.TOLERANCE);
            }
            for( int j = i+2; j < N; j++ ) {
                assertEquals(0,B.getReal(j,i),UtilEjml.TOLERANCE);
                assertEquals(0,B.getImaginary(j,i),UtilEjml.TOLERANCE);
            }
            CCommonOps.mult(B,Q,A);

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
        CDenseMatrix64F A = CRandomMatrices.createRandom(N,N,rand);

        HessenbergSimilarDecomposition_CD64 decomp = new HessenbergSimilarDecomposition_CD64(A.numRows);

        assertTrue(safeDecomposition(decomp,A));

        CDenseMatrix64F QH = decomp.getQH();

        double gammas[] = decomp.getGammas();

        CDenseMatrix64F u = new CDenseMatrix64F(N,1);

        CDenseMatrix64F Q = CCommonOps.identity(N);
        CDenseMatrix64F temp = new CDenseMatrix64F(N,N);

        for( int i = N-2; i >= 0; i-- ) {
            u.zero();
            u.data[(i+1)*2] = 1;
            for( int j = i+2; j < N; j++ ) {
                u.data[j*2]   = QH.getReal(j,i);
                u.data[j*2+1] = QH.getImaginary(j,i);
            }

            CDenseMatrix64F Qi = CSpecializedOps.createReflector(u,gammas[i]);

            CCommonOps.mult(Qi,Q,temp);
            Q.set(temp);
        }
        CDenseMatrix64F expectedH = new CDenseMatrix64F(N,N);

        CCommonOps.multTransA(Q,A,temp);
        CCommonOps.mult(temp,Q,expectedH);

//        UtilEjml.print(expectedH);

        CDenseMatrix64F foundH = decomp.getH(null);

//        UtilEjml.print(foundH);

        assertTrue(CMatrixFeatures.isIdentical(expectedH,foundH,UtilEjml.TOLERANCE));
    }
}
