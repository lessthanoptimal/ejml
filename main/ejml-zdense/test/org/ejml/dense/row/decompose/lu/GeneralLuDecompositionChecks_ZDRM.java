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

package org.ejml.dense.row.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_ZDRM {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<ZMatrixRMaj> create(int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(4, 4, -1, 1, rand);
        ZMatrixRMaj A_orig = A.copy();

        LUDecomposition<ZMatrixRMaj> alg = create(4,4);
        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_ZDRM.isEquals(A,A_orig);

        assertEquals(modified, alg.inputModified());
    }

    @Test
    public void testAllReal()
    {
        ZMatrixRMaj A = new ZMatrixRMaj(3,3, true, 5,0, 2,0, 3,0, 1.5,0, -2,0, 8,0, -3,0, 4.7,0, -0.5,0);

        LUDecomposition<ZMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A.copy()));

        assertFalse(alg.isSingular());

        ZMatrixRMaj L = alg.getLower(null);
        ZMatrixRMaj U = alg.getUpper(null);
        ZMatrixRMaj P = alg.getPivot(null);

        ZMatrixRMaj P_tran = new ZMatrixRMaj(P.numCols,P.numRows);
        ZMatrixRMaj PL = new ZMatrixRMaj(P.numRows,P.numCols);
        ZMatrixRMaj A_found = new ZMatrixRMaj(A.numRows,A.numCols);

        CommonOps_ZDRM.transpose(P,P_tran);
        CommonOps_ZDRM.mult(P_tran, L, PL);
        CommonOps_ZDRM.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A_found,A, UtilEjml.TEST_F64));
    }

    @Test
    public void testDecomposition_square_real()
    {
        for( int i = 2; i <= 20; i++ ) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(i,i,-1,1,rand);

            for (int j = 1; j < A.getDataLength(); j += 2) {
                A.data[j] = 0;
            }

            checkDecomposition(A);
        }
    }

    @Test
    public void testDecomposition_square_imaginary()
    {
        for( int i = 2; i <= 20; i++ ) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(i,i,-1,1,rand);

            for (int j = 0; j < A.getDataLength(); j += 2) {
                A.data[j] = 0;
            }

            checkDecomposition(A);
        }
    }

    @Test
    public void testDecomposition_square()
    {
        for( int i = 2; i <= 20; i++ ) {
            ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(i,i,-1,1,rand);

            checkDecomposition(A);
        }
    }

    @Test
    public void testFat() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(2,3,-1,1,rand);

        checkDecomposition(A);
    }

    @Test
    public void testTall() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(3,2,rand);

        checkDecomposition(A);
    }

    @Test
    public void zeroMatrix() {
        ZMatrixRMaj A = new ZMatrixRMaj(3,3);

        LUDecomposition<ZMatrixRMaj> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        ZMatrixRMaj L = alg.getLower(null);
        ZMatrixRMaj U = alg.getUpper(null);

        ZMatrixRMaj A_found = new ZMatrixRMaj(3,3);
        CommonOps_ZDRM.mult(L, U, A_found);

        assertFalse(MatrixFeatures_ZDRM.hasUncountable(A_found));
        assertTrue(MatrixFeatures_ZDRM.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test
    public void testSingular(){
        ZMatrixRMaj A = new ZMatrixRMaj(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6,6, 4,4, 4,4, 0,0);

        LUDecomposition<ZMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        ZMatrixRMaj A = new ZMatrixRMaj(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6.1,6.1, 4,4, 4,4, 0,0);

        LUDecomposition<ZMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test
    public void getLower_getUpper() {
        ZMatrixRMaj A = RandomMatrices_ZDRM.createRandom(3,3,rand);

        LUDecomposition<ZMatrixRMaj> alg = create(3,3);

        alg.decompose(A);

        ZMatrixRMaj L_provided = RandomMatrices_ZDRM.createRandom(3,3,rand);
        ZMatrixRMaj U_provided = RandomMatrices_ZDRM.createRandom(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        ZMatrixRMaj L_ret = alg.getLower(null);
        ZMatrixRMaj U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_ZDRM.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_ZDRM.isEquals(U_provided,U_ret));
    }

    private void checkDecomposition(ZMatrixRMaj a) {
        LUDecomposition<ZMatrixRMaj> alg = create(a.numRows, a.numCols);
        assertTrue(alg.decompose(a.copy()));

        if( a.numRows <= a.numCols)
            assertFalse(alg.isSingular());

        ZMatrixRMaj L = alg.getLower(null);
        ZMatrixRMaj U = alg.getUpper(null);
        ZMatrixRMaj P = alg.getPivot(null);

        ZMatrixRMaj P_tran  = new ZMatrixRMaj(P.numCols,P.numRows);
        ZMatrixRMaj PL      = new ZMatrixRMaj(P_tran.numRows,L.numCols);
        ZMatrixRMaj A_found = new ZMatrixRMaj(a.numRows, a.numCols);

        CommonOps_ZDRM.transpose(P, P_tran);
        CommonOps_ZDRM.mult(P_tran, L, PL);
        CommonOps_ZDRM.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_ZDRM.isIdentical(A_found, a, UtilEjml.TEST_F64));
    }
}
