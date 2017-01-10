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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.CheckDecompositionInterface_R64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public abstract class GenericQrCheck_R64 {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<DMatrixRow_F64> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface_R64.checkModifiedInput(createQRDecomposition());
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
        QRDecomposition<DMatrixRow_F64> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRow_F64.class);
        RandomMatrices_R64.setRandom((DMatrixRow_F64)A.getMatrix(),rand);

        assertTrue(alg.decompose((DMatrixRow_F64)A.copy().getMatrix()));

        int minStride = Math.min(height,width);

        SimpleMatrix Q = new SimpleMatrix(height,compact ? minStride : height, DMatrixRow_F64.class);
        alg.getQ((DMatrixRow_F64)Q.getMatrix(), compact);
        SimpleMatrix R = new SimpleMatrix(compact ? minStride : height,width, DMatrixRow_F64.class);
        alg.getR((DMatrixRow_F64)R.getMatrix(), compact);


        // see if Q has the expected properties
        assertTrue(MatrixFeatures_R64.isOrthogonal((DMatrixRow_F64)Q.getMatrix(), UtilEjml.TEST_F64_SQ));

//        UtilEjml.print(alg.getQR());
//        Q.print();
//        R.print();

        // see if it has the expected properties
        DMatrixRow_F64 A_found = Q.mult(R).getMatrix();

        EjmlUnitTests.assertEquals((DMatrixRow_F64)A.getMatrix(),A_found,UtilEjml.TEST_F64_SQ);
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

        QRDecomposition<DMatrixRow_F64> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRow_F64.class);
        RandomMatrices_R64.setRandom((DMatrixRow_F64)A.getMatrix(),rand);

        alg.decompose((DMatrixRow_F64)A.getMatrix());

        // get the results from a provided matrix
        DMatrixRow_F64 Q_provided = RandomMatrices_R64.createRandom(height,height,rand);
        DMatrixRow_F64 R_provided = RandomMatrices_R64.createRandom(height,width,rand);
        
        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        DMatrixRow_F64 Q_null = alg.getQ(null, false);
        DMatrixRow_F64 R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures_R64.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures_R64.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<DMatrixRow_F64> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRow_F64.class);
        RandomMatrices_R64.setRandom((DMatrixRow_F64)A.getMatrix(),rand);

        alg.decompose((DMatrixRow_F64)A.getMatrix());

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new DMatrixRow_F64(width,width),true);
        alg.getR(new DMatrixRow_F64(height,width),false);

        // check some negative cases
        try {
            alg.getR(new DMatrixRow_F64(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new DMatrixRow_F64(width-1,width),false);
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

        QRDecomposition<DMatrixRow_F64> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width, DMatrixRow_F64.class);
        RandomMatrices_R64.setRandom((DMatrixRow_F64)A.getMatrix(),rand);

        alg.decompose((DMatrixRow_F64)A.getMatrix());

        SimpleMatrix Q = new SimpleMatrix(height,width, DMatrixRow_F64.class);
        alg.getQ((DMatrixRow_F64)Q.getMatrix(), true);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures_R64.isOrthogonal((DMatrixRow_F64)Q.getMatrix(),UtilEjml.TEST_F64_SQ));

        // try to extract it with the wrong dimensions
        Q = new SimpleMatrix(height,height, DMatrixRow_F64.class);
        try {
            alg.getQ((DMatrixRow_F64)Q.getMatrix(), true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
