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

package org.ejml.alg.dense.decompose.qr;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_C64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps_CR64;
import org.ejml.ops.MatrixFeatures_CR64;
import org.ejml.ops.RandomMatrices_CR64;
import org.ejml.ops.SpecializedOps_CR64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholderTran_CR64 extends GenericQrCheck_CR64 {

    Random rand = new Random(0xff);

    @Override
    protected QRDecomposition<DMatrixRow_C64> createQRDecomposition() {
        return new QRDecompositionHouseholderTran_CR64();
    }

    /**
     * Sees if computing Q explicitly and applying Q produces the same results
     */
    @Test
    public void applyQ() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(5, 4, rand);

        QRDecompositionHouseholderTran_CR64 alg = new QRDecompositionHouseholderTran_CR64();

        assertTrue(alg.decompose(A));

        DMatrixRow_C64 Q = alg.getQ(null,false);
        DMatrixRow_C64 B = RandomMatrices_CR64.createRandom(5,2,rand);

        DMatrixRow_C64 expected = new DMatrixRow_C64(B.numRows,B.numCols);
        CommonOps_CR64.mult(Q,B,expected);

        alg.applyQ(B);

        assertTrue(MatrixFeatures_CR64.isIdentical(expected,B, UtilEjml.TEST_F64));
    }

    /**
     * Sees if computing Q^H explicitly and applying Q^H produces the same results
     */
    @Test
    public void applyTranQ() {
        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(5,4,rand);

        QRDecompositionHouseholderTran_CR64 alg = new QRDecompositionHouseholderTran_CR64();

        assertTrue(alg.decompose(A));

        DMatrixRow_C64 Q = alg.getQ(null,false);
        DMatrixRow_C64 B = RandomMatrices_CR64.createRandom(5,2,rand);

        DMatrixRow_C64 expected = new DMatrixRow_C64(B.numRows,B.numCols);
        CommonOps_CR64.transposeConjugate(Q);
        CommonOps_CR64.mult(Q, B, expected);

        alg.applyTranQ(B);

        assertTrue(MatrixFeatures_CR64.isIdentical(expected,B,UtilEjml.TEST_F64));
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

        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(width,width,rand);

        qr.householder(w,A);
        DMatrixRow_C64 U = qr.getU(w);

        // Q = I - gamma*u*u'
        DMatrixRow_C64 Q = SpecializedOps_CR64.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(MatrixFeatures_CR64.isHermitian(Q, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_CR64.isUnitary(Q, UtilEjml.TEST_F64));

        DMatrixRow_C64 result = new DMatrixRow_C64(Q.numRows,Q.numCols);
        DMatrixRow_C64 Asub = CommonOps_CR64.extract(A, w, width, w, width);
        CommonOps_CR64.mult(Q, Asub, result);

        Complex_F64 a = new Complex_F64();
        result.get(0,0,a);
        assertEquals(-qr.tau.real, a.real, UtilEjml.TEST_F64);
        assertEquals(-qr.tau.imaginary,a.imaginary,UtilEjml.TEST_F64);

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

        DMatrixRow_C64 A = RandomMatrices_CR64.createRandom(width,width,rand);
        CommonOps_CR64.transpose(A, qr.QR);

        // compute the results using standard matrix operations
        DMatrixRow_C64 u_sub = CommonOps_CR64.extract(A, w, width, w, w+1);
        DMatrixRow_C64 A_sub = CommonOps_CR64.extract(A, w, width, w, width);
        DMatrixRow_C64 expected = new DMatrixRow_C64(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        u_sub.set(0,0,1,0);
        DMatrixRow_C64 Q = SpecializedOps_CR64.householder(u_sub,gamma);

        CommonOps_CR64.mult(Q,A_sub,expected);

        qr.updateA(w,gamma);

        DMatrixRow_C64 found = qr.getQR();

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();

        for( int i = w; i < width; i++ ) {
            A.get(i,w,a);
            found.get(w,i,b);

            assertEquals(a.real, b.real, UtilEjml.TEST_F64);
            assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_F64);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                expected.get(i-w,j-w,a);
                found.get(j,i,b);

                assertEquals(a.real, b.real, UtilEjml.TEST_F64);
                assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholderTran_CR64
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , DMatrixRow_C64 A ) {
            CommonOps_CR64.transpose(A, QR);

            super.householder(j);
        }

        public void updateA( int w , double gamma ) {
            this.gamma = gamma;

            super.updateA(w);
        }

        public DMatrixRow_C64 getU(int w ) {
            DMatrixRow_C64 U = new DMatrixRow_C64(numRows-w,1);

            System.arraycopy(QR.data,(w*numRows+w)*2,U.data,0,(numRows-w)*2);
            U.set(0,0,1,0);
            return U;
        }

        public double getGamma() {
            return gamma;
        }
    }
}
