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

package org.ejml.dense.row.mult;

/// Experimental loop-ordering variants of [InnerMultiplication_DDRB.blockMultPlus].
///
/// All compute the same operation:
///
/// C = C + A \* B
///
/// Each variant uses one of the 6 permutations of the (i, j, k) loop nest with the
/// innermost loop unrolled by 4. The natural form (dot-product vs rank-1) is dictated
/// by which loop is innermost; loop-invariant subexpressions are hoisted as appropriate.
///
/// Indexing convention (matches [InnerMultiplication_DDRB]):
/// - dataA: heightA x widthA, element (i,k) at indexA + i*widthA + k
/// - dataB: widthA  x widthC, element (k,j) at indexB + k*widthC + j
/// - dataC: heightA x widthC, element (i,j) at indexC + i*widthC + j
///
/// Kept here for documentation of the optimization-space exploration.
/// Not part of the production API.
///
/// To simulate older hardware add the following flags:
/// -XX:UseSSE=4 -XX:UseAVX=0
///
/// | Variant                  |  m=40 |  m=60 |  m=80 |
/// |--------------------------|------:|------:|------:|
/// | packed_tile4x4_scratch   |  9.41 |  9.91 | 10.06 |
/// | packed                   |  9.16 |  9.70 | 10.09 |
/// | packed_scratch           |  8.03 |  8.82 |  8.49 |
/// | jik_I4                   |  8.23 |  8.63 |  8.79 |
/// | ikj_K4                   |  8.42 |  8.72 |  8.79 |
/// | packed_jik_I4_scratch    |  7.87 |  8.57 |  8.73 |
/// | tile4x4                  |  7.94 |  8.04 |  8.13 |
/// | ijk                      |  6.57 |  6.79 |  6.97 |
/// | baseline                 |  6.41 |  6.78 |  6.91 |
/// | ikj                      |  5.88 |  6.41 |  6.69 |
/// | kij                      |  5.68 |  6.21 |  6.31 |
/// | jik                      |  5.68 |  6.11 |  6.40 |
/// | ikj_for                  |  5.81 |  6.22 |  5.87 |
/// | kij_I4                   |  5.72 |  5.74 |  5.77 |
/// | kji                      |  2.78 |  2.83 |  2.92 |
/// | jki                      |  2.81 |  2.82 |  2.18 |
///
/// Units: GFLOPS. Higher is better. Computed as 2·m³ / time.
///
/// See BenchmarkMultPlusExperiments_DDRB
public class BlockMultPlusExperiments_DDRB {

    /// Plain for loop with no inlining
    public static void blockMultPlus_ikj_for( final double[] dataA, final double[] dataB, final double[] dataC,
                                               int indexA, int indexB, int indexC,
                                               final int heightA, final int widthA, final int widthC ) {
        for( int i = 0; i < heightA; i++ ) {
            for( int k = 0; k < widthA; k++ ) {
                double valA = dataA[i*widthA + k + indexA];
                int iterC = i*widthC + indexC;
                int iterB = k*widthC + indexB;
                for( int j = 0; j < widthC; j++ ) {
                    dataC[iterC++] += valA * dataB[iterB++];
                }
            }
        }
    }

    /// Order: i (outer), j (middle), k (inner).
    /// Form: dot-product. Inner k accumulates s across full k-sweep, KB=4 unroll.
    /// Hoist: per (i,j), pointer aIdx walks contiguously through k; bIdx walks strided.
    public static void blockMultPlus_ijk( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int kEnd4 = widthA & ~3;

        for (int i = 0; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            final int rowC = indexC + i*widthC;
            for (int j = 0; j < widthC; j++) {
                double s = 0.0;
                int aIdx = aRow;
                int bIdx = indexB + j;
                int k = 0;
                // KB=4: 4 unrolled k-steps with one accumulator
                for (; k < kEnd4; k += 4) {
                    s += dataA[aIdx]*dataB[bIdx]
                            + dataA[aIdx + 1]*dataB[bIdx + widthC]
                            + dataA[aIdx + 2]*dataB[bIdx + 2*widthC]
                            + dataA[aIdx + 3]*dataB[bIdx + 3*widthC];
                    aIdx += 4;
                    bIdx += 4*widthC;
                }
                for (; k < widthA; k++) {
                    s += dataA[aIdx]*dataB[bIdx];
                    aIdx++;
                    bIdx += widthC;
                }
                dataC[rowC + j] += s;
            }
        }
    }

