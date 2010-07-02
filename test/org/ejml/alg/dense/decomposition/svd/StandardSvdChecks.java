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
import org.ejml.data.UtilTestMatrix;
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

    boolean omitVerySmallValues = false;

    public void allTests() {
        testDecompositionOfTrivial();
        testWide();
        testTall();
        checkGetU();
        checkGetV();
        if( !omitVerySmallValues )
            testVerySmallValue();
        testZero();
        testLargeToSmall();
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
        UtilTestMatrix.checkNumFound(1,1e-5,9.59186,w);
        UtilTestMatrix.checkNumFound(1,1e-5,5.18005,w);
        UtilTestMatrix.checkNumFound(1,1e-5,4.55558,w);

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

    /**
     * See if it can handle very small values and not blow up.  This can some times
     * cause a zero to appear unexpectedly and thus a divided by zero.
     */
    public void testVerySmallValue() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,5,-1,1,rand);

        CommonOps.scale(1e-200,A);

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

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetU() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        DenseMatrix64F U = alg.getU(false);
        DenseMatrix64F Ut = alg.getU(true);

        DenseMatrix64F found = new DenseMatrix64F(U.numCols,U.numRows);

        CommonOps.transpose(U,found);

        assertTrue( MatrixFeatures.isIdentical(Ut,found));
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetV() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition alg = createSvd();
        assertTrue(alg.decompose(A));

        DenseMatrix64F V = alg.getV(false);
        DenseMatrix64F Vt = alg.getV(true);

        DenseMatrix64F found = new DenseMatrix64F(V.numCols,V.numRows);

        CommonOps.transpose(V,found);

        assertTrue( MatrixFeatures.isIdentical(Vt,found));
    }

    /**
     * Makes sure arrays are correctly set when it first computers a larger matrix
     * then a smaller one.  When going from small to large its often forces to declare
     * new memory, this way it actually uses memory.
     */
    public void testLargeToSmall() {
        SingularValueDecomposition alg = createSvd();

        // first the larger one
        DenseMatrix64F A = RandomMatrices.createRandom(10,10,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);

        // then the smaller one
        A = RandomMatrices.createRandom(5,5,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);
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
        SimpleMatrix U = SimpleMatrix.wrap(svd.getU(false));
        SimpleMatrix Vt = SimpleMatrix.wrap(svd.getV(true));
        SimpleMatrix W = SimpleMatrix.wrap(svd.getW(null));

        assertTrue( !U.hasUncountable() );
        assertTrue( !Vt.hasUncountable() );
        assertTrue( !W.hasUncountable() );

        if( svd.isCompact() ) {
            assertEquals(W.numCols(),W.numRows());
            assertEquals(U.numCols(),W.numRows());
            assertEquals(Vt.numRows(),W.numCols());
        } else {
            assertEquals(U.numCols(),W.numRows());
            assertEquals(W.numCols(),Vt.numRows());
            assertEquals(U.numCols(),U.numRows());
            assertEquals(Vt.numCols(),Vt.numRows());
        }

        DenseMatrix64F found = U.mult(W).mult(Vt).getMatrix();

//        found.print();
//        expected.print();

        assertTrue(MatrixFeatures.isIdentical(expected,found,1e-8));
    }
}
