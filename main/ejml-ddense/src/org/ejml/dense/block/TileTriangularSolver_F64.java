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

/// Contains triangular solvers and inverters for inner blocks of a [DMatrixRBlock].
///
/// Contracts:
/// - Reads only the input triangle indicated by `Low`/`Upp`. Opposite triangle is ignored.
/// - Two-array invert and solve: Writes and reads only the specified triangle.
/// - Two-array invert variants accept the same array for input and output.
/// - One-array invert writes the solution in the opposing triangle
/// - Solve functions overwrite B; the triangular factor is never modified.
/// - Inputs must be non-singular; no zero-diagonal check or overflow scaling is performed.
///
/// Solve functions use the following naming scheme with all 16 possible permutations provided:
///
/// ```
/// (l|r)solve(Low|Upp)[Trans][BTrans]
/// ```
///
/// Where:
/// - `l` / `r` — left-side (B = T<sup>-1</sup>B) or right-side (B = BT<sup>-1</sup>) solve
/// - `Low` / `Upp`: T is lower or upper triangular
/// - `Trans` (after Low/Upp, optional): solve against T<sup>T</sup> instead of T
/// - `BTrans` (optional): B is stored in transposed layout. (n by m instead of m by n)
///
/// Left Solve: T*X=B and for Right Solve: X*T=B. Where T and B are known, T is triangular and X is the unknown
/// being solved for. All inputs are assumed to be matrices. Note that in the code below X and B are the same
/// matrix with the solution being written in place.
///
/// ## Algorithmic Notes
///
/// Algorithm for lower triangular inverse:
///
/// ```
/// for i = 0:m-1
///     for j = 0:i-1
///         val = 0
///         for k = j:i-1
///             val = val - L(i,k) * X(k,j)
///         end
///         x(i,j) = val / L(i,i)
///     end
///     x(i,i) = 1 / L(i,i)
/// end
/// ```
///
/// Solvers have their loops ordered to process sequential data inside the inner loops to encourage vectorization.
/// Depending on the layout it might be a dot-product or rank-1 update based solver.
public class TileTriangularSolver_F64 {

    /// Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
    ///
    /// @param L Lower triangular matrix being inverted. Not modified.
    /// @param L_inv Where the inverse is stored. Can be the same as L. Modified.
    /// @param m The number of rows and columns.
    /// @param offsetL which index does the L matrix start at.
    /// @param offsetL_inv which index does the L\_inv matrix start at.
    public static void invertLower( double[] L,
                                    double[] L_inv,
                                    int m,
                                    int offsetL,
                                    int offsetL_inv ) {
//    for (int i = 0; i < m; i++) {
//        double L_ii = L[offsetL + i*m + i];
//        for (int j = 0; j < i; j++) {
//            double val = 0;
//            for (int k = j; k < i; k++) {
//                val += L[offsetL + i*m + k]*L_inv[offsetL_inv + k*m + j];
//            }
//            L_inv[offsetL_inv + i*m + j] = -val/L_ii;
//        }
//        L_inv[offsetL_inv + i*m + i] = 1.0/L_ii;
//    }

        for (int i = 0; i < m; i++) {
            double L_ii = L[offsetL + i*m + i];
            int j = 0;
            for (; j + 4 <= i; j += 4) {
                double val0 = 0, val1 = 0, val2 = 0, val3 = 0;

                // Ramp-up inlining to remove the read to have other triangle be non-zero
                {
                    double Lik = L[offsetL + i*m + (j + 0)];
                    val0 += Lik*L_inv[offsetL_inv + (j + 0)*m + (j + 0)];
                }
                {
                    double Lik = L[offsetL + i*m + (j + 1)];
                    val0 += Lik*L_inv[offsetL_inv + (j + 1)*m + (j + 0)];
                    val1 += Lik*L_inv[offsetL_inv + (j + 1)*m + (j + 1)];
                }
                {
                    double Lik = L[offsetL + i*m + (j + 2)];
                    val0 += Lik*L_inv[offsetL_inv + (j + 2)*m + (j + 0)];
                    val1 += Lik*L_inv[offsetL_inv + (j + 2)*m + (j + 1)];
                    val2 += Lik*L_inv[offsetL_inv + (j + 2)*m + (j + 2)];
                }

                for (int k = j + 3; k < i; k++) {
                    double Lik = L[offsetL + i*m + k];
                    val0 += Lik*L_inv[offsetL_inv + k*m + (j + 0)];
                    val1 += Lik*L_inv[offsetL_inv + k*m + (j + 1)];
                    val2 += Lik*L_inv[offsetL_inv + k*m + (j + 2)];
                    val3 += Lik*L_inv[offsetL_inv + k*m + (j + 3)];
                }
                L_inv[offsetL_inv + i*m + (j + 0)] = -val0/L_ii;
                L_inv[offsetL_inv + i*m + (j + 1)] = -val1/L_ii;
                L_inv[offsetL_inv + i*m + (j + 2)] = -val2/L_ii;
                L_inv[offsetL_inv + i*m + (j + 3)] = -val3/L_ii;
            }
            for (; j < i; j++) {
                double val = 0;
                for (int k = j; k < i; k++) {
                    val += L[offsetL + i*m + k]*L_inv[offsetL_inv + k*m + j];
                }
                L_inv[offsetL_inv + i*m + j] = -val/L_ii;
            }
            L_inv[offsetL_inv + i*m + i] = 1.0/L_ii;
        }
    }

    /// Inverts a square lower triangular matrix:  L = L<sup>-1</sup>
    ///
    /// @param L Lower triangular matrix being inverted. Overwritten with inverted matrix. Modified.
    /// @param m The number of rows and columns.
    /// @param offsetL which index does the L matrix start at.
    public static void invertLower( double[] L,
                                    int m,
                                    int offsetL ) {
//        for (int i = 0; i < m; i++) {
//            double L_ii = L[offsetL + i*m + i];
//            for (int j = 0; j < i; j++) {
//                double val = 0;
//                for (int k = j; k < i; k++) {
//                    val += L[offsetL + i*m + k]*L[offsetL + k*m + j];
//                }
//                L[offsetL + i*m + j] = -val/L_ii;
//            }
//            L[offsetL + i*m + i] = 1.0/L_ii;
//        }

        for (int i = 0; i < m; i++) {
            double L_ii = L[offsetL + i*m + i];
            int j = 0;
            for (; j + 4 <= i; j += 4) {
                double val0 = 0, val1 = 0, val2 = 0, val3 = 0;
                {
                    double Lik = L[offsetL + i*m + (j + 0)];
                    val0 += Lik*L[offsetL + (j + 0)*m + (j + 0)];
                }
                {
                    double Lik = L[offsetL + i*m + (j + 1)];
                    val0 += Lik*L[offsetL + (j + 1)*m + (j + 0)];
                    val1 += Lik*L[offsetL + (j + 1)*m + (j + 1)];
                }
                {
                    double Lik = L[offsetL + i*m + (j + 2)];
                    val0 += Lik*L[offsetL + (j + 2)*m + (j + 0)];
                    val1 += Lik*L[offsetL + (j + 2)*m + (j + 1)];
                    val2 += Lik*L[offsetL + (j + 2)*m + (j + 2)];
                }
                for (int k = j + 3; k < i; k++) {
                    double Lik = L[offsetL + i*m + k];
                    val0 += Lik*L[offsetL + k*m + (j + 0)];
                    val1 += Lik*L[offsetL + k*m + (j + 1)];
                    val2 += Lik*L[offsetL + k*m + (j + 2)];
                    val3 += Lik*L[offsetL + k*m + (j + 3)];
                }
                L[offsetL + i*m + (j + 0)] = -val0/L_ii;
                L[offsetL + i*m + (j + 1)] = -val1/L_ii;
                L[offsetL + i*m + (j + 2)] = -val2/L_ii;
                L[offsetL + i*m + (j + 3)] = -val3/L_ii;
            }
            for (; j < i; j++) {
                double val = 0;
                for (int k = j; k < i; k++) {
                    val += L[offsetL + i*m + k]*L[offsetL + k*m + j];
                }
                L[offsetL + i*m + j] = -val/L_ii;
            }
            L[offsetL + i*m + i] = 1.0/L_ii;
        }
    }

