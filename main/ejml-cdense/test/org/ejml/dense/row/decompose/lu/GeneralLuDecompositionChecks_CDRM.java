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

package org.ejml.dense.row.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_CDRM {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<CMatrixRMaj> create(int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4, 4, -1, 1, rand);
        CMatrixRMaj A_orig = A.copy();

        LUDecomposition<CMatrixRMaj> alg = create(4,4);
        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_CDRM.isEquals(A,A_orig);

        assertEquals(modified, alg.inputModified());
    }

    @Test
    public void testAllReal()
    {
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 5,0, 2,0, 3,0, 1.5f,0, -2,0, 8,0, -3,0, 4.7f,0, -0.5f,0);

        LUDecomposition<CMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A.copy()));

        assertFalse(alg.isSingular());

        CMatrixRMaj L = alg.getLower(null);
        CMatrixRMaj U = alg.getUpper(null);
        CMatrixRMaj P = alg.getRowPivot((CMatrixRMaj)null);

        CMatrixRMaj P_tran = new CMatrixRMaj(P.numCols,P.numRows);
        CMatrixRMaj PL = new CMatrixRMaj(P.numRows,P.numCols);
        CMatrixRMaj A_found = new CMatrixRMaj(A.numRows,A.numCols);

        CommonOps_CDRM.transpose(P,P_tran);
        CommonOps_CDRM.mult(P_tran, L, PL);
        CommonOps_CDRM.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_CDRM.isIdentical(A_found,A, UtilEjml.TEST_F32));
    }

    @Test
    public void testDecomposition_square_real()
    {
        for( int i = 2; i <= 20; i++ ) {
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,-1,1,rand);

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
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,-1,1,rand);

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
            CMatrixRMaj A = RandomMatrices_CDRM.rectangle(i,i,-1,1,rand);

            checkDecomposition(A);
        }
    }

    @Test
    public void testFat() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(2,3,-1,1,rand);

        checkDecomposition(A);
    }

    @Test
    public void testTall() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(3,2,rand);

        checkDecomposition(A);
    }

    @Test
    public void zeroMatrix() {
        CMatrixRMaj A = new CMatrixRMaj(3,3);

        LUDecomposition<CMatrixRMaj> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        CMatrixRMaj L = alg.getLower(null);
        CMatrixRMaj U = alg.getUpper(null);

        CMatrixRMaj A_found = new CMatrixRMaj(3,3);
        CommonOps_CDRM.mult(L, U, A_found);

        assertFalse(MatrixFeatures_CDRM.hasUncountable(A_found));
        assertTrue(MatrixFeatures_CDRM.isIdentical(A_found,A,UtilEjml.TEST_F32));
    }

    @Test
    public void testSingular(){
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6,6, 4,4, 4,4, 0,0);

        LUDecomposition<CMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        CMatrixRMaj A = new CMatrixRMaj(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6.1f,6.1f, 4,4, 4,4, 0,0);

        LUDecomposition<CMatrixRMaj> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test
    public void getLower_getUpper() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(3,3,rand);

        LUDecomposition<CMatrixRMaj> alg = create(3,3);

        alg.decompose(A);

        CMatrixRMaj L_provided = RandomMatrices_CDRM.rectangle(3,3,rand);
        CMatrixRMaj U_provided = RandomMatrices_CDRM.rectangle(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        CMatrixRMaj L_ret = alg.getLower(null);
        CMatrixRMaj U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_CDRM.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_CDRM.isEquals(U_provided,U_ret));
    }

    private void checkDecomposition(CMatrixRMaj a) {
        LUDecomposition<CMatrixRMaj> alg = create(a.numRows, a.numCols);
        assertTrue(alg.decompose(a.copy()));

        if( a.numRows <= a.numCols)
            assertFalse(alg.isSingular());

        CMatrixRMaj L = alg.getLower(null);
        CMatrixRMaj U = alg.getUpper(null);
        CMatrixRMaj P = alg.getRowPivot(null);

        CMatrixRMaj P_tran  = new CMatrixRMaj(P.numCols,P.numRows);
        CMatrixRMaj PL      = new CMatrixRMaj(P_tran.numRows,L.numCols);
        CMatrixRMaj A_found = new CMatrixRMaj(a.numRows, a.numCols);

        CommonOps_CDRM.transpose(P, P_tran);
        CommonOps_CDRM.mult(P_tran, L, PL);
        CommonOps_CDRM.mult(PL, U, A_found);

        assertTrue(MatrixFeatures_CDRM.isIdentical(A_found, a, UtilEjml.TEST_F32));
    }

    @Test
    public void testRowPivotVector() {
        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(4,5,rand);
        LUDecomposition<CMatrixRMaj> alg = create(A.numRows,A.numCols);

        assertTrue(alg.decompose(A));

        int []pivot = alg.getRowPivotV(null);
        CMatrixRMaj P = alg.getRowPivot(null);

        for (int i = 0; i < A.numRows; i++) {
            assertEquals(1,(int)P.getReal(i,pivot[i]));
        }
    }
}
