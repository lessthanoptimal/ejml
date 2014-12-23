/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.ops.SingularOps;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class StandardSvdChecks {

    Random rand = new Random(73675);

    public abstract SingularValueDecomposition<DenseMatrix64F> createSvd();

    boolean omitVerySmallValues = false;

    public void allTests() {
        testSizeZero();
        testDecompositionOfTrivial();
        testWide();
        testTall();
        checkGetU_Transpose();
        checkGetU_Storage();
        checkGetV_Transpose();
        checkGetV_Storage();

        if( !omitVerySmallValues )
            testVerySmallValue();
        testZero();
        testLargeToSmall();
        testIdentity();
        testLarger();
        testLots();
    }

    public void testSizeZero() {
        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();

        assertFalse(alg.decompose(new DenseMatrix64F(0, 0)));
        assertFalse(alg.decompose(new DenseMatrix64F(0,2)));
        assertFalse(alg.decompose(new DenseMatrix64F(2,0)));
    }

    public void testDecompositionOfTrivial()
    {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
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

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testTall() {
        DenseMatrix64F A = RandomMatrices.createRandom(21,5,-1,1,rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testZero() {

        for( int i = 1; i <= 16; i += 5 ) {
            for( int j = 1; j <= 16; j += 5 ) {
                DenseMatrix64F A = new DenseMatrix64F(i,j);

                SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
                assertTrue(alg.decompose(A));

                int min = Math.min(i,j);

                assertEquals(min,checkOccurrence(0,alg.getSingularValues(),min),UtilEjml.EPS);

                checkComponents(alg,A);
            }
        }
    }

    public void testIdentity() {
        DenseMatrix64F A = CommonOps.identity(6,6);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(1,alg.getSingularValues(),6),1e-5);

        checkComponents(alg,A);
    }

    public void testLarger() {
        DenseMatrix64F A = RandomMatrices.createRandom(200,200,-1,1,rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
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

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }


    public void testLots() {
        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();

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
    public void checkGetU_Transpose() {
        DenseMatrix64F A = RandomMatrices.createRandom(5, 7, -1, 1, rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        DenseMatrix64F U = alg.getU(null,false);
        DenseMatrix64F Ut = alg.getU(null,true);

        DenseMatrix64F found = new DenseMatrix64F(U.numCols,U.numRows);

        CommonOps.transpose(U,found);

        assertTrue( MatrixFeatures.isEquals(Ut,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetU_Storage() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DenseMatrix64F U = alg.getU(null,false);
        DenseMatrix64F storage = new DenseMatrix64F(U.numRows,U.numCols);

        alg.getU(storage,false);

        assertTrue( MatrixFeatures.isEquals(U,storage));

        U = alg.getU(null,true);
        storage = new DenseMatrix64F(U.numRows,U.numCols);

        alg.getU(storage,true);
        assertTrue( MatrixFeatures.isEquals(U,storage));

        // give it an incorrect sign
        try {
            alg.getU(new DenseMatrix64F(10,20),true);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
        try {
            alg.getU(new DenseMatrix64F(10,20),false);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetV_Transpose() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        DenseMatrix64F V = alg.getV(null,false);
        DenseMatrix64F Vt = alg.getV(null,true);

        DenseMatrix64F found = new DenseMatrix64F(V.numCols,V.numRows);

        CommonOps.transpose(V,found);

        assertTrue( MatrixFeatures.isEquals(Vt,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetV_Storage() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DenseMatrix64F V = alg.getV(null, false);
        DenseMatrix64F storage = new DenseMatrix64F(V.numRows,V.numCols);

        alg.getV(storage, false);

        assertTrue(MatrixFeatures.isEquals(V, storage));

        V = alg.getV(null, true);
        storage = new DenseMatrix64F(V.numRows,V.numCols);

        alg.getV(storage, true);
        assertTrue( MatrixFeatures.isEquals(V,storage));

        // give it an incorrect sign
        try {
            alg.getV(new DenseMatrix64F(10, 20), true);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
        try {
            alg.getV(new DenseMatrix64F(10, 20), false);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
    }

    /**
     * Makes sure arrays are correctly set when it first computers a larger matrix
     * then a smaller one.  When going from small to large its often forces to declare
     * new memory, this way it actually uses memory.
     */
    public void testLargeToSmall() {
        SingularValueDecomposition<DenseMatrix64F> alg = createSvd();

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

    private void checkComponents( SingularValueDecomposition<DenseMatrix64F> svd , DenseMatrix64F expected )
    {
        SimpleMatrix U = SimpleMatrix.wrap(svd.getU(null,false));
        SimpleMatrix Vt = SimpleMatrix.wrap(svd.getV(null,true));
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