    /// Inverts a square upper triangular matrix:  U = U<sup>-1</sup>
    ///
    /// @param U Upper triangular matrix being inverted. Not modified.
    /// @param U_inv Where the inverse is stored. Can be the same as U. Modified.
    /// @param m The number of rows and columns.
    /// @param offsetU which index does the U matrix start at.
    /// @param offsetU_inv which index does the U\_inv matrix start at.
    public static void invertUpper( double[] U,
                                    double[] U_inv,
                                    int m,
                                    int offsetU,
                                    int offsetU_inv ) {
//        for (int i = m - 1; i >= 0; i--) {
//            double U_ii = U[offsetU + i*m + i];
//            for (int j = m - 1; j > i; j--) {
//                double val = 0;
//                for (int k = i + 1; k <= j; k++) {
//                    val += U[offsetU + i*m + k]*U_inv[offsetU_inv + k*m + j];
//                }
//                U_inv[offsetU_inv + i*m + j] = -val/U_ii;
//            }
//            U_inv[offsetU_inv + i*m + i] = 1.0/U_ii;
//        }

        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            int j = m - 1;
            for (; j - 3 > i; j -= 4) {
                double valR = 0;  // col j
                double valR1 = 0;  // col j-1
                double valR2 = 0;  // col j-2
                double valL = 0;  // col j-3
                int k = i + 1;
                // Phase A: all four columns active.
                for (; k <= j - 3; k++) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + k*m + j];
                    valR1 += Uik*U_inv[offsetU_inv + k*m + (j - 1)];
                    valR2 += Uik*U_inv[offsetU_inv + k*m + (j - 2)];
                    valL += Uik*U_inv[offsetU_inv + k*m + (j - 3)];
                }
                // Phase B: column j-3 finished. Three remain.
                if (k <= j - 2) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + k*m + j];
                    valR1 += Uik*U_inv[offsetU_inv + k*m + (j - 1)];
                    valR2 += Uik*U_inv[offsetU_inv + k*m + (j - 2)];
                    k++;
                }
                // Phase C: column j-2 finished. Two remain.
                if (k <= j - 1) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + k*m + j];
                    valR1 += Uik*U_inv[offsetU_inv + k*m + (j - 1)];
                    k++;
                }
                // Phase D: column j-1 finished. Only column j remains.
                if (k <= j) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + k*m + j];
                }
                U_inv[offsetU_inv + i*m + j] = -valR/U_ii;
                U_inv[offsetU_inv + i*m + (j - 1)] = -valR1/U_ii;
                U_inv[offsetU_inv + i*m + (j - 2)] = -valR2/U_ii;
                U_inv[offsetU_inv + i*m + (j - 3)] = -valL/U_ii;
            }
            for (; j > i; j--) {
                double val = 0;
                for (int k = i + 1; k <= j; k++) {
                    val += U[offsetU + i*m + k]*U_inv[offsetU_inv + k*m + j];
                }
                U_inv[offsetU_inv + i*m + j] = -val/U_ii;
            }
            U_inv[offsetU_inv + i*m + i] = 1.0/U_ii;
        }
    }

    /// Inverts a square upper triangular matrix:  U = U<sup>-1</sup>
    ///
    /// @param U Upper triangular matrix being inverted. Overwritten with inverted matrix. Modified.
    /// @param m The number of rows and columns.
    /// @param offsetU which index does the U matrix start at.
    public static void invertUpper( double[] U,
                                    int m,
                                    int offsetU ) {
//        for (int i = m - 1; i >= 0; i--) {
//            double U_ii = U[offsetU + i*m + i];
//            for (int j = m - 1; j > i; j--) {
//                double val = 0;
//                for (int k = i + 1; k <= j; k++) {
//                    val += U[offsetU + i*m + k]*U[offsetU + k*m + j];
//                }
//                U[offsetU + i*m + j] = -val/U_ii;
//            }
//            U[offsetU + i*m + i] = 1.0/U_ii;
//        }

        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            int j = m - 1;
            for (; j - 3 > i; j -= 4) {
                double valR = 0;
                double valR1 = 0;
                double valR2 = 0;
                double valL = 0;
                int k = i + 1;
                for (; k <= j - 3; k++) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U[offsetU + k*m + j];
                    valR1 += Uik*U[offsetU + k*m + (j - 1)];
                    valR2 += Uik*U[offsetU + k*m + (j - 2)];
                    valL += Uik*U[offsetU + k*m + (j - 3)];
                }
                if (k <= j - 2) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U[offsetU + k*m + j];
                    valR1 += Uik*U[offsetU + k*m + (j - 1)];
                    valR2 += Uik*U[offsetU + k*m + (j - 2)];
                    k++;
                }
                if (k <= j - 1) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U[offsetU + k*m + j];
                    valR1 += Uik*U[offsetU + k*m + (j - 1)];
                    k++;
                }
                if (k <= j) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U[offsetU + k*m + j];
                }
                U[offsetU + i*m + j] = -valR/U_ii;
                U[offsetU + i*m + (j - 1)] = -valR1/U_ii;
                U[offsetU + i*m + (j - 2)] = -valR2/U_ii;
                U[offsetU + i*m + (j - 3)] = -valL/U_ii;
            }
            for (; j > i; j--) {
                double val = 0;
                for (int k = i + 1; k <= j; k++) {
                    val += U[offsetU + i*m + k]*U[offsetU + k*m + j];
                }
                U[offsetU + i*m + j] = -val/U_ii;
            }
            U[offsetU + i*m + i] = 1.0/U_ii;
        }
    }

    /// Inverts a square upper triangular matrix and writes the transpose of the inverse:
    /// U\_inv = U<sup>-T</sup>
    ///
    /// The result U<sup>-T</sup> is lower triangular and is written into the lower
    /// triangular portion of U\_inv. The upper triangular portion of U\_inv is not modified.
    ///
    /// @param U Upper triangular matrix being inverted. Not modified.
    /// @param U_inv Where the inverse-transpose is stored. Cannot be the same as U. Modified.
    /// @param m The number of rows and columns.
    /// @param offsetU which index does the U matrix start at.
    /// @param offsetU_inv which index does the U\_inv matrix start at.
    public static void invertUpperTran( double[] U,
                                        double[] U_inv,
                                        int m,
                                        int offsetU,
                                        int offsetU_inv ) {
//        for (int i = m - 1; i >= 0; i--) {
//            double U_ii = U[offsetU + i*m + i];
//            for (int j = m - 1; j > i; j--) {
//                double val = 0;
//                for (int k = i + 1; k <= j; k++) {
//                    val += U[offsetU + i*m + k]*U_inv[offsetU_inv + j*m + k];
//                }
//                U_inv[offsetU_inv + j*m + i] = -val/U_ii;
//            }
//            U_inv[offsetU_inv + i*m + i] = 1.0/U_ii;
//        }

        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            int j = m - 1;
            for (; j - 3 > i; j -= 4) {
                // Computing rows j, j-1, j-2, j-3 of U_inv at column i.
                // Each row q reads U_inv[(j-q)*m + k] for k = i+1..(j-q).
                //   Row j   (largest): k = i+1..j     (longest range)
                //   Row j-3 (smallest): k = i+1..j-3  (shortest range)
                double valR = 0;  // row j
                double valR1 = 0;  // row j-1
                double valR2 = 0;  // row j-2
                double valL = 0;  // row j-3
                int k = i + 1;
                for (; k <= j - 3; k++) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + j*m + k];
                    valR1 += Uik*U_inv[offsetU_inv + (j - 1)*m + k];
                    valR2 += Uik*U_inv[offsetU_inv + (j - 2)*m + k];
                    valL += Uik*U_inv[offsetU_inv + (j - 3)*m + k];
                }
                if (k <= j - 2) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + j*m + k];
                    valR1 += Uik*U_inv[offsetU_inv + (j - 1)*m + k];
                    valR2 += Uik*U_inv[offsetU_inv + (j - 2)*m + k];
                    k++;
                }
                if (k <= j - 1) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + j*m + k];
                    valR1 += Uik*U_inv[offsetU_inv + (j - 1)*m + k];
                    k++;
                }
                if (k <= j) {
                    double Uik = U[offsetU + i*m + k];
                    valR += Uik*U_inv[offsetU_inv + j*m + k];
                }
                U_inv[offsetU_inv + j*m + i] = -valR/U_ii;
                U_inv[offsetU_inv + (j - 1)*m + i] = -valR1/U_ii;
                U_inv[offsetU_inv + (j - 2)*m + i] = -valR2/U_ii;
                U_inv[offsetU_inv + (j - 3)*m + i] = -valL/U_ii;
            }
            for (; j > i; j--) {
                double val = 0;
                for (int k = i + 1; k <= j; k++) {
                    val += U[offsetU + i*m + k]*U_inv[offsetU_inv + j*m + k];
                }
                U_inv[offsetU_inv + j*m + i] = -val/U_ii;
            }
            U_inv[offsetU_inv + i*m + i] = 1.0/U_ii;
        }
    }

    /// Solves for non-singular lower triangular matrices using forward substitution.
    ///
    /// B = L<sup>-1</sup>B
    ///
    /// where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveLow( double[] L, double[] b,
                                  int m, int n,
                                  int strideL, int offsetL, int offsetB ) {
//    for (int j = 0; j < n; j++) {
//        for (int i = 0; i < m; i++) {
//            double sum = b[offsetB + i*n + j];
//            for (int k = 0; k < i; k++) {
//                sum -= L[offsetL + i*strideL + k]*b[offsetB + k*n + j];
//            }
//            b[offsetB + i*n + j] = sum/L[offsetL + i*strideL + i];
//        }
//    }

//        for (int i = 0; i < m; i++) {
//            int rowI = offsetB + i*n;
//
//            for (int k = 0; k < i; k++) {
//                double valL = L[offsetL + i*strideL + k];
//                int rowK = offsetB + k*n;
//
//                for (int j = 0; j < n; j++) {
//                    b[rowI + j] -= valL*b[rowK + j];
//                }
//            }
//
//            double diag = L[offsetL + i*strideL + i];
//            for (int j = 0; j < n; j++) {
//                b[rowI + j] /= diag;
//            }
//        }

        for (int i = 0; i < m; i++) {
            int rowI = offsetB + i*n;
            int k = 0;

            // Process k in groups of 4
            for (; k + 4 <= i; k += 4) {
                double v0 = L[offsetL + i*strideL + k + 0];
                double v1 = L[offsetL + i*strideL + k + 1];
                double v2 = L[offsetL + i*strideL + k + 2];
                double v3 = L[offsetL + i*strideL + k + 3];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= v0*b[row0 + j] + v1*b[row1 + j] + v2*b[row2 + j] + v3*b[row3 + j];
                }
            }

            // Tail for remaining k
            for (; k < i; k++) {
                double valL = L[offsetL + i*strideL + k];
                int rowK = offsetB + k*n;

                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= valL*b[rowK + j];
                }
            }

            double diag = L[offsetL + i*strideL + i];
            for (int j = 0; j < n; j++) {
                b[rowI + j] /= diag;
            }
        }
    }

    /// Solves for non-singular transposed lower triangular matrices using backwards substitution:
    ///
    /// B = L<sup>-T</sup>B
    ///
    /// where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveLowTrans( double[] L, double[] b,
                                       int m, int n,
                                       int strideL, int offsetL, int offsetB ) {
//    for (int j = 0; j < n; j++) {
//        for (int i = m - 1; i >= 0; i--) {
//            double sum = b[offsetB + i*n + j];
//            for (int k = i + 1; k < m; k++) {
//                sum -= L[offsetL + k*strideL + i]*b[offsetB + k*n + j];
//            }
//            b[offsetB + i*n + j] = sum/L[offsetL + i*strideL + i];
//        }
//    }

//        for (int i = m - 1; i >= 0; i--) {
//            int rowI = offsetB + i*n;
//
//            for (int k = i + 1; k < m; k++) {
//                double valL = L[offsetL + k*strideL + i];
//                int rowK = offsetB + k*n;
//
//                for (int j = 0; j < n; j++) {
//                    b[rowI + j] -= valL*b[rowK + j];
//                }
//            }
//
//            double diag = L[offsetL + i*strideL + i];
//            for (int j = 0; j < n; j++) {
//                b[rowI + j] /= diag;
//            }
//        }

        for (int i = m - 1; i >= 0; i--) {
            int rowI = offsetB + i*n;

            int k = i + 1;
            for (; k + 4 <= m; k += 4) {
                double v0 = L[offsetL + (k + 0)*strideL + i];
                double v1 = L[offsetL + (k + 1)*strideL + i];
                double v2 = L[offsetL + (k + 2)*strideL + i];
                double v3 = L[offsetL + (k + 3)*strideL + i];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= v0*b[row0 + j] + v1*b[row1 + j] + v2*b[row2 + j] + v3*b[row3 + j];
                }
            }

            for (; k < m; k++) {
                double valL = L[offsetL + k*strideL + i];
                int rowK = offsetB + k*n;
                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= valL*b[rowK + j];
                }
            }

            double diag = L[offsetL + i*strideL + i];
            for (int j = 0; j < n; j++) {
                b[rowI + j] /= diag;
            }
        }
    }

    /// Solves for non-singular lower triangular matrices using forward substitution.
    ///
    /// B<sup>T</sup> = L<sup>-1</sup>B<sup>T</sup>
    ///
    /// where B is a (n by m) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveLowBTrans( double[] L, double[] b,
                                        int m, int n,
                                        int strideL, int offsetL, int offsetB ) {
//        for( int j = 0; j < n; j++ ) {
//            for( int i = 0; i < m; i++ ) {
//                double sum = b[offsetB + j*m+i];
//                for( int k=0; k<i; k++ ) {
//                    sum -= L[offsetL + i*m+k]* b[offsetB + j*m+k];
//                }
//                b[offsetB + j*m+i] = sum / L[offsetL + i*m+i];
//            }
//        }

        int j;
        for (j = 0; j + 4 <= n; j += 4) {
            int row0 = offsetB + (j + 0)*m;
            int row1 = offsetB + (j + 1)*m;
            int row2 = offsetB + (j + 2)*m;
            int row3 = offsetB + (j + 3)*m;

            for (int i = 0; i < m; i++) {
                double s0 = b[row0 + i];
                double s1 = b[row1 + i];
                double s2 = b[row2 + i];
                double s3 = b[row3 + i];
                for (int k = 0; k < i; k++) {
                    double Lik = L[offsetL + i*strideL + k];
                    s0 -= Lik*b[row0 + k];
                    s1 -= Lik*b[row1 + k];
                    s2 -= Lik*b[row2 + k];
                    s3 -= Lik*b[row3 + k];
                }
                double valL = L[offsetL + i*strideL + i];
                b[row0 + i] = s0/valL;
                b[row1 + i] = s1/valL;
                b[row2 + i] = s2/valL;
                b[row3 + i] = s3/valL;
            }
        }
        for (; j < n; j++) {
            int rowJ = offsetB + j*m;
            for (int i = 0; i < m; i++) {
                double sum = b[rowJ + i];
                for (int k = 0; k < i; k++) {
                    sum -= L[offsetL + i*strideL + k]*b[rowJ + k];
                }
                b[rowJ + i] = sum/L[offsetL + i*strideL + i];
            }
        }
    }

    /// Solves for non-singular upper triangular matrices using backwards substitution.
    ///
    /// B = U<sup>-1</sup>B
    ///
    /// where B (m by n) is a matrix, U is a (m by m ) upper triangular matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveUpp( double[] U, double[] b,
                                  int m, int n,
                                  int strideU, int offsetU, int offsetB ) {
//        for (int j = 0; j < n; j++) {
//            for (int i = m - 1; i >= 0; i--) {
//                double sum = b[offsetB + i*n + j];
//                for (int k = i + 1; k < m; k++) {
//                    sum -= U[offsetU + i*strideU + k]*b[offsetB + k*n + j];
//                }
//                b[offsetB + i*n + j] = sum/U[offsetU + i*strideU + i];
//            }
//        }

        int j;
        for (j = 0; j + 4 <= n; j += 4) {
            for (int i = m - 1; i >= 0; i--) {
                int rowI = offsetB + i*n;
                double s0 = b[rowI + j + 0];
                double s1 = b[rowI + j + 1];
                double s2 = b[rowI + j + 2];
                double s3 = b[rowI + j + 3];
                for (int k = i + 1; k < m; k++) {
                    double Uik = U[offsetU + i*strideU + k];
                    int rowK = offsetB + k*n;
                    s0 -= Uik*b[rowK + j + 0];
                    s1 -= Uik*b[rowK + j + 1];
                    s2 -= Uik*b[rowK + j + 2];
                    s3 -= Uik*b[rowK + j + 3];
                }
                double valU = U[offsetU + i*strideU + i];
                b[rowI + j + 0] = s0/valU;
                b[rowI + j + 1] = s1/valU;
                b[rowI + j + 2] = s2/valU;
                b[rowI + j + 3] = s3/valU;
            }
        }
        for (; j < n; j++) {
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[offsetB + i*n + j];
                for (int k = i + 1; k < m; k++) {
                    sum -= U[offsetU + i*strideU + k]*b[offsetB + k*n + j];
                }
                b[offsetB + i*n + j] = sum/U[offsetU + i*strideU + i];
            }
        }

