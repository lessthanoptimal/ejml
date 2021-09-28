/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlStandardJUnit;
import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class GenericQrCheck_ZDRM extends EjmlStandardJUnit {
    abstract protected QRDecomposition<ZMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(6, 4, rand);
        ZMatrixRMaj A_orig = A.copy();

        assertTrue(alg.decompose(A));

        boolean modified = !MatrixFeatures_ZDRM.isEquals(A,A_orig);

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
        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(height,width,rand);

        assertTrue(alg.decompose(A.copy()));

        int minStride = Math.min(height,width);

        ZMatrixRMaj Q = new ZMatrixRMaj(height,compact ? minStride : height);
        alg.getQ(Q, compact);
        ZMatrixRMaj R = new ZMatrixRMaj(compact ? minStride : height,width);
        alg.getR(R, compact);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_ZDRM.isUnitary(Q, UtilEjml.TEST_F64));

        // see if it has the expected properties
        ZMatrixRMaj A_found = new ZMatrixRMaj(Q.numRows,R.numCols);
        CommonOps_ZDRM.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F64);
        ZMatrixRMaj R_found = new ZMatrixRMaj(R.numRows,R.numCols);
        CommonOps_ZDRM.transposeConjugate(Q);
        CommonOps_ZDRM.mult(Q, A, R_found);
    }

    /**
     * Test a pathological case for computing tau
     */
    @Test
    public void checkZeroInFirstElement() {
        int width = 4,height = 5;

        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(height,width,rand);

        // cause the pathological situation
        A.set(0,0,0,0);

        assertTrue(alg.decompose(A.copy()));

        ZMatrixRMaj Q = new ZMatrixRMaj(height,height);
        alg.getQ(Q, false);
        ZMatrixRMaj R = new ZMatrixRMaj(height,width);
        alg.getR(R, false);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_ZDRM.isUnitary(Q, UtilEjml.TEST_F64));

        // see if it has the expected properties
        ZMatrixRMaj A_found = new ZMatrixRMaj(Q.numRows,R.numCols);
        CommonOps_ZDRM.mult(Q,R,A_found);

        EjmlUnitTests.assertEquals(A,A_found,UtilEjml.TEST_F64);
        ZMatrixRMaj R_found = new ZMatrixRMaj(R.numRows,R.numCols);
        CommonOps_ZDRM.transposeConjugate(Q);
        CommonOps_ZDRM.mult(Q, A, R_found);
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(height,width,rand);

        alg.decompose(A);

        // get the results from a provided matrix
        ZMatrixRMaj Q_provided = RandomMatrices_ZDRM.rectangle(height,height,rand);
        ZMatrixRMaj R_provided = RandomMatrices_ZDRM.rectangle(height,width,rand);

        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        ZMatrixRMaj Q_null = alg.getQ(null, false);
        ZMatrixRMaj R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_ZDRM.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_ZDRM.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(height,width,rand);

        alg.decompose(A);

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new ZMatrixRMaj(width,width),true);
        alg.getR(new ZMatrixRMaj(height,width),false);

        // check some negative cases
        try {
            alg.getR(new ZMatrixRMaj(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new ZMatrixRMaj(width-1,width),false);
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

        QRDecomposition<ZMatrixRMaj> alg = createQRDecomposition();

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(height,width,rand);

        alg.decompose(A);

        ZMatrixRMaj Q = new ZMatrixRMaj(height,width);
        alg.getQ(Q, true);

        // see if Q has the expected properties
        assertEquals(height,Q.numRows);
        assertEquals(width,Q.numCols);
        assertTrue(MatrixFeatures_ZDRM.isUnitary(Q,UtilEjml.TEST_F64));

        // try to extract it with the wrong dimensions
        Q = new ZMatrixRMaj(height,height);
        try {
            alg.getQ(Q, true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
