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

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_FDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholder_FDRM extends GenericQrCheck_FDRM {

    Random rand = new Random(0xff);


    @Override
    protected QRDecomposition<FMatrixRMaj> createQRDecomposition() {
        return new QRDecompositionHouseholder_FDRM();
    }

    /**
     * Internall several house holder operations are performed.  This
     * checks to see if the householder operations and the expected result for all the
     * submatrices.
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

        SimpleMatrix A = new SimpleMatrix(width,width, FMatrixRMaj.class );
        RandomMatrices_FDRM.fillUniform(A.getFDRM(),rand);

        qr.householder(w,A.getFDRM());

        SimpleMatrix U = new SimpleMatrix(width,1, true, qr.getU()).extractMatrix(w,width,0,1);

        SimpleMatrix I = SimpleMatrix.identity(width-w, FMatrixRMaj.class);
        SimpleMatrix Q = I.minus(U.mult(U.transpose()).scale(qr.getGamma()));


        // check the expected properties of Q
        assertTrue(Q.isIdentical(Q.transpose(),UtilEjml.TEST_F32_SQ));
        assertTrue(Q.isIdentical(Q.invert(),UtilEjml.TEST_F32_SQ));

        SimpleMatrix result = Q.mult(A.extractMatrix(w,width,w,width));

        assertEquals(-qr.tau,result.get(0,0), UtilEjml.TEST_F32);
        for( int i = 1; i < width-w; i++ ) {
            assertEquals(0,result.get(i,0),UtilEjml.TEST_F32_SQ);
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

        float gamma = 0.2f;
        float tau = 0.75f;

        SimpleMatrix U = new SimpleMatrix(width,1, FMatrixRMaj.class);
        SimpleMatrix A = new SimpleMatrix(width,width, FMatrixRMaj.class);

        RandomMatrices_FDRM.fillUniform(U.getFDRM(),rand);
        RandomMatrices_FDRM.fillUniform(A.getFDRM(),rand);

        qr.getQR().set(A.getFDRM());

        // compute the results using standard matrix operations
        SimpleMatrix I = SimpleMatrix.identity(width-w, FMatrixRMaj.class);

        SimpleMatrix u_sub = U.extractMatrix(w,width,0,1);
        SimpleMatrix A_sub = A.extractMatrix(w,width,w,width);
        SimpleMatrix expected = I.minus(u_sub.mult(u_sub.transpose()).scale(gamma)).mult(A_sub);

        qr.updateA(w,U.getFDRM().getData(),gamma,tau);

        FMatrixRMaj found = qr.getQR();

        assertEquals(-tau,found.get(w,w),UtilEjml.TEST_F32);

        for( int i = w+1; i < width; i++ ) {
            assertEquals(U.get(i,0),found.get(i,w),UtilEjml.TEST_F32);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                float a = (float)expected.get(i-w,j-w);
                float b = found.get(i,j);

                assertEquals(a,b,UtilEjml.TEST_F32_SQ);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholder_FDRM
    {

        public DebugQR(int numRows, int numCols) {
            setExpectedMaxSize(numRows,numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j , FMatrixRMaj A ) {
            this.QR.set(A);

            super.householder(j);
        }

        public void updateA( int w , float u[] , float gamma , float tau ) {
            System.arraycopy(u,0,this.u,0,this.u.length);
            this.gamma = gamma;
            this.tau = tau;

            super.updateA(w);
        }

        public float[] getU() {
            return u;
        }

        public float getGamma() {
            return gamma;
        }
    }
}
