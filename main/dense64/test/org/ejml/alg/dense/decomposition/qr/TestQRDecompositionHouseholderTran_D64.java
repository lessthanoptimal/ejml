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

import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholderTran_D64 extends GenericQrCheck_D64 {

    Random rand = new Random(0xff);

    @Override
    protected QRDecomposition<DenseMatrix64F> createQRDecomposition() {
        return new QRDecompositionHouseholderTran_D64();
    }

    /**
     * Sees if computing Q explicitly and applying Q produces the same results
     */
    @Test
    public void applyQ() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,4,rand);

        QRDecompositionHouseholderTran_D64 alg = new QRDecompositionHouseholderTran_D64();

        assertTrue(alg.decompose(A));

        DenseMatrix64F Q = alg.getQ(null,false);
        DenseMatrix64F B = RandomMatrices.createRandom(5,2,rand);

        DenseMatrix64F expected = new DenseMatrix64F(B.numRows,B.numCols);
        CommonOps.mult(Q,B,expected);

        alg.applyQ(B);

        assertTrue(MatrixFeatures.isIdentical(expected,B,1e-8));
    }

    /**
     * Sees if computing Q^T explicitly and applying Q^T produces the same results
     */
    @Test
    public void applyTranQ() {
        DenseMatrix64F A = RandomMatrices.createRandom(5,4,rand);

        QRDecompositionHouseholderTran_D64 alg = new QRDecompositionHouseholderTran_D64();

        assertTrue(alg.decompose(A));

        DenseMatrix64F Q = alg.getQ(null,false);
        DenseMatrix64F B = RandomMatrices.createRandom(5,2,rand);

        DenseMatrix64F expected = new DenseMatrix64F(B.numRows,B.numCols);
        CommonOps.multTransA(Q,B,expected);

        alg.applyTranQ(B);

        assertTrue(MatrixFeatures.isIdentical(expected,B,1e-8));
    }

    /**
     * A focused check to see if the internal house holder operations are performed correctly.
     */
    @Test
    public void householder() {
        int width = 5;

        for( int i = 0; i < width; i++ ) {
            checkSubHouse(i , width);
        }
    }

    private void checkSubHouse(int w , int width) {
        DebugQR qr = new DebugQR(width,width);

        SimpleMatrix A = new SimpleMatrix(width,width);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        qr.householder(w,A.getMatrix());

        SimpleMatrix U = new SimpleMatrix(width,1, true, qr.getU(w)).extractMatrix(w,width,0,1);

        SimpleMatrix I = SimpleMatrix.identity(width-w);
        SimpleMatrix Q = I.minus(U.mult(U.transpose()).scale(qr.getGamma()));


        // check the expected properties of Q
        assertTrue(Q.isIdentical(Q.transpose(),1e-6));
        assertTrue(Q.isIdentical(Q.invert(),1e-6));

        SimpleMatrix result = Q.mult(A.extractMatrix(w,width,w,width));

        for( int i = 1; i < width-w; i++ ) {
            assertEquals(0,result.get(i,0),1e-5);
        }
    }

    /**
     * Check the results of this function against basic matrix operations
     * which are equivalent.
     */
    @Test
    public void updateA() {
        int width = 5;

        for( int i = 0; i < width; i++ )
            checkSubMatrix(width,i);
    }

    private void checkSubMatrix(int width , int w ) {
        DebugQR qr = new DebugQR(width,width);

        double gamma = 0.2;
        double tau = 0.75;

        SimpleMatrix U = new SimpleMatrix(width,1);
        SimpleMatrix A = new SimpleMatrix(width,width);

        RandomMatrices.setRandom(U.getMatrix(),rand);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        CommonOps.transpose(A.getMatrix(),qr.getQR());

        // compute the results using standard matrix operations
        SimpleMatrix I = SimpleMatrix.identity(width-w);

        SimpleMatrix u_sub = U.extractMatrix(w,width,0,1);
        u_sub.set(0,0,1);// assumed to be 1 in the algorithm
        SimpleMatrix A_sub = A.extractMatrix(w,width,w,width);
        SimpleMatrix expected = I.minus(u_sub.mult(u_sub.transpose()).scale(gamma)).mult(A_sub);

        qr.updateA(w,U.getMatrix().getData(),gamma,tau);

        DenseMatrix64F found = qr.getQR();

        for( int i = w+1; i < width; i++ ) {
            assertEquals(U.get(i,0),found.get(w,i),1e-8);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                double a = expected.get(i-w,j-w);
                double b = found.get(j,i);

                assertEquals(a,b,1e-6);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholderTran_D64
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , DenseMatrix64F A ) {
            CommonOps.transpose(A,QR);

            super.householder(j);
        }

        public void updateA( int w , double u[] , double gamma , double tau ) {
            System.arraycopy(u,0,this.QR.data,w*QR.numRows,u.length);
            this.gamma = gamma;
            this.tau = tau;

            super.updateA(w);
        }

        public double[] getU( int w ) {
            System.arraycopy(QR.data,w*numRows,v,0,numRows);
            v[w] = 1;
            return v;
        }

        public double getGamma() {
            return gamma;
        }
    }
}
