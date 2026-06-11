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
import org.ejml.data.DSubmatrixD1;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestVectorOps_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test void scale_row() {
        int rowA = 0;
        int rowB = 1;

        double alpha = 1.5;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true, rowA).scale(alpha);

            VectorOps_DDRB.scale_row(r, new DSubmatrixD1(Ab), rowA, alpha, new DSubmatrixD1(Bb), rowB, offset, end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test void div_row() {
        int rowA = 0;
        int rowB = 1;

        double alpha = 1.5;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(true, rowA).divide(alpha);

            VectorOps_DDRB.div_row(r, new DSubmatrixD1(Ab), rowA, alpha, new DSubmatrixD1(Bb), rowB, offset, end);

            checkVector_row(rowB, end, offset, A, Bb, b);
        }
    }

    @Test void add_row() {
        int rowA = 0;
        int rowB = 1;
        int rowC = 2;

        double alpha = 1.5;
        double beta = -0.7;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);
            DMatrixRBlock Cb = Ab.copy();

            SimpleMatrix a = A.extractVector(true, rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true, rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            VectorOps_DDRB.add_row(r,
                    new DSubmatrixD1(Ab), rowA, alpha,
                    new DSubmatrixD1(Bb), rowB, beta,
                    new DSubmatrixD1(Cb), rowC, offset, end);

            checkVector_row(rowC, end, offset, A, Cb, c);
        }
    }

    @Test void dot_row() {
        int rowA = 0;
        int rowB = 1;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            SimpleMatrix a = A.extractMatrix(rowA, rowA + 1, offset, SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random_DDRM(r, width, -1.0, 1.0, rand);
            SimpleMatrix b = B.extractMatrix(rowB, rowB + 1, offset, SimpleMatrix.END);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);

            double expected = (double)a.dot(b);

            double found = VectorOps_DDRB.dot_row(r, new DSubmatrixD1(Ab), rowA, new DSubmatrixD1(Bb), rowB, offset, end);

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }

    @Test void dot_row_col() {
        int rowA = 0;
        int colB = 1;

        for (int width = 1; width <= 3*r; width++) {
//            System.out.println("width "+width);
            int end = width;
            int offset = width > 1 ? 1 : 0;
            if (colB >= width) colB = 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(width, width, -1.0, 1.0, rand);
            SimpleMatrix a = A.extractMatrix(rowA, rowA + 1, offset, SimpleMatrix.END);
            SimpleMatrix B = SimpleMatrix.random_DDRM(width, width, -1.0, 1.0, rand);
            SimpleMatrix b = B.extractMatrix(offset, SimpleMatrix.END, colB, colB + 1);

            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);

            double expected = (double)a.dot(b);

            double found = VectorOps_DDRB.dot_row_col(r,
                    new DSubmatrixD1(Ab), rowA,
                    new DSubmatrixD1(Bb), colB,
                    offset, end);

            assertEquals(expected, found, UtilEjml.TEST_F64);
        }
    }

    @Test void add_row_acrossBlocks() {
        // each operand's row lives in a different block-row, exercising the per-operand block-row math
        int rowA = 1;
        int rowB = r + 2;
        int rowC = 2*r + 1;

        double alpha = 1.5;
        double beta = -0.7;

        for (int width = 1; width <= 3*r; width++) {
            int end = width;
            int offset = width > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(3*r, width, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(3*r, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);
            DMatrixRBlock Cb = Ab.copy();

            SimpleMatrix a = A.extractVector(true, rowA).scale(alpha);
            SimpleMatrix b = B.extractVector(true, rowB).scale(beta);
            SimpleMatrix c = a.plus(b);

            VectorOps_DDRB.add_row(r,
                    new DSubmatrixD1(Ab), rowA, alpha,
                    new DSubmatrixD1(Bb), rowB, beta,
                    new DSubmatrixD1(Cb), rowC, offset, end);

            checkVector_row(rowC, end, offset, A, Cb, c);
        }
    }

    @Test void scale_col() {
        int colA = 1;
        int colB = r + 1;

        double alpha = 1.5;
        int width = 2*r + 1;

        for (int height = 1; height <= 3*r; height++) {
            int end = height;
            int offset = height > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(height, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = Ab.copy();

            SimpleMatrix b = A.extractVector(false, colA).scale(alpha);

            VectorOps_DDRB.scale_col(r, new DSubmatrixD1(Ab), colA, alpha, new DSubmatrixD1(Bb), colB, offset, end);

            checkVector_col(colB, end, offset, A, Bb, b);
        }
    }

    @Test public void add_col() {
        int colA = 1;
        int colB = r + 1;
        int colC = 2*r;

        double alpha = 1.5;
        double beta = -0.7;
        int width = 2*r + 1;

        for (int height = 1; height <= 3*r; height++) {
            int end = height;
            int offset = height > 1 ? 1 : 0;

            SimpleMatrix A = SimpleMatrix.random_DDRM(height, width, -1.0, 1.0, rand);
            SimpleMatrix B = SimpleMatrix.random_DDRM(height, width, -1.0, 1.0, rand);
            DMatrixRBlock Ab = MatrixOps_DDRB.convert(A.getDDRM(), r);
            DMatrixRBlock Bb = MatrixOps_DDRB.convert(B.getDDRM(), r);
            DMatrixRBlock Cb = Ab.copy();

            SimpleMatrix a = A.extractVector(false, colA).scale(alpha);
            SimpleMatrix b = B.extractVector(false, colB).scale(beta);
            SimpleMatrix c = a.plus(b);

            VectorOps_DDRB.add_col(r,
                    new DSubmatrixD1(Ab), colA, alpha,
                    new DSubmatrixD1(Bb), colB, beta,
                    new DSubmatrixD1(Cb), colC, offset, end);

            checkVector_col(colC, end, offset, A, Cb, c);
        }
    }

    /// Checks to see if only the anticipated parts of the matrix have been modified and that
    /// they are the anticipated values.
    ///
    /// @param row The row modified in modMatrix.
    /// @param end end of the vector.
    /// @param offset start of the vector.
    /// @param untouched Original values of the modMatrix.
    /// @param modMatrix The matrix which have been modified by the function being tested.
    /// @param modVector Vector that contains the anticipated values.
    public static void checkVector_row( int row, int end, int offset,
                                        SimpleMatrix untouched,
                                        DMatrixRBlock modMatrix, SimpleMatrix modVector ) {
        for (int i = 0; i < modMatrix.numRows; i++) {
            if (i == row) {
                for (int j = 0; j < offset; j++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
                for (int j = offset; j < end; j++) {
                    assertEquals(modVector.get(j), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
                for (int j = end; j < modMatrix.numCols; j++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
            } else {
                for (int j = 0; j < modMatrix.numCols; j++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64, i + " " + j);
                }
            }
        }
    }

    /// Column-vector analogue of [#checkVector_row]: only column 'col', rows [offset,end), should change.
    public static void checkVector_col( int col, int end, int offset,
                                        SimpleMatrix untouched,
                                        DMatrixRBlock modMatrix, SimpleMatrix modVector ) {
        for (int j = 0; j < modMatrix.numCols; j++) {
            if (j == col) {
                for (int i = 0; i < offset; i++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
                for (int i = offset; i < end; i++) {
                    assertEquals(modVector.get(i), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
                for (int i = end; i < modMatrix.numRows; i++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64);
                }
            } else {
                for (int i = 0; i < modMatrix.numRows; i++) {
                    assertEquals(untouched.get(i, j), modMatrix.get(i, j), UtilEjml.TEST_F64, i + " " + j);
                }
            }
        }
    }
}
