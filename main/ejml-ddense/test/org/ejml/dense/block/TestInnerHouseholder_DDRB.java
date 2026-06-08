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

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway.Init"})
class TestInnerHouseholder_DDRB extends EjmlStandardJUnit {
    // the block length
    int r = 3;

    SimpleMatrix A;

    @Test
    void rank1UpdateMultR_Col() {
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

    @Test
    void rank1UpdateMultR_TopRow() {
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

    @Test
    void rank1UpdateMultL_Row() {
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

    @Test
    void rank1UpdateMultL_LeftCol() {
        double gamma = 2.5;
        A = SimpleMatrix.random_DDRM(r*2 + r - 1, r*2 + r - 1, -1.0, 1.0, rand);

        int row = 0;
        int zeroOffset = 1;
        SimpleMatrix U = A.extractMatrix(row, row + 1, 0, A.numCols()).transpose();
        for (int i = 0; i < row + zeroOffset; i++)
            U.set(i, 0);
        U.set(row + zeroOffset, 1);

        SimpleMatrix expected = A.minus(A.mult(U).mult(U.transpose()).scale(gamma));

        DMatrixRBlock Ab = MatrixOps_DDRB.convert((DMatrixRMaj)A.getMatrix(), r);

        InnerHouseholder_DDRB.rank1UpdateMultL_LeftCol(r, new DSubmatrixD1(Ab), row, gamma, zeroOffset);

        for (int i = r; i < A.numRows(); i++) {
            for (int j = 0; j < r; j++) {
                assertEquals(expected.get(i, j), Ab.get(i, j), UtilEjml.TEST_F64);
            }
        }
    }

    /**
     * Check inner product when column blocks have two different widths
     */
    @Test
    void innerProdCol() {
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

    @Test
    void innerProdRow() {
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

    @Test
    void divideElementsCol() {

        double div = 1.5;
        int col = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        InnerHouseholder_DDRB.divideElementsCol(r, new DSubmatrixD1(A), col, div);

        for (int i = col + 1; i < A.numRows; i++) {
            assertEquals(A_orig.get(i, col)/div, A.get(i, col), UtilEjml.TEST_F64);
        }
    }

    @Test
    void scale_row() {

        double div = 1.5;
        int row = 1;
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 + 1, -1, 1, rand, r);
        DMatrixRBlock A_orig = A.copy();

        InnerHouseholder_DDRB.scale_row(r, new DSubmatrixD1(A), new DSubmatrixD1(A), row, 1, div);

        // check the one
        assertEquals(div, A.get(row, row + 1), UtilEjml.TEST_F64);
        // check the rest
        for (int i = row + 2; i < A.numCols; i++) {
            assertEquals(A_orig.get(row, i)*div, A.get(row, i), UtilEjml.TEST_F64);
        }
    }

    @Test
    void add_row() {
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

            InnerHouseholder_DDRB.add_row(r,
                    new DSubmatrixD1(Ab), rowA, alpha,
                    new DSubmatrixD1(Bb), rowB, beta,
                    new DSubmatrixD1(Cb), rowC, 1, end);

            // skip over the zeros
            for (int j = rowA + 1; j < end; j++) {
                assertEquals(c.get(j), Cb.get(rowC, j), UtilEjml.TEST_F64);
            }
        }
    }

    @Test
    void computeTauAndDivideCol() {

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

    @Test
    void computeTauAndDivideRow() {
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

    @Test
    void testFindMaxCol() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r, -1, 1, rand, r);

        // make sure it ignores the first element
        A.set(0, 1, 100000);
        A.set(5, 1, -2346);

        double max = InnerHouseholder_DDRB.findMaxCol(r, new DSubmatrixD1(A), 1);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }

    @Test
    void testFindMaxRow() {
        DMatrixRBlock A = MatrixOps_DDRB.createRandom(r*2 + r - 1, r*2 - 1, -1, 1, rand, r);

        // make sure it ignores the first element
        A.set(1, 1, 100000);
        A.set(1, 4, -2346);

        double max = InnerHouseholder_DDRB.findMaxRow(r, new DSubmatrixD1(A), 1, 2);

        assertEquals(2346, max, UtilEjml.TEST_F64);
    }
}
