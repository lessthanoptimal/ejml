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

package org.ejml.dense.row.decomposition.svd.implicitqr;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.decomposition.bidiagonal.BidiagonalDecompositionRow_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSvdImplicitQrAlgorithm extends EjmlStandardJUnit {
    /**
     * Computes the singular values of a bidiagonal matrix that is all ones.
     * From exercise 5.9.45 in Fundamentals of Matrix Computations.
     */
    @Test void oneBidiagonalMatrix() {
        SvdImplicitQrAlgorithm_DDRM svd = new SvdImplicitQrAlgorithm_DDRM(true);
        for (int N = 5; N < 10; N++) {
            double[] diag = new double[N];
            double[] off = new double[N - 1];
            diag[0] = 1;
            for (int i = 0; i < N - 1; i++) {
                diag[i + 1] = 1;
                off[i] = 1;
            }

            svd.setMatrix(N, N, diag, off);

            assertTrue(svd.process());

            for (int i = 0; i < N; i++) {
                double val = 2.0*Math.cos((i + 1)*Math.PI/(2.0*N + 1.0));

                assertEquals(1, countNumFound(svd, val, UtilEjml.TEST_F64));
            }
        }
    }

    /**
     * A trivial case where all the elements are diagonal. It should do nothing here.
     */
    @Test void knownDiagonal() {
        double[] diag = new double[]{1, 2, 3, 4, 5};
        double[] off = new double[diag.length - 1];

        SvdImplicitQrAlgorithm_DDRM svd = new SvdImplicitQrAlgorithm_DDRM(true);
        svd.setMatrix(diag.length, diag.length, diag, off);

        assertTrue(svd.process());

        assertEquals(1, countNumFound(svd, 5, UtilEjml.TEST_F64));
        assertEquals(1, countNumFound(svd, 4, UtilEjml.TEST_F64));
        assertEquals(1, countNumFound(svd, 3, UtilEjml.TEST_F64));
        assertEquals(1, countNumFound(svd, 2, UtilEjml.TEST_F64));
        assertEquals(1, countNumFound(svd, 1, UtilEjml.TEST_F64));
    }

    /**
     * Sees if it handles the case where there is a zero on the diagonal
     */
    @Test void zeroOnDiagonal() {
        double[] diag = new double[]{1, 2, 3, 4, 5, 6};
        double[] off = new double[]{2, 2, 2, 2, 2};

        diag[2] = 0;

//        A.print();

        SvdImplicitQrAlgorithm_DDRM svd = new SvdImplicitQrAlgorithm_DDRM(false);
        svd.setMatrix(6, 6, diag, off);

        assertTrue(svd.process());

        assertEquals(1, countNumFound(svd, 6.82550, 1e-4));
        assertEquals(1, countNumFound(svd, 5.31496, 1e-4));
        assertEquals(1, countNumFound(svd, 3.76347, 1e-4));
        assertEquals(1, countNumFound(svd, 3.28207, 1e-4));
        assertEquals(1, countNumFound(svd, 1.49265, 1e-4));
        assertEquals(1, countNumFound(svd, 0.00000, 1e-4));
    }

    @Test void knownCaseSquare() {
        DMatrixRMaj A = UtilEjml.parse_DDRM("-3   1   3  -3   0\n" +
                "   2  -4   0  -2   0\n" +
                "   1  -4   4   1  -3\n" +
                "  -1  -3   2   2  -4\n" +
                "  -5   3   1   3   1", 5);

//        A.print();

        SvdImplicitQrAlgorithm_DDRM svd = createHelper(A);

        assertTrue(svd.process());

        assertEquals(1, countNumFound(svd, 9.3431, 1e-3));
        assertEquals(1, countNumFound(svd, 7.4856, 1e-3));
        assertEquals(1, countNumFound(svd, 4.9653, 1e-3));
        assertEquals(1, countNumFound(svd, 1.8178, 1e-3));
        assertEquals(1, countNumFound(svd, 1.6475, 1e-3));
    }

    /**
     * This makes sure the U and V matrices are being correctly by the push code.
     */
    @Test void zeroOnDiagonalFull() {
        for (int where = 0; where < 6; where++) {
            double[] diag = new double[]{1, 2, 3, 4, 5, 6};
            double[] off = new double[]{2, 2, 2, 2, 2};

            diag[where] = 0;

            checkFullDecomposition(6, diag, off);
        }
    }

    /**
     * Decomposes a random matrix and see if the decomposition can reconstruct the original
     */
    @Test void randomMatricesFullDecompose() {

        for (int N = 2; N <= 20; N++) {
            double[] diag = new double[N];
            double[] off = new double[N];

            diag[0] = rand.nextDouble();
            for (int i = 1; i < N; i++) {
                diag[i] = rand.nextDouble();
                off[i - 1] = rand.nextDouble();
            }

            checkFullDecomposition(N, diag, off);
        }
    }

    /**
     * Checks the full decomposing my multiplying the components together and seeing if it
     * gets the original matrix again.
     */
    private void checkFullDecomposition( int n, double[] diag, double[] off ) {
//        a.print();

        SvdImplicitQrAlgorithm_DDRM svd = createHelper(n, n, diag.clone(), off.clone());
        svd.setFastValues(true);
        assertTrue(svd.process());

//        System.out.println("Value total steps = "+svd.totalSteps);

        svd.setFastValues(false);
        double[] values = svd.diag.clone();
        svd.setMatrix(n, n, diag.clone(), off.clone());
        svd.setUt(CommonOps_DDRM.identity(n));
        svd.setVt(CommonOps_DDRM.identity(n));
        assertTrue(svd.process(values));

//        System.out.println("Vector total steps = "+svd.totalSteps);

        SimpleMatrix Ut = SimpleMatrix.wrap(Objects.requireNonNull(svd.getUt()));
        SimpleMatrix Vt = SimpleMatrix.wrap(Objects.requireNonNull(svd.getVt()));
        SimpleMatrix W = SimpleMatrix.diag(svd.diag);
//
//            Ut.mult(W).mult(V).print();
        SimpleMatrix A_found = Ut.transpose().mult(W).mult(Vt);
//            A_found.print();

        assertEquals(diag[0], A_found.get(0, 0), UtilEjml.TEST_F64);
        for (int i = 0; i < n - 1; i++) {
            assertEquals(diag[i + 1], A_found.get(i + 1, i + 1), UtilEjml.TEST_F64);
            assertEquals(off[i], A_found.get(i, i + 1), UtilEjml.TEST_F64);
        }
    }

    public static SvdImplicitQrAlgorithm_DDRM createHelper( DMatrixRMaj a ) {
        BidiagonalDecompositionRow_DDRM bidiag = new BidiagonalDecompositionRow_DDRM();
        assertTrue(bidiag.decompose(a.copy()));
        double[] diag = new double[a.numRows];
        double[] off = new double[diag.length - 1];
        bidiag.getDiagonal(diag, off);

        return createHelper(a.numRows, a.numCols, diag, off);
    }

    public static SvdImplicitQrAlgorithm_DDRM createHelper( int numRows, int numCols,
                                                            double[] diag, double[] off ) {

        SvdImplicitQrAlgorithm_DDRM helper = new SvdImplicitQrAlgorithm_DDRM();

        helper.setMatrix(numRows, numCols, diag, off);
        return helper;
    }

    /**
     * Counts the number of times the specified eigenvalue appears.
     */
    public int countNumFound( SvdImplicitQrAlgorithm_DDRM alg, double val, double tol ) {
        int total = 0;

        for (int i = 0; i < alg.getNumberOfSingularValues(); i++) {
            double a = Math.abs(alg.getSingularValue(i));

            if (Math.abs(a - val) <= tol) {
                total++;
            }
        }

        return total;
    }
}