    /// Order: i (outer), k (middle), j (inner).
    /// Form: rank-1. Inner j is JB=4 unroll, walking dataB and dataC contiguously.
    /// Hoist: valA = dataA[i*widthA+k] is loop-invariant in j; computed once per (i,k).
    public static void blockMultPlus_ikj( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int jEnd4 = widthC & ~3;

        for (int i = 0; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            final int aEnd = aRow + widthA;
            final int rowC = indexC + i*widthC;
            int b = indexB;
            for (int a = aRow; a != aEnd; a++) {
                double valA = dataA[a];
                int j = 0;
                for (; j < jEnd4; j += 4) {
                    dataC[rowC + j] += valA*dataB[b];
                    dataC[rowC + j + 1] += valA*dataB[b + 1];
                    dataC[rowC + j + 2] += valA*dataB[b + 2];
                    dataC[rowC + j + 3] += valA*dataB[b + 3];
                    b += 4;
                }
                for (; j < widthC; j++) {
                    dataC[rowC + j] += valA*dataB[b++];
                }
            }
        }
    }

    /// Order: j (outer), i (middle), k (inner).
    /// Form: dot-product. Inner k with KB=4 unroll, single accumulator.
    /// Hoist: per (j,i), bIdx walks down a column of dataB strided by widthC.
    /// Outer-j means dataB column is held in cache across all i for the same j.
    public static void blockMultPlus_jik( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int kEnd4 = widthA & ~3;

        for (int j = 0; j < widthC; j++) {
            for (int i = 0; i < heightA; i++) {
                double s = 0.0;
                int aIdx = indexA + i*widthA;
                int bIdx = indexB + j;
                int k = 0;
                for (; k < kEnd4; k += 4) {
                    s += dataA[aIdx]*dataB[bIdx]
                            + dataA[aIdx + 1]*dataB[bIdx + widthC]
                            + dataA[aIdx + 2]*dataB[bIdx + 2*widthC]
                            + dataA[aIdx + 3]*dataB[bIdx + 3*widthC];
                    aIdx += 4;
                    bIdx += 4*widthC;
                }
                for (; k < widthA; k++) {
                    s += dataA[aIdx]*dataB[bIdx];
                    aIdx++;
                    bIdx += widthC;
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }

    /// Order: j (outer), k (middle), i (inner).
    /// Form: rank-1 with i innermost. IB=4 unroll on i.
    /// Hoist: bVal = dataB[k*widthC+j] is loop-invariant in i (computed once per (j,k)).
    /// Note: dataA and dataC are both strided in i (by widthA and widthC), so this is
    /// the "all-strided inner" pattern. The single-scalar bVal hoist is the only natural win.
    public static void blockMultPlus_jki( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int iEnd4 = heightA & ~3;

        for (int j = 0; j < widthC; j++) {
            for (int k = 0; k < widthA; k++) {
                double bVal = dataB[indexB + k*widthC + j];
                int i = 0;
                for (; i < iEnd4; i += 4) {
                    dataC[indexC + (i)*widthC + j] += dataA[indexA + (i)*widthA + k]*bVal;
                    dataC[indexC + (i + 1)*widthC + j] += dataA[indexA + (i + 1)*widthA + k]*bVal;
                    dataC[indexC + (i + 2)*widthC + j] += dataA[indexA + (i + 2)*widthA + k]*bVal;
                    dataC[indexC + (i + 3)*widthC + j] += dataA[indexA + (i + 3)*widthA + k]*bVal;
                }
                for (; i < heightA; i++) {
                    dataC[indexC + i*widthC + j] += dataA[indexA + i*widthA + k]*bVal;
                }
            }
        }
    }

    /// Order: k (outer), i (middle), j (inner).
    /// Form: rank-1. Inner j is JB=4 unroll.
    /// Hoist: valA = dataA[i*widthA+k] loop-invariant in j.
    /// Outer-k means a "row of B" (dataB[k*widthC+..]) is held in L1 across all i.
    public static void blockMultPlus_kij( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int jEnd4 = widthC & ~3;

        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            for (int i = 0; i < heightA; i++) {
                double valA = dataA[indexA + i*widthA + k];
                final int rowC = indexC + i*widthC;
                int j = 0;
                for (; j < jEnd4; j += 4) {
                    dataC[rowC + j] += valA*dataB[bRow + j];
                    dataC[rowC + j + 1] += valA*dataB[bRow + j + 1];
                    dataC[rowC + j + 2] += valA*dataB[bRow + j + 2];
                    dataC[rowC + j + 3] += valA*dataB[bRow + j + 3];
                }
                for (; j < widthC; j++) {
                    dataC[rowC + j] += valA*dataB[bRow + j];
                }
            }
        }
    }

    /// Order: k (outer), j (middle), i (inner).
    /// Form: rank-1 with i innermost. IB=4 unroll on i.
    /// Hoist: bVal = dataB[k*widthC+j] is loop-invariant in i.
    /// Same all-strided inner pattern as jki; differs in outer cache behavior.
    public static void blockMultPlus_kji( final double[] dataA, final double[] dataB, final double[] dataC,
                                          int indexA, int indexB, int indexC,
                                          final int heightA, final int widthA, final int widthC ) {
        final int iEnd4 = heightA & ~3;

        for (int k = 0; k < widthA; k++) {
            for (int j = 0; j < widthC; j++) {
                double bVal = dataB[indexB + k*widthC + j];
                int i = 0;
                for (; i < iEnd4; i += 4) {
                    dataC[indexC + (i)*widthC + j] += dataA[indexA + (i)*widthA + k]*bVal;
                    dataC[indexC + (i + 1)*widthC + j] += dataA[indexA + (i + 1)*widthA + k]*bVal;
                    dataC[indexC + (i + 2)*widthC + j] += dataA[indexA + (i + 2)*widthA + k]*bVal;
                    dataC[indexC + (i + 3)*widthC + j] += dataA[indexA + (i + 3)*widthA + k]*bVal;
                }
                for (; i < heightA; i++) {
                    dataC[indexC + i*widthC + j] += dataA[indexA + i*widthA + k]*bVal;
                }
            }
        }
    }

    /// Order: i, k, j with k blocked by 4 at the middle (KB=4).
    /// 4 valA's hoisted from a-row; 4 b-row starts cached. Inner j does ONE c-RMW
    /// fusing 4 multiplies, reducing dataC store traffic by 4x vs baseline ikj.
    public static void blockMultPlus_ikj_K4( final double[] dataA, final double[] dataB, final double[] dataC,
                                             int indexA, int indexB, int indexC,
                                             final int heightA, final int widthA, final int widthC ) {
        final int kEnd4 = widthA & ~3;

        for (int i = 0; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            final int rowC = indexC + i*widthC;
            int k = 0;
            for (; k < kEnd4; k += 4) {
                double a0 = dataA[aRow + k    ];
                double a1 = dataA[aRow + k + 1];
                double a2 = dataA[aRow + k + 2];
                double a3 = dataA[aRow + k + 3];
                final int b0 = indexB + (k    )*widthC;
                final int b1 = indexB + (k + 1)*widthC;
                final int b2 = indexB + (k + 2)*widthC;
                final int b3 = indexB + (k + 3)*widthC;
                for (int j = 0; j < widthC; j++) {
                    dataC[rowC + j] += a0*dataB[b0 + j]
                            + a1*dataB[b1 + j]
                            + a2*dataB[b2 + j]
                            + a3*dataB[b3 + j];
                }
            }
            // k tail
            for (; k < widthA; k++) {
                double valA = dataA[aRow + k];
                final int b = indexB + k*widthC;
                for (int j = 0; j < widthC; j++) {
                    dataC[rowC + j] += valA*dataB[b + j];
                }
            }
        }
    }

    /// Order: k, i, j with i blocked by 4 at the middle (IB=4).
    /// 4 valA's hoisted; inner j shares a single dataB[bRow+j] load across 4 FMAs,
    /// cutting redundant b-loads by 4x. Writes to 4 different c-rows per j.
    public static void blockMultPlus_kij_I4( final double[] dataA, final double[] dataB, final double[] dataC,
                                             int indexA, int indexB, int indexC,
                                             final int heightA, final int widthA, final int widthC ) {
        final int iEnd4 = heightA & ~3;

        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            int i = 0;
            for (; i < iEnd4; i += 4) {
                double a0 = dataA[indexA + (i    )*widthA + k];
                double a1 = dataA[indexA + (i + 1)*widthA + k];
                double a2 = dataA[indexA + (i + 2)*widthA + k];
                double a3 = dataA[indexA + (i + 3)*widthA + k];
                final int rowC0 = indexC + (i    )*widthC;
                final int rowC1 = indexC + (i + 1)*widthC;
                final int rowC2 = indexC + (i + 2)*widthC;
                final int rowC3 = indexC + (i + 3)*widthC;
                for (int j = 0; j < widthC; j++) {
                    double bv = dataB[bRow + j];
                    dataC[rowC0 + j] += a0*bv;
                    dataC[rowC1 + j] += a1*bv;
                    dataC[rowC2 + j] += a2*bv;
                    dataC[rowC3 + j] += a3*bv;
                }
            }
            // i tail
            for (; i < heightA; i++) {
                double valA = dataA[indexA + i*widthA + k];
                final int rowC = indexC + i*widthC;
                for (int j = 0; j < widthC; j++) {
                    dataC[rowC + j] += valA*dataB[bRow + j];
                }
            }
        }
    }

    /// Order: j, i, k with i blocked by 4 at the middle (IB=4).
    /// 4 a-row-walks share one strided b-column-walk in inner k.
    /// 4 dot-product accumulators in registers; 4 stores at end of k-sweep.
    public static void blockMultPlus_jik_I4( final double[] dataA, final double[] dataB, final double[] dataC,
                                             int indexA, int indexB, int indexC,
                                             final int heightA, final int widthA, final int widthC ) {
        final int iEnd4 = heightA & ~3;

        for (int j = 0; j < widthC; j++) {
            int i = 0;
            for (; i < iEnd4; i += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                final int a0 = indexA + (i    )*widthA;
                final int a1 = indexA + (i + 1)*widthA;
                final int a2 = indexA + (i + 2)*widthA;
                final int a3 = indexA + (i + 3)*widthA;
                int bIdx = indexB + j;
                for (int k = 0; k < widthA; k++) {
                    double bv = dataB[bIdx];
                    s0 += dataA[a0 + k]*bv;
                    s1 += dataA[a1 + k]*bv;
                    s2 += dataA[a2 + k]*bv;
                    s3 += dataA[a3 + k]*bv;
                    bIdx += widthC;
                }
                dataC[indexC + (i    )*widthC + j] += s0;
                dataC[indexC + (i + 1)*widthC + j] += s1;
                dataC[indexC + (i + 2)*widthC + j] += s2;
                dataC[indexC + (i + 3)*widthC + j] += s3;
            }
            // i tail
            for (; i < heightA; i++) {
                double s = 0.0;
                final int a = indexA + i*widthA;
                int bIdx = indexB + j;
                for (int k = 0; k < widthA; k++) {
                    s += dataA[a + k]*dataB[bIdx];
                    bIdx += widthC;
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }

    /// 4x4 register tile (i,j outer-blocked, k inner). 16 scalar accumulators
    /// hold a 4x4 c-tile across the full k-sweep. Classic GEMM register-tile shape.
    /// Risk: 16 accumulators may spill in HotSpot.
    public static void blockMultPlus_tile4x4( final double[] dataA, final double[] dataB, final double[] dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC ) {
        final int iEnd = heightA & ~3;
        final int jEnd = widthC & ~3;

        for (int i = 0; i < iEnd; i += 4) {
            for (int j = 0; j < jEnd; j += 4) {
                final int c0 = indexC + (i    )*widthC + j;
                final int c1 = indexC + (i + 1)*widthC + j;
                final int c2 = indexC + (i + 2)*widthC + j;
                final int c3 = indexC + (i + 3)*widthC + j;
                double t00 = dataC[c0    ], t01 = dataC[c0 + 1], t02 = dataC[c0 + 2], t03 = dataC[c0 + 3];
                double t10 = dataC[c1    ], t11 = dataC[c1 + 1], t12 = dataC[c1 + 2], t13 = dataC[c1 + 3];
                double t20 = dataC[c2    ], t21 = dataC[c2 + 1], t22 = dataC[c2 + 2], t23 = dataC[c2 + 3];
                double t30 = dataC[c3    ], t31 = dataC[c3 + 1], t32 = dataC[c3 + 2], t33 = dataC[c3 + 3];

                for (int k = 0; k < widthA; k++) {
                    double a0 = dataA[indexA + (i    )*widthA + k];
                    double a1 = dataA[indexA + (i + 1)*widthA + k];
                    double a2 = dataA[indexA + (i + 2)*widthA + k];
                    double a3 = dataA[indexA + (i + 3)*widthA + k];
                    final int b = indexB + k*widthC + j;
                    double b0 = dataB[b], b1 = dataB[b + 1], b2 = dataB[b + 2], b3 = dataB[b + 3];
                    t00 += a0*b0; t01 += a0*b1; t02 += a0*b2; t03 += a0*b3;
                    t10 += a1*b0; t11 += a1*b1; t12 += a1*b2; t13 += a1*b3;
                    t20 += a2*b0; t21 += a2*b1; t22 += a2*b2; t23 += a2*b3;
                    t30 += a3*b0; t31 += a3*b1; t32 += a3*b2; t33 += a3*b3;
                }

                dataC[c0    ] = t00; dataC[c0 + 1] = t01; dataC[c0 + 2] = t02; dataC[c0 + 3] = t03;
                dataC[c1    ] = t10; dataC[c1 + 1] = t11; dataC[c1 + 2] = t12; dataC[c1 + 3] = t13;
                dataC[c2    ] = t20; dataC[c2 + 1] = t21; dataC[c2 + 2] = t22; dataC[c2 + 3] = t23;
                dataC[c3    ] = t30; dataC[c3 + 1] = t31; dataC[c3 + 2] = t32; dataC[c3 + 3] = t33;
            }
            // j tail
            for (int j = jEnd; j < widthC; j++) {
                for (int ii = i; ii < i + 4; ii++) {
                    double s = dataC[indexC + ii*widthC + j];
                    for (int k = 0; k < widthA; k++) {
                        s += dataA[indexA + ii*widthA + k]*dataB[indexB + k*widthC + j];
                    }
                    dataC[indexC + ii*widthC + j] = s;
                }
            }
        }
        // i tail
        for (int i = iEnd; i < heightA; i++) {
            for (int j = 0; j < widthC; j++) {
                double s = dataC[indexC + i*widthC + j];
                for (int k = 0; k < widthA; k++) {
                    s += dataA[indexA + i*widthA + k]*dataB[indexB + k*widthC + j];
                }
                dataC[indexC + i*widthC + j] = s;
            }
        }
    }

    /// Packing variant: pack B into a transposed scratch buffer (B_pack[j,k] = B[k,j]),
    /// then run a TransB-shape JB=4 kernel against the packed buffer.
    /// Includes per-call allocation of the pack buffer -- not realistic for production
    /// (would use caller-provided/ThreadLocal scratch), but indicates whether the
    /// algorithmic win exists at all.
    public static void blockMultPlus_packed( final double[] dataA, final double[] dataB, final double[] dataC,
                                             int indexA, int indexB, int indexC,
                                             final int heightA, final int widthA, final int widthC ) {
        // Pack B: B_pack[j*widthA + k] = dataB[indexB + k*widthC + j]
        final double[] B_pack = new double[widthA*widthC];
        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            for (int j = 0; j < widthC; j++) {
                B_pack[j*widthA + k] = dataB[bRow + j];
            }
        }

        // TransB-shape JB=4 against packed buffer
        final int jEnd4 = widthC & ~3;
        for (int i = 0; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            final int aEnd = aRow + widthA;
            int j = 0;
            for (; j < jEnd4; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int a = aRow;
                int b0 = (j    )*widthA;
                int b1 = (j + 1)*widthA;
                int b2 = (j + 2)*widthA;
                int b3 = (j + 3)*widthA;
                while (a != aEnd) {
                    double valA = dataA[a++];
                    s0 += valA*B_pack[b0++];
                    s1 += valA*B_pack[b1++];
                    s2 += valA*B_pack[b2++];
                    s3 += valA*B_pack[b3++];
                }
                int c = indexC + i*widthC + j;
                dataC[c    ] += s0;
                dataC[c + 1] += s1;
                dataC[c + 2] += s2;
                dataC[c + 3] += s3;
            }
            for (; j < widthC; j++) {
                double s = 0.0;
                int a = aRow;
                int b = j*widthA;
                while (a != aEnd) {
                    s += dataA[a++]*B_pack[b++];
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }

    /// Like blockMultPlus_packed but takes a caller-provided scratch buffer.
    /// scratch must have length >= widthA*widthC. Pack region [0, widthA*widthC) is used.
    /// Removes per-call allocation cost; otherwise identical to blockMultPlus_packed.
    public static void blockMultPlus_packed_scratch( final double[] dataA, final double[] dataB, final double[] dataC,
                                                     int indexA, int indexB, int indexC,
                                                     final int heightA, final int widthA, final int widthC,
                                                     final double[] scratch ) {
        // Pack B: scratch[j*widthA + k] = dataB[indexB + k*widthC + j]
        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            for (int j = 0; j < widthC; j++) {
                scratch[j*widthA + k] = dataB[bRow + j];
            }
        }

        // TransB-shape JB=4 against packed buffer
        final int jEnd4 = widthC & ~3;
        for (int i = 0; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            final int aEnd = aRow + widthA;
            int j = 0;
            for (; j < jEnd4; j += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                int a = aRow;
                int b0 = (j    )*widthA;
                int b1 = (j + 1)*widthA;
                int b2 = (j + 2)*widthA;
                int b3 = (j + 3)*widthA;
                while (a != aEnd) {
                    double valA = dataA[a++];
                    s0 += valA*scratch[b0++];
                    s1 += valA*scratch[b1++];
                    s2 += valA*scratch[b2++];
                    s3 += valA*scratch[b3++];
                }
                int c = indexC + i*widthC + j;
                dataC[c    ] += s0;
                dataC[c + 1] += s1;
                dataC[c + 2] += s2;
                dataC[c + 3] += s3;
            }
            for (; j < widthC; j++) {
                double s = 0.0;
                int a = aRow;
                int b = j*widthA;
                while (a != aEnd) {
                    s += dataA[a++]*scratch[b++];
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }

    /// Pack B once into scratch (transposed), then run a 4x4 register tile against
    /// the packed buffer. After packing, both A and packed-B are accessed contiguously
    /// in k from the inner loop. scratch must have length >= widthA*widthC.
    public static void blockMultPlus_packed_tile4x4_scratch( final double[] dataA, final double[] dataB, final double[] dataC,
                                                             int indexA, int indexB, int indexC,
                                                             final int heightA, final int widthA, final int widthC,
                                                             final double[] scratch ) {
        // Pack B: scratch[j*widthA + k] = dataB[indexB + k*widthC + j]
        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            for (int j = 0; j < widthC; j++) {
                scratch[j*widthA + k] = dataB[bRow + j];
            }
        }

        final int iEnd = heightA & ~3;
        final int jEnd = widthC & ~3;

        for (int i = 0; i < iEnd; i += 4) {
            final int a0Row = indexA + (i    )*widthA;
            final int a1Row = indexA + (i + 1)*widthA;
            final int a2Row = indexA + (i + 2)*widthA;
            final int a3Row = indexA + (i + 3)*widthA;
            for (int j = 0; j < jEnd; j += 4) {
                double t00 = 0.0, t01 = 0.0, t02 = 0.0, t03 = 0.0;
                double t10 = 0.0, t11 = 0.0, t12 = 0.0, t13 = 0.0;
                double t20 = 0.0, t21 = 0.0, t22 = 0.0, t23 = 0.0;
                double t30 = 0.0, t31 = 0.0, t32 = 0.0, t33 = 0.0;
                final int b0Row = (j    )*widthA;
                final int b1Row = (j + 1)*widthA;
                final int b2Row = (j + 2)*widthA;
                final int b3Row = (j + 3)*widthA;
                // Both a-rows and packed-b-rows are contiguous in k. 16 FMAs per k.
                for (int k = 0; k < widthA; k++) {
                    double a0 = dataA[a0Row + k];
                    double a1 = dataA[a1Row + k];
                    double a2 = dataA[a2Row + k];
                    double a3 = dataA[a3Row + k];
                    double b0 = scratch[b0Row + k];
                    double b1 = scratch[b1Row + k];
                    double b2 = scratch[b2Row + k];
                    double b3 = scratch[b3Row + k];
                    t00 += a0*b0; t01 += a0*b1; t02 += a0*b2; t03 += a0*b3;
                    t10 += a1*b0; t11 += a1*b1; t12 += a1*b2; t13 += a1*b3;
                    t20 += a2*b0; t21 += a2*b1; t22 += a2*b2; t23 += a2*b3;
                    t30 += a3*b0; t31 += a3*b1; t32 += a3*b2; t33 += a3*b3;
                }
                final int c0 = indexC + (i    )*widthC + j;
                final int c1 = indexC + (i + 1)*widthC + j;
                final int c2 = indexC + (i + 2)*widthC + j;
                final int c3 = indexC + (i + 3)*widthC + j;
                dataC[c0    ] += t00; dataC[c0 + 1] += t01; dataC[c0 + 2] += t02; dataC[c0 + 3] += t03;
                dataC[c1    ] += t10; dataC[c1 + 1] += t11; dataC[c1 + 2] += t12; dataC[c1 + 3] += t13;
                dataC[c2    ] += t20; dataC[c2 + 1] += t21; dataC[c2 + 2] += t22; dataC[c2 + 3] += t23;
                dataC[c3    ] += t30; dataC[c3 + 1] += t31; dataC[c3 + 2] += t32; dataC[c3 + 3] += t33;
            }
            // j tail for the 4 i rows
            for (int j = jEnd; j < widthC; j++) {
                final int bRow = j*widthA;
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                for (int k = 0; k < widthA; k++) {
                    double bv = scratch[bRow + k];
                    s0 += dataA[a0Row + k]*bv;
                    s1 += dataA[a1Row + k]*bv;
                    s2 += dataA[a2Row + k]*bv;
                    s3 += dataA[a3Row + k]*bv;
                }
                dataC[indexC + (i    )*widthC + j] += s0;
                dataC[indexC + (i + 1)*widthC + j] += s1;
                dataC[indexC + (i + 2)*widthC + j] += s2;
                dataC[indexC + (i + 3)*widthC + j] += s3;
            }
        }
        // i tail
        for (int i = iEnd; i < heightA; i++) {
            final int aRow = indexA + i*widthA;
            for (int j = 0; j < widthC; j++) {
                final int bRow = j*widthA;
                double s = 0.0;
                for (int k = 0; k < widthA; k++) {
                    s += dataA[aRow + k]*scratch[bRow + k];
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }

    /// Pack B transposed into scratch, then run jik_I4 against the packed buffer.
    /// The strided-in-k B reads of jik_I4 become contiguous-in-k reads from scratch.
    /// scratch must have length >= widthA*widthC.
    public static void blockMultPlus_packed_jik_I4_scratch( final double[] dataA, final double[] dataB, final double[] dataC,
                                                            int indexA, int indexB, int indexC,
                                                            final int heightA, final int widthA, final int widthC,
                                                            final double[] scratch ) {
        // Pack B: scratch[j*widthA + k] = dataB[indexB + k*widthC + j]
        for (int k = 0; k < widthA; k++) {
            final int bRow = indexB + k*widthC;
            for (int j = 0; j < widthC; j++) {
                scratch[j*widthA + k] = dataB[bRow + j];
            }
        }

        // jik with i blocked by 4, reading bv contig-in-k from packed scratch
        final int iEnd4 = heightA & ~3;
        for (int j = 0; j < widthC; j++) {
            final int sBase = j*widthA;
            int i = 0;
            for (; i < iEnd4; i += 4) {
                double s0 = 0.0, s1 = 0.0, s2 = 0.0, s3 = 0.0;
                final int a0 = indexA + (i    )*widthA;
                final int a1 = indexA + (i + 1)*widthA;
                final int a2 = indexA + (i + 2)*widthA;
                final int a3 = indexA + (i + 3)*widthA;
                for (int k = 0; k < widthA; k++) {
                    double bv = scratch[sBase + k];
                    s0 += dataA[a0 + k]*bv;
                    s1 += dataA[a1 + k]*bv;
                    s2 += dataA[a2 + k]*bv;
                    s3 += dataA[a3 + k]*bv;
                }
                dataC[indexC + (i    )*widthC + j] += s0;
                dataC[indexC + (i + 1)*widthC + j] += s1;
                dataC[indexC + (i + 2)*widthC + j] += s2;
                dataC[indexC + (i + 3)*widthC + j] += s3;
            }
            for (; i < heightA; i++) {
                double s = 0.0;
                final int a = indexA + i*widthA;
                for (int k = 0; k < widthA; k++) {
                    s += dataA[a + k]*scratch[sBase + k];
                }
                dataC[indexC + i*widthC + j] += s;
            }
        }
    }
}