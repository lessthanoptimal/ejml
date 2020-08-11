/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.FMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.SingularOps_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class StandardSvdChecks_FDRM {

    Random rand = new Random(73675);

    public abstract SingularValueDecomposition_F32<FMatrixRMaj> createSvd();

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
        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();

        assertFalse(alg.decompose(new FMatrixRMaj(0, 0)));
        assertFalse(alg.decompose(new FMatrixRMaj(0,2)));
        assertFalse(alg.decompose(new FMatrixRMaj(2,0)));
    }

    public void testDecompositionOfTrivial()
    {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 5, 2, 3, 1.5f, -2, 8, -3, 4.7f, -0.5f);

        SingularValueDecomposition_F32<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(3, SingularOps_FDRM.rank(alg, UtilEjml.F_EPS));
        assertEquals(0, SingularOps_FDRM.nullity(alg, UtilEjml.F_EPS));

        float []w = alg.getSingularValues();
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F32_SQ,9.59186f,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F32_SQ,5.18005f,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F32_SQ,4.55558f,w);

        checkComponents(alg,A);
    }

    public void testWide() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,20,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testTall() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(21,5,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testZero() {

        for( int i = 1; i <= 16; i += 5 ) {
            for( int j = 1; j <= 16; j += 5 ) {
                FMatrixRMaj A = new FMatrixRMaj(i,j);

                SingularValueDecomposition_F32<FMatrixRMaj> alg = createSvd();
                assertTrue(alg.decompose(A));

                int min = Math.min(i,j);

                assertEquals(min,checkOccurrence(0,alg.getSingularValues(),min),UtilEjml.F_EPS);

                checkComponents(alg,A);
            }
        }
    }

    public void testIdentity() {
        FMatrixRMaj A = CommonOps_FDRM.identity(6,6);

        SingularValueDecomposition_F32<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(1,alg.getSingularValues(),6),UtilEjml.TEST_F32_SQ);

        checkComponents(alg,A);
    }

    public void testLarger() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(200,200,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    /**
     * See if it can handle very small values and not blow up.  This can some times
     * cause a zero to appear unexpectedly and thus a divided by zero.
     */
    public void testVerySmallValue() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,5,-1,1,rand);

        CommonOps_FDRM.scale( (float)Math.pow(UtilEjml.F_EPS, 12) ,A);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }


    public void testLots() {
        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();

        for( int i = 1; i < 10; i++ ) {
            for( int j = 1; j < 10; j++ ) {
                FMatrixRMaj A = RandomMatrices_FDRM.rectangle(i,j,-1,1,rand);

                assertTrue(alg.decompose(A));

                checkComponents(alg,A);
            }
        }
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetU_Transpose() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5, 7, -1, 1, rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        FMatrixRMaj U = alg.getU(null,false);
        FMatrixRMaj Ut = alg.getU(null,true);

        FMatrixRMaj found = new FMatrixRMaj(U.numCols,U.numRows);

        CommonOps_FDRM.transpose(U,found);

        assertTrue( MatrixFeatures_FDRM.isEquals(Ut,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetU_Storage() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        FMatrixRMaj U = alg.getU(null,false);

        FMatrixRMaj storage = alg.getU(new FMatrixRMaj(U.numRows,U.numCols),false);
        assertTrue( MatrixFeatures_FDRM.isEquals(U,storage));
        storage = alg.getU(new FMatrixRMaj(10,20),false);
        assertTrue( MatrixFeatures_FDRM.isEquals(U,storage));

        U = alg.getU(null,true);
        storage = alg.getU(new FMatrixRMaj(U.numRows,U.numCols),true);
        assertTrue( MatrixFeatures_FDRM.isEquals(U,storage));
        storage = alg.getU(new FMatrixRMaj(10,20),true);
        assertTrue( MatrixFeatures_FDRM.isEquals(U,storage));
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetV_Transpose() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        FMatrixRMaj V = alg.getV(null,false);
        FMatrixRMaj Vt = alg.getV(null,true);

        FMatrixRMaj found = new FMatrixRMaj(V.numCols,V.numRows);

        CommonOps_FDRM.transpose(V,found);

        assertTrue( MatrixFeatures_FDRM.isEquals(Vt,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetV_Storage() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        FMatrixRMaj V = alg.getV(null, false);
        FMatrixRMaj storage = alg.getV(new FMatrixRMaj(V.numRows,V.numCols), false);
        assertTrue(MatrixFeatures_FDRM.isEquals(V, storage));
        storage = alg.getV(new FMatrixRMaj(10,20), false);
        assertTrue(MatrixFeatures_FDRM.isEquals(V, storage));

        V = alg.getV(null, true);
        storage = alg.getV(new FMatrixRMaj(V.numRows,V.numCols), true);
        assertTrue(MatrixFeatures_FDRM.isEquals(V, storage));
        storage = alg.getV(new FMatrixRMaj(10,20), true);
        assertTrue(MatrixFeatures_FDRM.isEquals(V, storage));
    }

    /**
     * Makes sure arrays are correctly set when it first computers a larger matrix
     * then a smaller one.  When going from small to large its often forces to declare
     * new memory, this way it actually uses memory.
     */
    public void testLargeToSmall() {
        SingularValueDecomposition<FMatrixRMaj> alg = createSvd();

        // first the larger one
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(10,10,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);

        // then the smaller one
        A = RandomMatrices_FDRM.rectangle(5,5,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);
    }

    private int checkOccurrence( float check , float[]values , int numSingular ) {
        int num = 0;

        for( int i = 0; i < numSingular; i++ ) {
            if( Math.abs(values[i]-check)<UtilEjml.TEST_F32)
                num++;
        }

        return num;
    }

    private void checkComponents(SingularValueDecomposition<FMatrixRMaj> svd , FMatrixRMaj expected )
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

        FMatrixRMaj found = U.mult(W).mult(Vt).getMatrix();

//        found.print();
//        expected.print();

        assertTrue(MatrixFeatures_FDRM.isIdentical(expected,found,UtilEjml.TEST_F32));
    }
}
