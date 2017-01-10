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

package org.ejml.dense.row.decomposition.lu;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.MatrixFeatures_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_R64;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks_R64 {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<DMatrixRow_F64> create(int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_R64.checkModifiedInput(create(0,0));
    }

    /**
     * Uses the decomposition returned from octave, which uses LAPACK
     */
    @Test
    public void testDecomposition()
    {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        DMatrixRow_F64 octLower = new DMatrixRow_F64(3,3, true, 1, 0, 0, -0.6, 1, 0, 0.3, -0.44068, 1);
        DMatrixRow_F64 octUpper = new DMatrixRow_F64(3,3, true, 5, 2, 3, 0, 5.9, 1.3, 0, 0, 7.67288);

        LUDecomposition<DMatrixRow_F64> alg = create(3,3);
        assertTrue(alg.decompose(A));

        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        EjmlUnitTests.assertEquals(octLower,L.matrix_F64(),UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(octUpper,U.matrix_F64(),UtilEjml.TEST_F64_SQ);

        DMatrixRow_F64 A_found = P.mult(L).mult(U).getMatrix();
        assertTrue(MatrixFeatures_R64.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test
    public void testDecomposition2()
    {
        for( int i = 2; i <= 20; i++ ) {
            DMatrixRow_F64 A = RandomMatrices_R64.createRandom(i,i,-1,1,rand);

            LUDecomposition<DMatrixRow_F64> alg = create(i,i);
            assertTrue(alg.decompose(A));

            assertFalse(alg.isSingular());

            SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
            SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
            SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

            DMatrixRow_F64 A_found = P.transpose().mult(L).mult(U).getMatrix();
            assertTrue(MatrixFeatures_R64.isIdentical(A_found,A,UtilEjml.TEST_F64));
        }
    }

    @Test
    public void zeroMatrix() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3);

        LUDecomposition<DMatrixRow_F64> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        DMatrixRow_F64 L = alg.getLower(null);
        DMatrixRow_F64 U = alg.getUpper(null);

        DMatrixRow_F64 A_found = new DMatrixRow_F64(3,3);
        CommonOps_R64.mult(L,U,A_found);

        assertFalse(MatrixFeatures_R64.hasUncountable(A_found));
        assertTrue(MatrixFeatures_R64.isIdentical(A_found,A, UtilEjml.TEST_F64));
    }

    @Test
    public void testSingular(){
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 3, 2, 4, 6, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 1, 2, 3, 2, 4, 6.1, 4, 4, 0);

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
        DMatrixRow_F64 A = new DMatrixRow_F64(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        LUDecomposition<DMatrixRow_F64> alg = create(3,3);

        alg.decompose(A);

        DMatrixRow_F64 L_provided = RandomMatrices_R64.createRandom(3,3,rand);
        DMatrixRow_F64 U_provided = RandomMatrices_R64.createRandom(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        DMatrixRow_F64 L_ret = alg.getLower(null);
        DMatrixRow_F64 U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures_R64.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures_R64.isEquals(U_provided,U_ret));
    }

    @Test
    public void testFat() {
        DMatrixRow_F64 A = new DMatrixRow_F64(2,3, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DMatrixRow_F64> alg = create(2,3);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        DMatrixRow_F64 A_found = P.mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_R64.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }

    @Test
    public void testTall() {
        DMatrixRow_F64 A = new DMatrixRow_F64(3,2, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DMatrixRow_F64> alg = create(3,2);

        assertTrue(alg.decompose(A));
//        assertFalse(dense.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        DMatrixRow_F64 A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures_R64.isIdentical(A_found,A,UtilEjml.TEST_F64));
    }
}
