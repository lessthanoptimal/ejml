/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_CD64 {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<CDenseMatrix64F> create( int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(4, 4, -1, 1, rand);
        CDenseMatrix64F A_orig = A.copy();

        LUDecomposition<CDenseMatrix64F> alg = create(4,4);
        assertTrue(alg.decompose(A));

        boolean modified = !CMatrixFeatures.isEquals(A,A_orig);

        assertEquals(modified, alg.inputModified());
    }

    @Test
    public void testAllReal()
    {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 5,0, 2,0, 3,0, 1.5,0, -2,0, 8,0, -3,0, 4.7,0, -0.5,0);

        LUDecomposition<CDenseMatrix64F> alg = create(3,3);
        assertTrue(alg.decompose(A.copy()));

        assertFalse(alg.isSingular());

        CDenseMatrix64F L = alg.getLower(null);
        CDenseMatrix64F U = alg.getUpper(null);
        CDenseMatrix64F P = alg.getPivot(null);

        CDenseMatrix64F P_tran = new CDenseMatrix64F(P.numCols,P.numRows);
        CDenseMatrix64F PL = new CDenseMatrix64F(P.numRows,P.numCols);
        CDenseMatrix64F A_found = new CDenseMatrix64F(A.numRows,A.numCols);

        CCommonOps.transpose(P,P_tran);
        CCommonOps.mult(P_tran, L, PL);
        CCommonOps.mult(PL, U, A_found);

        assertTrue(CMatrixFeatures.isIdentical(A_found,A,1e-8));
    }

    @Test
    public void testDecomposition_square_real()
    {
        for( int i = 2; i <= 20; i++ ) {
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,-1,1,rand);

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
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,-1,1,rand);

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
            CDenseMatrix64F A = CRandomMatrices.createRandom(i,i,-1,1,rand);

            checkDecomposition(A);
        }
    }

    @Test
    public void testFat() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(2,3,-1,1,rand);

        checkDecomposition(A);
    }

    @Test
    public void testTall() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(3,2,rand);

        checkDecomposition(A);
    }

    @Test
    public void zeroMatrix() {
        CDenseMatrix64F A = new CDenseMatrix64F(3,3);

        LUDecomposition<CDenseMatrix64F> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        CDenseMatrix64F L = alg.getLower(null);
        CDenseMatrix64F U = alg.getUpper(null);

        CDenseMatrix64F A_found = new CDenseMatrix64F(3,3);
        CCommonOps.mult(L, U, A_found);

        assertFalse(CMatrixFeatures.hasUncountable(A_found));
        assertTrue(CMatrixFeatures.isIdentical(A_found,A,1e-8));
    }

    @Test
    public void testSingular(){
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6,6, 4,4, 4,4, 0,0);

        LUDecomposition<CDenseMatrix64F> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        CDenseMatrix64F A = new CDenseMatrix64F(3,3, true, 1,1, 2,2, 3,3, 2,2, 4,4, 6.1,6.1, 4,4, 4,4, 0,0);

        LUDecomposition<CDenseMatrix64F> alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertFalse(alg.isSingular());
    }

    /**
     * Checks to see how it handles getLower getUpper functions with and without
     * a matrix being provided.
     */
    @Test
    public void getLower_getUpper() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(3,3,rand);

        LUDecomposition<CDenseMatrix64F> alg = create(3,3);

        alg.decompose(A);

        CDenseMatrix64F L_provided = CRandomMatrices.createRandom(3,3,rand);
        CDenseMatrix64F U_provided = CRandomMatrices.createRandom(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        CDenseMatrix64F L_ret = alg.getLower(null);
        CDenseMatrix64F U_ret = alg.getUpper(null);

        assertTrue(CMatrixFeatures.isEquals(L_provided,L_ret));
        assertTrue(CMatrixFeatures.isEquals(U_provided,U_ret));
    }

    private void checkDecomposition(CDenseMatrix64F a) {
        LUDecomposition<CDenseMatrix64F> alg = create(a.numRows, a.numCols);
        assertTrue(alg.decompose(a.copy()));

        if( a.numRows <= a.numCols)
            assertFalse(alg.isSingular());

        CDenseMatrix64F L = alg.getLower(null);
        CDenseMatrix64F U = alg.getUpper(null);
        CDenseMatrix64F P = alg.getPivot(null);

        CDenseMatrix64F P_tran  = new CDenseMatrix64F(P.numCols,P.numRows);
        CDenseMatrix64F PL      = new CDenseMatrix64F(P_tran.numRows,L.numCols);
        CDenseMatrix64F A_found = new CDenseMatrix64F(a.numRows, a.numCols);

        CCommonOps.transpose(P, P_tran);
        CCommonOps.mult(P_tran, L, PL);
        CCommonOps.mult(PL, U, A_found);

        assertTrue(CMatrixFeatures.isIdentical(A_found, a, 1e-8));
    }
}
