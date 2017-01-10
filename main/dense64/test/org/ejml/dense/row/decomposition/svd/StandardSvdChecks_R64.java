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

package org.ejml.dense.row.decomposition.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.SingularOps_R64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class StandardSvdChecks_R64 {

    Random rand = new Random(73675);

    public abstract SingularValueDecomposition_F64<DMatrixRow_F64> createSvd();

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
        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();

        assertFalse(alg.decompose(new DMatrixRow_F64(0, 0)));
        assertFalse(alg.decompose(new DMatrixRow_F64(0,2)));
        assertFalse(alg.decompose(new DMatrixRow_F64(2,0)));
    }

    public void testDecompositionOfTrivial()
    {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        SingularValueDecomposition_F64<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(3, SingularOps_R64.rank(alg, UtilEjml.EPS));
        assertEquals(0, SingularOps_R64.nullity(alg, UtilEjml.EPS));

        double []w = alg.getSingularValues();
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,9.59186,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,5.18005,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,4.55558,w);

        checkComponents(alg,A);
    }

    public void testWide() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,20,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testTall() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(21,5,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testZero() {

        for( int i = 1; i <= 16; i += 5 ) {
            for( int j = 1; j <= 16; j += 5 ) {
                DMatrixRow_F64 A = new DMatrixRow_F64(i,j);

                SingularValueDecomposition_F64<DMatrixRow_F64> alg = createSvd();
                assertTrue(alg.decompose(A));

                int min = Math.min(i,j);

                assertEquals(min,checkOccurrence(0,alg.getSingularValues(),min),UtilEjml.EPS);

                checkComponents(alg,A);
            }
        }
    }

    public void testIdentity() {
        DMatrixRow_F64 A = CommonOps_R64.identity(6,6);

        SingularValueDecomposition_F64<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(1,alg.getSingularValues(),6),UtilEjml.TEST_F64_SQ);

        checkComponents(alg,A);
    }

    public void testLarger() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(200,200,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    /**
     * See if it can handle very small values and not blow up.  This can some times
     * cause a zero to appear unexpectedly and thus a divided by zero.
     */
    public void testVerySmallValue() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,5,-1,1,rand);

        CommonOps_R64.scale( Math.pow(UtilEjml.EPS, 12) ,A);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }


    public void testLots() {
        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();

        for( int i = 1; i < 10; i++ ) {
            for( int j = 1; j < 10; j++ ) {
                DMatrixRow_F64 A = RandomMatrices_R64.createRandom(i,j,-1,1,rand);

                assertTrue(alg.decompose(A));

                checkComponents(alg,A);
            }
        }
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetU_Transpose() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5, 7, -1, 1, rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        DMatrixRow_F64 U = alg.getU(null,false);
        DMatrixRow_F64 Ut = alg.getU(null,true);

        DMatrixRow_F64 found = new DMatrixRow_F64(U.numCols,U.numRows);

        CommonOps_R64.transpose(U,found);

        assertTrue( MatrixFeatures_R64.isEquals(Ut,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetU_Storage() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DMatrixRow_F64 U = alg.getU(null,false);
        DMatrixRow_F64 storage = new DMatrixRow_F64(U.numRows,U.numCols);

        alg.getU(storage,false);

        assertTrue( MatrixFeatures_R64.isEquals(U,storage));

        U = alg.getU(null,true);
        storage = new DMatrixRow_F64(U.numRows,U.numCols);

        alg.getU(storage,true);
        assertTrue( MatrixFeatures_R64.isEquals(U,storage));

        // give it an incorrect sign
        try {
            alg.getU(new DMatrixRow_F64(10,20),true);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
        try {
            alg.getU(new DMatrixRow_F64(10,20),false);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetV_Transpose() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        DMatrixRow_F64 V = alg.getV(null,false);
        DMatrixRow_F64 Vt = alg.getV(null,true);

        DMatrixRow_F64 found = new DMatrixRow_F64(V.numCols,V.numRows);

        CommonOps_R64.transpose(V,found);

        assertTrue( MatrixFeatures_R64.isEquals(Vt,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetV_Storage() {
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DMatrixRow_F64 V = alg.getV(null, false);
        DMatrixRow_F64 storage = new DMatrixRow_F64(V.numRows,V.numCols);

        alg.getV(storage, false);

        assertTrue(MatrixFeatures_R64.isEquals(V, storage));

        V = alg.getV(null, true);
        storage = new DMatrixRow_F64(V.numRows,V.numCols);

        alg.getV(storage, true);
        assertTrue( MatrixFeatures_R64.isEquals(V,storage));

        // give it an incorrect sign
        try {
            alg.getV(new DMatrixRow_F64(10, 20), true);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
        try {
            alg.getV(new DMatrixRow_F64(10, 20), false);
            fail("Exception should have been thrown");
        } catch( RuntimeException e ){}
    }

    /**
     * Makes sure arrays are correctly set when it first computers a larger matrix
     * then a smaller one.  When going from small to large its often forces to declare
     * new memory, this way it actually uses memory.
     */
    public void testLargeToSmall() {
        SingularValueDecomposition<DMatrixRow_F64> alg = createSvd();

        // first the larger one
        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(10,10,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);

        // then the smaller one
        A = RandomMatrices_R64.createRandom(5,5,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);
    }

    private int checkOccurrence( double check , double[]values , int numSingular ) {
        int num = 0;

        for( int i = 0; i < numSingular; i++ ) {
            if( Math.abs(values[i]-check)<UtilEjml.TEST_F64)
                num++;
        }

        return num;
    }

    private void checkComponents(SingularValueDecomposition<DMatrixRow_F64> svd , DMatrixRow_F64 expected )
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

        DMatrixRow_F64 found = U.mult(W).mult(Vt).getMatrix();

//        found.print();
//        expected.print();

        assertTrue(MatrixFeatures_R64.isIdentical(expected,found,UtilEjml.TEST_F64));
    }
}
