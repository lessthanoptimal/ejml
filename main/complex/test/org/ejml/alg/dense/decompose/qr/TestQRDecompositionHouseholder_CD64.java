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
import org.ejml.ops.CSpecializedOps;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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

        // Q = I - gamma*u*u'
        CDenseMatrix64F Q = CSpecializedOps.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(CMatrixFeatures.isHermitian(Q, 1e-6));
        assertTrue(CMatrixFeatures.isUnitary(Q, 1e-6));

        CDenseMatrix64F result = new CDenseMatrix64F(Q.numRows,Q.numCols);
        CDenseMatrix64F Asub = CCommonOps.extract(A,w,width,w,width);
        CCommonOps.mult(Q, Asub, result);

        Complex64F a = new Complex64F();
        result.get(0,0,a);
        assertEquals(-qr.realTau, a.real, 1e-8);
        assertEquals(-qr.imagTau,a.imaginary,1e-8);

        for( int i = 1; i < result.numRows; i++ ) {
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
    }

    private void checkSubMatrix(int width , int w ) {
        DebugQR qr = new DebugQR(width,width);

        double gamma = 0.2;
        double realTau = 0.75;
        double imagTau = -0.6;

        CDenseMatrix64F U = CRandomMatrices.createRandom(width, 1,rand);
        CDenseMatrix64F A = CRandomMatrices.createRandom(width,width,rand);

        qr.getQR().set(A);

        // compute the results using standard matrix operations
        CDenseMatrix64F u_sub = CCommonOps.extract(U, w, width, 0, 1);
        CDenseMatrix64F A_sub = CCommonOps.extract(A, w, width, w, width);
        CDenseMatrix64F expected = new CDenseMatrix64F(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        CDenseMatrix64F Q = CSpecializedOps.householder(u_sub,gamma);

        CCommonOps.mult(Q,A_sub,expected);

        qr.updateA(w,U.getData(),gamma,realTau,imagTau);

        CDenseMatrix64F found = qr.getQR();

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();
        found.get(w,w,a);

        assertEquals(-realTau,a.real,1e-8);
        assertEquals(-imagTau,a.imaginary,1e-8);

        for( int i = w+1; i < width; i++ ) {
            U.get(i,0,a);
            found.get(i,w,b);

            assertEquals(a.real, b.real, 1e-8);
            assertEquals(a.imaginary,b.imaginary,1e-8);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                expected.get(i-w,j-w,a);
                found.get(i,j,b);

                assertEquals(a.real, b.real, 1e-6);
                assertEquals(a.imaginary,b.imaginary,1e-6);
            }
        }
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
                             double realGamma, double realTau, double imagTau ) {
            System.arraycopy(u,0,this.u,0,this.u.length);
            this.realGamma = realGamma;
            this.realTau = realTau;
            this.imagTau = imagTau;


            super.updateA(w);
        }

        public double[] getU() {
            return u;
        }

        public double getGamma() {
            return realGamma;
        }
    }
}
