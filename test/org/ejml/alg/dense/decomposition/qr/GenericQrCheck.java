/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.decomposition.qr;

import org.ejml.alg.dense.decomposition.CheckDecompositionInterface;
import org.ejml.alg.dense.decomposition.QRDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author Peter Abeles
 */
public abstract class GenericQrCheck {
    Random rand = new Random(0xff);

    abstract protected QRDecomposition createQRDecomposition();

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
        QRDecomposition alg = createQRDecomposition();

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

        QRDecomposition alg = createQRDecomposition();

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
        assertTrue(MatrixFeatures.isIdentical(Q_provided,Q_null));
        assertTrue(MatrixFeatures.isIdentical(R_provided,R_null));
    }

    /**
     * Depending on if setZero being true or not the size of the R matrix changes
     */
    @Test
    public void checkGetRInputSize()
    {
        int width = 5;
        int height = 10;

        QRDecomposition alg = createQRDecomposition();

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

        QRDecomposition alg = createQRDecomposition();

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
