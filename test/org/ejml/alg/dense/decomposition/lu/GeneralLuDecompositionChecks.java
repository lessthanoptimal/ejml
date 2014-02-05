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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.alg.dense.decomposition.CheckDecompositionInterface;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public abstract class GeneralLuDecompositionChecks {

    Random rand = new Random(0xff);

    public abstract LUDecomposition<DenseMatrix64F> create( int numRows , int numCols );

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface.checkModifiedInput(create(0,0));
    }

    /**
     * Uses the decomposition returned from octave, which uses LAPACK
     */
    @Test
    public void testDecomposition()
    {
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        DenseMatrix64F octLower = new DenseMatrix64F(3,3, true, 1, 0, 0, -0.6, 1, 0, 0.3, -0.44068, 1);
        DenseMatrix64F octUpper = new DenseMatrix64F(3,3, true, 5, 2, 3, 0, 5.9, 1.3, 0, 0, 7.67288);

        LUDecomposition<DenseMatrix64F> alg = create(3,3);
        assertTrue(alg.decompose(A));

        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        EjmlUnitTests.assertEquals(octLower,L.getMatrix(),1e-5);
        EjmlUnitTests.assertEquals(octUpper,U.getMatrix(),1e-5);

        DenseMatrix64F A_found = P.mult(L).mult(U).getMatrix();
        assertTrue(MatrixFeatures.isIdentical(A_found,A,1e-8));
    }

    @Test
    public void testDecomposition2()
    {
        for( int i = 2; i <= 20; i++ ) {
            DenseMatrix64F A = RandomMatrices.createRandom(i,i,-1,1,rand);

            LUDecomposition<DenseMatrix64F> alg = create(i,i);
            assertTrue(alg.decompose(A));

            assertFalse(alg.isSingular());

            SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
            SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
            SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

            DenseMatrix64F A_found = P.transpose().mult(L).mult(U).getMatrix();
            assertTrue(MatrixFeatures.isIdentical(A_found,A,1e-8));
        }
    }

    @Test
    public void zeroMatrix() {
        DenseMatrix64F A = new DenseMatrix64F(3,3);

        LUDecomposition<DenseMatrix64F> alg = create(3,3);

        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());

        DenseMatrix64F L = alg.getLower(null);
        DenseMatrix64F U = alg.getUpper(null);

        DenseMatrix64F A_found = new DenseMatrix64F(3,3);
        CommonOps.mult(L,U,A_found);

        assertFalse(MatrixFeatures.hasUncountable(A_found));
        assertTrue(MatrixFeatures.isIdentical(A_found,A,1e-8));
    }

    @Test
    public void testSingular(){
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 3, 2, 4, 6, 4, 4, 0);

        LUDecomposition alg = create(3,3);
        assertTrue(alg.decompose(A));
        assertTrue(alg.isSingular());
    }

    @Test
    public void testNearlySingular(){
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 1, 2, 3, 2, 4, 6.1, 4, 4, 0);

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
        DenseMatrix64F A = new DenseMatrix64F(3,3, true, 5, 2, 3, 1.5, -2, 8, -3, 4.7, -0.5);

        LUDecomposition<DenseMatrix64F> alg = create(3,3);

        alg.decompose(A);

        DenseMatrix64F L_provided = RandomMatrices.createRandom(3,3,rand);
        DenseMatrix64F U_provided = RandomMatrices.createRandom(3,3,rand);

        assertTrue(L_provided == alg.getLower(L_provided));
        assertTrue(U_provided == alg.getUpper(U_provided));

        DenseMatrix64F L_ret = alg.getLower(null);
        DenseMatrix64F U_ret = alg.getUpper(null);

        assertTrue(MatrixFeatures.isEquals(L_provided,L_ret));
        assertTrue(MatrixFeatures.isEquals(U_provided,U_ret));
    }

    @Test
    public void testFat() {
        DenseMatrix64F A = new DenseMatrix64F(2,3, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DenseMatrix64F> alg = create(2,3);

        assertTrue(alg.decompose(A));
//        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        DenseMatrix64F A_found = P.mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures.isIdentical(A_found,A,1e-8));
    }

    @Test
    public void testTall() {
        DenseMatrix64F A = new DenseMatrix64F(3,2, true, 1, 2, 3, 2, 4, 6.1);

        LUDecomposition<DenseMatrix64F> alg = create(3,2);

        assertTrue(alg.decompose(A));
//        assertFalse(alg.isSingular());

        SimpleMatrix L = SimpleMatrix.wrap(alg.getLower(null));
        SimpleMatrix U = SimpleMatrix.wrap(alg.getUpper(null));
        SimpleMatrix P = SimpleMatrix.wrap(alg.getPivot(null));

        DenseMatrix64F A_found = P.transpose().mult(L).mult(U).getMatrix();

        assertTrue(MatrixFeatures.isIdentical(A_found,A,1e-8));
    }
}
