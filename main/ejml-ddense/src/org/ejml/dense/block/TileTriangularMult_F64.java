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

/// Triangular matrix multiplication for block matrices — the LAPACK `TRMM` analog. Each op multiplies a
/// square implicitly-triangular matrix `t` by a general matrix `b` into `c`, either setting (`c = ...`) or
/// accumulating (`c += ...`).
///
/// Function name: `(l|r)mult[Add][Unit](Low|Upp)[TransT]`
/// * `lmult` / `rmult` — triangle on the left (`c = t*b`) or right (`c = b*t`).
/// * `Add` — accumulate into `c`; absent means set.
/// * `Unit` — `t` has an implicit unit diagonal.
/// * `Low` / `Upp` — `t` is lower or upper triangular; the opposite triangle is implicitly zero.
/// * `TransT` — apply transpose to `t` before mult
///
/// Matrix Shapes: `t` is `m` by `m`. `b` and `c` are `m` by `n` for `lmult`, `n` by `m` for `rmult`.
///
/// Arguments (order: arrays, dimensions, strides, offsets):
/// * `t` — implicit triangular matrix: `m` by `m`. Not modified.
/// * `b` — general input. Not modified.
/// * `c` — general output.
/// * `m` — dimension of the square triangle `t`.
/// * `n` — other dimension of `b` and `c`.
/// * `strideT`, `strideB`, `strideC` — row stride of each matrix.
/// * `offsetT`, `offsetB`, `offsetC` — start index of each matrix.
///
/// `t(row,col) = t[offsetT + row*strideT + col]`; `b` and `c` are addressed the same way. Loops use the standard
/// convention: `i` = output row, `j` = output column, `k` = contraction.
public class TileTriangularMult_F64 {
    /// `c = t*b`, with unit lower-triangular `t`.
    public static void lmultUnitLow( double[] t, double[] b, double[] c,
                                     int m, int n,
                                     int strideT, int strideB, int strideC,
                                     int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = b[offsetB + i*strideB + j];
                for (int k = 0; k < i; k++) {
                    sum += t[offsetT + i*strideT + k]*b[offsetB + k*strideB + j];
                }
                c[offsetC + i*strideC + j] = sum;
            }
        }
    }

    /// `c = c + t*b`, with unit lower-triangular `t`.
    public static void lmultAddUnitLow( double[] t, double[] b, double[] c,
                                        int m, int n,
                                        int strideT, int strideB, int strideC,
                                        int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int k = 0; k <= i; k++) {
                double valT = i == k ? 1.0 : t[offsetT + i*strideT + k];
                for (int j = 0; j < n; j++) {
                    c[offsetC + i*strideC + j] += valT*b[offsetB + k*strideB + j];
                }
            }
        }
    }

    /// `c = t`<sup>T</sup>`*b`, with unit lower-triangular `t`.
    public static void lmultUnitLowTransT( double[] t, double[] b, double[] c,
                                           int m, int n,
                                           int strideT, int strideB, int strideC,
                                           int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = b[offsetB + i*strideB + j];
                for (int k = i + 1; k < m; k++) {
                    sum += t[offsetT + k*strideT + i]*b[offsetB + k*strideB + j];
                }
                c[offsetC + i*strideC + j] = sum;
            }
        }
    }

    /// `c = c + t`<sup>T</sup>`*b`, with unit lower-triangular `t`.
    public static void lmultAddUnitLowTransT( double[] t, double[] b, double[] c,
                                              int m, int n,
                                              int strideT, int strideB, int strideC,
                                              int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int k = i; k < m; k++) {
                double valT = i == k ? 1.0 : t[offsetT + k*strideT + i];
                for (int j = 0; j < n; j++) {
                    c[offsetC + i*strideC + j] += valT*b[offsetB + k*strideB + j];
                }
            }
        }
    }

    /// `c = t*b`, with unit upper-triangular `t`.
    public static void lmultUnitUpp( double[] t, double[] b, double[] c,
                                     int m, int n,
                                     int strideT, int strideB, int strideC,
                                     int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = b[offsetB + i*strideB + j];
                for (int k = i + 1; k < m; k++) {
                    sum += t[offsetT + i*strideT + k]*b[offsetB + k*strideB + j];
                }
                c[offsetC + i*strideC + j] = sum;
            }
        }
    }

    /// `c = c + t*b`, with unit upper-triangular `t`.
    public static void lmultAddUnitUpp( double[] t, double[] b, double[] c,
                                        int m, int n,
                                        int strideT, int strideB, int strideC,
                                        int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int k = i; k < m; k++) {
                double valT = i == k ? 1.0 : t[offsetT + i*strideT + k];
                for (int j = 0; j < n; j++) {
                    c[offsetC + i*strideC + j] += valT*b[offsetB + k*strideB + j];
                }
            }
        }
    }

    /// `c = t`<sup>T</sup>`*b`, with unit upper-triangular `t`.
    public static void lmultUnitUppTransT( double[] t, double[] b, double[] c,
                                           int m, int n,
                                           int strideT, int strideB, int strideC,
                                           int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = b[offsetB + i*strideB + j];
                for (int k = 0; k < i; k++) {
                    sum += t[offsetT + k*strideT + i]*b[offsetB + k*strideB + j];
                }
                c[offsetC + i*strideC + j] = sum;
            }
        }
    }

    /// `c = c + t`<sup>T</sup>`*b`, with unit upper-triangular `t`.
    public static void lmultAddUnitUppTransT( double[] t, double[] b, double[] c,
                                              int m, int n,
                                              int strideT, int strideB, int strideC,
                                              int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < m; i++) {
            for (int k = 0; k <= i; k++) {
                double valT = i == k ? 1.0 : t[offsetT + k*strideT + i];
                for (int j = 0; j < n; j++) {
                    c[offsetC + i*strideC + j] += valT*b[offsetB + k*strideB + j];
                }
            }
        }
    }

    /// `c = c + b*t`, with unit lower-triangular `t`.
    public static void rmultAddUnitLow( double[] t, double[] b, double[] c,
                                        int m, int n,
                                        int strideT, int strideB, int strideC,
                                        int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                double valB = b[offsetB + i*strideB + k];
                c[offsetC + i*strideC + k] += valB;
                for (int j = 0; j < k; j++) {
                    c[offsetC + i*strideC + j] += valB*t[offsetT + k*strideT + j];
                }
            }
        }
    }

    /// `c = c + b*t`, with unit upper-triangular `t`.
    public static void rmultAddUnitUpp( double[] t, double[] b, double[] c,
                                        int m, int n,
                                        int strideT, int strideB, int strideC,
                                        int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                double valB = b[offsetB + i*strideB + k];
                c[offsetC + i*strideC + k] += valB;
                for (int j = k + 1; j < m; j++) {
                    c[offsetC + i*strideC + j] += valB*t[offsetT + k*strideT + j];
                }
            }
        }
    }

    /// `c = b*t`<sup>T</sup>, with unit upper-triangular `t`.
    public static void rmultUnitUppTransT( double[] t, double[] b, double[] c,
                                           int m, int n,
                                           int strideT, int strideB, int strideC,
                                           int offsetT, int offsetB, int offsetC ) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = b[offsetB + i*strideB + j];
                for (int k = j + 1; k < m; k++) {
                    sum += b[offsetB + i*strideB + k]*t[offsetT + j*strideT + k];
                }
                c[offsetC + i*strideC + j] = sum;
            }
        }
    }
}
