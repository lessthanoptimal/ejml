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
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_FDRM;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.dense.row.decomposition.CheckDecompositionInterface_FDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
public abstract class GenericQrCheck_FDRM {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<FMatrixRMaj> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_FDRM.checkModifiedInput(createQRDecomposition());
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
        QRDecomposition<FMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)A.getMatrix(),rand);

        assertTrue(alg.decompose((FMatrixRMaj)A.copy().getMatrix()));

        int minStride = Math.min(height,width);

        SimpleMatrix Q = new SimpleMatrix(height,compact ? minStride : height, FMatrixRMaj.class);
        alg.getQ((FMatrixRMaj)Q.getMatrix(), compact);
        SimpleMatrix R = new SimpleMatrix(compact ? minStride : height,width, FMatrixRMaj.class);
        alg.getR((FMatrixRMaj)R.getMatrix(), compact);


        // see if Q has the expected properties
        assertTrue(MatrixFeatures_FDRM.isOrthogonal((FMatrixRMaj)Q.getMatrix(), UtilEjml.TEST_F32_SQ));

//        UtilEjml.print(dense.getQR());
//        Q.print();
//        R.print();

        // see if it has the expected properties
        FMatrixRMaj A_found = Q.mult(R).getMatrix();

        EjmlUnitTests.assertEquals((FMatrixRMaj)A.getMatrix(),A_found,UtilEjml.TEST_F32_SQ);
        assertTrue(Q.transpose().mult(A).isIdentical(R,UtilEjml.TEST_F32_SQ));
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<FMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((FMatrixRMaj)A.getMatrix());

        // get the results from a provided matrix
        FMatrixRMaj Q_provided = RandomMatrices_FDRM.rectangle(height,height,rand);
        FMatrixRMaj R_provided = RandomMatrices_FDRM.rectangle(height,width,rand);
        
        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        FMatrixRMaj Q_null = alg.getQ(null, false);
        FMatrixRMaj R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_FDRM.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_FDRM.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<FMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((FMatrixRMaj)A.getMatrix());

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new FMatrixRMaj(width,width),true);
        alg.getR(new FMatrixRMaj(height,width),false);

        // check some negative cases
        {
            FMatrixRMaj R = new FMatrixRMaj(height,width);
            alg.getR(R,true);
            assertEquals(width,R.numCols);
            assertEquals(width,R.numRows);
        }

        {
            FMatrixRMaj R = new FMatrixRMaj(width-1,width);
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

        QRDecomposition<FMatrixRMaj> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, FMatrixRMaj.class);
        RandomMatrices_FDRM.fillUniform((FMatrixRMaj)A.getMatrix(),rand);

        alg.decompose((FMatrixRMaj)A.getMatrix());

        SimpleMatrix Q = new SimpleMatrix(height,width, FMatrixRMaj.class);
        alg.getQ((FMatrixRMaj)Q.getMatrix(), true);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_FDRM.isOrthogonal((FMatrixRMaj)Q.getMatrix(),UtilEjml.TEST_F32_SQ));

        // try to extract it with the wrong dimensions
        Q = new SimpleMatrix(height,height, FMatrixRMaj.class);
        try {
            alg.getQ((FMatrixRMaj)Q.getMatrix(), true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
