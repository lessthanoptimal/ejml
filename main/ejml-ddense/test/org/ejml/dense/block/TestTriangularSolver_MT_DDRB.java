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
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.junit.jupiter.api.Test;
import pabeles.concurrency.GrowArray;

import static org.ejml.dense.block.TestTriangularSolver_DDRB.makeSolvable;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTriangularSolver_MT_DDRB extends EjmlStandardJUnit {
    int r = 3;

    @Test void invert_two() {
        var workspace = new GrowArray<>(DGrowArray::new);

        // harder test with the triangle not aligned along a block. Enough blocks so that threads should be called
        int size = r*5 + 2;
        for (boolean lower : new boolean[]{true, false}) {
            DMatrixRBlock T = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, r);
            makeSolvable(T, rand);
            MatrixOps_DDRB.zeroTriangle(lower, T);

            DMatrixRBlock T_orig = T.copy();

            DMatrixRBlock foundSingle = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, r);
            DMatrixRBlock foundMT = foundSingle.copy();

            // Run code we are testing
            TriangularSolver_MT_DDRB.invert(r, !lower, new DSubmatrixD1(T), new DSubmatrixD1(foundMT), workspace);
            assertTrue(MatrixOps_DDRB.isEquals(T, T_orig));

            // Run single thread variant
            TriangularSolver_DDRB.invert(r, !lower, new DSubmatrixD1(T), new DSubmatrixD1(foundSingle), workspace);

            // Output should be identical
            assertTrue(MatrixOps_DDRB.isEquals(foundSingle, foundMT));
        }
    }

    @Test void lsolve() {
        // Size of triangle matrix
        int triangleSize = r*5;

        DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, triangleSize);
        makeSolvable(T, rand);
        DMatrixRBlock T_orig = T.copy();

        // Iterate through various sizes of B matrices
        for (int cols : new int[]{1, r, 2*r, 2*r + 1}) {
            DMatrixRBlock B = MatrixOps_DDRB.createRandom(T.numRows, cols, -1, 1, rand, r);
            DMatrixRBlock expected = new DMatrixRBlock(B.numRows, B.numCols, r);
            DMatrixRBlock found = expected.copy();

            for (boolean upperTri : new boolean[]{true, false}) {
                for (boolean transposedTri : new boolean[]{true, false}) {
                    TriangularSolver_MT_DDRB.lsolve(r, upperTri, new DSubmatrixD1(T), new DSubmatrixD1(found), transposedTri);
                    assertTrue(MatrixOps_DDRB.isEquals(T_orig, T, UtilEjml.TEST_F64_SQ));

                    TriangularSolver_DDRB.lsolve(r, upperTri, new DSubmatrixD1(T), new DSubmatrixD1(expected), transposedTri);

                    assertTrue(MatrixOps_DDRB.isEquals(expected, found, UtilEjml.TEST_F64_SQ));
                }
            }
        }
    }

    @Test void rsolve() {
        // Size of triangle matrix
        int triangleSize = r*5;

        DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, triangleSize);
        makeSolvable(T, rand);
        DMatrixRBlock T_orig = T.copy();

        // Iterate through various sizes of B matrices
        for (int rows : new int[]{1, r, 2*r, 2*r + 1}) {
            DMatrixRBlock B = MatrixOps_DDRB.createRandom(rows, T.numCols, -1, 1, rand, r);
            DMatrixRBlock expected = new DMatrixRBlock(B.numRows, B.numCols, r);
            DMatrixRBlock found = expected.copy();

            for (boolean upperTri : new boolean[]{true, false}) {
                for (boolean transposedTri : new boolean[]{true, false}) {
                    TriangularSolver_MT_DDRB.rsolve(r, upperTri, new DSubmatrixD1(T), new DSubmatrixD1(found), transposedTri);
                    assertTrue(MatrixOps_DDRB.isEquals(T_orig, T, UtilEjml.TEST_F64_SQ));

                    TriangularSolver_DDRB.rsolve(r, upperTri, new DSubmatrixD1(T), new DSubmatrixD1(expected), transposedTri);

                    assertTrue(MatrixOps_DDRB.isEquals(expected, found, UtilEjml.TEST_F64_SQ));
                }
            }
        }
    }

    @Test void lsolveBlock() {
        // Size of triangle matrix
        int triangleSize = r*5;

        DMatrixRBlock T = MatrixOps_DDRB.createRandom(r, r, -1, 1, rand, triangleSize);
        makeSolvable(T, rand);
        DMatrixRBlock T_orig = T.copy();

        // Iterate through various sizes of B matrices
        for (int cols : new int[]{1, r, 2*r, 2*r + 1}) {
            for (boolean upperTri : new boolean[]{true, false}) {
                for (boolean transposedT : new boolean[]{true, false}) {
                    for (boolean transposedB : new boolean[]{true, false}) {
//                        System.out.println("cols "+cols+" upper "+upperTri+" transposedT "+transposedT+" transposedB "+transposedB);

                        int rowsB = transposedB ? cols : T.numRows;
                        int colsB = transposedB ? T.numRows : cols;
                        DMatrixRBlock B = MatrixOps_DDRB.createRandom(rowsB, colsB, -1, 1, rand, r);
                        DMatrixRBlock expected = new DMatrixRBlock(B.numRows, B.numCols, r);
                        DMatrixRBlock found = expected.copy();

                        TriangularSolver_MT_DDRB.lsolveBlock(r, upperTri, new DSubmatrixD1(T),
                                new DSubmatrixD1(found), transposedT, transposedB);
                        assertTrue(MatrixOps_DDRB.isEquals(T_orig, T, UtilEjml.TEST_F64_SQ));

                        TriangularSolver_DDRB.lsolveBlock(r, upperTri, new DSubmatrixD1(T),
                                new DSubmatrixD1(expected), transposedT, transposedB);

                        assertTrue(MatrixOps_DDRB.isEquals(expected, found, UtilEjml.TEST_F64_SQ));
                    }
                }
            }
        }
    }

    @Test void rsolveBlock() {
        // Size of triangle matrix
        int triangleSize = r*5;

        DMatrixRBlock T = MatrixOps_DDRB.createRandom(r, r, -1, 1, rand, triangleSize);
        makeSolvable(T, rand);
        DMatrixRBlock T_orig = T.copy();

        // Iterate through various sizes of B matrices
        for (int rows : new int[]{1, r, 2*r, 2*r + 1}) {
            for (boolean upperTri : new boolean[]{true, false}) {
                for (boolean transposedT : new boolean[]{true, false}) {
                    for (boolean transposedB : new boolean[]{true, false}) {
//                        System.out.println("rows "+rows+" upper "+upperTri+" transposedT "+transposedT+" transposedB "+transposedB);

                        int rowsB = transposedB ? T.numRows : rows;
                        int colsB = transposedB ? rows : T.numRows;
                        DMatrixRBlock B = MatrixOps_DDRB.createRandom(rowsB, colsB, -1, 1, rand, r);
                        DMatrixRBlock expected = new DMatrixRBlock(B.numRows, B.numCols, r);
                        DMatrixRBlock found = expected.copy();

                        TriangularSolver_MT_DDRB.rsolveBlock(r, upperTri, new DSubmatrixD1(T),
                                new DSubmatrixD1(found), transposedT, transposedB);
                        assertTrue(MatrixOps_DDRB.isEquals(T_orig, T, UtilEjml.TEST_F64_SQ));

                        TriangularSolver_DDRB.rsolveBlock(r, upperTri, new DSubmatrixD1(T),
                                new DSubmatrixD1(expected), transposedT, transposedB);

                        assertTrue(MatrixOps_DDRB.isEquals(expected, found, UtilEjml.TEST_F64_SQ));
                    }
                }
            }
        }
    }
}

