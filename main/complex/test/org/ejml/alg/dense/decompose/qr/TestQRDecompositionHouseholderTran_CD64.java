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
public class TestQRDecompositionHouseholderTran_CD64 extends GenericQrCheck_CD64 {

    Random rand = new Random(0xff);

    @Override
    protected QRDecomposition<CDenseMatrix64F> createQRDecomposition() {
        return new QRDecompositionHouseholderTran_CD64();
    }

    /**
     * Sees if computing Q explicitly and applying Q produces the same results
     */
    @Test
    public void applyQ() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(5, 4, rand);

        QRDecompositionHouseholderTran_CD64 alg = new QRDecompositionHouseholderTran_CD64();

        assertTrue(alg.decompose(A));

        CDenseMatrix64F Q = alg.getQ(null,false);
        CDenseMatrix64F B = CRandomMatrices.createRandom(5,2,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(B.numRows,B.numCols);
        CCommonOps.mult(Q,B,expected);

        alg.applyQ(B);

        assertTrue(CMatrixFeatures.isIdentical(expected,B,1e-8));
    }

    /**
     * Sees if computing Q^H explicitly and applying Q^H produces the same results
     */
    @Test
    public void applyTranQ() {
        CDenseMatrix64F A = CRandomMatrices.createRandom(5,4,rand);

        QRDecompositionHouseholderTran_CD64 alg = new QRDecompositionHouseholderTran_CD64();

        assertTrue(alg.decompose(A));

        CDenseMatrix64F Q = alg.getQ(null,false);
        CDenseMatrix64F B = CRandomMatrices.createRandom(5,2,rand);

        CDenseMatrix64F expected = new CDenseMatrix64F(B.numRows,B.numCols);
        CCommonOps.transposeConjugate(Q);
        CCommonOps.mult(Q, B, expected);

        alg.applyTranQ(B);

        assertTrue(CMatrixFeatures.isIdentical(expected,B,1e-8));
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

        CDenseMatrix64F A = CRandomMatrices.createRandom(width,width,rand);

        qr.householder(w,A);
        CDenseMatrix64F U = qr.getU(w);

        // Q = I - gamma*u*u'
        CDenseMatrix64F Q = CSpecializedOps.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(CMatrixFeatures.isHermitian(Q, 1e-6));
        assertTrue(CMatrixFeatures.isUnitary(Q, 1e-6));

        CDenseMatrix64F result = new CDenseMatrix64F(Q.numRows,Q.numCols);
        CDenseMatrix64F Asub = CCommonOps.extract(A, w, width, w, width);
        CCommonOps.mult(Q, Asub, result);

        Complex64F a = new Complex64F();
        result.get(0,0,a);
        assertEquals(-qr.tau.real, a.real, 1e-8);
        assertEquals(-qr.tau.imaginary,a.imaginary,1e-8);

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

        CDenseMatrix64F A = CRandomMatrices.createRandom(width,width,rand);
        CCommonOps.transpose(A, qr.QR);

        // compute the results using standard matrix operations
        CDenseMatrix64F u_sub = CCommonOps.extract(A, w, width, w, w+1);
        CDenseMatrix64F A_sub = CCommonOps.extract(A, w, width, w, width);
        CDenseMatrix64F expected = new CDenseMatrix64F(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        u_sub.set(0,0,1,0);
        CDenseMatrix64F Q = CSpecializedOps.householder(u_sub,gamma);

        CCommonOps.mult(Q,A_sub,expected);

        qr.updateA(w,gamma);

        CDenseMatrix64F found = qr.getQR();

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();

        for( int i = w; i < width; i++ ) {
            A.get(i,w,a);
            found.get(w,i,b);

            assertEquals(a.real, b.real, 1e-8);
            assertEquals(a.imaginary,b.imaginary,1e-8);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                expected.get(i-w,j-w,a);
                found.get(j,i,b);

                assertEquals(a.real, b.real, 1e-6);
                assertEquals(a.imaginary,b.imaginary,1e-6);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholderTran_CD64
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , CDenseMatrix64F A ) {
            CCommonOps.transpose(A, QR);

            super.householder(j);
        }

        public void updateA( int w , double gamma ) {
            this.gamma = gamma;

            super.updateA(w);
        }

        public CDenseMatrix64F getU( int w ) {
            CDenseMatrix64F U = new CDenseMatrix64F(numRows-w,1);

            System.arraycopy(QR.data,(w*numRows+w)*2,U.data,0,(numRows-w)*2);
            U.set(0,0,1,0);
            return U;
        }

        public double getGamma() {
            return gamma;
        }
    }
}
