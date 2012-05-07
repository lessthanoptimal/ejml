/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestQRColPivDecompositionHouseholderColumn {

    Random rand = new Random(234);

    /**
     * Test it against a specially created matrix which should not require pivots
     * and is full rank.
     */
    @Test
    public void noPivot() {
        DenseMatrix64F A = RandomMatrices.createOrthogonal(6, 3, rand);

        // make sure the columns have norms in descending magnitude
        for( int i = 0; i < A.numCols; i++ ) {

            double scale = (A.numCols-i)*3;

            for( int j = 0; j < A.numRows; j++ ) {
                A.set(j,i,scale*A.get(j,i));
            }
        }

        // because no pivots are happening this should be equivalent of the normal QR
        QRColPivDecompositionHouseholderColumn alg = new QRColPivDecompositionHouseholderColumn();
        assertTrue(alg.decompose(A));

        DenseMatrix64F Q = alg.getQ(null, false);
        DenseMatrix64F R = alg.getR(null, false);

        DenseMatrix64F found = new DenseMatrix64F(A.numRows,A.numCols);
        CommonOps.mult(Q,R,found);

        assertTrue(MatrixFeatures.isIdentical(A,found,1e-8));

        // check the pivots
        int pivots[] = alg.getPivots();
        for( int i = 0; i < A.numCols; i++ ) {
            assertEquals(i,pivots[i]);
        }

        DenseMatrix64F P = alg.getPivotMatrix(null);
        assertTrue(MatrixFeatures.isIdentity(P, 1e-8));
    }

    /**
     * Test it against a rank deficient matrix
     */
    @Test
    public void testRankDeficient() {
        int numRows = 10;

        for( int numSingular = 0; numSingular < numRows-1; numSingular++ )  {

            // construct a singular matrix from its SVD decomposition
            SimpleMatrix U = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,numRows,rand));
            SimpleMatrix S = SimpleMatrix.diag(1,2,3,4,5,6,7,8,9,10);
            SimpleMatrix V = SimpleMatrix.wrap(RandomMatrices.createOrthogonal(numRows,numRows,rand));

            for( int i = 0; i < numSingular; i++ ) {
                S.set(i,i,0);
            }

            SimpleMatrix A = U.mult(S).mult(V.transpose());

            QRColPivDecompositionHouseholderColumn alg = new QRColPivDecompositionHouseholderColumn();
            assertTrue(alg.decompose(A.getMatrix()));

            checkDecomposition(false,A.getMatrix(),alg);
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
        DenseMatrix64F A = new DenseMatrix64F(5,5);

        QRColPivDecompositionHouseholderColumn alg = new QRColPivDecompositionHouseholderColumn();
        assertTrue(alg.decompose(A));

        checkDecomposition(false, A, alg);
        checkDecomposition(true, A, alg);
    }

    private void checkDecomposition( int numRows , int numCols , boolean compact )
    {

        for( int i = 0; i < 10; i++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(numRows, numCols, rand);

            QRColPivDecompositionHouseholderColumn alg = new QRColPivDecompositionHouseholderColumn();
            assertTrue(alg.decompose(A));

            checkDecomposition(compact, A, alg);
        }
    }

    private void checkDecomposition(boolean compact, DenseMatrix64F a,
                                    QRColPivDecompositionHouseholderColumn alg) {
        SimpleMatrix Q = SimpleMatrix.wrap(alg.getQ(null, compact));
        SimpleMatrix R = SimpleMatrix.wrap(alg.getR(null, compact));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivotMatrix(null));

        SimpleMatrix AA = SimpleMatrix.wrap(a);

        SimpleMatrix expected = AA.mult(P);
        SimpleMatrix found = Q.mult(R);

//        Q.print();
//        R.print();
//        P.print();
//        System.out.println("asdfasdf");
//        expected.print();
//        found.print();
        assertTrue(expected.isIdentical(found,1e-8));
    }

}
