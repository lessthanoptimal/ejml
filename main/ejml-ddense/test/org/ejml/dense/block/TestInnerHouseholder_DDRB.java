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
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"NullAway.Init"})
class TestInnerHouseholder_DDRB extends EjmlStandardJUnit {
    // the block length
    int r = 3;

    SimpleMatrix A;

    @Test void computeHouseholderCol() {
        // generate a reflector for an interior column, then verify it zeros that column below the pivot
        DMatrixRMaj M = RandomMatrices_DDRM.rectangle(r*2 + r - 1, r, -1, 1, rand);
        DMatrixRBlock Mb = MatrixOps_DDRB.convert(M, r);
        int col = 1;
        int n = M.numRows;
        double[] gamma = new double[r];

        // original column before the reflector overwrites it
        SimpleMatrix x = SimpleMatrix.wrap(CommonOps_DDRM.extract(M, 0, n, col, col + 1));

        assertTrue(InnerHouseholder_DDRB.computeHouseholderCol(r, new DSubmatrixD1(Mb), gamma, col));

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

        assertTrue(InnerHouseholder_DDRB.computeHouseholderRow(r, new DSubmatrixD1(Mb), gamma, row));

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
        double found = InnerHouseholder_DDRB.innerProdRow_symm(r,
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

        InnerHouseholder_DDRB.rank1UpdateMultR_Col(r, new DSubmatrixD1(Ab), 1, gamma);

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

        InnerHouseholder_DDRB.rank1UpdateMultR_TopRow(r, new DSubmatrixD1(Ab), 1, gamma);

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

        InnerHouseholder_DDRB.rank1UpdateMultL_Row(r, new DSubmatrixD1(Ab), 1, 1, gamma);

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

            double found = InnerHouseholder_DDRB.innerProdCol(r, subAb, colA - colBlock, widthA, colB - colBlock, widthB);

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

            double found = InnerHouseholder_DDRB.innerProdRow(r, subAb, rowA, subAb, rowB, zeroOffset);

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }

    @Test void divideElementsCol() {
        double div = 1.5;
        int col = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        InnerHouseholder_DDRB.divideElementsCol(r, new DSubmatrixD1(A), col, div);

        for (int i = col + 1; i < A.numRows; i++) {
            assertEquals(A_orig.get(i, col)/div, A.get(i, col), UtilEjml.TEST_F64);
        }
    }

    @Test void scaleRow() {

        double div = 1.5;
        int row = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 + 1, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        InnerHouseholder_DDRB.scaleRow(r, new DSubmatrixD1(A), new DSubmatrixD1(A), row, 1, div);

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

            InnerHouseholder_DDRB.addRow(r,
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

        double found = InnerHouseholder_DDRB.computeTauAndDivideCol(r, new DSubmatrixD1(A), col, max);

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

        double found = InnerHouseholder_DDRB.computeTauAndDivideRow(r, new DSubmatrixD1(A), row, colStart, max);

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

        double max = InnerHouseholder_DDRB.findMaxCol(r, new DSubmatrixD1(A), 1);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }

    @Test void testFindMaxRow() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 - 1, -1, 1, rand, r);

        // make sure it ignores the first element
        A.set(1, 1, 100000);
        A.set(1, 4, -2346);

        double max = InnerHouseholder_DDRB.findMaxRow(r, new DSubmatrixD1(A), 1, 2);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }
}
