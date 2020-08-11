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

package org.ejml.dense.row.decomposition.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_FDRM {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<FMatrixRMaj> create(int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_FDRM.checkModifiedInput(create(0,0));
    }

    /**
     * Uses the decomposition returned from octave, which uses LAPACK
     */
    @Test
    public void testDecomposition()
    {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 5, 2, 3, 1.5f, -2, 8, -3, 4.7f, -0.5f);

        FMatrixRMaj octLower = new FMatrixRMaj(3,3, true, 1, 0, 0, -0.6f, 1, 0, 0.3f, -0.44068f, 1);
        FMatrixRMaj octUpper = new FMatrixRMaj(3,3, true, 5, 2, 3, 0, 5.9f, 1.3f, 0, 0, 7.67288f);

        LUDecomposition<FMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));

        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        EjmlUnitTests.assertEquals(octLower,L.getFDRM(),UtilEjml.TEST_F32_SQ);
        EjmlUnitTests.assertEquals(octUpper,U.getFDRM(),UtilEjml.TEST_F32_SQ);

        FMatrixRMaj A_found = P.mult(L).mult(U).getMatrix();
        assertTrue(MatrixFeatures_FDRM.isIdentical(A_found,A,UtilEjml.TEST_F32));
    }

    @Test
    public void testDecomposition2()
    {
        for( int i = 2; i <= 20; i++ ) {
            FMatrixRMaj A = RandomMatrices_FDRM.rectangle(i,i,-1,1,rand);

            LUDecomposition<FMatrixRMaj> alg = create(i,i);
            assertTrue(alg.decompose(A));

            assertFalse(alg.isSingular());

            SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
            SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
            SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

            FMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();
            assertTrue(MatrixFeatures_FDRM.isIdentical(A_found,A,UtilEjml.TEST_F32));
        }
    }

    @Test
    public void zeroMatrix() {
        FMatrixRMaj A = new FMatrixRMaj(3,3);

        LUDecomposition<FMatrixRMaj> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        FMatrixRMaj L = alg.getLower(null);
        FMatrixRMaj U = alg.getUpper(null);

        FMatrixRMaj A_found = new FMatrixRMaj(3,3);
        CommonOps_FDRM.mult(L,U,A_found);

        assertFalse(MatrixFeatures_FDRM.hasUncountable(A_found));
        assertTrue(MatrixFeatures_FDRM.isIdentical(A_found,A, UtilEjml.TEST_F32));
    }

    @Test
    public void testSingular(){
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6.1f, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test
    public void getLower_getUpper() {
        FMatrixRMaj A = new FMatrixRMaj(3,3, true, 5, 2, 3, 1.5f, -2, 8, -3, 4.7f, -0.5f);

        LUDecomposition<FMatrixRMaj> alg = create(3,3);

        alg.decompose(A);

        FMatrixRMaj L_provided = RandomMatrices_FDRM.rectangle(3,3,rand);
        FMatrixRMaj U_provided = RandomMatrices_FDRM.rectangle(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        FMatrixRMaj L_ret = alg.getLower(null);
        FMatrixRMaj U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_FDRM.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_FDRM.isEquals(U_provided,U_ret));
    }

    @Test
    public void testFat() {
        FMatrixRMaj A = new FMatrixRMaj(2,3, true, 1, 2, 3, 2, 4, 6.1f);

        LUDecomposition<FMatrixRMaj> alg = create(2,3);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        FMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_FDRM.isIdentical(A_found,A,UtilEjml.TEST_F32));
    }

    @Test
    public void testTall() {
        FMatrixRMaj A = new FMatrixRMaj(3,2, true, 1, 2, 3, 2, 4, 6.1f);

        LUDecomposition<FMatrixRMaj> alg = create(3,2);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        FMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_FDRM.isIdentical(A_found,A,UtilEjml.TEST_F32));
    }

    @Test
    public void testRowPivotVector() {
        FMatrixRMaj A = RandomMatrices_FDRM.rectangle(4,5,rand);
        LUDecomposition<FMatrixRMaj> alg = create(A.numRows,A.numCols);

        assertTrue(alg.decompose(A));

        int []pivot = alg.getRowPivotV(null);
        FMatrixRMaj P = alg.getRowPivot(null);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(1,(int)P.get(i,pivot[i]));
        }
    }
}
