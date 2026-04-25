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
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.generic.GenericMatrixOps_F64;
import org.junit.jupiter.api.Test;
import pabeles.concurrency.GrowArray;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTriangularSolver_DDRB extends EjmlStandardJUnit {
    @Test void invert_two() {
        // block size
        int r = 3;

        var workspace = new GrowArray<>(DGrowArray::new);

        for (boolean lower : new boolean[]{true, false}) {
            for (int size = 1; size <= 9; size++) {
                DMatrixRBlock T = MatrixOps_DDRB.createRandom(size, size, -1, 1, rand, r);
                makeSolvable(T);
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
                makeSolvable(T);
                MatrixOps_DDRB.zeroTriangle(lower, T);

                DMatrixRBlock T_inv = T.copy();

                TriangularSolver_DDRB.invert(r, !lower, new DSubmatrixD1(T_inv), workspace);

                var C = new DMatrixRBlock(size, size, r);

                MatrixOps_DDRB.mult(T, T_inv, C);

                assertTrue(GenericMatrixOps_F64.isIdentity(C, UtilEjml.TEST_F64));
            }
        }
    }

    private void makeSolvable( DMatrixRBlock T ) {
        // Attempt to ensure it's a numerically stable invertible matrix
        for (int i = 0; i < T.numCols; i++) {
            T.set(i, i, 1.0 + rand.nextDouble());
        }
    }

    // Test solving several different triangular systems with different sizes.
    // All matrices begin and end along block boundaries.
    @Test void lsolve() {
        // block size
        int r = 3;

        for (int dir = 0; dir < 2; dir++) {
            boolean upper = dir == 0;
            for (int triangleSize = 1; triangleSize <= 9; triangleSize++) {
                for (int cols = 1; cols <= 9; cols++) {
//                System.out.println("triangle "+triangleSize+" cols "+cols);
                    DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, r);
                    makeSolvable(T);
                    MatrixOps_DDRB.zeroTriangle(true, T);

                    if (upper) {
                        T = MatrixOps_DDRB.transpose(T, null);
                    }

                    DMatrixRBlock B = MatrixOps_DDRB.createRandom(triangleSize, cols, -1, 1, rand, r);
                    DMatrixRBlock Y = new DMatrixRBlock(B.numRows, B.numCols, r);

                    checkLeftSolve(T, B, Y, r, upper, false);
                    checkLeftSolve(T, B, Y, r, upper, true);

                    // test cases where the submatrix is not aligned with the inner
                    // blocks
                    checkLeftSolveUnaligned(T, B, Y, r, upper, false);
                    checkLeftSolveUnaligned(T, B, Y, r, upper, true);
                }
            }
        }
    }

    /// Checks to see if BlockTriangularSolver.lsolve produces the expected output given
    /// these inputs. The solution is computed directly.
    private void checkLeftSolve( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                 int r, boolean upper, boolean transT ) {
        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);

            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T_tran, B, Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T, B, Y);
        }

        // Y is overwritten with the solution
        TriangularSolver_DDRB.lsolve(r, upper, new DSubmatrixD1(T), new DSubmatrixD1(Y), transT);

        assertTrue(MatrixOps_DDRB.isEquals(B, Y, UtilEjml.TEST_F64_SQ));
    }

    /// Checks to see if BlockTriangularSolver.lsolve produces the expected output given
    /// these inputs. The solution is computed directly.
    private void checkLeftSolveUnaligned( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                          int r, boolean upper, boolean transT ) {
        DMatrixRBlock T2;

        if (upper)
            T2 = MatrixOps_DDRB.createRandom(T.numRows + 1, T.numCols, -1, 1, rand, T.blockLength);
        else
            T2 = MatrixOps_DDRB.createRandom(T.numRows, T.numCols + 1, -1, 1, rand, T.blockLength);

        CommonOps_DDRM.insert(T, T2, 0, 0);

        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);

            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T_tran, B, Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T, B, Y);
        }

        int size = T.numRows;

        // Y is overwritten with the solution
        TriangularSolver_DDRB.lsolve(r, upper, new DSubmatrixD1(T2, 0, size, 0, size), new DSubmatrixD1(Y), transT);

        assertTrue(MatrixOps_DDRB.isEquals(B, Y, UtilEjml.TEST_F64_SQ),
                "Failed upper = " + upper + " transT = " + transT + " T.length " + T.numRows + " B.cols " + B.numCols);
    }

    // Test solving several different triangular systems with different sizes.
    // All matrices begin and end along block boundaries.
    @Test void rsolve() {
        // block size
        int r = 3;

        for (int dir = 0; dir < 2; dir++) {
            boolean upper = dir == 0;
            for (int triangleSize = 1; triangleSize <= 9; triangleSize++) {
                for (int cols = 1; cols <= 9; cols++) {
//                System.out.println("triangle "+triangleSize+" cols "+cols);
                    DMatrixRBlock T = MatrixOps_DDRB.createRandom(triangleSize, triangleSize, -1, 1, rand, r);
                    makeSolvable(T);
                    MatrixOps_DDRB.zeroTriangle(true, T);

                    if (upper) {
                        T = MatrixOps_DDRB.transpose(T, null);
                    }

                    DMatrixRBlock B = MatrixOps_DDRB.createRandom(triangleSize, cols, -1, 1, rand, r);
                    DMatrixRBlock Y = new DMatrixRBlock(B.numRows, B.numCols, r);

                    checkLeftSolve(T, B, Y, r, upper, false);
                    checkLeftSolve(T, B, Y, r, upper, true);

                    // test cases where the submatrix is not aligned with the inner
                    // blocks
                    checkLeftSolveUnaligned(T, B, Y, r, upper, false);
                    checkLeftSolveUnaligned(T, B, Y, r, upper, true);
                }
            }
        }
    }

    /// Checks to see if BlockTriangularSolver.rsolve produces the expected output given
    /// these inputs. The solution is computed directly.
    private void checkRightSolve( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                 int r, boolean upper, boolean transT ) {
        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);

            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T_tran, B, Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T, B, Y);
        }

        // Y is overwritten with the solution
        TriangularSolver_DDRB.rsolve(r, upper, new DSubmatrixD1(T), new DSubmatrixD1(Y), transT);

        assertTrue(MatrixOps_DDRB.isEquals(B, Y, UtilEjml.TEST_F64_SQ));
    }

    /// Checks to see if BlockTriangularSolver.rsolve produces the expected output given
    /// these inputs. The solution is computed directly.
    private void checkRightSolveUnaligned( DMatrixRBlock T, DMatrixRBlock B, DMatrixRBlock Y,
                                          int r, boolean upper, boolean transT ) {
        DMatrixRBlock T2;

        if (upper)
            T2 = MatrixOps_DDRB.createRandom(T.numRows + 1, T.numCols, -1, 1, rand, T.blockLength);
        else
            T2 = MatrixOps_DDRB.createRandom(T.numRows, T.numCols + 1, -1, 1, rand, T.blockLength);

        CommonOps_DDRM.insert(T, T2, 0, 0);

        if (transT) {
            DMatrixRBlock T_tran = MatrixOps_DDRB.transpose(T, null);

            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T_tran, B, Y);
        } else {
            // Compute Y directly from the expected result B
            MatrixOps_DDRB.mult(T, B, Y);
        }

        int size = T.numRows;

        // Y is overwritten with the solution
        TriangularSolver_DDRB.rsolve(r, upper, new DSubmatrixD1(T2, 0, size, 0, size), new DSubmatrixD1(Y), transT);

        assertTrue(MatrixOps_DDRB.isEquals(B, Y, UtilEjml.TEST_F64_SQ),
                "Failed upper = " + upper + " transT = " + transT + " T.length " + T.numRows + " B.cols " + B.numCols);
    }

    /// Check all permutations of lsolveBlock
    @Test void lsolveBlock() {
        for (boolean transB : new boolean[]{false, true}) {
            for (boolean transT : new boolean[]{false, true}) {
                for (boolean solveL : new boolean[]{false, true}) {
                    check_lsolveBlock_submatrix(solveL, transT, transB);
                }
            }
        }
    }

    /// Checks to see if solve functions that use sub matrices as input work correctly
    private void check_lsolveBlock_submatrix( boolean solveL, boolean transT, boolean transB ) {
        // compute expected solution
        DMatrixRMaj L = createRandomLowerTriangular(3);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(3, 5, rand);
        DMatrixRMaj X = new DMatrixRMaj(3, 5);

        if (!solveL) {
            CommonOps_DDRM.transpose(L);
        }

        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        CommonOps_DDRM.solve(L, B, X);

        // do it again using block matrices
        DMatrixRBlock b_L = MatrixOps_DDRB.convert(L, 3);
        DMatrixRBlock b_B = MatrixOps_DDRB.convert(B, 3);

        DSubmatrixD1 sub_L = new DSubmatrixD1(b_L, 0, 3, 0, 3);
        DSubmatrixD1 sub_B = new DSubmatrixD1(b_B, 0, 3, 0, 5);

        if (transT) {
            sub_L.original = MatrixOps_DDRB.transpose((DMatrixRBlock)sub_L.original, null);
            TestMatrixMult_DDRB.transposeSub(sub_L);
        }

        if (transB) {
            sub_B.original = b_B = MatrixOps_DDRB.transpose((DMatrixRBlock)sub_B.original, null);
            TestMatrixMult_DDRB.transposeSub(sub_B);
            CommonOps_DDRM.transpose(X);
        }

//        sub_L.original.print();
//        sub_B.original.print();

        TriangularSolver_DDRB.lsolveBlock(3, !solveL, sub_L, sub_B, transT, transB);

        assertTrue(GenericMatrixOps_F64.isEquivalent(X, b_B, UtilEjml.TEST_F64));
    }

    /// Check all permutations of lsolveBlock
    @Test void rsolveBlock() {
        for (boolean transB : new boolean[]{false, true}) {
            for (boolean transT : new boolean[]{false, true}) {
                for (boolean solveL : new boolean[]{false, true}) {
                    check_rsolveBlock_submatrix(solveL, transT, transB);
                }
            }
        }
    }

    /// Checks to see if solve functions that use sub matrices as input work correctly
    private void check_rsolveBlock_submatrix( boolean solveL, boolean transT, boolean transB ) {
        int r = 3; // block size
        int length = r + 2; // more than one block

        // compute expected solution
        DMatrixRMaj L = createRandomLowerTriangular(r);
        DMatrixRMaj B = RandomMatrices_DDRM.rectangle(length, r, rand);
        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(length, r, rand);

        if (!solveL) {
            CommonOps_DDRM.transpose(L);
        }

        if (transT) {
            CommonOps_DDRM.transpose(L);
        }

        CommonOps_DDRM.mult(X, L, B);

        // do it again using block matrices
        DMatrixRBlock b_L = MatrixOps_DDRB.convert(L, r);
        DMatrixRBlock b_B = MatrixOps_DDRB.convert(B, r);

        DSubmatrixD1 sub_L = new DSubmatrixD1(b_L, 0, r, 0, r);
        DSubmatrixD1 sub_B = new DSubmatrixD1(b_B, 0, length, 0, r);

        if (transT) {
            sub_L.original = MatrixOps_DDRB.transpose((DMatrixRBlock)sub_L.original, null);
            TestMatrixMult_DDRB.transposeSub(sub_L);
        }

        if (transB) {
            sub_B.original = b_B = MatrixOps_DDRB.transpose((DMatrixRBlock)sub_B.original, null);
            TestMatrixMult_DDRB.transposeSub(sub_B);
            CommonOps_DDRM.transpose(X);
        }

        TriangularSolver_DDRB.rsolveBlock(r, !solveL, sub_L, sub_B, transT, transB);

        assertTrue(GenericMatrixOps_F64.isEquivalent(X, b_B, UtilEjml.TEST_F64));
    }

    private DMatrixRMaj createRandomLowerTriangular( int N ) {
        DMatrixRMaj U = RandomMatrices_DDRM.triangularUpper(N, 0, -1, 1, rand);

        CommonOps_DDRM.transpose(U);

        return U;
    }
}
