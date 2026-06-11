/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.block;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway.Init"})
class TestHouseholder_DDRB extends EjmlStandardJUnit {
    // the block length
    int r = 3;

    SimpleMatrix A, Y, V, W;

    @Test void computeWCol() {
        double[] betas = new double[]{1.2, 2, 3};

        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);

        // Compute W directly using SimpleMatrix
        SimpleMatrix V = A.extractMatrix(0, A.numRows(), 0, 1);
        V.set(0, 0, 1);
        SimpleMatrix Y = V;
        SimpleMatrix W = V.scale(-betas[0]);

        for (int i = 1; i < A.numCols(); i++) {
            V = A.extractMatrix(0, A.numRows(), i, i + 1);

            for (int j = 0; j < i; j++)
                V.set(j, 0, 0);
            V.set(i, 0, 1);

            SimpleMatrix z = V.plus(W.mult(Y.transpose().mult(V))).scale(-betas[i]);
            W = W.combine(0, i, z);
            Y = Y.combine(0, i, V);
        }

        // now compute it using the block matrix stuff
        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);
        DMatrixRBlock Wb = new DMatrixRBlock(Ab.numRows, Ab.numCols, r);

        DSubmatrixD1 Ab_sub = new DSubmatrixD1(Ab);
        DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb);

        Householder_DDRB.computeWCol(r, Ab_sub, Wb_sub, null, betas, 0);

        // see if the result is the same
        assertTrue(GenericMatrixOps_F64.isEquivalent(Wb, (DMatrixRMaj)W.getMatrix(), UtilEjml.TEST_F64));
    }

    @Test
    public void computeWRow() {

        for (int width = r; width <= 3*r; width++) {
//            System.out.println("width!!!  "+width);
            double betas[] = new double[r];
            for (int i = 0; i < r; i++)
                betas[i] = i + 0.5;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);

            // Compute W directly using SimpleMatrix
            SimpleMatrix v = A.extractVector(true, 0);
            v.set(0, 0);
            v.set(1, 1);
            SimpleMatrix Y = v;
            SimpleMatrix W = v.scale(-betas[0]);

            for (int i = 1; i < A.numRows(); i++) {
                v = A.extractVector(true, i);

                for (int j = 0; j <= i; j++)
                    v.set(j, 0);
                if (i + 1 < A.numCols())
                    v.set(i + 1, 1);

                SimpleMatrix z = v.transpose().plus(W.transpose().mult(Y.mult(v.transpose()))).scale(-betas[i]);

                W = W.combine(i, 0, z.transpose());
                Y = Y.combine(i, 0, v);
            }

            // now compute it using the block matrix stuff
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Wb = new DMatrixRBlock(Ab.numRows, Ab.numCols, r);

            DSubmatrixD1 Ab_sub = new DSubmatrixD1(Ab);
            DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb);

            Householder_DDRB.computeWRow(r, Ab_sub, Wb_sub, betas, 0);

            // see if the result is the same
            assertTrue(GenericMatrixOps_F64.isEquivalent(Wb, W.getDDRM(), UtilEjml.TEST_F64));
        }
    }

    @Test void initializeW() {
        initMatrices(r - 1);

        double beta = 1.5;

        DMatrixRBlock Wb = MatrixOps_DDRB.convert((DMatrixRMaj)W.getMatrix(), r);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        DSubmatrixD1 Wb_sub = new DSubmatrixD1(Wb, 0, W.numRows(), 0, r);
        DSubmatrixD1 Yb_sub = new DSubmatrixD1(Ab, 0, A.numRows(), 0, r);

        Householder_DDRB.initializeW(r, Wb_sub, Yb_sub, beta);

        assertEquals(-beta, Wb.get(0, 0), UtilEjml.TEST_F64);

        for (int i = 1; i < Wb.numRows; i++) {
            assertEquals(-beta*Ab.get(i, 0), Wb.get(i, 0), UtilEjml.TEST_F64);
        }
    }

    @Test void computeZ() {
        int M = r - 1;
        initMatrices(M);

        double beta = 2.5;

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);
        DMatrixRBlock Aw = MatrixOps_DDRB.convert((DMatrixRMaj)W.getMatrix(), r);

        // need to extract only the elements in W that are currently being used when
        // computing the expected Z
        W = W.extractMatrix(0, W.numRows(), 0, M);
        SimpleMatrix T = SimpleMatrix.random_DDRM(M, 1, -1, 1, rand);

        // -beta * (V + W*T)
        SimpleMatrix expected = V.plus(W.mult(T)).scale(-beta);

        Householder_DDRB.computeZ(r, new DSubmatrixD1(Ab, 0, A.numRows(), 0, r),
                new DSubmatrixD1(Aw, 0, A.numRows(), 0, r),
                M, T.getDDRM().data, beta);

        for (int i = 0; i < A.numRows(); i++) {
            assertEquals(expected.get(i), Aw.get(i, M), UtilEjml.TEST_F64);
        }
    }

    @Test void computeY_t_V() {
        int M = r - 2;
        initMatrices(M);

        // Y'*V
        SimpleMatrix expected = Y.transpose().mult(V);

        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
        double[] found = new double[M];

        Householder_DDRB.computeY_t_V(r, new DSubmatrixD1(Ab, 0, A.numRows(), 0, r), M, found);

        for (int i = 0; i < M; i++) {
            assertEquals(expected.get(i), found[i], UtilEjml.TEST_F64);
        }
    }

    @Test
    public void multPlusTransA_symm() {
        for (int width = r + 1; width <= r*3; width++) {
            SimpleMatrix A = SimpleMatrix.random_DDRM(width, width, -1.0, 1.0, rand);
            SimpleMatrix U = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            SimpleMatrix V = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Ub = MatrixOps_DDRB.convert(U.getDDRM(), r);
            DMatrixRBlock Vb = MatrixOps_DDRB.convert(V.getDDRM(), r);

            SimpleMatrix expected = A.plus(U.transpose().mult(V));

            Householder_DDRB.multPlusTransA_symm(r, new DSubmatrixD1(Ub)
                    , new DSubmatrixD1(Vb), new DSubmatrixD1(Ab));


            for (int i = r; i < width; i++) {
                for (int j = i; j < width; j++) {
                    assertEquals(expected.get(i, j), Ab.get(i, j), UtilEjml.TEST_F64, i + " " + j);
                }
            }
        }
    }

    @Test void multPlus_TriLL0() {
        for (int height = r + 1; height <= 3*r; height++) {
            // Y is a column panel (one block wide): top r×r unit-lower-triangular, data below
            SimpleMatrix Y = SimpleMatrix.random_DDRM(height, r, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(r, height, -1.0, 1.0, rand);

            // materialize Y's implicit structure for the oracle (zeros above diagonal, ones on it)
            SimpleMatrix Ym = Y.copy();
            for (int i = 0; i < r; i++) {
                for (int j = i + 1; j < r; j++)
                    Ym.set(i, j, 0);
                Ym.set(i, i, 1);
            }
            SimpleMatrix expected = Ym.mult(B);

            DMatrixRBlock Yb = MatrixOps_DDRB.convert(Y.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);
            DMatrixRBlock Cb = new DMatrixRBlock(height, height, r);

            // C starts at zero, so multPlus computes Y*B; pass the un-materialized Y
            Householder_DDRB.multPlus_TriLL0(r, new DSubmatrixD1(Yb), new DSubmatrixD1(Bb), new DSubmatrixD1(Cb));

            assertTrue(GenericMatrixOps_F64.isEquivalent(Cb, expected.getDDRM(), UtilEjml.TEST_F64), "height " + height);
        }
    }

    @Test void multTransA_TriLL0() {
        for (int height = r + 1; height <= 3*r; height++) {
            SimpleMatrix Y = SimpleMatrix.random_DDRM(height, r, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(height, height, -1.0, 1.0, rand);

            SimpleMatrix Ym = Y.copy();
            for (int i = 0; i < r; i++) {
                for (int j = i + 1; j < r; j++)
                    Ym.set(i, j, 0);
                Ym.set(i, i, 1);
            }
            SimpleMatrix expected = Ym.transpose().mult(B);

            DMatrixRBlock Yb = MatrixOps_DDRB.convert(Y.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);
            DMatrixRBlock Cb = new DMatrixRBlock(r, height, r);

            Householder_DDRB.multTransA_TriLL0(r, new DSubmatrixD1(Yb), new DSubmatrixD1(Bb), new DSubmatrixD1(Cb));

            assertTrue(GenericMatrixOps_F64.isEquivalent(Cb, expected.getDDRM(), UtilEjml.TEST_F64), "height " + height);
        }
    }

    @Test void mult_TriUR1() {
        for (int N = 2; N <= 3*r; N++) {
            int bs = Math.min(r, N);
            // U is a one-block-tall row panel; reflector data lives strictly past the super-diagonal
            SimpleMatrix Araw = SimpleMatrix.random_DDRM(bs, N, -1.0, 1.0, rand);
            SimpleMatrix Q = SimpleMatrix.random_DDRM(N, N, -1.0, 1.0, rand);

            // materialize the implicit structure for the oracle: zeros up to the diagonal, unit super-diagonal
            SimpleMatrix Um = Araw.copy();
            for (int i = 0; i < bs; i++) {
                for (int c = 0; c <= i && c < N; c++)
                    Um.set(i, c, 0);
                if (i + 1 < N)
                    Um.set(i, i + 1, 1);
            }
            SimpleMatrix expected = Um.mult(Q);

            DMatrixRBlock Ub = MatrixOps_DDRB.convert(Araw.getDDRM(), r);
            DMatrixRBlock Qb = MatrixOps_DDRB.convert(Q.getDDRM(), r);
            DMatrixRBlock Cb = new DMatrixRBlock(bs, N, r);

            Householder_DDRB.mult_TriUR1(r, new DSubmatrixD1(Ub), new DSubmatrixD1(Qb), new DSubmatrixD1(Cb));

            assertTrue(GenericMatrixOps_F64.isEquivalent(Cb, expected.getDDRM(), UtilEjml.TEST_F64), "N " + N);
        }
    }

    @Test void multTransB_TriUR1() {
        for (int N = 2; N <= 3*r; N++) {
            int bs = Math.min(r, N);
            SimpleMatrix Araw = SimpleMatrix.random_DDRM(bs, N, -1.0, 1.0, rand);
            SimpleMatrix Q = SimpleMatrix.random_DDRM(N, N, -1.0, 1.0, rand);

            SimpleMatrix Um = Araw.copy();
            for (int i = 0; i < bs; i++) {
                for (int c = 0; c <= i && c < N; c++)
                    Um.set(i, c, 0);
                if (i + 1 < N)
                    Um.set(i, i + 1, 1);
            }
            SimpleMatrix expected = Q.mult(Um.transpose());

            DMatrixRBlock Ub = MatrixOps_DDRB.convert(Araw.getDDRM(), r);
            DMatrixRBlock Qb = MatrixOps_DDRB.convert(Q.getDDRM(), r);
            DMatrixRBlock Cb = new DMatrixRBlock(N, bs, r);

            Householder_DDRB.multTransB_TriUR1(r, new DSubmatrixD1(Qb), new DSubmatrixD1(Ub), new DSubmatrixD1(Cb));

            assertTrue(GenericMatrixOps_F64.isEquivalent(Cb, expected.getDDRM(), UtilEjml.TEST_F64), "N " + N);
        }
    }

    @Test void computeHouseholderCol() {
        // generate a reflector for an interior column, then verify it zeros that column below the pivot
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(r*2 + r - 1, r, -1, 1, rand);
        DMatrixRBlock Mb = MatrixOps_DDRB.convert(M, r);
        int col = 1;
        int n = M.numRows;
        double[] gamma = new double[r];

        // original column before the reflector overwrites it
        SimpleMatrix x = SimpleMatrix.wrap(CommonOps_DDRM.extract(M, 0, n, col, col + 1));

        assertTrue(Householder_DDRB.computeHouseholderCol(r, new DSubmatrixD1(Mb), gamma, col));

        // reflector u: implicit zeros above the pivot, implicit one on the diagonal, stored tail below
        SimpleMatrix u = new SimpleMatrix(n, 1);
        u.set(col, 0, 1);
        for (int k = col + 1; k < n; k++)
            u.set(k, 0, Mb.get(k, col));

        // applying (I - gamma*u*u^T) to the original column must zero everything below the pivot
        SimpleMatrix xnew = x.minus(u.scale(gamma[col]*u.transpose().mult(x).get(0)));
        for (int k = col + 1; k < n; k++)
            assertEquals(0, xnew.get(k, 0), UtilEjml.TEST_F64, "row " + k);
        assertEquals(Mb.get(col, col), xnew.get(col, 0), UtilEjml.TEST_F64);
    }

    @Test void computeHouseholderRow() {
        // generate a reflector for an interior row, then verify it zeros that row past the super-diagonal
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(r, r*2 + r - 1, -1, 1, rand);
        DMatrixRBlock Mb = MatrixOps_DDRB.convert(M, r);
        int row = 1;
        int n = M.numCols;
        double[] gamma = new double[r];

        SimpleMatrix x = SimpleMatrix.wrap(CommonOps_DDRM.extract(M, row, row + 1, 0, n)).transpose();

        assertTrue(Householder_DDRB.computeHouseholderRow(r, new DSubmatrixD1(Mb), gamma, row));

        // reflector u: implicit zeros up to the super-diagonal, implicit one at column row+1, tail after
        SimpleMatrix u = new SimpleMatrix(n, 1);
        u.set(row + 1, 0, 1);
        for (int j = row + 2; j < n; j++)
            u.set(j, 0, Mb.get(row, j));

        SimpleMatrix xnew = x.minus(u.scale(gamma[row]*u.transpose().mult(x).get(0)));
        for (int j = row + 2; j < n; j++)
            assertEquals(0, xnew.get(j, 0), UtilEjml.TEST_F64, "col " + j);
        assertEquals(Mb.get(row, row + 1), xnew.get(row + 1, 0), UtilEjml.TEST_F64);
    }

    @Test void innerProdRow_symm() {
        int n = r*2 + r - 1;
        DMatrixRMaj Araw = RandomMatrices_DDRM.rectangle(n, n, -1, 1, rand);
        // B is NOT symmetric: its lower triangle holds garbage the op must ignore (it reads the
        // upper triangle symmetrically). The oracle uses the symmetric matrix built from B's upper triangle.
        DMatrixRMaj Braw = RandomMatrices_DDRM.rectangle(n, n, -1, 1, rand);
        DMatrixRMaj Bsym = Braw.copy();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < i; j++)
                Bsym.set(i, j, Braw.get(j, i));

        int rowA = 0, rowB = 3, zeroOffset = 1; // offset = 1 < rowB -> exercises the symmetric branch

        // reflector row a: implicit zeros before the leading one at rowA+zeroOffset, stored tail after
        DMatrixRMaj a = CommonOps_DDRM.extract(Araw, rowA, rowA + 1, 0, n);
        for (int j = 0; j < rowA + zeroOffset; j++)
            a.set(j, 0);
        a.set(rowA + zeroOffset, 1);
        DMatrixRMaj b = CommonOps_DDRM.extract(Bsym, rowB, rowB + 1, 0, n);
        double expected = VectorVectorMult_DDRM.innerProd(a, b);

        DMatrixRBlock Ab = MatrixOps_DDRB.convert(Araw, r);
        DMatrixRBlock Bb = MatrixOps_DDRB.convert(Braw, r); // pass the un-symmetrized B
        double found = Householder_DDRB.innerProdRow_symm(r,
                new DSubmatrixD1(Ab), rowA, new DSubmatrixD1(Bb), rowB, zeroOffset);

        assertEquals(expected, found, UtilEjml.TEST_F64);
    }

    @Test void rank1UpdateMultR_Col() {
        // check various sized matrices
        double gamma = 2.5;
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r*2 - 1, -1, 1, rand);

        SimpleMatrix U = A.extractMatrix(0, A.numRows(), 1, 2);
        U.set(0, 0, 0);
        U.set(1, 0, 1);

        SimpleMatrix V = A.extractMatrix(0, A.numRows(), 2, 3);
        SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        Householder_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(Ab), 1, gamma);

        for (int i = 1; i < expected.numRows(); i++) {
            assertEquals(expected.get(i, 0), Ab.get(i, 2), UtilEjml.TEST_F64);
        }
    }

    @Test void rank1UpdateMultR_TopRow() {
        double gamma = 2.5;
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r*2 - 1, -1.0, 1.0, rand);

        SimpleMatrix U = A.extractMatrix(0, A.numRows(), 1, 2);
        U.set(0, 0, 0);
        U.set(1, 0, 1);

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        Householder_DDRB.rank1UpdateMultR_TopRow(r, new DSubmatrixD1(Ab), 1, gamma);

        // check all the columns now
        for (int i = 0; i < r; i++) {
            for (int j = r; j < A.numCols(); j++) {
                SimpleMatrix V = A.extractMatrix(0, A.numRows(), j, j + 1);
                SimpleMatrix expected = V.minus(U.mult(U.transpose().mult(V)).scale(gamma));

                assertEquals(expected.get(i, 0), Ab.get(i, j), UtilEjml.TEST_F64, i + " " + j);
            }
        }
    }

    @Test void rank1UpdateMultL_Row() {
        double gamma = 2.5;
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r*2 + r - 1, -1.0, 1.0, rand);

        SimpleMatrix U = A.extractMatrix(1, 2, 0, A.numCols()).transpose();
        U.set(0, 0);
        U.set(1, 1);

        SimpleMatrix expected = A.minus(A.mult(U).mult(U.transpose()).scale(gamma));

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        Householder_DDRB.rank1UpdateMultL_Row(r, new DSubmatrixD1(Ab), 1, 1, gamma);

        for (int j = 1; j < expected.numCols(); j++) {
            assertEquals(expected.get(2, j), Ab.get(2, j), UtilEjml.TEST_F64);
        }
    }

    /**
     * Check inner product when column blocks have two different widths
     */
    @Test void innerProdCol() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(r*2 + r - 1, r*3 - 1, -1, 1, rand);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A, r);

        int row = 0;
        int innerCol = 1;
        for (int colBlock = 0; colBlock < r*2; colBlock += r) {
            int colA = colBlock + innerCol;
            int colB = colA + innerCol + 1;
            int widthA = Math.min(r, A.numCols - (colA - colA%r));
            int widthB = Math.min(r, A.numCols - (colB - colB%r));

            DMatrixRMaj v0 = CommonOps_DDRM.extract(A, row, A.numRows, colA, colA + 1);
            DMatrixRMaj v1 = CommonOps_DDRM.extract(A, row, A.numRows, colB, colB + 1);
            for (int j = 0; j < innerCol; j++) {
                v0.set(j, 0.0);
            }
            v0.set(innerCol, 1.0);

            double expected = VectorVectorMult_DDRM.innerProd(v0, v1);

            DSubmatrixD1 subAb = new DSubmatrixD1(Ab, row, A.numRows, colBlock, A.numCols);

            double found = Householder_DDRB.innerProdCol(r, subAb, colA - colBlock, widthA, colB - colBlock, widthB);

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }

    @Test void innerProdRow() {
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(r*3 - 1, r*2 + r - 1, -1, 1, rand);
        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A, r);

        int zeroOffset = 1;
        for (int rowBlock = 0; rowBlock < r*2; rowBlock += r) {
            int rowA = 2;
            int rowB = 1;

            DMatrixRMaj v0 = CommonOps_DDRM.extract(A, rowBlock + rowA, rowBlock + rowA + 1, 0, A.numCols);
            DMatrixRMaj v1 = CommonOps_DDRM.extract(A, rowBlock + rowB, rowBlock + rowB + 1, 0, A.numCols);
            for (int j = 0; j < rowA + zeroOffset; j++) {
                v0.set(j, 0.0);
            }
            v0.set(rowA + zeroOffset, 1.0);

            double expected = VectorVectorMult_DDRM.innerProd(v0, v1);

            DSubmatrixD1 subAb = new DSubmatrixD1(Ab, rowBlock, A.numRows, 0, A.numCols);

            double found = Householder_DDRB.innerProdRow(r, subAb, rowA, subAb, rowB, zeroOffset);

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }

    @Test void divideElementsCol() {
        double div = 1.5;
        int col = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        Householder_DDRB.divideElementsCol(r, new DSubmatrixD1(A), col, div);

        for (int i = col + 1; i < A.numRows; i++) {
            assertEquals(A_orig.get(i, col)/div, A.get(i, col), UtilEjml.TEST_F64);
        }
    }

    @Test void scaleRow() {

        double div = 1.5;
        int row = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 + 1, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        Householder_DDRB.scaleRow(r, new DSubmatrixD1(A), new DSubmatrixD1(A), row, 1, div);

        // check the one
        assertEquals(div, A.get(row, row + 1), UtilEjml.TEST_F64);
        // check the rest
        for (int i = row + 2; i < A.numCols; i++) {
            assertEquals(A_orig.get(row, i)*div, A.get(row, i), UtilEjml.TEST_F64);
        }
    }

    @Test void addRow() {
        int rowA = 0;
        int rowB = 1;
        int rowC = 2;

        double alpha = 1.5;
        double beta = -0.7;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert((DMatrixRMaj)B.getMatrix(), r);
            DMatrixRBlock Cb = Ab.copy();

            // turn A into householder vectors
            for (int i = 0; i < A.numRows(); i++) {
                for (int j = 0; j <= i; j++) {
                    if (A.isInBounds(i, j))
                        A.set(i, j, 0);
                }
                if (A.isInBounds(i, i + 1))
                    A.set(i, i + 1, 1);
            }

            SimpleMatrix a = A.extractVector(true, rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true, rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            Householder_DDRB.addRow(r,
                    new DSubmatrixD1(Ab), rowA, alpha,
                    new DSubmatrixD1(Bb), rowB, beta,
                    new DSubmatrixD1(Cb), rowC, 1, end);

            // skip over the zeros
            for (int j = rowA + 1; j < end; j++) {
                assertEquals(c.get(j), Cb.get(rowC, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test void computeTauAndDivideCol() {

        double max = 1.5;
        int col = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        // manual dense
        double expected = 0;
        for (int i = col; i < A.numRows; i++) {
            double val = A.get(i, col)/max;
            expected += val*val;
        }
        expected = Math.sqrt(expected);
        if (A.get(col, col) < 0)
            expected *= -1;

        double found = Householder_DDRB.computeTauAndDivideCol(r, new DSubmatrixD1(A), col, max);

        assertEquals(expected, found, UtilEjml.TEST_F64);

        for (int i = col; i < A.numRows; i++) {
            assertEquals(A_orig.get(i, col)/max, A.get(i, col), UtilEjml.TEST_F64);
        }
    }

    @Test void computeTauAndDivideRow() {
        double max = 1.5;
        int row = 1;
        int colStart = row + 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 + 1, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        // manual dense
        double expected = 0;
        for (int j = colStart; j < A.numCols; j++) {
            double val = A.get(row, j)/max;
            expected += val*val;
        }
        expected = Math.sqrt(expected);
        if (A.get(row, colStart) < 0)
            expected *= -1;

        double found = Householder_DDRB.computeTauAndDivideRow(r, new DSubmatrixD1(A), row, colStart, max);

        assertEquals(expected, found, UtilEjml.TEST_F64);

        for (int j = colStart; j < A.numCols; j++) {
            assertEquals(A_orig.get(row, j)/max, A.get(row, j), UtilEjml.TEST_F64);
        }
    }

    @Test void testFindMaxCol() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);

        // make sure it ignores the first element
        A.set(0, 1, 100000);
        A.set(5, 1, -2346);

        double max = Householder_DDRB.findMaxCol(r, new DSubmatrixD1(A), 1);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }

    @Test void testFindMaxRow() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 - 1, -1, 1, rand, r);

        // make sure it ignores the first element
        A.set(1, 1, 100000);
        A.set(1, 4, -2346);

        double max = Householder_DDRB.findMaxRow(r, new DSubmatrixD1(A), 1, 2);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }

    private void initMatrices( int M ) {
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);

        // create matrices that are used to test
        Y = A.extractMatrix(0, A.numRows(), 0, M);
        V = A.extractMatrix(0, A.numRows(), M, M + 1);

        // add in zeros and ones
        setZerosY();
        for (int i = 0; i < M; i++) {
            V.set(i, 0);
        }
        V.set(M, 1);

        W = SimpleMatrix.random_DDRM(r*2 + r - 1, r, -1.0, 1.0, rand);
    }

    private void setZerosY() {
        for (int j = 0; j < Y.numCols(); j++) {
            for (int i = 0; i < j; i++) {
                Y.set(i, j, 0);
            }
            Y.set(j, j, 1);
        }
    }
}
