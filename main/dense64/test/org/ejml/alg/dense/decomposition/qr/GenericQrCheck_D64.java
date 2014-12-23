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

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decomposition.CheckDecompositionInterface;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public abstract class GenericQrCheck_D64 {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition<DenseMatrix64F> createQRDecomposition();

    @Test
    public void testModifiedInput() {
        CheckDecompositionInterface.checkModifiedInput(createQRDecomposition());
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
        QRDecomposition<DenseMatrix64F> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        assertTrue(alg.decompose(A.copy().getMatrix()));

        int minStride = Math.min(height,width);

        SimpleMatrix Q = new SimpleMatrix(height,compact ? minStride : height);
        alg.getQ(Q.getMatrix(), compact);
        SimpleMatrix R = new SimpleMatrix(compact ? minStride : height,width);
        alg.getR(R.getMatrix(), compact);


        // see if Q has the expected properties
        assertTrue(MatrixFeatures.isOrthogonal(Q.getMatrix(),1e-6));

//        UtilEjml.print(alg.getQR());
//        Q.print();
//        R.print();

        // see if it has the expected properties
        DenseMatrix64F A_found = Q.mult(R).getMatrix();

        EjmlUnitTests.assertEquals(A.getMatrix(),A_found,1e-6);
        assertTrue(Q.transpose().mult(A).isIdentical(R,1e-6));
    }

    /**
     * See if passing in a matrix or not providing one to getQ and getR functions
     * has the same result
     */
    @Test
    public void checkGetNullVersusNot() {
        int width = 5;
        int height = 10;

        QRDecomposition<DenseMatrix64F> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        alg.decompose(A.getMatrix());

        // get the results from a provided matrix
        DenseMatrix64F Q_provided = RandomMatrices.createRandom(height,height,rand);
        DenseMatrix64F R_provided = RandomMatrices.createRandom(height,width,rand);
        
        assertTrue(R_provided == alg.getR(R_provided, false));
        assertTrue(Q_provided == alg.getQ(Q_provided, false));

        // get the results when no matrix is provided
        DenseMatrix64F Q_null = alg.getQ(null, false);
        DenseMatrix64F R_null = alg.getR(null,false);

        // see if they are the same
        assertTrue(MatrixFeatures.isEquals(Q_provided,Q_null));
        assertTrue(MatrixFeatures.isEquals(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition<DenseMatrix64F> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        alg.decompose(A.getMatrix());

        // check the case where it creates the matrix first
        assertTrue(alg.getR(null,true).numRows == width);
        assertTrue(alg.getR(null,false).numRows == height);

        // check the case where a matrix is provided
        alg.getR(new DenseMatrix64F(width,width),true);
        alg.getR(new DenseMatrix64F(height,width),false);

        // check some negative cases
        try {
            alg.getR(new DenseMatrix64F(height,width),true);
            fail("Should have thrown an exception");
        } catch( IllegalArgumentException e ) {}

        try {
            alg.getR(new DenseMatrix64F(width-1,width),false);
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

        QRDecomposition<DenseMatrix64F> alg = createQRDecomposition();

        SimpleMatrix A = new SimpleMatrix(height,width);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        alg.decompose(A.getMatrix());

        SimpleMatrix Q = new SimpleMatrix(height,width);
        alg.getQ(Q.getMatrix(), true);

        // see if Q has the expected properties
        assertTrue(MatrixFeatures.isOrthogonal(Q.getMatrix(),1e-6));

        // try to extract it with the wrong dimensions
        Q = new SimpleMatrix(height,height);
        try {
            alg.getQ(Q.getMatrix(), true);
            fail("Didn't fail");
        } catch( RuntimeException e ) {}
    }

}
