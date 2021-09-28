/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.UtilTestMatrix;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.SingularOps_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.ejml.simple.SimpleMatrix;

import static org.junit.jupiter.api.Assertions.*;

public abstract class StandardSvdChecks_DDRM extends EjmlStandardJUnit {
    public abstract SingularValueDecomposition_F64<DMatrixRMaj> createSvd();

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
        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();

        assertFalse(alg.decompose(new DMatrixRMaj(0, 0)));
        assertFalse(alg.decompose(new DMatrixRMaj(0,2)));
        assertFalse(alg.decompose(new DMatrixRMaj(2,0)));
    }

    public void testDecompositionOfTrivial()
    {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        SingularValueDecomposition_F64<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(3, SingularOps_DDRM.rank(alg, UtilEjml.EPS));
        assertEquals(0, SingularOps_DDRM.nullity(alg, UtilEjml.EPS));

        double []w = alg.getSingularValues();
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,9.59186,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,5.18005,w);
        UtilTestMatrix.checkNumFound(1,UtilEjml.TEST_F64_SQ,4.55558,w);

        checkComponents(alg,A);
    }

    public void testWide() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,20,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testTall() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(21,5,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    public void testZero() {

        for( int i = 1; i <= 16; i += 5 ) {
            for( int j = 1; j <= 16; j += 5 ) {
                DMatrixRMaj A = new DMatrixRMaj(i,j);

                SingularValueDecomposition_F64<DMatrixRMaj> alg = createSvd();
                assertTrue(alg.decompose(A));

                int min = Math.min(i,j);

                assertEquals(min,checkOccurrence(0,alg.getSingularValues(),min),UtilEjml.EPS);

                checkComponents(alg,A);
            }
        }
    }

    public void testIdentity() {
        DMatrixRMaj A = CommonOps_DDRM.identity(6,6);

        SingularValueDecomposition_F64<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        assertEquals(6,checkOccurrence(1,alg.getSingularValues(),6),UtilEjml.TEST_F64_SQ);

        checkComponents(alg,A);
    }

    public void testLarger() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(200,200,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }

    /**
     * See if it can handle very small values and not blow up.  This can some times
     * cause a zero to appear unexpectedly and thus a divided by zero.
     */
    public void testVerySmallValue() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,5,-1,1,rand);

        CommonOps_DDRM.scale( Math.pow(UtilEjml.EPS, 12) ,A);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        checkComponents(alg,A);
    }


    public void testLots() {
        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();

        for( int i = 1; i < 10; i++ ) {
            for( int j = 1; j < 10; j++ ) {
                DMatrixRMaj A = RandomMatrices_DDRM.rectangle(i,j,-1,1,rand);

                assertTrue(alg.decompose(A));

                checkComponents(alg,A);
            }
        }
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetU_Transpose() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5, 7, -1, 1, rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        DMatrixRMaj U = alg.getU(null,false);
        DMatrixRMaj Ut = alg.getU(null,true);

        DMatrixRMaj found = new DMatrixRMaj(U.numCols,U.numRows);

        CommonOps_DDRM.transpose(U,found);

        assertTrue( MatrixFeatures_DDRM.isEquals(Ut,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetU_Storage() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DMatrixRMaj U = alg.getU(null,false);

        DMatrixRMaj storage = alg.getU(new DMatrixRMaj(U.numRows,U.numCols),false);
        assertTrue( MatrixFeatures_DDRM.isEquals(U,storage));
        storage = alg.getU(new DMatrixRMaj(10,20),false);
        assertTrue( MatrixFeatures_DDRM.isEquals(U,storage));

        U = alg.getU(null,true);
        storage = alg.getU(new DMatrixRMaj(U.numRows,U.numCols),true);
        assertTrue( MatrixFeatures_DDRM.isEquals(U,storage));
        storage = alg.getU(new DMatrixRMaj(10,20),true);
        assertTrue( MatrixFeatures_DDRM.isEquals(U,storage));
    }

    /**
     * Makes sure transposed flag is correctly handled.
     */
    public void checkGetV_Transpose() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        DMatrixRMaj V = alg.getV(null,false);
        DMatrixRMaj Vt = alg.getV(null,true);

        DMatrixRMaj found = new DMatrixRMaj(V.numCols,V.numRows);

        CommonOps_DDRM.transpose(V,found);

        assertTrue( MatrixFeatures_DDRM.isEquals(Vt,found));
    }

    /**
     * Makes sure the optional storage parameter is handled correctly
     */
    public void checkGetV_Storage() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(5,7,-1,1,rand);

        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();
        assertTrue(alg.decompose(A));

        // test positive cases
        DMatrixRMaj V = alg.getV(null, false);
        DMatrixRMaj storage = alg.getV(new DMatrixRMaj(V.numRows,V.numCols), false);
        assertTrue(MatrixFeatures_DDRM.isEquals(V, storage));
        storage = alg.getV(new DMatrixRMaj(10,20), false);
        assertTrue(MatrixFeatures_DDRM.isEquals(V, storage));

        V = alg.getV(null, true);
        storage = alg.getV(new DMatrixRMaj(V.numRows,V.numCols), true);
        assertTrue(MatrixFeatures_DDRM.isEquals(V, storage));
        storage = alg.getV(new DMatrixRMaj(10,20), true);
        assertTrue(MatrixFeatures_DDRM.isEquals(V, storage));
    }

    /**
     * Makes sure arrays are correctly set when it first computers a larger matrix
     * then a smaller one.  When going from small to large its often forces to declare
     * new memory, this way it actually uses memory.
     */
    public void testLargeToSmall() {
        SingularValueDecomposition<DMatrixRMaj> alg = createSvd();

        // first the larger one
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(10,10,-1,1,rand);
        assertTrue(alg.decompose(A));
        checkComponents(alg,A);

        // then the smaller one
        A = RandomMatrices_DDRM.rectangle(5,5,-1,1,rand);
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

    private void checkComponents(SingularValueDecomposition<DMatrixRMaj> svd , DMatrixRMaj expected )
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

        DMatrixRMaj found = U.mult(W).mult(Vt).getMatrix();

//        found.print();
//        expected.print();

        assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
    }
}
