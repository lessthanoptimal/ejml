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

import org.ejml.data.DSubmatrixD1;

import static org.ejml.dense.block.MatrixMultInternalHarness_DDRB.pickMiddleAxis;
import static org.ejml.dense.block.MatrixMultInternalHarness_DDRB.pickOuterAxis;
import static org.ejml.dense.block.MatrixOps_DDRB.checkShapeMult;

//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

//CONCURRENT_MACRO MatrixMultInternalHarness_DDRB MatrixMultInternalHarness_MT_DDRB

/// Matrix multiplication for [DMatrixRBlock]. All sub-matrices must be block aligned.
///
/// To optimize concurrent performance, internal loop order (ijk) is dynamically selected so that
/// the longest length i or j is used. k being the longest axis is a special case that isn't handled
/// right now. It will select either i or j axis instead.
public class MatrixMult_DDRB {
    /// c = a \* b
    public static void mult( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, false, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.col1 - B.col0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.mult(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultSet,
                TileMultiplication_F64::tileMultPlus);
    }

    /// c = c + a \* b
    public static void multPlus( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, false, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.col1 - B.col0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.mult(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultPlus,
                TileMultiplication_F64::tileMultPlus);
    }

    /// c = c - a \* b
    public static void multMinus( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, false, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.col1 - B.col0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.mult(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultMinus,
                TileMultiplication_F64::tileMultMinus);
    }

    /// c = a<sup>T</sup> \* b
    public static void multTransA( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, true, B, false, C);

        int outer = pickOuterAxis(A.col1 - A.col0, B.col1 - B.col0, A.row1 - A.row0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransA(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultSetTransA,
                TileMultiplication_F64::tileMultPlusTransA);
    }

    /// c = a \* b<sup>T</sup>
    public static void multTransB( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, true, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.row1 - B.row0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransB(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultSetTransB,
                TileMultiplication_F64::tileMultPlusTransB);
    }

    /// c = c + a<sup>T</sup> \* b
    public static void multPlusTransA( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B,  DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, true, B, false, C);

        int outer = pickOuterAxis(A.col1 - A.col0, B.col1 - B.col0, A.row1 - A.row0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransA(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultPlusTransA,
                TileMultiplication_F64::tileMultPlusTransA);
    }

    /// c = c + a \* b<sup>T</sup>
    public static void multPlusTransB( int blockLength,  DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, true, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.row1 - B.row0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransB(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultPlusTransB,
                TileMultiplication_F64::tileMultPlusTransB);
    }

    /// c = c - a<sup>T</sup> \* b
    public static void multMinusTransA( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, true, B, false, C);

        int outer = pickOuterAxis(A.col1 - A.col0, B.col1 - B.col0, A.row1 - A.row0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransA(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultMinusTransA,
                TileMultiplication_F64::tileMultMinusTransA);
    }

    /// c = c - a \* b<sup>T</sup>
    public static void multMinusTransB( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C ) {
        checkShapeMult(blockLength, A, false, B, true, C);

        int outer = pickOuterAxis(A.row1 - A.row0, B.row1 - B.row0, A.col1 - A.col0);
        int middle = pickMiddleAxis(outer);
        MatrixMultInternalHarness_DDRB.multTransB(blockLength, A, B, C, outer, middle,
                TileMultiplication_F64::tileMultMinusTransB,
                TileMultiplication_F64::tileMultMinusTransB);
    }
}
