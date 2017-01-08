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
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.ops.CommonOps_CD64;
import org.ejml.ops.MatrixFeatures_CD64;
import org.ejml.ops.RandomMatrices_CD64;
import org.ejml.ops.SpecializedOps_CD64;
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
    protected QRDecomposition<RowMatrix_C64> createQRDecomposition() {
        return new QRDecompositionHouseholderTran_CD64();
    }

    /**
     * Sees if computing Q explicitly and applying Q produces the same results
     */
    @Test
    public void applyQ() {
        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(5, 4, rand);

        QRDecompositionHouseholderTran_CD64 alg = new QRDecompositionHouseholderTran_CD64();

        assertTrue(alg.decompose(A));

        RowMatrix_C64 Q = alg.getQ(null,false);
        RowMatrix_C64 B = RandomMatrices_CD64.createRandom(5,2,rand);

        RowMatrix_C64 expected = new RowMatrix_C64(B.numRows,B.numCols);
        CommonOps_CD64.mult(Q,B,expected);

        alg.applyQ(B);

        assertTrue(MatrixFeatures_CD64.isIdentical(expected,B, UtilEjml.TEST_F64));
    }

    /**
     * Sees if computing Q^H explicitly and applying Q^H produces the same results
     */
    @Test
    public void applyTranQ() {
        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(5,4,rand);

        QRDecompositionHouseholderTran_CD64 alg = new QRDecompositionHouseholderTran_CD64();

        assertTrue(alg.decompose(A));

        RowMatrix_C64 Q = alg.getQ(null,false);
        RowMatrix_C64 B = RandomMatrices_CD64.createRandom(5,2,rand);

        RowMatrix_C64 expected = new RowMatrix_C64(B.numRows,B.numCols);
        CommonOps_CD64.transposeConjugate(Q);
        CommonOps_CD64.mult(Q, B, expected);

        alg.applyTranQ(B);

        assertTrue(MatrixFeatures_CD64.isIdentical(expected,B,UtilEjml.TEST_F64));
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

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(width,width,rand);

        qr.householder(w,A);
        RowMatrix_C64 U = qr.getU(w);

        // Q = I - gamma*u*u'
        RowMatrix_C64 Q = SpecializedOps_CD64.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(MatrixFeatures_CD64.isHermitian(Q, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_CD64.isUnitary(Q, UtilEjml.TEST_F64));

        RowMatrix_C64 result = new RowMatrix_C64(Q.numRows,Q.numCols);
        RowMatrix_C64 Asub = CommonOps_CD64.extract(A, w, width, w, width);
        CommonOps_CD64.mult(Q, Asub, result);

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

        RowMatrix_C64 A = RandomMatrices_CD64.createRandom(width,width,rand);
        CommonOps_CD64.transpose(A, qr.QR);

        // compute the results using standard matrix operations
        RowMatrix_C64 u_sub = CommonOps_CD64.extract(A, w, width, w, w+1);
        RowMatrix_C64 A_sub = CommonOps_CD64.extract(A, w, width, w, width);
        RowMatrix_C64 expected = new RowMatrix_C64(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        u_sub.set(0,0,1,0);
        RowMatrix_C64 Q = SpecializedOps_CD64.householder(u_sub,gamma);

        CommonOps_CD64.mult(Q,A_sub,expected);

        qr.updateA(w,gamma);

        RowMatrix_C64 found = qr.getQR();

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

    private static class DebugQR extends QRDecompositionHouseholderTran_CD64
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , RowMatrix_C64 A ) {
            CommonOps_CD64.transpose(A, QR);

            super.householder(j);
        }

        public void updateA( int w , double gamma ) {
            this.gamma = gamma;

            super.updateA(w);
        }

        public RowMatrix_C64 getU(int w ) {
            RowMatrix_C64 U = new RowMatrix_C64(numRows-w,1);

            System.arraycopy(QR.data,(w*numRows+w)*2,U.data,0,(numRows-w)*2);
            U.set(0,0,1,0);
            return U;
        }

        public double getGamma() {
            return gamma;
        }
    }
}