//        for (int i = m - 1; i >= 0; i--) {
//            double diag = U[offsetU + i*strideU + i];
//
//            for (int j = 0; j < n; j++) {
//                b[offsetB + i*n + j] /= diag;
//            }
//
//            for (int k = 0; k < i; k++) {
//                double valU = U[offsetU + k*strideU + i];
//                int rowK = offsetB + k*n;
//                int rowI = offsetB + i*n;
//
//                for (int j = 0; j < n; j++) {
//                    b[rowK + j] -= valU*b[rowI + j];
//                }
//            }
//        }

//        for (int i = m - 1; i >= 0; i--) {
//            double diag = U[offsetU + i*strideU + i];
//            int rowI = offsetB + i*n;
//            for (int j = 0; j < n; j++) {
//                b[rowI + j] /= diag;
//            }
//
//            int k = 0;
//            for (; k + 4 <= i; k += 4) {
//                double v0 = U[offsetU + (k + 0)*strideU + i];
//                double v1 = U[offsetU + (k + 1)*strideU + i];
//                double v2 = U[offsetU + (k + 2)*strideU + i];
//                double v3 = U[offsetU + (k + 3)*strideU + i];
//                int row0 = offsetB + (k + 0)*n;
//                int row1 = offsetB + (k + 1)*n;
//                int row2 = offsetB + (k + 2)*n;
//                int row3 = offsetB + (k + 3)*n;
//
//                for (int j = 0; j < n; j++) {
//                    double biJ = b[rowI + j];
//                    b[row0 + j] -= v0*biJ;
//                    b[row1 + j] -= v1*biJ;
//                    b[row2 + j] -= v2*biJ;
//                    b[row3 + j] -= v3*biJ;
//                }
//            }
//
//            for (; k < i; k++) {
//                double valU = U[offsetU + k*strideU + i];
//                int rowK = offsetB + k*n;
//                for (int j = 0; j < n; j++) {
//                    b[rowK + j] -= valU*b[rowI + j];
//                }
//            }
//        }
    }

    /// Solves for non-singular upper triangular matrices using forward substitution.
    ///
    /// B = U<sup>-T</sup>B
    ///
    /// where B (m by n) is a matrix, U is a (m by m ) upper triangular matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveUppTrans( double[] U, double[] b,
                                       int m, int n,
                                       int strideU, int offsetU, int offsetB ) {
//    for (int j = 0; j < n; j++) {
//        for (int i = 0; i < m; i++) {
//            double sum = b[offsetB + i*n + j];
//            for (int k = 0; k < i; k++) {
//                sum -= U[offsetU + k*strideU + i]*b[offsetB + k*n + j];
//            }
//            b[offsetB + i*n + j] = sum/U[offsetU + i*strideU + i];
//        }
//    }

//        for (int i = 0; i < m; i++) {
//            int rowI = offsetB + i*n;
//
//            for (int k = 0; k < i; k++) {
//                double valU = U[offsetU + k*strideU + i];
//                int rowK = offsetB + k*n;
//
//                for (int j = 0; j < n; j++) {
//                    b[rowI + j] -= valU*b[rowK + j];
//                }
//            }
//
//            double diag = U[offsetU + i*strideU + i];
//            for (int j = 0; j < n; j++) {
//                b[rowI + j] /= diag;
//            }
//        }

        for (int i = 0; i < m; i++) {
            int rowI = offsetB + i*n;

            int k = 0;
            for (; k + 4 <= i; k += 4) {
                double v0 = U[offsetU + (k + 0)*strideU + i];
                double v1 = U[offsetU + (k + 1)*strideU + i];
                double v2 = U[offsetU + (k + 2)*strideU + i];
                double v3 = U[offsetU + (k + 3)*strideU + i];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= v0*b[row0 + j] + v1*b[row1 + j] + v2*b[row2 + j] + v3*b[row3 + j];
                }
            }

            for (; k < i; k++) {
                double valU = U[offsetU + k*strideU + i];
                int rowK = offsetB + k*n;
                for (int j = 0; j < n; j++) {
                    b[rowI + j] -= valU*b[rowK + j];
                }
            }

            double diag = U[offsetU + i*strideU + i];
            for (int j = 0; j < n; j++) {
                b[rowI + j] /= diag;
            }
        }
    }

    /// Solves for non-singular transposed lower triangular matrices using backwards substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = L<sup>-T</sup>B<sup>T</sup>
    ///
    /// where B is a (n by m) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveLowTransBTrans( double[] L, double[] b,
                                             int m, int n,
                                             int strideL, int offsetL, int offsetB ) {
//        for (int j = 0; j < n; j++) {
//            for (int i = m - 1; i >= 0; i--) {
//                double sum = b[offsetB + j*m + i];
//                for (int k = i + 1; k < m; k++) {
//                    sum -= L[offsetL + k*strideL + i]*b[offsetB + j*m + k];
//                }
//                b[offsetB + j*m + i] = sum/L[offsetL + i*strideL + i];
//            }
//        }

//    for (int i = m - 1; i >= 0; i--) {
//        double diag = L[offsetL + i*strideL + i];
//        for (int j = 0; j < n; j++) {
//            b[offsetB + j*m + i] /= diag;
//        }
//        for (int k = 0; k < i; k++) {
//            double valL = L[offsetL + i*strideL + k];
//            for (int j = 0; j < n; j++) {
//                b[offsetB + j*m + k] -= b[offsetB + j*m + i]*valL;
//            }
//        }
//    }

        int j;
        for (j = 0; j + 4 <= n; j += 4) {
            int row0 = offsetB + (j + 0)*m;
            int row1 = offsetB + (j + 1)*m;
            int row2 = offsetB + (j + 2)*m;
            int row3 = offsetB + (j + 3)*m;

            for (int i = m - 1; i >= 0; i--) {
                double s0 = b[row0 + i];
                double s1 = b[row1 + i];
                double s2 = b[row2 + i];
                double s3 = b[row3 + i];
                for (int k = i + 1; k < m; k++) {
                    double Lki = L[offsetL + k*strideL + i];
                    s0 -= Lki*b[row0 + k];
                    s1 -= Lki*b[row1 + k];
                    s2 -= Lki*b[row2 + k];
                    s3 -= Lki*b[row3 + k];
                }
                double valL = L[offsetL + i*strideL + i];
                b[row0 + i] = s0/valL;
                b[row1 + i] = s1/valL;
                b[row2 + i] = s2/valL;
                b[row3 + i] = s3/valL;
            }
        }
        for (; j < n; j++) {
            int rowJ = offsetB + j*m;
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[rowJ + i];
                for (int k = i + 1; k < m; k++) {
                    sum -= L[offsetL + k*strideL + i]*b[rowJ + k];
                }
                b[rowJ + i] = sum/L[offsetL + i*strideL + i];
            }
        }
    }

    /// Solves for non-singular upper triangular matrices using backwards substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = U<sup>-1</sup>B<sup>T</sup>
    ///
    /// where B is a (n by m) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveUppBTrans( double[] U, double[] b,
                                        int m, int n,
                                        int strideU, int offsetU, int offsetB ) {
//        for (int j = 0; j < n; j++) {
//            for (int i = m - 1; i >= 0; i--) {
//                double sum = b[offsetB + j*m + i];
//                for (int k = i + 1; k < m; k++) {
//                    sum -= U[offsetU + i*strideU + k]*b[offsetB + j*m + k];
//                }
//                b[offsetB + j*m + i] = sum/U[offsetU + i*strideU + i];
//            }
//        }

        // Almost the same speed as the unrolled code below. 60 us vs 50 us.
//        for (int i = m - 1; i >= 0; i--) {
//            double diag = U[offsetU + i*strideU + i];
//
//            for (int j = 0; j < n; j++) {
//                b[offsetB + j*m + i] /= diag;
//            }
//
//            for (int k = 0; k < i; k++) {
//                double valU = U[offsetU + k*strideU + i];
//                for (int j = 0; j < n; j++) {
//                    b[offsetB + j*m + k] -= b[offsetB + j*m + i]*valU;
//                }
//            }
//        }

        final int JB = 4;
        int j;
        for (j = 0; j + JB <= n; j += JB) {
            for (int i = m - 1; i >= 0; i--) {
                int rowJ0 = offsetB + (j + 0)*m;
                int rowJ1 = offsetB + (j + 1)*m;
                int rowJ2 = offsetB + (j + 2)*m;
                int rowJ3 = offsetB + (j + 3)*m;

                double s0 = b[rowJ0 + i];
                double s1 = b[rowJ1 + i];
                double s2 = b[rowJ2 + i];
                double s3 = b[rowJ3 + i];

                for (int k = i + 1; k < m; k++) {
                    double Uik = U[offsetU + i*strideU + k];
                    s0 -= Uik*b[rowJ0 + k];
                    s1 -= Uik*b[rowJ1 + k];
                    s2 -= Uik*b[rowJ2 + k];
                    s3 -= Uik*b[rowJ3 + k];
                }

                double valU = U[offsetU + i*strideU + i];
                b[rowJ0 + i] = s0/valU;
                b[rowJ1 + i] = s1/valU;
                b[rowJ2 + i] = s2/valU;
                b[rowJ3 + i] = s3/valU;
            }
        }
        // tail loop for remaining n % JB rows
        for (; j < n; j++) {
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[offsetB + j*m + i];
                for (int k = i + 1; k < m; k++) {
                    sum -= U[offsetU + i*strideU + k]*b[offsetB + j*m + k];
                }
                b[offsetB + j*m + i] = sum/U[offsetU + i*strideU + i];
            }
        }
    }

    /// Solves for non-singular transposed upper triangular matrices using forward substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = U<sup>-T</sup>B<sup>T</sup>
    ///
    /// where B is a (n by m) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void lsolveUppTransBTrans( double[] U, double[] b,
                                             int m, int n,
                                             int strideU, int offsetU, int offsetB ) {
//        for (int j = 0; j < n; j++) {
//            for (int i = 0; i < m; i++) {
//                double sum = b[offsetB + j*m + i];
//                for (int k = 0; k < i; k++) {
//                    sum -= U[offsetU + k*strideU + i]*b[offsetB + j*m + k];
//                }
//                b[offsetB + j*m + i] = sum/U[offsetU + i*strideU + i];
//            }
//        }

        int j;
        for (j = 0; j + 4 <= n; j += 4) {
            int row0 = offsetB + (j + 0)*m;
            int row1 = offsetB + (j + 1)*m;
            int row2 = offsetB + (j + 2)*m;
            int row3 = offsetB + (j + 3)*m;

            for (int i = 0; i < m; i++) {
                double s0 = b[row0 + i];
                double s1 = b[row1 + i];
                double s2 = b[row2 + i];
                double s3 = b[row3 + i];
                for (int k = 0; k < i; k++) {
                    double Uki = U[offsetU + k*strideU + i];
                    s0 -= Uki*b[row0 + k];
                    s1 -= Uki*b[row1 + k];
                    s2 -= Uki*b[row2 + k];
                    s3 -= Uki*b[row3 + k];
                }
                double valU = U[offsetU + i*strideU + i];
                b[row0 + i] = s0/valU;
                b[row1 + i] = s1/valU;
                b[row2 + i] = s2/valU;
                b[row3 + i] = s3/valU;
            }
        }
        for (; j < n; j++) {
            int rowJ = offsetB + j*m;
            for (int i = 0; i < m; i++) {
                double sum = b[rowJ + i];
                for (int k = 0; k < i; k++) {
                    sum -= U[offsetU + k*strideU + i]*b[rowJ + k];
                }
                b[rowJ + i] = sum/U[offsetU + i*strideU + i];
            }
        }
    }

    /// Solves for non-singular lower triangular matrices using right-side forward substitution.
    ///
    /// B = BL<sup>-1</sup>
    ///
    /// where B is a (n by m) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of rows in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveLow( double[] L, double[] b,
                                  int m, int n,
                                  int strideL, int offsetL, int offsetB ) {
//        for (int i = 0; i < n; i++) {
//            for (int j = m - 1; j >= 0; j--) {
//                double sum = b[offsetB + i*m + j];
//                for (int k = j + 1; k < m; k++) {
//                    sum -= b[offsetB + i*m + k]*L[offsetL + k*strideL + j];
//                }
//                b[offsetB + i*m + j] = sum/L[offsetL + j*strideL + j];
//            }
//        }

//        for (int j = m - 1; j >= 0; j--) {
//            double diag = L[offsetL + j*strideL + j];
//
//            for (int i = 0; i < n; i++) {
//                b[offsetB + i*m + j] /= diag;
//            }
//
//            for (int k = 0; k < j; k++) {
//                double valL = L[offsetL + j*strideL + k];
//                for (int i = 0; i < n; i++) {
//                    b[offsetB + i*m + k] -= b[offsetB + i*m + j]*valL;
//                }
//            }
//        }

        int i;
        for (i = 0; i + 4 <= n; i += 4) {
            int row0 = offsetB + (i + 0)*m;
            int row1 = offsetB + (i + 1)*m;
            int row2 = offsetB + (i + 2)*m;
            int row3 = offsetB + (i + 3)*m;

            for (int j = m - 1; j >= 0; j--) {
                double s0 = b[row0 + j];
                double s1 = b[row1 + j];
                double s2 = b[row2 + j];
                double s3 = b[row3 + j];
                for (int k = j + 1; k < m; k++) {
                    double Lkj = L[offsetL + k*strideL + j];
                    s0 -= b[row0 + k]*Lkj;
                    s1 -= b[row1 + k]*Lkj;
                    s2 -= b[row2 + k]*Lkj;
                    s3 -= b[row3 + k]*Lkj;
                }
                double valL = L[offsetL + j*strideL + j];
                b[row0 + j] = s0/valL;
                b[row1 + j] = s1/valL;
                b[row2 + j] = s2/valL;
                b[row3 + j] = s3/valL;
            }
        }
        for (; i < n; i++) {
            int rowI = offsetB + i*m;
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[rowI + j];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[rowI + k]*L[offsetL + k*strideL + j];
                }
                b[rowI + j] = sum/L[offsetL + j*strideL + j];
            }
        }
    }

    /// Solves for non-singular transposed lower triangular matrices using right-side backward substitution.
    ///
    /// B = BL<sup>-T</sup>
    ///
    /// where B is a (n by m) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of rows in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveLowTrans( double[] L, double[] b,
                                       int m, int n,
                                       int strideL, int offsetL, int offsetB ) {
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < m; j++) {
//                double sum = b[offsetB + i*m + j];
//                for (int k = 0; k < j; k++) {
//                    sum -= b[offsetB + i*m + k]*L[offsetL + j*strideL + k];
//                }
//                b[offsetB + i*m + j] = sum/L[offsetL + j*strideL + j];
//            }
//        }

        int i;
        for (i = 0; i + 4 <= n; i += 4) {
            int row0 = offsetB + (i + 0)*m;
            int row1 = offsetB + (i + 1)*m;
            int row2 = offsetB + (i + 2)*m;
            int row3 = offsetB + (i + 3)*m;

            for (int j = 0; j < m; j++) {
                double s0 = b[row0 + j];
                double s1 = b[row1 + j];
                double s2 = b[row2 + j];
                double s3 = b[row3 + j];
                for (int k = 0; k < j; k++) {
                    double Ljk = L[offsetL + j*strideL + k];
                    s0 -= b[row0 + k]*Ljk;
                    s1 -= b[row1 + k]*Ljk;
                    s2 -= b[row2 + k]*Ljk;
                    s3 -= b[row3 + k]*Ljk;
                }
                double valL = L[offsetL + j*strideL + j];
                b[row0 + j] = s0/valL;
                b[row1 + j] = s1/valL;
                b[row2 + j] = s2/valL;
                b[row3 + j] = s3/valL;
            }
        }
        for (; i < n; i++) {
            int rowI = offsetB + i*m;
            for (int j = 0; j < m; j++) {
                double sum = b[rowI + j];
                for (int k = 0; k < j; k++) {
                    sum -= b[rowI + k]*L[offsetL + j*strideL + k];
                }
                b[rowI + j] = sum/L[offsetL + j*strideL + j];
            }
        }
    }

    /// Solves for non-singular lower triangular matrices using right-side forward substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = B<sup>T</sup>L<sup>-1</sup>
    ///
    /// where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveLowBTrans( double[] L, double[] b,
                                        int m, int n,
                                        int strideL, int offsetL, int offsetB ) {
//    for (int i = 0; i < n; i++) {
//        for (int j = m - 1; j >= 0; j--) {
//            double sum = b[offsetB + j*n + i];
//            for (int k = j + 1; k < m; k++) {
//                sum -= b[offsetB + k*n + i]*L[offsetL + k*strideL + j];
//            }
//            b[offsetB + j*n + i] = sum/L[offsetL + j*strideL + j];
//        }
//    }

//        for (int j = m - 1; j >= 0; j--) {
//            int rowJ = offsetB + j*n;
//
//            for (int k = j + 1; k < m; k++) {
//                double valL = L[offsetL + k*strideL + j];
//                int rowK = offsetB + k*n;
//
//                for (int i = 0; i < n; i++) {
//                    b[rowJ + i] -= valL*b[rowK + i];
//                }
//            }
//
//            double diag = L[offsetL + j*strideL + j];
//            for (int i = 0; i < n; i++) {
//                b[rowJ + i] /= diag;
//            }
//        }

        for (int j = m - 1; j >= 0; j--) {
            int rowJ = offsetB + j*n;

            int k = j + 1;
            for (; k + 4 <= m; k += 4) {
                double v0 = L[offsetL + (k + 0)*strideL + j];
                double v1 = L[offsetL + (k + 1)*strideL + j];
                double v2 = L[offsetL + (k + 2)*strideL + j];
                double v3 = L[offsetL + (k + 3)*strideL + j];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= v0*b[row0 + i] + v1*b[row1 + i] + v2*b[row2 + i] + v3*b[row3 + i];
                }
            }

            for (; k < m; k++) {
                double valL = L[offsetL + k*strideL + j];
                int rowK = offsetB + k*n;
                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= valL*b[rowK + i];
                }
            }

            double diag = L[offsetL + j*strideL + j];
            for (int i = 0; i < n; i++) {
                b[rowJ + i] /= diag;
            }
        }
    }

    /// Solves for non-singular transposed lower triangular matrices using right-side backward substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = B<sup>T</sup>L<sup>-T</sup>
    ///
    /// where B is a (m by n) matrix, L is a lower triangular (m by m) matrix.
    ///
    /// @param L An m by m non-singular lower triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the L matrix
    /// @param n number of columns in the B matrix.
    /// @param strideL number of elements that need to be added to go to the next row in L
    /// @param offsetL initial index in L where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveLowTransBTrans( double[] L, double[] b,
                                             int m, int n,
                                             int strideL, int offsetL, int offsetB ) {
//    for (int i = 0; i < n; i++) {
//        for (int j = 0; j < m; j++) {
//            double sum = b[offsetB + j*n + i];
//            for (int k = 0; k < j; k++) {
//                sum -= b[offsetB + k*n + i]*L[offsetL + j*strideL + k];
//            }
//            b[offsetB + j*n + i] = sum/L[offsetL + j*strideL + j];
//        }
//    }

//        for (int j = 0; j < m; j++) {
//            int rowJ = offsetB + j*n;
//
//            for (int k = 0; k < j; k++) {
//                double valL = L[offsetL + j*strideL + k];
//                int rowK = offsetB + k*n;
//
//                for (int i = 0; i < n; i++) {
//                    b[rowJ + i] -= valL*b[rowK + i];
//                }
//            }
//
//            double diag = L[offsetL + j*strideL + j];
//            for (int i = 0; i < n; i++) {
//                b[rowJ + i] /= diag;
//            }
//        }

        for (int j = 0; j < m; j++) {
            int rowJ = offsetB + j*n;

            int k = 0;
            for (; k + 4 <= j; k += 4) {
                double v0 = L[offsetL + j*strideL + (k + 0)];
                double v1 = L[offsetL + j*strideL + (k + 1)];
                double v2 = L[offsetL + j*strideL + (k + 2)];
                double v3 = L[offsetL + j*strideL + (k + 3)];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= v0*b[row0 + i] + v1*b[row1 + i] + v2*b[row2 + i] + v3*b[row3 + i];
                }
            }

            for (; k < j; k++) {
                double valL = L[offsetL + j*strideL + k];
                int rowK = offsetB + k*n;
                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= valL*b[rowK + i];
                }
            }

            double diag = L[offsetL + j*strideL + j];
            for (int i = 0; i < n; i++) {
                b[rowJ + i] /= diag;
            }
        }
    }

    /// Solves for non-singular upper triangular matrices using right-side backward substitution.
    ///
    /// B = BU<sup>-1</sup>
    ///
    /// where B is a (n by m) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of rows in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveUpp( double[] U, double[] b,
                                  int m, int n,
                                  int strideU, int offsetU, int offsetB ) {
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < m; j++) {
//                double sum = b[offsetB + i*m + j];
//                for (int k = 0; k < j; k++) {
//                    sum -= b[offsetB + i*m + k]*U[offsetU + k*strideU + j];
//                }
//                b[offsetB + i*m + j] = sum/U[offsetU + j*strideU + j];
//            }
//        }

//        for (int j = 0; j < m; j++) {
//            double diag = U[offsetU + j*strideU + j];
//
//            for (int i = 0; i < n; i++) {
//                b[offsetB + i*m + j] /= diag;
//            }
//
//            for (int k = j + 1; k < m; k++) {
//                double valU = U[offsetU + j*strideU + k];
//                for (int i = 0; i < n; i++) {
//                    b[offsetB + i*m + k] -= b[offsetB + i*m + j]*valU;
//                }
//            }
//        }

        int i;
        for (i = 0; i + 4 <= n; i += 4) {
            int row0 = offsetB + (i + 0)*m;
            int row1 = offsetB + (i + 1)*m;
            int row2 = offsetB + (i + 2)*m;
            int row3 = offsetB + (i + 3)*m;

            for (int j = 0; j < m; j++) {
                double s0 = b[row0 + j];
                double s1 = b[row1 + j];
                double s2 = b[row2 + j];
                double s3 = b[row3 + j];
                for (int k = 0; k < j; k++) {
                    double Ukj = U[offsetU + k*strideU + j];
                    s0 -= b[row0 + k]*Ukj;
                    s1 -= b[row1 + k]*Ukj;
                    s2 -= b[row2 + k]*Ukj;
                    s3 -= b[row3 + k]*Ukj;
                }
                double valU = U[offsetU + j*strideU + j];
                b[row0 + j] = s0/valU;
                b[row1 + j] = s1/valU;
                b[row2 + j] = s2/valU;
                b[row3 + j] = s3/valU;
            }
        }
        for (; i < n; i++) {
            int rowI = offsetB + i*m;
            for (int j = 0; j < m; j++) {
                double sum = b[rowI + j];
                for (int k = 0; k < j; k++) {
                    sum -= b[rowI + k]*U[offsetU + k*strideU + j];
                }
                b[rowI + j] = sum/U[offsetU + j*strideU + j];
            }
        }
    }

    /// Solves for non-singular transposed upper triangular matrices using right-side forward substitution.
    ///
    /// B = BU<sup>-T</sup>
    ///
    /// where B is a (n by m) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An n by m matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of rows in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveUppTrans( double[] U, double[] b,
                                       int m, int n,
                                       int strideU, int offsetU, int offsetB ) {
//        for (int i = 0; i < n; i++) {
//            for (int j = m - 1; j >= 0; j--) {
//                double sum = b[offsetB + i*m + j];
//                for (int k = j + 1; k < m; k++) {
//                    sum -= b[offsetB + i*m + k]*U[offsetU + j*strideU + k];
//                }
//                b[offsetB + i*m + j] = sum/U[offsetU + j*strideU + j];
//            }
//        }

//        for (int j = m - 1; j >= 0; j--) {
//            double diag = U[offsetU + j*strideU + j];
//
//            for (int i = 0; i < n; i++) {
//                b[offsetB + i*m + j] /= diag;
//            }
//
//            for (int k = 0; k < j; k++) {
//                double valU = U[offsetU + k*strideU + j];
//                for (int i = 0; i < n; i++) {
//                    b[offsetB + i*m + k] -= b[offsetB + i*m + j]*valU;
//                }
//            }
//        }

        int i;
        for (i = 0; i + 4 <= n; i += 4) {
            int row0 = offsetB + (i + 0)*m;
            int row1 = offsetB + (i + 1)*m;
            int row2 = offsetB + (i + 2)*m;
            int row3 = offsetB + (i + 3)*m;

            for (int j = m - 1; j >= 0; j--) {
                double s0 = b[row0 + j];
                double s1 = b[row1 + j];
                double s2 = b[row2 + j];
                double s3 = b[row3 + j];
                for (int k = j + 1; k < m; k++) {
                    double Ujk = U[offsetU + j*strideU + k];
                    s0 -= b[row0 + k]*Ujk;
                    s1 -= b[row1 + k]*Ujk;
                    s2 -= b[row2 + k]*Ujk;
                    s3 -= b[row3 + k]*Ujk;
                }
                double valU = U[offsetU + j*strideU + j];
                b[row0 + j] = s0/valU;
                b[row1 + j] = s1/valU;
                b[row2 + j] = s2/valU;
                b[row3 + j] = s3/valU;
            }
        }
        for (; i < n; i++) {
            int rowI = offsetB + i*m;
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[rowI + j];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[rowI + k]*U[offsetU + j*strideU + k];
                }
                b[rowI + j] = sum/U[offsetU + j*strideU + j];
            }
        }
    }

    /// Solves for non-singular upper triangular matrices using right-side backward substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = B<sup>T</sup>U<sup>-1</sup>
    ///
    /// where B is a (m by n) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveUppBTrans( double[] U, double[] b,
                                        int m, int n,
                                        int strideU, int offsetU, int offsetB ) {
//    for (int i = 0; i < n; i++) {
//        for (int j = 0; j < m; j++) {
//            double sum = b[offsetB + j*n + i];
//            for (int k = 0; k < j; k++) {
//                sum -= b[offsetB + k*n + i]*U[offsetU + k*strideU + j];
//            }
//            b[offsetB + j*n + i] = sum/U[offsetU + j*strideU + j];
//        }
//    }

//        for (int j = 0; j < m; j++) {
//            int rowJ = offsetB + j*n;
//
//            for (int k = 0; k < j; k++) {
//                double valU = U[offsetU + k*strideU + j];
//                int rowK = offsetB + k*n;
//
//                for (int i = 0; i < n; i++) {
//                    b[rowJ + i] -= valU*b[rowK + i];
//                }
//            }
//
//            double diag = U[offsetU + j*strideU + j];
//            for (int i = 0; i < n; i++) {
//                b[rowJ + i] /= diag;
//            }
//        }

        for (int j = 0; j < m; j++) {
            int rowJ = offsetB + j*n;

            int k = 0;
            for (; k + 4 <= j; k += 4) {
                double v0 = U[offsetU + (k + 0)*strideU + j];
                double v1 = U[offsetU + (k + 1)*strideU + j];
                double v2 = U[offsetU + (k + 2)*strideU + j];
                double v3 = U[offsetU + (k + 3)*strideU + j];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= v0*b[row0 + i] + v1*b[row1 + i] + v2*b[row2 + i] + v3*b[row3 + i];
                }
            }

            for (; k < j; k++) {
                double valU = U[offsetU + k*strideU + j];
                int rowK = offsetB + k*n;
                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= valU*b[rowK + i];
                }
            }

            double diag = U[offsetU + j*strideU + j];
            for (int i = 0; i < n; i++) {
                b[rowJ + i] /= diag;
            }
        }
    }

    /// Solves for non-singular transposed upper triangular matrices using right-side forward substitution,
    /// where B is laid out transposed in storage.
    ///
    /// B<sup>T</sup> = B<sup>T</sup>U<sup>-T</sup>
    ///
    /// where B is a (m by n) matrix, U is an upper triangular (m by m) matrix.
    ///
    /// @param U An m by m non-singular upper triangular matrix. Not modified.
    /// @param b An m by n matrix. Modified.
    /// @param m size of the U matrix
    /// @param n number of columns in the B matrix.
    /// @param strideU number of elements that need to be added to go to the next row in U
    /// @param offsetU initial index in U where the matrix starts
    /// @param offsetB initial index in B where the matrix starts
    public static void rsolveUppTransBTrans( double[] U, double[] b,
                                             int m, int n,
                                             int strideU, int offsetU, int offsetB ) {
//    for (int i = 0; i < n; i++) {
//        for (int j = m - 1; j >= 0; j--) {
//            double sum = b[offsetB + j*n + i];
//            for (int k = j + 1; k < m; k++) {
//                sum -= b[offsetB + k*n + i]*U[offsetU + j*strideU + k];
//            }
//            b[offsetB + j*n + i] = sum/U[offsetU + j*strideU + j];
//        }
//    }

//        for (int j = m - 1; j >= 0; j--) {
//            int rowJ = offsetB + j*n;
//
//            for (int k = j + 1; k < m; k++) {
//                double valU = U[offsetU + j*strideU + k];
//                int rowK = offsetB + k*n;
//
//                for (int i = 0; i < n; i++) {
//                    b[rowJ + i] -= valU*b[rowK + i];
//                }
//            }
//
//            double diag = U[offsetU + j*strideU + j];
//            for (int i = 0; i < n; i++) {
//                b[rowJ + i] /= diag;
//            }
//        }

        for (int j = m - 1; j >= 0; j--) {
            int rowJ = offsetB + j*n;

            int k = j + 1;
            for (; k + 4 <= m; k += 4) {
                double v0 = U[offsetU + j*strideU + (k + 0)];
                double v1 = U[offsetU + j*strideU + (k + 1)];
                double v2 = U[offsetU + j*strideU + (k + 2)];
                double v3 = U[offsetU + j*strideU + (k + 3)];
                int row0 = offsetB + (k + 0)*n;
                int row1 = offsetB + (k + 1)*n;
                int row2 = offsetB + (k + 2)*n;
                int row3 = offsetB + (k + 3)*n;

                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= v0*b[row0 + i] + v1*b[row1 + i] + v2*b[row2 + i] + v3*b[row3 + i];
                }
            }

            for (; k < m; k++) {
                double valU = U[offsetU + j*strideU + k];
                int rowK = offsetB + k*n;
                for (int i = 0; i < n; i++) {
                    b[rowJ + i] -= valU*b[rowK + i];
                }
            }

            double diag = U[offsetU + j*strideU + j];
            for (int i = 0; i < n; i++) {
                b[rowJ + i] /= diag;
            }
        }
    }
}
