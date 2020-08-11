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

package org.ejml.dense.row.decompose.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.CommonOps_CDRM;
import org.ejml.dense.row.MatrixFeatures_CDRM;
import org.ejml.dense.row.RandomMatrices_CDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
* @author Peter Abeles
*/
public abstract class GenericQrCheck_CDRM {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<CMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(6, 4, rand);
        CMatrixRMaj A_orig = A.copy();

        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_CDRM.isEquals(A,A_orig);

        assertEquals(alg.inputModified(), modified, modified + " " + alg.inputModified());
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
        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(height,width,rand);

        assertTrue(alg.decompose(A.copy()));

        int minStride = Math.min(height,width);

        CMatrixRMaj Q = new CMatrixRMaj(height,compact ? minStride : height);
        alg.getQ(Q, compact);
        CMatrixRMaj R = new CMatrixRMaj(compact ? minStride : height,width);
        alg.getR(R, compact);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_CDRM.isUnitary(Q, UtilEjml.TEST_F32));

        // see if it has the expected properties
        CMatrixRMaj A_found = new CMatrixRMaj(Q.numRows,R.numCols);
        CommonOps_CDRM.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F32);
        CMatrixRMaj R_found = new CMatrixRMaj(R.numRows,R.numCols);
        CommonOps_CDRM.transposeConjugate(Q);
        CommonOps_CDRM.mult(Q, A, R_found);
    }

    /**
     * Test a pathological case for computing tau
     */
    @Test
    public void checkZeroInFirstElement() {
        int width = 4,height = 5;

        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(height,width,rand);

        // cause the pathological situation
        A.set(0,0,0,0);

        assertTrue(alg.decompose(A.copy()));

        CMatrixRMaj Q = new CMatrixRMaj(height,height);
        alg.getQ(Q, false);
        CMatrixRMaj R = new CMatrixRMaj(height,width);
        alg.getR(R, false);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_CDRM.isUnitary(Q, UtilEjml.TEST_F32));

        // see if it has the expected properties
        CMatrixRMaj A_found = new CMatrixRMaj(Q.numRows,R.numCols);
        CommonOps_CDRM.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F32);
        CMatrixRMaj R_found = new CMatrixRMaj(R.numRows,R.numCols);
        CommonOps_CDRM.transposeConjugate(Q);
        CommonOps_CDRM.mult(Q, A, R_found);
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(height,width,rand);

        alg.decompose(A);

        // get the results from a provided matrix
        CMatrixRMaj Q_provided = RandomMatrices_CDRM.rectangle(height,height,rand);
        CMatrixRMaj R_provided = RandomMatrices_CDRM.rectangle(height,width,rand);

        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        CMatrixRMaj Q_null = alg.getQ(null, false);
        CMatrixRMaj R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_CDRM.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_CDRM.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(height,width,rand);

        alg.decompose(A);

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new CMatrixRMaj(width,width),true);
        alg.getR(new CMatrixRMaj(height,width),false);

        // check some negative cases
        try {
            alg.getR(new CMatrixRMaj(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new CMatrixRMaj(width-1,width),false);
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

        QRDecomposition<CMatrixRMaj> alg = createQRDecomposition();

        CMatrixRMaj A = RandomMatrices_CDRM.rectangle(height,width,rand);

        alg.decompose(A);

        CMatrixRMaj Q = new CMatrixRMaj(height,width);
        alg.getQ(Q, true);

        // see if Q has the expected properties
        assertEquals(height,Q.numRows);
        assertEquals(width,Q.numCols);
        assertTrue(MatrixFeatures_CDRM.isUnitary(Q,UtilEjml.TEST_F32));

        // try to extract it with the wrong dimensions
        Q = new CMatrixRMaj(height,height);
        try {
            alg.getQ(Q, true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
