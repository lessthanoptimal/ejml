/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decompose.qr;

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.MatrixFeatures_ZDRM;
import org.ejml.dense.row.RandomMatrices_ZDRM;
import org.ejml.dense.row.SpecializedOps_ZDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestQRDecompositionHouseholderColumn_ZDRM extends GenericQrCheck_ZDRM {

    Random rand = new Random(0xff);


    @Override
    protected QRDecomposition<ZMatrixRMaj> createQRDecomposition() {
        return new QRDecompositionHouseholderColumn_ZDRM();
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

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(width,width,rand);

        qr.householder(w,A);

        ZMatrixRMaj U = new ZMatrixRMaj(width-w,1);
        System.arraycopy(qr.dataQR[w],w*2,U.data,0,(width-w)*2);

        // it already wrote over the first element with tau. Make it 1 + 0i again
        U.set(0,0,1,0);

        // Q = I - gamma*u*u'
        ZMatrixRMaj Q = SpecializedOps_ZDRM.householder(U,qr.getGamma());

        // check the expected properties of Q
        assertTrue(MatrixFeatures_ZDRM.isHermitian(Q, UtilEjml.TEST_F64));
        assertTrue(MatrixFeatures_ZDRM.isUnitary(Q, UtilEjml.TEST_F64));

        ZMatrixRMaj result = new ZMatrixRMaj(Q.numRows,Q.numCols);
        ZMatrixRMaj Asub = CommonOps_ZDRM.extract(A, w, width, w, width);
        CommonOps_ZDRM.mult(Q, Asub, result);

        Complex_F64 a = new Complex_F64();
        result.get(0,0,a);
        assertEquals(-qr.tau.real, a.real, UtilEjml.TEST_F64);
        assertEquals(-qr.tau.imaginary,a.imaginary, UtilEjml.TEST_F64);

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

        ZMatrixRMaj A = RandomMatrices_ZDRM.rectangle(width,width,rand);

        qr.convertToColumnMajor(A);

        // compute the results using standard matrix operations
        ZMatrixRMaj u_sub = CommonOps_ZDRM.extract(A, w, width, w, w+1);
        ZMatrixRMaj A_sub = CommonOps_ZDRM.extract(A, w, width, w, width);
        ZMatrixRMaj expected = new ZMatrixRMaj(u_sub.numRows,u_sub.numRows);

        // Q = I - gamma*u*u'
        u_sub.set(0,0,1,0);
        ZMatrixRMaj Q = SpecializedOps_ZDRM.householder(u_sub,gamma);

        CommonOps_ZDRM.mult(Q,A_sub,expected);

        qr.updateA(w,gamma);

        double[][] found = qr.getQR();

        Complex_F64 a = new Complex_F64();
        Complex_F64 b = new Complex_F64();

        for( int i = w; i < width; i++ ) {
            A.get(i,w,a);
            b.setTo(found[w][i*2],found[w][i*2+1]);

            assertEquals(a.real, b.real, UtilEjml.TEST_F64);
            assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_F64);
        }

        // the right should be the same
        for( int i = w; i < width; i++ ) {
            for( int j = w+1; j < width; j++ ) {
                expected.get(i-w,j-w,a);
                b.setTo(found[j][i*2],found[j][i*2+1]);
//                found.get(i,j,b);

                assertEquals(a.real, b.real, UtilEjml.TEST_F64);
                assertEquals(a.imaginary,b.imaginary,UtilEjml.TEST_F64);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholderColumn_ZDRM
    {
        public DebugQR( int numRows , int numCols ) {
            setExpectedMaxSize(numRows,numCols);
            this.numCols = numCols;
            this.numRows = numRows;
        }

        public void householder( int j , ZMatrixRMaj A ) {
            convertToColumnMajor(A);

            super.householder(j);
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