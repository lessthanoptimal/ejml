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
import org.ejml.data.Complex64F;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CMatrixFeatures;
import org.ejml.ops.CRandomMatrices;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholder_CD64 extends GenericQrCheck_CD64 {

    Random rand = new Random(0xff);


    @Override
    protected QRDecomposition<CDenseMatrix64F> createQRDecomposition() {
        return new QRDecompositionHouseholder_CD64();
    }

    /**
     * Internally several house holder operations are performed.  This
     * checks to see if the householder operations and the expected result for all the
     * submatrices.
     */
    @Test
    public void householder() {
        int width = 6;

        for( int i = 0; i < width; i++ ) {
            checkSubHouse(i, width);
        }
    }

    private void checkSubHouse(int w , int width) {
        DebugQR qr = new DebugQR(width,width);

        CDenseMatrix64F A = CRandomMatrices.createRandom(width,width,rand);

        qr.householder(w,A);

        CDenseMatrix64F U = new CDenseMatrix64F(width-w,1);
        System.arraycopy(qr.getU(),w*2,U.data,0,(width-w)*2);
        CDenseMatrix64F Ut = CCommonOps.transposeConjugate(U,null);

        CDenseMatrix64F I = CCommonOps.identity(width-w);
        CDenseMatrix64F UUt = new CDenseMatrix64F(I.numRows,I.numCols);
        CDenseMatrix64F gamma_UUt = new CDenseMatrix64F(I.numRows,I.numCols);
        CDenseMatrix64F Q = new CDenseMatrix64F(I.numRows,I.numCols);

        // Q = I - gamma*u*u'
        CCommonOps.mult(U, Ut, UUt);
        CCommonOps.elementMultiply(UUt, qr.getRealGamma(), 0, gamma_UUt);
        CCommonOps.subtract(I,gamma_UUt,Q);

        // check the expected properties of Q
        assertTrue(CMatrixFeatures.isHermitian(Q, 1e-6));
        assertTrue(CMatrixFeatures.isUnitary(Q, 1e-6));

        CDenseMatrix64F result = new CDenseMatrix64F(I.numRows,I.numCols);
        CDenseMatrix64F Asub = CCommonOps.extract(A,w,width,w,width);
        CCommonOps.mult(Q,Asub,result);

        Complex64F a = new Complex64F();
        for( int i = 1; i < width-w; i++ ) {
            result.get(i,0,a);
            assertEquals(0, a.getMagnitude2(),1e-5);
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
        fail("update");
    }

    private void checkSubMatrix(int width , int w ) {
        DebugQR qr = new DebugQR(width,width);

        double gamma = 0.2;
        double tau = 0.75;

        SimpleMatrix U = new SimpleMatrix(width,1);
        SimpleMatrix A = new SimpleMatrix(width,width);

        RandomMatrices.setRandom(U.getMatrix(),rand);
        RandomMatrices.setRandom(A.getMatrix(),rand);

        qr.getQR().set(A.getMatrix());

        // compute the results using standard matrix operations
        SimpleMatrix I = SimpleMatrix.identity(width-w);

        SimpleMatrix u_sub = U.extractMatrix(w,width,0,1);
        SimpleMatrix A_sub = A.extractMatrix(w,width,w,width);
        SimpleMatrix expected = I.minus(u_sub.mult(u_sub.transpose()).scale(gamma)).mult(A_sub);

        qr.updateA(w,U.getMatrix().getData(),gamma,999999,tau);

        CDenseMatrix64F found = qr.getQR();

//        assertEquals(-tau,found.get(w,w),1e-8);
//
//        for( int i = w+1; i < width; i++ ) {
//            assertEquals(U.get(i,0),found.get(i,w),1e-8);
//        }

//        // the right should be the same
//        for( int i = w; i < width; i++ ) {
//            for( int j = w+1; j < width; j++ ) {
//                double a = expected.get(i-w,j-w);
//                double b = found.get(i,j);
//
//                assertEquals(a,b,1e-6);
//            }
//        }
    }

    private static class DebugQR extends QRDecompositionHouseholder_CD64
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , CDenseMatrix64F A ) {
            this.QR.set(A);

            super.householder(j);
        }

        public void updateA( int w , double u[] ,
                             double realGamma, double imagGamma , double tau ) {
            System.arraycopy(u,0,this.u,0,this.u.length);
            this.realGamma = realGamma;
            this.tau = tau;

            super.updateA(w);
        }

        public double[] getU() {
            return u;
        }

        public double getRealGamma() {
            return realGamma;
        }
    }
}
