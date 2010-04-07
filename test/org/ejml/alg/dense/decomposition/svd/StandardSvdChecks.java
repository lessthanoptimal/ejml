/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.ops.SingularOps;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class StandardSvdChecks {

    Random rand = new Random(73675);

    public abstract SingularValueDecomposition createSvd();

    public void allTests() {
        testDecompositionOfTrivial();
        testWide();
        testTall();
        testZero();
        testIdentity();
        testLarger();
        testLots();
    }

    public void testDecompositionOfTrivial()
    {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(3, SingularOps.rank(alg, UtilEjml.EPS));
        assertEquals(0, SingularOps.nullity(alg, UtilEjml.EPS));

        double []w = alg.getSingularValues();
        assertEquals(9.59186,w[0],1e-5);
        assertEquals(5.18005,w[1],1e-5);
        assertEquals(4.55558,w[2],1e-5);

        checkComponents(alg,A);
    }

    public void testWide() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,20,-1,1,rand);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testTall() {
        DenseMatrix64F A = RandomMatrices.createRandom(21,5,-1,1,rand);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testZero() {
        DenseMatrix64F A = new DenseMatrix64F(6,6);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(0,alg.getSingularValues(),6),1e-5);

        checkComponents(alg,A);
    }

    public void testIdentity() {
        DenseMatrix64F A = CommonOps.identity(6,6);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(1,alg.getSingularValues(),6),1e-5);

        checkComponents(alg,A);
    }

    public void testLarger() {
        DenseMatrix64F A = RandomMatrices.createRandom(200,200,-1,1,rand);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testLots() {
        SingularValueDecomposition alg = createSvd();

        for( int i = 1; i < 10; i++ ) {
            for( int j = 1; j < 10; j++ ) {
                DenseMatrix64F A = RandomMatrices.createRandom(i,j,-1,1,rand);

                assertTrue(alg.decompose(A));

                checkComponents(alg,A);
            }
        }
    }

    private int checkOccurrence( double check , double[]values , int numSingular ) {
        int num = 0;

        for( int i = 0; i < numSingular; i++ ) {
            if( Math.abs(values[i]-check)<1e-8)
                num++;
        }

        return num;
    }

    private void checkComponents( SingularValueDecomposition svd , DenseMatrix64F expected )
    {
        SimpleMatrix U = SimpleMatrix.wrap(svd.getU());
        SimpleMatrix V = SimpleMatrix.wrap(svd.getV());
        SimpleMatrix W = SimpleMatrix.wrap(svd.getW(null));

        if( svd.isCompact() ) {
            assertEquals(W.numCols(),W.numRows());
            assertEquals(U.numCols(),W.numRows());
            assertEquals(V.numCols(),W.numCols());
        } else {
            assertEquals(U.numCols(),W.numRows());
            assertEquals(W.numCols(),V.numCols());
            assertEquals(U.numCols(),U.numRows());
            assertEquals(V.numCols(),V.numRows());
        }

        DenseMatrix64F found = U.mult(W).mult(V.transpose()).getMatrix();

//        found.print();
//        expected.print();

        assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
    }
}
