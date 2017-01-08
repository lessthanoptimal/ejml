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

package org.ejml.alg.dense.decompose.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
* @author Peter Abeles
*/
public abstract class GenericQrCheck_CD64 {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<RowMatrix_C64> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(6, 4, rand);
        RowMatrix_C64 A_orig = A.copy();

        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_CD64.isEquals(A,A_orig);

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
        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(height,width,rand);

        assertTrue(alg.decompose(A.copy()));

        int minStride = Math.min(height,width);

        RowMatrix_C64 Q = new RowMatrix_C64(height,compact ? minStride : height);
        alg.getQ(Q, compact);
        RowMatrix_C64 R = new RowMatrix_C64(compact ? minStride : height,width);
        alg.getR(R, compact);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_CD64.isUnitary(Q, UtilEjml.TEST_F64));

        // see if it has the expected properties
        RowMatrix_C64 A_found = new RowMatrix_C64(Q.numRows,R.numCols);
        CommonOps_CD64.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F64);
        RowMatrix_C64 R_found = new RowMatrix_C64(R.numRows,R.numCols);
        CommonOps_CD64.transposeConjugate(Q);
        CommonOps_CD64.mult(Q, A, R_found);
    }

    /**
     * Test a pathological case for computing tau
     */
    @Test
    public void checkZeroInFirstElement() {
        int width = 4,height = 5;

        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(height,width,rand);

        // cause the pathological situation
        A.set(0,0,0,0);

        assertTrue(alg.decompose(A.copy()));

        RowMatrix_C64 Q = new RowMatrix_C64(height,height);
        alg.getQ(Q, false);
        RowMatrix_C64 R = new RowMatrix_C64(height,width);
        alg.getR(R, false);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_CD64.isUnitary(Q, UtilEjml.TEST_F64));

        // see if it has the expected properties
        RowMatrix_C64 A_found = new RowMatrix_C64(Q.numRows,R.numCols);
        CommonOps_CD64.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F64);
        RowMatrix_C64 R_found = new RowMatrix_C64(R.numRows,R.numCols);
        CommonOps_CD64.transposeConjugate(Q);
        CommonOps_CD64.mult(Q, A, R_found);
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(height,width,rand);

        alg.decompose(A);

        // get the results from a provided matrix
        RowMatrix_C64 Q_provided = RandomMatrices_CD64.createRandom(height,height,rand);
        RowMatrix_C64 R_provided = RandomMatrices_CD64.createRandom(height,width,rand);

        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        RowMatrix_C64 Q_null = alg.getQ(null, false);
        RowMatrix_C64 R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_CD64.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_CD64.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(height,width,rand);

        alg.decompose(A);

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new RowMatrix_C64(width,width),true);
        alg.getR(new RowMatrix_C64(height,width),false);

        // check some negative cases
        try {
            alg.getR(new RowMatrix_C64(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new RowMatrix_C64(width-1,width),false);
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

        QRDecomposition<RowMatrix_C64> alg = createQRDecomposition();

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(height,width,rand);

        alg.decompose(A);

        RowMatrix_C64 Q = new RowMatrix_C64(height,width);
        alg.getQ(Q, true);

        // see if Q has the expected properties
        assertEquals(height,Q.numRows);
        assertEquals(width,Q.numCols);
        assertTrue(MatrixFeatures_CD64.isUnitary(Q,UtilEjml.TEST_F64));

        // try to extract it with the wrong dimensions
        Q = new RowMatrix_C64(height,height);
        try {
            alg.getQ(Q, true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
