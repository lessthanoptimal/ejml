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

package org.ejml.alg.dense.decompose.qr;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.EjmlUnitTests;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
public abstract class GenericQrCheck_CD64 {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<CDenseMatrix64F> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(6, 4, rand);
        CDenseMatrix64F A_orig = A.copy();

        assertTrue(alg.decompose(A));

        boolean modified = !CMatrixFeatures.isEquals(A,A_orig);

        assertTrue(modified + " " + alg.inputModified(), alg.inputModified() == modified);
    }

    /**
     * See if it correctly decomposes a square, tall, or wide matrix.
     */
    @Test
    public void decompositionShape() {
        checkDecomposition(5, 5 ,false);
        checkDecomposition(10, 5,false);
        checkDecomposition(5, 10,false);
        checkDecomposition(5, 5 ,true);
        checkDecomposition(10, 5,true);
        checkDecomposition(5, 10,true);
    }

    private void checkDecomposition(int height, int width, boolean compact ) {
        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(height,width,rand);

        assertTrue(alg.decompose(A.copy()));

        int minStride = Math.min(height,width);

        CDenseMatrix64F Q = new CDenseMatrix64F(height,compact ? minStride : height);
        alg.getQ(Q, compact);
        CDenseMatrix64F R = new CDenseMatrix64F(compact ? minStride : height,width);
        alg.getR(R, compact);

        // see if Q has the expected properties
        assertTrue(CMatrixFeatures.isUnitary(Q, 1e-6));

        // see if it has the expected properties
        CDenseMatrix64F A_found = new CDenseMatrix64F(Q.numRows,R.numCols);
        CCommonOps.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,1e-6);
        CDenseMatrix64F R_found = new CDenseMatrix64F(R.numRows,R.numCols);
        CCommonOps.transposeConjugate(Q);
        CCommonOps.mult(Q, A, R_found);
    }

    /**
     * Test a pathological case for computing tau
     */
    @Test
    public void checkZeroInFirstElement() {
        int width = 4,height = 5;

        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(height,width,rand);

        // cause the pathological situation
        A.set(0,0,0,0);

        assertTrue(alg.decompose(A.copy()));

        CDenseMatrix64F Q = new CDenseMatrix64F(height,height);
        alg.getQ(Q, false);
        CDenseMatrix64F R = new CDenseMatrix64F(height,width);
        alg.getR(R, false);

        // see if Q has the expected properties
        assertTrue(CMatrixFeatures.isUnitary(Q, 1e-6));

        // see if it has the expected properties
        CDenseMatrix64F A_found = new CDenseMatrix64F(Q.numRows,R.numCols);
        CCommonOps.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,1e-6);
        CDenseMatrix64F R_found = new CDenseMatrix64F(R.numRows,R.numCols);
        CCommonOps.transposeConjugate(Q);
        CCommonOps.mult(Q, A, R_found);
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(height,width,rand);

        alg.decompose(A);

        // get the results from a provided matrix
        CDenseMatrix64F Q_provided = CRandomMatrices.createRandom(height,height,rand);
        CDenseMatrix64F R_provided = CRandomMatrices.createRandom(height,width,rand);

        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        CDenseMatrix64F Q_null = alg.getQ(null, false);
        CDenseMatrix64F R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(CMatrixFeatures.isEquals(Q_provided,Q_null));
        assertTrue(CMatrixFeatures.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(height,width,rand);

        alg.decompose(A);

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new CDenseMatrix64F(width,width),true);
        alg.getR(new CDenseMatrix64F(height,width),false);

        // check some negative cases
        try {
            alg.getR(new CDenseMatrix64F(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new CDenseMatrix64F(width-1,width),false);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}
    }

    /**
     * See if the compact format for Q works
     */
    @Test
    public void checkCompactFormat()
    {
        int height = 10;
        int width = 5;

        QRDecomposition<CDenseMatrix64F> alg = createQRDecomposition();

        CDenseMatrix64F A = CRandomMatrices.createRandom(height,width,rand);

        alg.decompose(A);

        CDenseMatrix64F Q = new CDenseMatrix64F(height,width);
        alg.getQ(Q, true);

        // see if Q has the expected properties
        assertEquals(height,Q.numRows);
        assertEquals(width,Q.numCols);
        assertTrue(CMatrixFeatures.isUnitary(Q,1e-6));

        // try to extract it with the wrong dimensions
        Q = new CDenseMatrix64F(height,height);
        try {
            alg.getQ(Q, true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
