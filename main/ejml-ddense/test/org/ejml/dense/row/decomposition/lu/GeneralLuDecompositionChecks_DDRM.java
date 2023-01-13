/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_DDRM extends EjmlStandardJUnit {
    public abstract LUDecomposition<DMatrixRMaj> create(int numRows , int numCols );

    @Test void testModifiedInput() {
        CheckDecompositionInterface_DDRM.checkModifiedInput(create(0,0));
    }

    /**
     * Uses the decomposition returned from octave, which uses LAPACK
     */
    @Test void testDecomposition()
    {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        DMatrixRMaj octLower = new DMatrixRMaj(3,3, true, 1, 0, 0, -0.6, 1, 0, 0.3, -0.44068, 1);
        DMatrixRMaj octUpper = new DMatrixRMaj(3,3, true, 5, 2, 3, 0, 5.9, 1.3, 0, 0, 7.67288);

        LUDecomposition<DMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));

        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        EjmlUnitTests.assertEquals(octLower,L.getDDRM(),UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(octUpper,U.getDDRM(),UtilEjml.TEST_F64_SQ);

        DMatrixRMaj A_found = P.mult(L).mult(U).getMatrix();
        assertTrue(MatrixFeatures_DDRM.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test void testDecomposition2()
    {
        for( int i = 2; i <= 20; i++ ) {
            DMatrixRMaj A = RandomMatrices_DDRM.rectangle(i,i,-1,1,rand);

            LUDecomposition<DMatrixRMaj> alg = create(i,i);
            assertTrue(alg.decompose(A));

            assertFalse(alg.isSingular());

            SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
            SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
            SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

            DMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();
            assertTrue(MatrixFeatures_DDRM.isIdentical(A_found,A,UtilEjml.TEST_F64));
        }
    }

    @Test void zeroMatrix() {
        DMatrixRMaj A = new DMatrixRMaj(3,3);

        LUDecomposition<DMatrixRMaj> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        DMatrixRMaj L = alg.getLower(null);
        DMatrixRMaj U = alg.getUpper(null);

        DMatrixRMaj A_found = new DMatrixRMaj(3,3);
        CommonOps_DDRM.mult(L,U,A_found);

        assertFalse(MatrixFeatures_DDRM.hasUncountable(A_found));
        assertTrue(MatrixFeatures_DDRM.isIdentical(A_found,A, UtilEjml.TEST_F64));
    }

    @Test void testSingular(){
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test void testNearlySingular(){
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 1, 2, 3, 2, 4, 6.1, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test void getLower_getUpper() {
        DMatrixRMaj A = new DMatrixRMaj(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        LUDecomposition<DMatrixRMaj> alg = create(3,3);

        alg.decompose(A);

        DMatrixRMaj L_provided = RandomMatrices_DDRM.rectangle(3,3,rand);
        DMatrixRMaj U_provided = RandomMatrices_DDRM.rectangle(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        DMatrixRMaj L_ret = alg.getLower(null);
        DMatrixRMaj U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_DDRM.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_DDRM.isEquals(U_provided,U_ret));
    }

    @Test void testFat() {
        DMatrixRMaj A = new DMatrixRMaj(2,3, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DMatrixRMaj> alg = create(2,3);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        DMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_DDRM.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test void testTall() {
        DMatrixRMaj A = new DMatrixRMaj(3,2, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DMatrixRMaj> alg = create(3,2);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getRowPivot(null));

        DMatrixRMaj A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_DDRM.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test void testRowPivotVector() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,5,rand);
        LUDecomposition<DMatrixRMaj> alg = create(A.numRows,A.numCols);

        assertTrue(alg.decompose(A));

        int []pivot = alg.getRowPivotV(null);
        DMatrixRMaj P = alg.getRowPivot(null);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(1,(int)P.get(i,pivot[i]));
        }
    }
}
