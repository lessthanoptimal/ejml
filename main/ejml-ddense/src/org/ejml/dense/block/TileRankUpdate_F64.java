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

/// Performs rank-n update operations on the inner blocks of a [DMatrixRBlock]
/// It is assumed and not checked that the submatrices are aligned along the matrix's blocks.
public class TileRankUpdate_F64 {
    /// Upper triangle only write: c = c - a<sup>T</sup>b, where `a` and `b` are tiles inside of `dataAB`
    protected static void tileMultMinusTransA_U( double[] dataAB, double[] dataC,
                                                 final int heightA, final int widthA, final int widthC,
                                                 int offsetA, int offsetB, int offsetC ) {
//        for (int i = 0; i < widthA; i++) {
//            for (int k = 0; k < heightA; k++) {
//
//                double valA = dataAB[k*widthA + i + offsetA];
//                for (int j = i; j < widthC; j++) {
//                    dataC[i*widthC + j + offsetC] -= valA*dataAB[k*widthC + j + offsetB];
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
                int aIdx = offsetA + i;
                int bIdx = offsetB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    s += dataAB[aIdx]*dataAB[bIdx];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                dataC[offsetC + i*widthC + j] -= s;
            }

            // steady-state JB=4
            for (; j < jBlockEnd; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int aIdx = offsetA + i;
                int bIdx = offsetB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    double valA = dataAB[aIdx];
                    s0 += valA*dataAB[bIdx];
                    s1 += valA*dataAB[bIdx + 1];
                    s2 += valA*dataAB[bIdx + 2];
                    s3 += valA*dataAB[bIdx + 3];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                int cIdx = offsetC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // scalar tail
            for (; j < widthC; j++) {
                double s = 0.0;
                int aIdx = offsetA + i;
                int bIdx = offsetB + j;
                final int aEnd = aIdx + heightA*widthA;
                while (aIdx != aEnd) {
                    s += dataAB[aIdx]*dataAB[bIdx];
                    aIdx += widthA;
                    bIdx += widthC;
                }
                dataC[offsetC + i*widthC + j] -= s;
            }
        }
    }

    /// Performs the following operation on the lower triangular portion of a block:
    ///
    /// Lower triangle only write: c = c - a\*b<sup>T</sup>, where `a` and `b` are tiles inside of `dataAB`
    protected static void tileMultMinusTransB_L( double[] dataAB, double[] dataC,
                                                 final int widthA, final int heightA, final int widthC,
                                                 int offsetA, int offsetB, int offsetC ) {
//        for (int i = 0; i < heightA; i++) {
//            for (int j = 0; j <= i; j++) {
//                double sum = 0;
//                for (int k = 0; k < widthA; k++) {
//                    sum += dataA[i*widthA + k + offsetA]*dataA[j*widthA + k + offsetB];
//                }
//                dataC[i*widthC + j + offsetC] -= sum;
//            }
//        }

        for (int i = 0; i < heightA; i++) {
            final int aIdx = offsetA + i*widthA;
            final int aEnd = aIdx + widthA;
            int jBlockEnd = (i + 1) & ~3;
            int j = 0;

            for (; j < jBlockEnd; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int a = aIdx;
                int b0 = offsetB + (j + 0)*widthA;
                int b1 = offsetB + (j + 1)*widthA;
                int b2 = offsetB + (j + 2)*widthA;
                int b3 = offsetB + (j + 3)*widthA;
                while (a != aEnd) {
                    double valA = dataAB[a++];
                    s0 += valA*dataAB[b0++];
                    s1 += valA*dataAB[b1++];
                    s2 += valA*dataAB[b2++];
                    s3 += valA*dataAB[b3++];
                }
                int cIdx = offsetC + i*widthC + j;
                dataC[cIdx] -= s0;
                dataC[cIdx + 1] -= s1;
                dataC[cIdx + 2] -= s2;
                dataC[cIdx + 3] -= s3;
            }

            // ramp-down tail: j from jBlockEnd up to and including i
            for (; j <= i; j++) {
                double sum = 0.0;
                int a = aIdx;
                int b = offsetB + j*widthA;
                while (a != aEnd) {
                    sum += dataAB[a++]*dataAB[b++];
                }
                dataC[offsetC + i*widthC + j] -= sum;
            }
        }
    }
}
