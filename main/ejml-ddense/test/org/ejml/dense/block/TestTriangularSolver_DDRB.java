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
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;
import pabeles.concurrency.GrowArray;

import java.util.Random;

import static org.ejml.dense.block.MatrixOps_DDRB.embedInBlock;
import static org.ejml.dense.block.MatrixOps_DDRB.extractSubmatrix;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTriangularSolver_DDRB extends EjmlStandardJUnit {
    @Test void invert_two() {
        // block size
        int r = 3;

        var workspace = new GrowArray<>(DGrowArray::new);

        for (boolean lower : new boolean[]{true, false}) {
            for (int size = 1; size <= 9; size++) {
                DMatrixRBlock T = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, r);
                makeSolvable(T, rand);
                MatrixOps_DDRB.zeroTriangle(lower, T);

                DMatrixRBlock T_inv = T.copy();

                // Solve with the same input being passed in
                TriangularSolver_DDRB.invert(r, !lower, new DSubmatrixD1(T), new DSubmatrixD1(T_inv), workspace);

                var C = new DMatrixRBlock(size, size, r);
                MatrixOps_DDRB.mult(T, T_inv, C);

                assertTrue(GenericMatrixOps_F64.isIdentity(C, UtilEjml.TEST_F64));

                // see if passing in the same matrix instance twice messes it up or not
                TriangularSolver_DDRB.invert(r, !lower, new DSubmatrixD1(T), new DSubmatrixD1(T), workspace);
                assertTrue(MatrixOps_DDRB.isEquals(T, T_inv, UtilEjml.TEST_F64));
            }
        }
    }

    @Test void invert_one() {
        // block size
        int r = 3;

        var workspace = new GrowArray<>(DGrowArray::new);

        for (boolean lower : new boolean[]{true, false}) {
            for (int size = 1; size <= 9; size++) {
                DMatrixRBlock T = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, r);
                makeSolvable(T, rand);
                MatrixOps_DDRB.zeroTriangle(lower, T);

                DMatrixRBlock T_inv = T.copy();

                TriangularSolver_DDRB.invert(r, !lower, new DSubmatrixD1(T_inv), workspace);

                var C = new DMatrixRBlock(size, size, r);

                MatrixOps_DDRB.mult(T, T_inv, C);

                assertTrue(GenericMatrixOps_F64.isIdentity(C, UtilEjml.TEST_F64));
            }
        }
    }

    public static void makeSolvable( DMatrixRBlock T, Random rand ) {
        // Attempt to ensure it's a numerically stable invertible matrix
        for (int i = 0; i < T.numCols; i++) {
            T.set(i, i, 1.0 + rand.nextDouble());
        }
    }

    // Test solving several different triangular systems with different sizes.
    // All matrices begin and end along block boundaries.
    @Test void lsolve() {
        int r = 3;

        for (int dir = 0; dir < 2; dir++) {
            boolean upper = dir == 0;
            for (int triangleSize = 1; triangleSize <= 9; triangleSize++) {
                for (int cols = 1; cols <= 9; cols++) {
                    DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, r);
                    makeSolvable(T, rand);
                    MatrixOps_DDRB.zeroTriangle(true, T);

                    if (upper) {
                        T = MatrixOps_DDRB.transpose(T, null);
                    }

                    DMatrixRBlock B = MatrixOps_DDRB.createRandom(triangleSize, cols, -1, 1, rand, r);
                    DMatrixRBlock Y = new DMatrixRBlock(B.numRows, B.numCols, r);

                    checkLeftSolve(T, B, Y, r, upper, false);
                    checkLeftSolve(T, B, Y, r, upper, true);
                }
            }
        }
    }

    /// Checks to see if BlockTriangularSolver.lsolve produces the expected output given
    /// these inputs. T and B are embedded into larger block matrices with random padding
    /// to verify that submatrix bounds are respected.
    private void checkLeftSolve( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                 int r, boolean upper, boolean transT ) {
        // Fixed padding (in multiples of r so embedded matrices stay block-aligned).
        int padTopT = 1*r;
        int padLeftT = 2*r;
        int padTopB = 3*r;
        int padLeftB = 4*r;

        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);
            MatrixOps_DDRB.mult(T_tran, B, Y);
        } else {
            MatrixOps_DDRB.mult(T, B, Y);
        }

        DMatrixRMaj T_rmaj = MatrixOps_DDRB.convert(T, (DMatrixRMaj)null);
        DMatrixRMaj Y_rmaj = MatrixOps_DDRB.convert(Y, (DMatrixRMaj)null);

        DSubmatrixD1 sub_T = embedInBlock(T_rmaj, r, padTopT, padLeftT, rand);
        DSubmatrixD1 sub_Y = embedInBlock(Y_rmaj, r, padTopB, padLeftB, rand);

        TriangularSolver_DDRB.lsolve(r, upper, sub_T, sub_Y, transT);

        DMatrixRMaj result = extractSubmatrix(sub_Y);
        DMatrixRMaj B_rmaj = MatrixOps_DDRB.convert(B, (DMatrixRMaj)null);

        assertTrue(MatrixFeatures_DDRM.isIdentical(B_rmaj, result, UtilEjml.TEST_F64_SQ),
                "Failed: upper=" + upper + " transT=" + transT
                        + " triangleSize=" + T.numRows + " cols=" + B.numCols);
    }
    // Test solving several different triangular systems with different sizes.
    // All matrices begin and end along block boundaries.
    @Test void rsolve() {
        int r = 3;

        for (int dir = 0; dir < 2; dir++) {
            boolean upper = dir == 0;
            for (int triangleSize = 1; triangleSize <= 9; triangleSize++) {
                for (int rows = 1; rows <= 9; rows++) {
                    DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, r);
                    makeSolvable(T, rand);
                    MatrixOps_DDRB.zeroTriangle(true, T);

                    if (upper) {
                        T = MatrixOps_DDRB.transpose(T, null);
                    }

                    DMatrixRBlock B = MatrixOps_DDRB.createRandom(rows, triangleSize, -1, 1, rand, r);
                    DMatrixRBlock Y = new DMatrixRBlock(B.numRows, B.numCols, r);

                    checkRightSolve(T, B, Y, r, upper, false);
                    checkRightSolve(T, B, Y, r, upper, true);
                }
            }
        }
    }

    /// Checks to see if BlockTriangularSolver.rsolve produces the expected output given
    /// these inputs. T and B are embedded into larger block matrices with random padding
    /// to verify that submatrix bounds are respected.
    private void checkRightSolve( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                  int r, boolean upper, boolean transT ) {
        // Fixed padding (in multiples of r so embedded matrices stay block-aligned).
        int padTopT = 1*r;
        int padLeftT = 2*r;
        int padTopB = 3*r;
        int padLeftB = 4*r;

        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);
            MatrixOps_DDRB.mult(B, T_tran, Y);
        } else {
            MatrixOps_DDRB.mult(B, T, Y);
        }

        // Convert T and Y to row-major form for embedding via the helper.
        DMatrixRMaj T_rmaj = MatrixOps_DDRB.convert(T, (DMatrixRMaj)null);
        DMatrixRMaj Y_rmaj = MatrixOps_DDRB.convert(Y, (DMatrixRMaj)null);

        DSubmatrixD1 sub_T = embedInBlock(T_rmaj, r, padTopT, padLeftT, rand);
        DSubmatrixD1 sub_Y = embedInBlock(Y_rmaj, r, padTopB, padLeftB, rand);

        TriangularSolver_DDRB.rsolve(r, upper, sub_T, sub_Y, transT);

        DMatrixRMaj result = extractSubmatrix(sub_Y);
        DMatrixRMaj B_rmaj = MatrixOps_DDRB.convert(B, (DMatrixRMaj)null);

        assertTrue(MatrixFeatures_DDRM.isIdentical(B_rmaj, result, UtilEjml.TEST_F64_SQ),
                "Failed: upper=" + upper + " transT=" + transT
                        + " triangleSize=" + T.numRows + " rows=" + B.numRows);
    }

    @Test void rsolveBlock() {
        int r = 3;
        for (boolean transB : new boolean[]{false, true}) {
            for (boolean transT : new boolean[]{false, true}) {
                for (boolean solveL : new boolean[]{false, true}) {
                    // Sweep T sizes from 1 to r to exercise both partial and full diagonal blocks.
                    for (int triSize = 1; triSize <= r; triSize++) {
                        check_rsolveBlock_submatrix(solveL, transT, transB, r, triSize);
                    }
                }
            }
        }
    }

    /// Checks rsolveBlock with T of the given size embedded inside a larger block matrix.
    /// Padding is added around the embedded matrices to verify that the solver respects
    /// submatrix bounds and only operates on the declared region.
    private void check_rsolveBlock_submatrix( boolean solveL, boolean transT, boolean transB,
                                              int r, int triSize ) {
        // Fixed padding (in multiples of r so embedded matrices stay block-aligned).
        int padTopT = 1*r;
        int padLeftT = 2*r;
        int padTopB = 3*r;
        int padLeftB = 4*r;

        int length = r + 2;

        DMatrixRMaj L = createRandomLowerTriangular(triSize);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(length, triSize, rand);
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(length, triSize, rand);

        if (!solveL) {
            CommonOps_DDRM.transpose(L);
        }
        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        CommonOps_DDRM.mult(X, L, B);

        DSubmatrixD1 sub_L = embedInBlock(L, r, padTopT, padLeftT, rand);
        DSubmatrixD1 sub_B = embedInBlock(B, r, padTopB, padLeftB, rand);

        if (transT) {
            DMatrixRMaj L_extracted = extractSubmatrix(sub_L);
            CommonOps_DDRM.transpose(L_extracted);
            sub_L = embedInBlock(L_extracted, r, padTopT, padLeftT, rand);
        }

        if (transB) {
            DMatrixRMaj B_extracted = extractSubmatrix(sub_B);
            CommonOps_DDRM.transpose(B_extracted);
            sub_B = embedInBlock(B_extracted, r, padTopB, padLeftB, rand);
            CommonOps_DDRM.transpose(X);
        }

        TriangularSolver_DDRB.rsolveBlock(r, !solveL, sub_L, sub_B, transT, transB);

        DMatrixRMaj result = extractSubmatrix(sub_B);
        assertTrue(GenericMatrixOps_F64.isEquivalent(X, result, UtilEjml.TEST_F64),
                "Failed: solveL=" + solveL + " transT=" + transT + " transB=" + transB
                        + " triSize=" + triSize);
    }

    @Test void lsolveBlock() {
        int r = 3;
        for (boolean transB : new boolean[]{false, true}) {
            for (boolean transT : new boolean[]{false, true}) {
                for (boolean solveL : new boolean[]{false, true}) {
                    // Sweep T sizes from 1 to r to exercise both partial and full diagonal blocks.
                    for (int triSize = 1; triSize <= r; triSize++) {
                        check_lsolveBlock_submatrix(solveL, transT, transB, r, triSize);
                    }
                }
            }
        }
    }

    /// Checks lsolveBlock with T of the given size embedded inside a larger block matrix.
    /// Padding is added around the embedded matrices to verify that the solver respects
    /// submatrix bounds and only operates on the declared region.
    private void check_lsolveBlock_submatrix( boolean solveL, boolean transT, boolean transB,
                                              int r, int triSize ) {
        // Fixed padding (in multiples of r so embedded matrices stay block-aligned).
        int padTopT = 1*r;
        int padLeftT = 2*r;
        int padTopB = 3*r;
        int padLeftB = 4*r;

        int length = r + 2;

        DMatrixRMaj L = createRandomLowerTriangular(triSize);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(triSize, length, rand);
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(triSize, length, rand);

        if (!solveL) {
            CommonOps_DDRM.transpose(L);
        }
        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        CommonOps_DDRM.mult(L, X, B);

        DSubmatrixD1 sub_L = embedInBlock(L, r, padTopT, padLeftT, rand);
        DSubmatrixD1 sub_B = embedInBlock(B, r, padTopB, padLeftB, rand);

        if (transT) {
            DMatrixRMaj L_extracted = extractSubmatrix(sub_L);
            CommonOps_DDRM.transpose(L_extracted);
            sub_L = embedInBlock(L_extracted, r, padTopT, padLeftT, rand);
        }

        if (transB) {
            DMatrixRMaj B_extracted = extractSubmatrix(sub_B);
            CommonOps_DDRM.transpose(B_extracted);
            sub_B = embedInBlock(B_extracted, r, padTopB, padLeftB, rand);
            CommonOps_DDRM.transpose(X);
        }

        TriangularSolver_DDRB.lsolveBlock(r, !solveL, sub_L, sub_B, transT, transB);

        DMatrixRMaj result = extractSubmatrix(sub_B);
        assertTrue(GenericMatrixOps_F64.isEquivalent(X, result, UtilEjml.TEST_F64),
                "Failed: solveL=" + solveL + " transT=" + transT + " transB=" + transB
                        + " triSize=" + triSize);
    }

    private DMatrixRMaj createRandomLowerTriangular( int N ) {
        DMatrixRMaj U = RandomMatrices_DDRM.triangularUpper(N, 0, -1, 1, rand);

        CommonOps_DDRM.transpose(U);

        return U;
    }
}
