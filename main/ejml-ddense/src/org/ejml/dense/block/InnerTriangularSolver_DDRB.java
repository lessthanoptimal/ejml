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

import org.ejml.data.DMatrixRBlock;

/// Contains triangular solvers and inverters for inner blocks of a [DMatrixRBlock]. Solve functions use
/// the following naming scheme with all 16 possible permutations provided:
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
/// Left Solve: T*X=B and for Right Solve: X*T=B. Where T and B are known, T is triangular and X is the unknown being solved for.
/// All inputs are assumed to be matrices. Note that in the code below X and B are the same matrix with the solution
/// being written in place.
///
/// Values in the "opposite" triangle from what it processes are ignored. For "Low" it will only look
/// at the lower triangle and conversely for "Upp" it will look only at the upper motion.
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
public class InnerTriangularSolver_DDRB {

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
        for (int i = 0; i < m; i++) {
            double L_ii = L[offsetL + i*m + i];
            for (int j = 0; j < i; j++) {
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
        for (int i = 0; i < m; i++) {
            double L_ii = L[offsetL + i*m + i];
            for (int j = 0; j < i; j++) {
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
        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            for (int j = m - 1; j > i; j--) {
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
        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            for (int j = m - 1; j > i; j--) {
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
        for (int i = m - 1; i >= 0; i--) {
            double U_ii = U[offsetU + i*m + i];
            for (int j = m - 1; j > i; j--) {
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
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = b[offsetB + i*n + j];
                for (int k = 0; k < i; k++) {
                    sum -= L[offsetL + i*strideL + k]*b[offsetB + k*n + j];
                }
                b[offsetB + i*n + j] = sum/L[offsetL + i*strideL + i];
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
        for (int j = 0; j < n; j++) {
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[offsetB + i*n + j];
                for (int k = i + 1; k < m; k++) {
                    sum -= L[offsetL + k*strideL + i]*b[offsetB + k*n + j];
                }
                b[offsetB + i*n + j] = sum/L[offsetL + i*strideL + i];
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
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = b[offsetB + j*m + i];
                int l = offsetL + i*strideL;
                int bb = offsetB + j*m;
                int endL = l + i;
                while (l != endL) {
//                for( int k=0; k<i; k++ ) {
                    sum -= L[l++]*b[bb++];
                }
                b[offsetB + j*m + i] = sum/L[offsetL + i*strideL + i];
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
        for (int j = 0; j < n; j++) {
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[offsetB + i*n + j];
                for (int k = i + 1; k < m; k++) {
                    sum -= U[offsetU + i*strideU + k]*b[offsetB + k*n + j];
                }
                b[offsetB + i*n + j] = sum/U[offsetU + i*strideU + i];
            }
        }
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
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = b[offsetB + i*n + j];
                for (int k = 0; k < i; k++) {
                    sum -= U[offsetU + k*strideU + i]*b[offsetB + k*n + j];
                }
                b[offsetB + i*n + j] = sum/U[offsetU + i*strideU + i];
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
        for (int j = 0; j < n; j++) {
            for (int i = m - 1; i >= 0; i--) {
                double sum = b[offsetB + j*m + i];
                for (int k = i + 1; k < m; k++) {
                    sum -= L[offsetL + k*strideL + i]*b[offsetB + j*m + k];
                }
                b[offsetB + j*m + i] = sum/L[offsetL + i*strideL + i];
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
        for (int j = 0; j < n; j++) {
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
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = b[offsetB + j*m + i];
                for (int k = 0; k < i; k++) {
                    sum -= U[offsetU + k*strideU + i]*b[offsetB + j*m + k];
                }
                b[offsetB + j*m + i] = sum/U[offsetU + i*strideU + i];
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
        for (int i = 0; i < n; i++) {
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[offsetB + i*m + j];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[offsetB + i*m + k]*L[offsetL + k*strideL + j];
                }
                b[offsetB + i*m + j] = sum/L[offsetL + j*strideL + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = b[offsetB + i*m + j];
                for (int k = 0; k < j; k++) {
                    sum -= b[offsetB + i*m + k]*L[offsetL + j*strideL + k];
                }
                b[offsetB + i*m + j] = sum/L[offsetL + j*strideL + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[offsetB + j*n + i];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[offsetB + k*n + i]*L[offsetL + k*strideL + j];
                }
                b[offsetB + j*n + i] = sum/L[offsetL + j*strideL + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = b[offsetB + j*n + i];
                for (int k = 0; k < j; k++) {
                    sum -= b[offsetB + k*n + i]*L[offsetL + j*strideL + k];
                }
                b[offsetB + j*n + i] = sum/L[offsetL + j*strideL + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = b[offsetB + i*m + j];
                for (int k = 0; k < j; k++) {
                    sum -= b[offsetB + i*m + k]*U[offsetU + k*strideU + j];
                }
                b[offsetB + i*m + j] = sum/U[offsetU + j*strideU + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[offsetB + i*m + j];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[offsetB + i*m + k]*U[offsetU + j*strideU + k];
                }
                b[offsetB + i*m + j] = sum/U[offsetU + j*strideU + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double sum = b[offsetB + j*n + i];
                for (int k = 0; k < j; k++) {
                    sum -= b[offsetB + k*n + i]*U[offsetU + k*strideU + j];
                }
                b[offsetB + j*n + i] = sum/U[offsetU + j*strideU + j];
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
        for (int i = 0; i < n; i++) {
            for (int j = m - 1; j >= 0; j--) {
                double sum = b[offsetB + j*n + i];
                for (int k = j + 1; k < m; k++) {
                    sum -= b[offsetB + k*n + i]*U[offsetU + j*strideU + k];
                }
                b[offsetB + j*n + i] = sum/U[offsetU + j*strideU + j];
            }
        }
    }
}
