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

//CONCURRENT_INLINE import static org.ejml.dense.block.InnerRankUpdate_DDRB.*;
//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

/// Performs rank-n update operations on the inner blocks of a [DMatrixRBlock]
/// It is assumed and not checked that the submatrices are aligned along the matrix's blocks.
///
/// @author Peter Abeles
public class InnerRankUpdate_DDRB {

    /// Performs:
    ///
    /// A = A + α B <sup>T</sup>B
    ///
    /// @param blockLength Size of the block in the block matrix.
    /// @param alpha scaling factor for right hand side.
    /// @param A Block aligned submatrix.
    /// @param B Block aligned submatrix.
    public static void rankNUpdate( int blockLength, double alpha, DSubmatrixD1 A, DSubmatrixD1 B ) {

        int heightB = B.row1 - B.row0;
        if (heightB > blockLength)
            throw new IllegalArgumentException("Rows of B cannot be greater than the block length");

        int N = B.col1 - B.col0;

        if (A.col1 - A.col0 != N)
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if (A.row1 - A.row0 != N)
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.col0,B.col1,blockLength,i->{
        for (int i = B.col0; i < B.col1; i += blockLength) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength, B.col1 - i);

            int rowA = i - B.col0 + A.row0;
            int heightA = Math.min(blockLength, A.row1 - rowA);

            for (int j = B.col0; j < B.col1; j += blockLength) {

                int widthB_j = Math.min(blockLength, B.col1 - j);

                int indexA = rowA*A.original.numCols + (j - B.col0 + A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;

                InnerMultiplication_DDRB.blockMultPlusTransA(alpha,
                        B.original.data, B.original.data, A.original.data,
                        heightB, widthB_i, widthB_j, indexB_i, indexB_j, indexA);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Rank N update function for a symmetric inner submatrix and only operates on the upper
    /// triangular portion of the submatrix.
    ///
    /// A = A - B <sup>T</sup>B
    ///
    public static void symmRankNMinus_U( int blockLength,
                                         DSubmatrixD1 A, DSubmatrixD1 B ) {

        int heightB = B.row1 - B.row0;
        if (heightB > blockLength)
            throw new IllegalArgumentException("Rows of B cannot be greater than the block length");

        int N = B.col1 - B.col0;

        if (A.col1 - A.col0 != N)
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's width");
        if (A.row1 - A.row0 != N)
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's width");


        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.col0,B.col1,blockLength,i->{
        for (int i = B.col0; i < B.col1; i += blockLength) {

            int indexB_i = B.row0*B.original.numCols + i*heightB;
            int widthB_i = Math.min(blockLength, B.col1 - i);

            int rowA = i - B.col0 + A.row0;
            int heightA = Math.min(blockLength, A.row1 - rowA);

            for (int j = i; j < B.col1; j += blockLength) {

                int widthB_j = Math.min(blockLength, B.col1 - j);

                int indexA = rowA*A.original.numCols + (j - B.col0 + A.col0)*heightA;
                int indexB_j = B.row0*B.original.numCols + j*heightB;

                if (i == j) {
                    // only the upper portion of this block needs to be modified since it is along a diagonal
                    multTransABlockMinus_U(B.original.data, A.original.data,
                            indexB_i, indexB_j, indexA, heightB, widthB_i, widthB_j);
                } else {
                    multTransABlockMinus(B.original.data, A.original.data,
                            indexB_i, indexB_j, indexA, heightB, widthB_i, widthB_j);
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Rank N update function for a symmetric inner submatrix and only operates on the lower
    /// triangular portion of the submatrix.
    ///
    /// A = A - B\*B<sup>T</sup>
    public static void symmRankNMinus_L( int blockLength,
                                         DSubmatrixD1 A, DSubmatrixD1 B ) {
        int widthB = B.col1 - B.col0;
        if (widthB > blockLength)
            throw new IllegalArgumentException("Cols of B cannot be greater than the block length");

        int N = B.row1 - B.row0;

        if (A.col1 - A.col0 != N)
            throw new IllegalArgumentException("A does not have the expected number of columns based on B's height");
        if (A.row1 - A.row0 != N)
            throw new IllegalArgumentException("A does not have the expected number of rows based on B's height");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.row0,B.row1,blockLength,i->{
        for (int i = B.row0; i < B.row1; i += blockLength) {
            int heightB_i = Math.min(blockLength, B.row1 - i);
            int indexB_i = i*B.original.numCols + heightB_i*B.col0;

            int rowA = i - B.row0 + A.row0;
            int heightA = Math.min(blockLength, A.row1 - rowA);

            for (int j = B.row0; j <= i; j += blockLength) {

                int widthB_j = Math.min(blockLength, B.row1 - j);

                int indexA = rowA*A.original.numCols + (j - B.row0 + A.col0)*heightA;
                int indexB_j = j*B.original.numCols + widthB_j*B.col0;

                if (i == j) {
                    multTransBBlockMinus_L(B.original.data, A.original.data,
                            indexB_i, indexB_j, indexA, widthB, heightB_i, widthB_j);
                } else {
                    multTransBBlockMinus(B.original.data, A.original.data,
                            indexB_i, indexB_j, indexA, widthB, heightB_i, widthB_j);
                }
            }
        }
        //CONCURRENT_ABOVE });
    }
    //CONCURRENT_OMIT_BEGIN

    /// Performs the following operation on a block:
    ///
    /// c = c - a<sup>T</sup>a
    protected static void multTransABlockMinus( double[] dataA, double[] dataC,
                                                int indexA, int indexB, int indexC,
                                                final int heightA, final int widthA, final int widthC ) {
//        for (int i = 0; i < widthA; i++) {
//            for (int k = 0; k < heightA; k++) {
//
//                double valA = dataA[k*widthA + i + indexA];
//                for (int j = 0; j < widthC; j++) {
//                    dataC[i*widthC + j + indexC] -= valA*dataA[k*widthC + j + indexB];
//                }
//            }
//        }

        final int jBlockedEnd = widthC & ~3;

        for (int i = 0; i < widthA; i++) {
            int j = 0;

            for (; j < jBlockedEnd; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int aIdx = indexA + i;       // dataA[k*widthA + i]
                int bIdx = indexB + j;       // dataA[k*widthC + j]
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    double valA = dataA[aIdx];
                    s0 += valA*dataA[bIdx];
                    s1 += valA*dataA[bIdx + 1];
                    s2 += valA*dataA[bIdx + 2];
                    s3 += valA*dataA[bIdx + 3];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                int cIdx = indexC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // tail: widthC not a multiple of 4
            for (; j < widthC; j++) {
                double s = 0.0;
                int aIdx = indexA + i;
                int bIdx = indexB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    s += dataA[aIdx]*dataA[bIdx];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                dataC[indexC + i*widthC + j] -= s;
            }
        }
    }

    /// Performs the following operation on the upper triangular portion of a block:
    ///
    /// c = c - a<sup>T</sup>a
    protected static void multTransABlockMinus_U( double[] dataA, double[] dataC,
                                                  int indexA, int indexB, int indexC,
                                                  final int heightA, final int widthA, final int widthC ) {
//        for (int i = 0; i < widthA; i++) {
//            for (int k = 0; k < heightA; k++) {
//
//                double valA = dataA[k*widthA + i + indexA];
//                for (int j = i; j < widthC; j++) {
//                    dataC[i*widthC + j + indexC] -= valA*dataA[k*widthC + j + indexB];
//                }
//            }
//        }

        for (int i = 0; i < widthA; i++) {
            int jRampEnd = (i + 3) & ~3;
            if (jRampEnd > widthC) jRampEnd = widthC;
            int jBlockEnd = widthC & ~3;
            if (jBlockEnd < jRampEnd) jBlockEnd = jRampEnd;

            int j = i;

            // ramp: scalar from i up to next multiple of 4
            for (; j < jRampEnd; j++) {
                double s = 0.0;
                int aIdx = indexA + i;
                int bIdx = indexB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    s += dataA[aIdx]*dataA[bIdx];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                dataC[indexC + i*widthC + j] -= s;
            }

            // steady-state JB=4
            for (; j < jBlockEnd; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int aIdx = indexA + i;
                int bIdx = indexB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    double valA = dataA[aIdx];
                    s0 += valA*dataA[bIdx];
                    s1 += valA*dataA[bIdx + 1];
                    s2 += valA*dataA[bIdx + 2];
                    s3 += valA*dataA[bIdx + 3];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                int cIdx = indexC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // scalar tail
            for (; j < widthC; j++) {
                double s = 0.0;
                int aIdx = indexA + i;
                int bIdx = indexB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    s += dataA[aIdx]*dataA[bIdx];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                dataC[indexC + i*widthC + j] -= s;
            }
        }
    }

    /// Performs the following operation on a block:
    ///
    /// c = c - a\*a<sup>T</sup>
    protected static void multTransBBlockMinus( final double[] dataA, final double[] dataC,
                                                final int indexA, final int indexB, final int indexC,
                                                final int widthA, final int heightA, final int widthC ) {
//        for (int i = 0; i < heightA; i++) {
//            for (int j = 0; j < widthC; j++) {
//                double sum = 0;
//                for (int k = 0; k < widthA; k++) {
//                    sum += dataA[i*widthA + k + indexA]*dataA[j*widthA + k + indexB];
//                }
//                dataC[i*widthC + j + indexC] -= sum;
//            }
//        }

        final int jEnd4 = widthC & ~3;

        for (int i = 0; i < heightA; i++) {
            final int aIdx = indexA + i*widthA;
            final int aEnd = aIdx + widthA;
            int j = 0;

            for (; j < jEnd4; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int a = aIdx;
                int b0 = indexB + (j + 0)*widthA;
                int b1 = indexB + (j + 1)*widthA;
                int b2 = indexB + (j + 2)*widthA;
                int b3 = indexB + (j + 3)*widthA;
                while (a != aEnd) {
                    double valA = dataA[a++];
                    s0 += valA*dataA[b0++];
                    s1 += valA*dataA[b1++];
                    s2 += valA*dataA[b2++];
                    s3 += valA*dataA[b3++];
                }
                int cIdx = indexC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // scalar tail
            for (; j < widthC; j++) {
                double sum = 0.0;
                int a = aIdx;
                int b = indexB + j*widthA;
                while (a != aEnd) {
                    sum += dataA[a++]*dataA[b++];
                }
                dataC[indexC + i*widthC + j] -= sum;
            }
        }
    }

    /// Performs the following operation on the lower triangular portion of a block:
    ///
    /// c = c - a\*a<sup>T</sup>
    protected static void multTransBBlockMinus_L( double[] dataA, double[] dataC,
                                                  int indexA, int indexB, int indexC,
                                                  final int widthA, final int heightA, final int widthC ) {
//        for (int i = 0; i < heightA; i++) {
//            for (int j = 0; j <= i; j++) {
//                double sum = 0;
//                for (int k = 0; k < widthA; k++) {
//                    sum += dataA[i*widthA + k + indexA]*dataA[j*widthA + k + indexB];
//                }
//                dataC[i*widthC + j + indexC] -= sum;
//            }
//        }

        for (int i = 0; i < heightA; i++) {
            final int aIdx = indexA + i*widthA;
            final int aEnd = aIdx + widthA;
            int jBlockEnd = (i + 1) & ~3;
            int j = 0;

            for (; j < jBlockEnd; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int a = aIdx;
                int b0 = indexB + (j + 0)*widthA;
                int b1 = indexB + (j + 1)*widthA;
                int b2 = indexB + (j + 2)*widthA;
                int b3 = indexB + (j + 3)*widthA;
                while (a != aEnd) {
                    double valA = dataA[a++];
                    s0 += valA*dataA[b0++];
                    s1 += valA*dataA[b1++];
                    s2 += valA*dataA[b2++];
                    s3 += valA*dataA[b3++];
                }
                int cIdx = indexC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // ramp-down tail: j from jBlockEnd up to and including i
            for (; j <= i; j++) {
                double sum = 0.0;
                int a = aIdx;
                int b = indexB + j*widthA;
                while (a != aEnd) {
                    sum += dataA[a++]*dataA[b++];
                }
                dataC[indexC + i*widthC + j] -= sum;
            }
        }
    }
    //CONCURRENT_OMIT_END
}
