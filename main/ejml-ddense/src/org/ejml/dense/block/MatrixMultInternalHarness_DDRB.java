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
//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;
//CONCURRENT_INLINE import org.ejml.dense.block.MatrixMultInternalHarness_DDRB.*;
//CONCURRENT_INLINE import static org.ejml.dense.block.MatrixMultInternalHarness_DDRB.*;

//CONCURRENT_CLASS_NAME MatrixMultInternalHarness_MT_DDRB

/// Internal harness for block matrix multiplication with parameterized outer and middle
/// loop axes. Because it's internal the API is subject to change between released.
/// This harness enables you to change the loop order (IJK) and call various different kernels all
/// from the same code base with minimal overhead.
///
/// Being able to select the order becomes important with concurrent code as threads are spawned from the
/// outer loop. If the outer loop has 1 block then in the old design it became single threaded.
/// There are also theoretical advantages of being able to keep different blocks in memory to improve
/// performance. This is not observable in EJML's internal benchmarks.
///
/// K is never selected as the outer axis. To parallelize K requires a completely different approach and involves
/// an accumulator, which will break the strict requirement that single and MT code produce identical results.
/// In the future this might be relaxed with explicit consent.
///
/// Note: Care has been taken to avoid all unnecessary memory creation, even if it makes the algorithm more complex.
public class MatrixMultInternalHarness_DDRB {
    //CONCURRENT_OMIT_BEGIN
    public static final int I = 0;
    public static final int J = 1;
    public static final int K = 2;

    /// Functional interface for an inner block kernel.
    @FunctionalInterface
    public interface BlockKernel {
        void apply( double[] dataA, double[] dataB, double[] dataC,
                    int indexA, int indexB, int indexC,
                    int heightA, int widthA, int widthB );
    }

    /// Picks the outermost loop axis. Always returns I or J — the larger of the two
    /// dimensions. K is excluded because parallelizing K creates a C-accumulator race.
    /// Same selection is used for both ST and MT to keep results deterministic.
    public static int pickOuterAxis( int sizeI, int sizeJ, int sizeK ) {
        return sizeI >= sizeJ ? I : J;
    }

    /// Picks the middle loop axis given the outer axis. Inner is then determined by
    /// elimination. Convention: I-outer→ijk, J-outer→jik.
    public static int pickMiddleAxis( int outerAxis ) {
        return switch (outerAxis) {
            case I -> J;
            case J -> I;
            case K -> I;
            default -> throw new IllegalArgumentException("axis must be I, J, or K");
        };
    }

    /// Returns the size of the specified axis
    static int sizeOf( int axis, int sizeI, int sizeJ, int sizeK ) {
        return switch (axis) {
            case I -> sizeI;
            case J -> sizeJ;
            case K -> sizeK;
            default -> throw new IllegalArgumentException();
        };
    }

    /// Returns a value associated with an axis based on the provided axis order
    ///
    /// @param outer size of outer axis
    /// @param middle size of middle axis
    /// @param inner size of inner axis
    /// @param outerAxis Which axis is the outer axis
    /// @param middleAxis Which axis is the middle axis
    /// @param innerAxis Which axis is the inner axis
    static int axisValue( int targetAxis, int outer, int middle, int inner,
                          int outerAxis, int middleAxis, int innerAxis ) {
        if (targetAxis == outerAxis) return outer;
        if (targetAxis == middleAxis) return middle;
        if (targetAxis == innerAxis) return inner;
        throw new IllegalArgumentException();
    }
    //CONCURRENT_OMIT_END

