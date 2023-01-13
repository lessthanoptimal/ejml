/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.interfaces.decomposition.QRDecomposition;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQRDecompositionHouseholder_DDRM extends GenericQrCheck_DDRM {
    @Override
    protected QRDecomposition<DMatrixRMaj> createQRDecomposition() {
        return new QRDecompositionHouseholder_DDRM();
    }

    /**
     * Internal several householder operations are performed. This
     * checks to see if the householder operations and the expected result for all the
     * submatrices.
     */
    @Test void householder() {
        int width = 5;

        for (int i = 0; i < width; i++) {
            checkSubHouse(i, width);
        }
    }

    private void checkSubHouse( int w, int width ) {
        DebugQR qr = new DebugQR(width, width);

        SimpleMatrix A = new SimpleMatrix(width, width, DMatrixRMaj.class);
        RandomMatrices_DDRM.fillUniform(A.getDDRM(), rand);

        qr.householder(w, A.getDDRM());

        SimpleMatrix U = new SimpleMatrix(width, 1, true, qr.getU()).extractMatrix(w, width, 0, 1);

        SimpleMatrix I = SimpleMatrix.identity(width - w, DMatrixRMaj.class);
        SimpleMatrix Q = I.minus(U.mult(U.transpose()).scale(qr.getGamma()));


        // check the expected properties of Q
        assertTrue(Q.isIdentical(Q.transpose(), UtilEjml.TEST_F64_SQ));
        assertTrue(Q.isIdentical(Q.invert(), UtilEjml.TEST_F64_SQ));

        SimpleMatrix result = Q.mult(A.extractMatrix(w, width, w, width));

        assertEquals(-qr.tau, result.get(0, 0), UtilEjml.TEST_F64);
        for (int i = 1; i < width - w; i++) {
            assertEquals(0, result.get(i, 0), UtilEjml.TEST_F64_SQ);
        }
    }

    /**
     * Check the results of this function against basic matrix operations
     * which are equivalent.
     */
    @Test void updateA() {
        int width = 5;

        for (int i = 0; i < width; i++)
            checkSubMatrix(width, i);
    }

    private void checkSubMatrix( int width, int w ) {
        DebugQR qr = new DebugQR(width, width);

        double gamma = 0.2;
        double tau = 0.75;

        SimpleMatrix U = new SimpleMatrix(width, 1, DMatrixRMaj.class);
        SimpleMatrix A = new SimpleMatrix(width, width, DMatrixRMaj.class);

        RandomMatrices_DDRM.fillUniform(U.getDDRM(), rand);
        RandomMatrices_DDRM.fillUniform(A.getDDRM(), rand);

        qr.getQR().setTo(A.getDDRM());

        // compute the results using standard matrix operations
        SimpleMatrix I = SimpleMatrix.identity(width - w, DMatrixRMaj.class);

        SimpleMatrix u_sub = U.extractMatrix(w, width, 0, 1);
        SimpleMatrix A_sub = A.extractMatrix(w, width, w, width);
        SimpleMatrix expected = I.minus(u_sub.mult(u_sub.transpose()).scale(gamma)).mult(A_sub);

        qr.updateA(w, U.getDDRM().getData(), gamma, tau);

        DMatrixRMaj found = qr.getQR();

        assertEquals(-tau, found.get(w, w), UtilEjml.TEST_F64);

        for (int i = w + 1; i < width; i++) {
            assertEquals(U.get(i, 0), found.get(i, w), UtilEjml.TEST_F64);
        }

        // the right should be the same
        for (int i = w; i < width; i++) {
            for (int j = w + 1; j < width; j++) {
                double a = (double)expected.get(i - w, j - w);
                double b = found.get(i, j);

                assertEquals(a, b, UtilEjml.TEST_F64_SQ);
            }
        }
    }

    private static class DebugQR extends QRDecompositionHouseholder_DDRM {

        public DebugQR( int numRows, int numCols ) {
            setExpectedMaxSize(numRows, numCols);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void householder( int j, DMatrixRMaj A ) {
            this.QR.setTo(A);

            super.householder(j);
        }

        public void updateA( int w, double u[], double gamma, double tau ) {
            System.arraycopy(u, 0, this.u, 0, this.u.length);
            this.gamma = gamma;
            this.tau = tau;

            super.updateA(w);
        }

        public double[] getU() {
            return u;
        }

        public double getGamma() {
            return gamma;
        }
    }
}
