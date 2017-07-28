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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRColPivDecompositionHouseholderColumn_DDRM {

    Random rand = new Random(234);

    /**
     * Test it against a specially created matrix which should not require pivots
     * and is full rank.
     */
    @Test
    public void noPivot() {
        DMatrixRMaj A = RandomMatrices_DDRM.orthogonal(6, 3, rand);

        // make sure the columns have norms in descending magnitude
        for( int i = 0; i < A.numCols; i++ ) {

            double scale = (A.numCols-i)*3;

            for( int j = 0; j < A.numRows; j++ ) {
                A.set(j,i,scale*A.get(j,i));
            }
        }

        // because no pivots are happening this should be equivalent of the normal QR
        QRColPivDecompositionHouseholderColumn_DDRM alg = new QRColPivDecompositionHouseholderColumn_DDRM();
        assertTrue(alg.decompose(A));

        DMatrixRMaj Q = alg.getQ(null, false);
        DMatrixRMaj R = alg.getR(null, false);

        DMatrixRMaj found = new DMatrixRMaj(A.numRows,A.numCols);
        CommonOps_DDRM.mult(Q,R,found);

        assertTrue(MatrixFeatures_DDRM.isIdentical(A,found, UtilEjml.TEST_F64));

        // check the pivots
        int pivots[] = alg.getColPivots();
        for( int i = 0; i < A.numCols; i++ ) {
            assertEquals(i,pivots[i]);
        }

        DMatrixRMaj P = alg.getColPivotMatrix(null);
        assertTrue(MatrixFeatures_DDRM.isIdentity(P, UtilEjml.TEST_F64));
    }

    /**
     * Test it against a rank deficient matrix
     */
    @Test
    public void testRankDeficient() {
        int numRows = 10;

        for( int numSingular = 0; numSingular < numRows-1; numSingular++ )  {

            // construct a singular matrix from its SVD decomposition
            SimpleMatrix U = SimpleMatrix.wrap(RandomMatrices_DDRM.orthogonal(numRows,numRows,rand));
            SimpleMatrix S = SimpleMatrix.diag( DMatrixRMaj.class, 1,2,3,4,5,6,7,8,9,10);
            SimpleMatrix V = SimpleMatrix.wrap(RandomMatrices_DDRM.orthogonal(numRows,numRows,rand));

            for( int i = 0; i < numSingular; i++ ) {
                S.set(i,i,0);
            }

            SimpleMatrix A = U.mult(S).mult(V.transpose());

            QRColPivDecompositionHouseholderColumn_DDRM alg = new QRColPivDecompositionHouseholderColumn_DDRM();
            assertTrue(alg.decompose((DMatrixRMaj)A.getMatrix()));

            checkDecomposition(false,(DMatrixRMaj)A.getMatrix(),alg);
        }
    }

    /**
     * See how the decomposition goes against various matrices of different sizes
     */
    @Test
    public void testRandomMatrix() {
        checkDecomposition(5, 5 ,false);
        checkDecomposition(10, 5,false);
        checkDecomposition(5, 10,false);
        checkDecomposition(5, 5 ,true);
        checkDecomposition(10, 5,true);
        checkDecomposition(5, 10,true);
    }

    /**
     * See if a zero matrix is gracefully handled
     */
    @Test
    public void testZeroMatrix() {
        DMatrixRMaj A = new DMatrixRMaj(5,5);

        QRColPivDecompositionHouseholderColumn_DDRM alg = new QRColPivDecompositionHouseholderColumn_DDRM();
        assertTrue(alg.decompose(A));

        checkDecomposition(false, A, alg);
        checkDecomposition(true, A, alg);
    }

    private void checkDecomposition( int numRows , int numCols , boolean compact )
    {

        for( int i = 0; i < 10; i++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(numRows, numCols, rand);

            QRColPivDecompositionHouseholderColumn_DDRM alg = new QRColPivDecompositionHouseholderColumn_DDRM();
            assertTrue(alg.decompose(A));

            checkDecomposition(compact, A, alg);
        }
    }

    private void checkDecomposition(boolean compact, DMatrixRMaj a,
                                    QRColPivDecompositionHouseholderColumn_DDRM alg) {
        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null, compact));
        SimpleMatrix R = SimpleMatrix.wrap(alg.getR(null, compact));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getColPivotMatrix(null));

        SimpleMatrix AA = SimpleMatrix.wrap(a);

        SimpleMatrix expected = AA.mult(P);
        SimpleMatrix found = Q.mult(R);

//        Q.print();
//        R.print();
//        P.print();
//        System.out.println("asdfasdf");
//        expected.print();
//        found.print();
        assertTrue(expected.isIdentical(found,UtilEjml.TEST_F64));
    }

}