    /// Block walker for C = A * B.
    ///
    /// @param firstTouch Operation applied the first time a submatrix in C is encountered. Used to initialize.
    /// @param accumulate Operation after the first time and used to accumulate results.
    public static void mult( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C,
                             int outerAxis, int middleAxis,
                             BlockKernel firstTouch, BlockKernel accumulate ) {
        if (outerAxis == middleAxis)
            throw new IllegalArgumentException("outer and middle axes must differ");
        final int innerAxis = 3 - outerAxis - middleAxis;

        final int sizeI = A.row1 - A.row0;
        final int sizeJ = B.col1 - B.col0;
        final int sizeK = A.col1 - A.col0;
        final int sizeOuter = sizeOf(outerAxis, sizeI, sizeJ, sizeK);
        final int sizeMiddle = sizeOf(middleAxis, sizeI, sizeJ, sizeK);
        final int sizeInner = sizeOf(innerAxis, sizeI, sizeJ, sizeK);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, sizeOuter, blockLength, outer -> {
        for (int outer = 0; outer < sizeOuter; outer += blockLength) {
            for (int middle = 0; middle < sizeMiddle; middle += blockLength) {
                for (int inner = 0; inner < sizeInner; inner += blockLength) {
                    int i = axisValue(I, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int j = axisValue(J, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int k = axisValue(K, outer, middle, inner, outerAxis, middleAxis, innerAxis);

                    int heightA = Math.min(blockLength, sizeI - i);
                    int widthB = Math.min(blockLength, sizeJ - j);
                    int widthA = Math.min(blockLength, sizeK - k);

                    int indexA = (A.row0 + i)*A.original.numCols + (A.col0 + k)*heightA;
                    int indexB = (B.row0 + k)*B.original.numCols + (B.col0 + j)*widthA;
                    int indexC = (C.row0 + i)*C.original.numCols + (C.col0 + j)*heightA;

                    if (k == 0) {
                        firstTouch.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthB);
                    } else {
                        accumulate.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Block walker for C = A^T * B.
    ///
    /// @param firstTouch Operation applied the first time a submatrix in C is encountered. Used to initialize.
    /// @param accumulate Operation after the first time and used to accumulate results.
    public static void multTransA( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C,
                                   int outerAxis, int middleAxis,
                                   BlockKernel firstTouch, BlockKernel accumulate ) {
        if (outerAxis == middleAxis)
            throw new IllegalArgumentException("outer and middle axes must differ");
        final int innerAxis = 3 - outerAxis - middleAxis;

        final int sizeI = A.col1 - A.col0;
        final int sizeJ = B.col1 - B.col0;
        final int sizeK = A.row1 - A.row0;
        final int sizeOuter = sizeOf(outerAxis, sizeI, sizeJ, sizeK);
        final int sizeMiddle = sizeOf(middleAxis, sizeI, sizeJ, sizeK);
        final int sizeInner = sizeOf(innerAxis, sizeI, sizeJ, sizeK);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, sizeOuter, blockLength, outer -> {
        for (int outer = 0; outer < sizeOuter; outer += blockLength) {
            for (int middle = 0; middle < sizeMiddle; middle += blockLength) {
                for (int inner = 0; inner < sizeInner; inner += blockLength) {
                    int i = axisValue(I, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int j = axisValue(J, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int k = axisValue(K, outer, middle, inner, outerAxis, middleAxis, innerAxis);

                    int widthA = Math.min(blockLength, sizeI - i);
                    int widthB = Math.min(blockLength, sizeJ - j);
                    int heightA = Math.min(blockLength, sizeK - k);

                    int indexA = (A.row0 + k)*A.original.numCols + (A.col0 + i)*heightA;
                    int indexB = (B.row0 + k)*B.original.numCols + (B.col0 + j)*heightA;
                    int indexC = (C.row0 + i)*C.original.numCols + (C.col0 + j)*widthA;

                    if (k == 0) {
                        firstTouch.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthB);
                    } else {
                        accumulate.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthB);
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Block walker for C = A * B^T.
    ///
    /// @param firstTouch Operation applied the first time a submatrix in C is encountered. Used to initialize.
    /// @param accumulate Operation after the first time and used to accumulate results.
    public static void multTransB( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B, DSubmatrixD1 C,
                                   int outerAxis, int middleAxis,
                                   BlockKernel firstTouch, BlockKernel accumulate ) {
        if (outerAxis == middleAxis)
            throw new IllegalArgumentException("outer and middle axes must differ");
        final int innerAxis = 3 - outerAxis - middleAxis;

        final int sizeI = A.row1 - A.row0;
        final int sizeJ = B.row1 - B.row0;
        final int sizeK = A.col1 - A.col0;
        final int sizeOuter = sizeOf(outerAxis, sizeI, sizeJ, sizeK);
        final int sizeMiddle = sizeOf(middleAxis, sizeI, sizeJ, sizeK);
        final int sizeInner = sizeOf(innerAxis, sizeI, sizeJ, sizeK);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, sizeOuter, blockLength, outer -> {
        for (int outer = 0; outer < sizeOuter; outer += blockLength) {
            for (int middle = 0; middle < sizeMiddle; middle += blockLength) {
                for (int inner = 0; inner < sizeInner; inner += blockLength) {
                    int i = axisValue(I, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int j = axisValue(J, outer, middle, inner, outerAxis, middleAxis, innerAxis);
                    int k = axisValue(K, outer, middle, inner, outerAxis, middleAxis, innerAxis);

                    int heightA = Math.min(blockLength, sizeI - i);
                    int widthC = Math.min(blockLength, sizeJ - j);
                    int widthA = Math.min(blockLength, sizeK - k);

                    int indexA = (A.row0 + i)*A.original.numCols + (A.col0 + k)*heightA;
                    int indexB = (B.row0 + j)*B.original.numCols + (B.col0 + k)*widthC;
                    int indexC = (C.row0 + i)*C.original.numCols + (C.col0 + j)*heightA;

                    if (k == 0) {
                        firstTouch.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthC);
                    } else {
                        accumulate.apply(A.original.data, B.original.data, C.original.data,
                                indexA, indexB, indexC, heightA, widthA, widthC);
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }
}
