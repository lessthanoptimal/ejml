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

package org.ejml.dense.row.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericQrCheck_DDRM {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<DMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_DDRM.checkModifiedInput(createQRDecomposition());
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
        QRDecomposition<DMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRMaj.class);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)A.getMatrix(),rand);

        assertTrue(alg.decompose((DMatrixRMaj)A.copy().getMatrix()));

        int minStride = Math.min(height,width);

        SimpleMatrix Q = new SimpleMatrix(height,compact ? minStride : height, DMatrixRMaj.class);
        alg.getQ((DMatrixRMaj)Q.getMatrix(), compact);
        SimpleMatrix R = new SimpleMatrix(compact ? minStride : height,width, DMatrixRMaj.class);
        alg.getR((DMatrixRMaj)R.getMatrix(), compact);


        // see if Q has the expected properties
        assertTrue(MatrixFeatures_DDRM.isOrthogonal((DMatrixRMaj)Q.getMatrix(), UtilEjml.TEST_F64_SQ));

//        UtilEjml.print(dense.getQR());
//        Q.print();
//        R.print();

        // see if it has the expected properties
        DMatrixRMaj A_found = Q.mult(R).getMatrix();

        EjmlUnitTests.assertEquals((DMatrixRMaj)A.getMatrix(),A_found,UtilEjml.TEST_F64_SQ);
        assertTrue(Q.transpose().mult(A).isIdentical(R,UtilEjml.TEST_F64_SQ));
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<DMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRMaj.class);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((DMatrixRMaj)A.getMatrix());

        // get the results from a provided matrix
        DMatrixRMaj Q_provided = RandomMatrices_DDRM.rectangle(height,height,rand);
        DMatrixRMaj R_provided = RandomMatrices_DDRM.rectangle(height,width,rand);

        assertSame(R_provided, alg.getR(R_provided, false));
        assertSame(Q_provided, alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        DMatrixRMaj Q_null = alg.getQ(null, false);
        DMatrixRMaj R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_DDRM.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_DDRM.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<DMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRMaj.class);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((DMatrixRMaj)A.getMatrix());

        // check the case where it creates the matrix first
        assertEquals(width, alg.getR(null, true).numRows);
        assertEquals(height, alg.getR(null, false).numRows);

        // check the case where a matrix is provided
        alg.getR(new DMatrixRMaj(width,width),true);
        alg.getR(new DMatrixRMaj(height,width),false);

        // check some negative cases
        {
            DMatrixRMaj R = new DMatrixRMaj(height,width);
            alg.getR(R,true);
            assertEquals(width,R.numCols);
            assertEquals(width,R.numRows);
        }

        {
            DMatrixRMaj R = new DMatrixRMaj(width-1,width);
            alg.getR(R,false);
            assertEquals(width,R.numCols);
            assertEquals(height,R.numRows);
        }
    }

    /**
     * See if the compact format for Q works
     */
    @Test
    public void checkCompactFormat()
    {
        int height = 10;
        int width = 5;

        QRDecomposition<DMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRMaj.class);
        RandomMatrices_DDRM.fillUniform((DMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((DMatrixRMaj)A.getMatrix());

        SimpleMatrix Q = new SimpleMatrix(height,width, DMatrixRMaj.class);
        alg.getQ((DMatrixRMaj)Q.getMatrix(), true);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_DDRM.isOrthogonal((DMatrixRMaj)Q.getMatrix(),UtilEjml.TEST_F64_SQ));

        // try to extract it with the wrong dimensions
        Q = new SimpleMatrix(height,height, DMatrixRMaj.class);
        alg.getQ((DMatrixRMaj)Q.getMatrix(), true);
        assertEquals(height,Q.numRows());
        assertEquals(width,Q.numCols());
    }

}
