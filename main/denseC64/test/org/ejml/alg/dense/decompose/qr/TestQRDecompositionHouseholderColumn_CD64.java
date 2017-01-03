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
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.Complex64F;
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
public class TestQRDecompositionHouseholderColumn_CD64 extends GenericQrCheck_CD64 {

    Random rand = new Random(0xff);


    @Override
    protected QRDecomposition<CDenseMatrix64F> createQRDecomposition() {
        return new QRDecompositionHouseholderColumn_CD64();
    }

    /**
     * Internal several householder operations are performed.  This
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

        CDenseMatrix64F A = RandomMatrices_CD64.createRandom(width,width,rand);

        qr.householder(w,A);

        CDenseMatrix64F U = new CDenseMatrix64F(width-w,1);
        System.arraycopy(qr.dataQR[w],w*2,U.data,0,(width-w)*2);

        // it already wrote over the first element with tau. Make it 1 + 0i again
        U.set(0,0,1,0);

        // Q = I - gamma*u*u'
        CDenseMatrix64F Q = SpecializedOps_CD64.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(MatrixFeatures_CD64.isHermitian(Q, UtilEjml.TEST_64F));
        assertTrue(MatrixFeatures_CD64.isUnitary(Q, UtilEjml.TEST_64F));

        CDenseMatrix64F result = new CDenseMatrix64F(Q.numRows,Q.numCols);
        CDenseMatrix64F Asub = CommonOps_CD64.extract(A, w, width, w, width);
        CommonOps_CD64.mult(Q, Asub, result);

        Complex64F a = new Complex64F();
        result.get(0,0,a);
        assertEquals(-qr.tau.real, a.real, UtilEjml.TEST_64F);
        assertEquals(-qr.tau.imaginary,a.imaginary, UtilEjml.TEST_64F);

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

        CDenseMatrix64F A = RandomMatrices_CD64.createRandom(width,width,rand);

        qr.convertToColumnMajor(A);

        // compute the results using standard matrix operations
        CDenseMatrix64F u_sub = CommonOps_CD64.extract(A, w, width, w, w+1);
        CDenseMatrix64F A_sub = CommonOps_CD64.extract(A, w, width, w, width);
        CDenseMatrix64F expected = new CDenseMatrix64F(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        u_sub.set(0,0,1,0);
        CDenseMatrix64F Q = SpecializedOps_CD64.householder(u_sub,gamma);

        CommonOps_CD64.mult(Q,A_sub,expected);

        qr.updateA(w,gamma);

        double[][] found = qr.getQR();

        Complex64F a = new Complex64F();
        Complex64F b = new Complex64F();

        for( int i = w; i < width; i++ ) {
            A.get(i,w,a);
            b.set(found[w][i*2],found[w][i*2+1]);

            assertEquals(a.real, b.real, UtilEjml.TEST_64F);
            assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_64F);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                expected.get(i-w,j-w,a);
                b.set(found[j][i*2],found[j][i*2+1]);
//                found.get(i,j,b);

                assertEquals(a.real, b.real, UtilEjml.TEST_64F);
                assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_64F);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholderColumn_CD64
    {

        public DebugQR( int numRows , int numCols ) {
            setExpectedMaxSize(numRows,numCols);
            this.numCols = numCols;
            this.numRows = numRows;
        }

        public void householder( int j , CDenseMatrix64F A ) {
            convertToColumnMajor(A);

            super.householder(j);
        }

        protected void convertToColumnMajor(CDenseMatrix64F A) {
            super.convertToColumnMajor(A);
        }

        public void updateA( int w , double gamma ) {
            this.gamma = gamma;

            super.updateA(w);
        }

        public double getGamma() {
            return gamma;
        }
    }
}