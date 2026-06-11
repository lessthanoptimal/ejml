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

import org.ejml.UtilEjml;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.row.CommonOps_DDRM;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

/// Blocked Householder operations using the WY representation (LAPACK xLARFT + xLARFB analogues): build the
/// WY factor of a panel of reflectors and apply the resulting block reflector to a submatrix. Built on the
/// per-reflector ops in [InnerHouseholder_DDRB].
///
/// Assumptions:
///
///   - All submatrices are aligned along the inner blocks of the [DMatrixRBlock].
///   - Sometimes vectors are assumed to have leading zeros and a one.
///
/// Parameter conventions (ops document only what departs from these):
///
///   - blockLength: inner block size.
///   - [DSubmatrixD1] args (A, B, C, Y, W, V, ...) are block-aligned submatrix views; a paired row/col index
///     selects a stored vector (usually a reflector) within one.
///   - Raw kernels follow the [TileMultiplication_F64] convention: dataA/dataB/dataC backing arrays,
///     indexA/indexB/indexC block offsets, heightA/widthA/widthC block dimensions.
///   - The output is the pseudocode's left-hand side (e.g. C in C = C + A<sup>T</sup>B); ops whose output or
///     in-place modification isn't evident from the pseudocode say so explicitly.
public class Householder_DDRB {

    /// Computes W from the householder reflectors stored in the columns of the column block
    /// submatrix Y.
    ///
    /// ```
    /// Y = v(1)
    /// W = -β(1) v(1)
    /// for j=2:r
    ///   z = -β (I + W Y^T) v(j)
    ///   W = [W z]
    ///   Y = [Y v(j)]
    /// end
    /// ```
    ///
    /// where v(.) are the householder vectors, and r is the block length. Note that
    /// Y already contains the householder vectors so it does not need to be modified.
    ///
    /// Y and W are assumed to have the same number of rows and columns.
    ///
    /// @param workspace (Optional) Storage for workspace. Can be null.
    /// @param beta Beta's for householder vectors.
    /// @param betaIndex Index of first relevant beta.
    public static void computeWCol( final int blockLength,
                                    final DSubmatrixD1 Y, final DSubmatrixD1 W,
                                    @Nullable GrowArray<DGrowArray> workspace, final double[] beta, int betaIndex ) {

        workspace = UtilEjml.checkDeclare_F64(workspace);
        final int widthB = W.col1 - W.col0;

        // set the first column in W
        initializeW(blockLength, W, Y, beta[betaIndex]);

        final int min = Math.min(widthB, W.row1 - W.row0);

        final double[] temp = workspace.grow().reshape(Y.col1 - Y.col0).data;

        // set up rest of the columns
        for (int j = 1; j < min; j++) {
            //compute the z vector and insert it into W
            computeY_t_V(blockLength, Y, j, temp);
            computeZ(blockLength, Y, W, j, temp, beta[betaIndex + j]);
        }
    }

    /// Computes W from the householder reflectors stored in the rows of the row block
    /// submatrix Y.
    ///
    /// ```
    /// Y = v(1)
    /// W = -β(1) v(1)
    /// for j=2:r
    ///   z = -β (I + W Y^T) v(j)
    ///   W = [W z]
    ///   Y = [Y v(j)]
    /// end
    /// ```
    ///
    /// where v(.) are the householder vectors, and r is the block length. Note that
    /// Y already contains the householder vectors so it does not need to be modified.
    ///
    /// Y and W are assumed to have the same number of rows and columns.
    ///
    /// @param beta Beta's for householder vectors.
    /// @param betaIndex Index of first relevant beta.
    public static void computeWRow( final int blockLength,
                                    final DSubmatrixD1 Y, final DSubmatrixD1 W,
                                    final double[] beta, int betaIndex ) {

        final int heightY = Y.row1 - Y.row0;
        CommonOps_DDRM.fill(W.original, 0);

        // W = -beta*v(1)
        scaleRow(blockLength, Y, W, 0, 1, -beta[betaIndex++]);

        final int min = Math.min(heightY, W.col1 - W.col0);

        // set up rest of the rows
        for (int i = 1; i < min; i++) {
            // w=-beta*(I + W*Y^T)*u
            double b = -beta[betaIndex++];

            // w = w -beta*W*(Y^T*u)
            for (int j = 0; j < i; j++) {
                double yv = innerProdRow(blockLength, Y, i, Y, j, 1);
                VectorOps_DDRB.add_row(blockLength, W, i, 1, W, j, b*yv, W, i, 1, Y.col1 - Y.col0);
            }

            //w=w -beta*u + stuff above
            addRow(blockLength, Y, i, b, W, i, 1, W, i, 1, Y.col1 - Y.col0);
        }
    }

    /// Sets W to its initial value using the first column of Y and the value of beta:
    ///
    /// W = -βv
    ///
    /// where v = Y(:,0).
    public static void initializeW( final int blockLength,
                                    final DSubmatrixD1 W, final DSubmatrixD1 Y,
                                    final double beta ) {
        final int widthW = W.col1 - W.col0;
        final int widthY = Y.col1 - Y.col0;
        if (widthW <= 0)
            return;

        final double[] dataW = W.original.data;
        final double[] dataY = Y.original.data;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(W.row0, W.row1, blockLength, i -> {
        for (int i = W.row0; i < W.row1; i += blockLength) {
            final int heightW = Math.min(blockLength, W.row1 - i);

            int indexW = i*W.original.numCols + heightW*W.col0;
            int indexY = i*Y.original.numCols + heightW*Y.col0;

            // take in account the first element in V being 1
            if (i == W.row0) {
                dataW[indexW] = -beta;
                indexW += widthW;
                indexY += widthY;
                for (int k = 1; k < heightW; k++, indexW += widthW, indexY += widthY) {
                    dataW[indexW] = -beta*dataY[indexY];
                }
            } else {
                for (int k = 0; k < heightW; k++, indexW += widthW, indexY += widthY) {
                    dataW[indexW] = -beta*dataY[indexY];
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Computes the vector z and inserts it into 'W':
    ///
    /// z = - β<sub>j</sub>\*(V<sup>j</sup> + W\*h)
    ///
    /// where h is a vector of length 'col' and was computed using [#computeY_t_V]. V is a column in the Y matrix.
    /// Z is a column in the W matrix. Both Z and V are column 'col'.
    ///
    /// @param temp Temporary storage of at least length 'col'.
    public static void computeZ( final int blockLength, final DSubmatrixD1 Y, final DSubmatrixD1 W,
                                 final int col, final double[] temp, final double beta ) {
        final int widthW = W.col1 - W.col0;
        final int widthY = Y.col1 - Y.col0;

        final double[] dataW = W.original.data;
        final double[] dataY = Y.original.data;

        final int colsW = W.original.numCols;

        final double beta_neg = -beta;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(Y.row0, Y.row1, blockLength, i -> {
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int heightW = Math.min(blockLength, Y.row1 - i);

            int indexW = i*colsW + heightW*W.col0;
            int indexZ = i*colsW + heightW*W.col0 + col;
            int indexV = i*Y.original.numCols + heightW*Y.col0 + col;

            if (i == Y.row0) {
                // handle the triangular portion with the leading zeros and the one
                for (int k = 0; k < heightW; k++, indexZ += widthW, indexW += widthW, indexV += widthY) {
                    // compute the rows of W * h
                    double total = 0;

                    for (int j = 0; j < col; j++) {
                        total += dataW[indexW + j]*temp[j];
                    }

                    // add the two vectors together and multiply by -beta
                    if (k < col) {  // zeros
                        dataW[indexZ] = -beta*total;
                    } else if (k == col) { // one
                        dataW[indexZ] = beta_neg*(1.0 + total);
                    } else { // normal data
                        dataW[indexZ] = beta_neg*(dataY[indexV] + total);
                    }
                }
            } else {
                final int endZ = indexZ + widthW*heightW;
//                for( int k = 0; k < heightW; k++ ,
                while (indexZ != endZ) {
                    // compute the rows of W * h
                    double total = 0;

                    for (int j = 0; j < col; j++) {
                        total += dataW[indexW + j]*temp[j];
                    }

                    // add the two vectors together and multiply by -beta
                    dataW[indexZ] = beta_neg*(dataY[indexV] + total);

                    indexZ += widthW;
                    indexW += widthW;
                    indexV += widthY;
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Computes Y<sup>T</sup>v<sup>(j)</sup>, the projection of column 'col' onto the earlier columns of Y.
    ///
    /// Y are the columns before 'col' and v is column 'col'; the implicit zeros and ones are taken into account.
    /// The result is a vector of length 'col', and Y must be one block wide.
    ///
    /// @param temp Temporary storage of at least length 'col'
    public static void computeY_t_V( final int blockLength, final DSubmatrixD1 Y,
                                     final int col, final double[] temp ) {
        final int widthB = Y.col1 - Y.col0;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, col, j -> {
        for (int j = 0; j < col; j++) {
            temp[j] = innerProdCol(blockLength, Y, col, widthB, j, widthB);
        }
        //CONCURRENT_ABOVE });
    }

    /// Block multiply-add using Y's implicit zeros and unit diagonal (Y holds the reflectors). `DLARFB`
    /// analog.
    ///
    /// C = C + Y \* B
    public static void multPlus_TriLL0( final int blockLength,
                                        final DSubmatrixD1 Y, final DSubmatrixD1 B,
                                        final DSubmatrixD1 C ) {
        final int widthY = Y.col1 - Y.col0;
        // Cap block size based on reflector length. Otherwise, you need to fill extra space with zeros.
        final int r = Math.min(Y.row1 - Y.row0, widthY);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(Y.row0, Y.row1, blockLength, i -> {
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int heightY = Math.min(blockLength, Y.row1 - i);

            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);

                int indexC = (i - Y.row0 + C.row0)*C.original.numCols + (j - B.col0 + C.col0)*heightY;

                for (int k = Y.col0; k < Y.col1; k += blockLength) {
                    int indexY = i*Y.original.numCols + k*heightY;
                    int indexB = (k - Y.col0 + B.row0)*B.original.numCols + j*r;

                    if (i == Y.row0) {
                        TileTriangularMult_F64.lmultAddUnitLow(Y.original.data, B.original.data, C.original.data,
                                r, widthB, widthY, widthB, widthB, indexY, indexB, indexC);
                        if (heightY > r)
                            TileMultiplication_F64.tileMultPlus(Y.original.data, B.original.data, C.original.data,
                                    heightY - r, r, widthB, widthY, widthB, widthB,
                                    indexY + r*widthY, indexB, indexC + r*widthB);
                    } else {
                        TileMultiplication_F64.tileMultPlus(Y.original.data, B.original.data, C.original.data,
                                heightY, r, widthB, widthY, widthB, widthB, indexY, indexB, indexC);
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Multiplies the transpose of a one-block-wide column of reflectors `A`
    ///
    /// C = A<sup>T</sup> \* B
    public static void multTransA_TriLL0( final int blockLength,
                                          DSubmatrixD1 A, DSubmatrixD1 B,
                                          DSubmatrixD1 C ) {
        int widthA = A.col1 - A.col0;
        if (widthA > blockLength)
            throw new IllegalArgumentException("A is expected to be at most one block wide.");

        // Cap block size based on reflector length. Otherwise, you need to fill extra space with zeros.
        int r = Math.min(A.row1 - A.row0, widthA);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.col0, B.col1, blockLength, j -> {
        for (int j = B.col0; j < B.col1; j += blockLength) {
            int widthB = Math.min(blockLength, B.col1 - j);

            int indexC = C.row0*C.original.numCols + (j - B.col0 + C.col0)*r;

            for (int k = A.row0; k < A.row1; k += blockLength) {
                int heightA = Math.min(blockLength, A.row1 - k);

                int indexA = k*A.original.numCols + A.col0*heightA;
                int indexB = (k - A.row0 + B.row0)*B.original.numCols + j*heightA;

                if (k == A.row0) {
                    TileTriangularMult_F64.lmultUnitLowTransT(A.original.data, B.original.data, C.original.data,
                            r, widthB, widthA, widthB, widthB, indexA, indexB, indexC);
                    if (heightA > r)
                        TileMultiplication_F64.tileMultPlusTransA(A.original.data, B.original.data, C.original.data,
                                heightA - r, r, widthB, widthA, widthB, widthB,
                                indexA + r*widthA, indexB + r*widthB, indexC);
                } else {
                    TileMultiplication_F64.tileMultPlusTransA(A.original.data, B.original.data, C.original.data,
                            heightA, r, widthB, widthA, widthB, widthB, indexA, indexB, indexC);
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Multiplies a one-block-tall row-panel of reflectors `U` (unit upper-triangular with the implicit
    /// unit on the super-diagonal — the Tridiagonal `TriUR1` layout) by `B`. Row-form `DLARFB` analog:
    /// the first reflector block is a unit-upper TRMM on the triangle (read one column to the right so the
    /// super-diagonal becomes the main diagonal) plus GEMM on the body; the trailing block's super-diagonal
    /// unit straddles into the next block-column and is restored by a read-only correction on the last row.
    ///
    /// C = U \* B
    public static void mult_TriUR1( final int blockLength,
                                    DSubmatrixD1 U, DSubmatrixD1 B,
                                    DSubmatrixD1 C ) {
        int bs = U.row1 - U.row0;

        // The square triangle writes only the first bs-1 output rows; the last reflector's row is
        // accumulated by the tail GEMM and finished by the seam correction, so zero it first.
        VectorOps_DDRB.scale_row(blockLength, C, bs - 1, 0.0, C, bs - 1, 0, C.col1 - C.col0);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.col0, B.col1, blockLength, j -> {
        for (int j = B.col0; j < B.col1; j += blockLength) {
            int widthB = Math.min(blockLength, B.col1 - j);

            int indexC = C.row0*C.original.numCols + (j - B.col0 + C.col0)*bs;

            for (int k = U.col0; k < U.col1; k += blockLength) {
                int widthK = Math.min(blockLength, U.col1 - k);

                int indexU = U.row0*U.original.numCols + k*bs;
                int indexB = (k - U.col0 + B.row0)*B.original.numCols + j*widthK;

                if (k == U.col0) {
                    TileTriangularMult_F64.lmultUnitUpp(U.original.data, B.original.data, C.original.data,
                            bs - 1, widthB, widthK, widthB, widthB, indexU + 1, indexB + widthB, indexC);
                } else {
                    TileMultiplication_F64.tileMultPlus(U.original.data, B.original.data, C.original.data,
                            bs, widthK, widthB, widthK, widthB, widthB, indexU, indexB, indexC);
                }
            }
        }
        //CONCURRENT_ABOVE });

        // The last reflector's super-diagonal unit lives in the next block-column, so the GEMM above read a
        // stored value there instead of 1. Correct that row without writing into U: C[bs-1] += (1-g)*B[bs].
        if (U.col0 + bs < U.original.numCols) {
            double g = U.get(bs - 1, bs);
            VectorOps_DDRB.add_row(blockLength, C, bs - 1, 1.0, B, bs, 1.0 - g, C, bs - 1, 0, C.col1 - C.col0);
        }
    }

    /// Transposed twin of [#mult_TriUR1]: multiplies `B` by the transpose of a one-block-tall row-panel of
    /// reflectors `U` (`TriUR1` layout). Row-form `DLARFB` analog for the `Q*Q` direction.
    ///
    /// C = B \* U<sup>T</sup>
    public static void multTransB_TriUR1( final int blockLength,
                                          DSubmatrixD1 B, DSubmatrixD1 U,
                                          DSubmatrixD1 C ) {
        int bs = U.row1 - U.row0;
        int sizeI = B.row1 - B.row0;
        int sizeK = B.col1 - B.col0;

        // The square triangle writes only the first bs-1 output columns; the last reflector's column is
        // accumulated by the tail GEMM and finished by the seam correction, so zero it first.
        VectorOps_DDRB.scale_col(blockLength, C, bs - 1, 0.0, C, bs - 1, 0, sizeI);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(0, sizeI, blockLength, i -> {
        for (int i = 0; i < sizeI; i += blockLength) {
            int heightA = Math.min(blockLength, sizeI - i);

            int indexC = (C.row0 + i)*C.original.numCols + C.col0*heightA;

            for (int k = 0; k < sizeK; k += blockLength) {
                int widthK = Math.min(blockLength, sizeK - k);

                int indexA = (B.row0 + i)*B.original.numCols + (B.col0 + k)*heightA;
                int indexU = U.row0*U.original.numCols + (U.col0 + k)*bs;

                if (k == 0) {
                    TileTriangularMult_F64.rmultUnitUppTransT(U.original.data, B.original.data, C.original.data,
                            bs - 1, heightA, bs, bs, bs, indexU + 1, indexA + 1, indexC);
                } else {
                    TileMultiplication_F64.tileMultPlusTransB(B.original.data, U.original.data, C.original.data,
                            heightA, widthK, bs, widthK, widthK, bs, indexA, indexU, indexC);
                }
            }
        }
        //CONCURRENT_ABOVE });

        // The last reflector's super-diagonal unit straddles into the next block-column; fix that output
        // column without writing into U: C[:,bs-1] += (1-g)*B[:,bs].
        if (U.col0 + bs < U.original.numCols) {
            double g = U.get(bs - 1, bs);
            VectorOps_DDRB.add_col(blockLength, C, bs - 1, 1.0, B, bs, 1.0 - g, C, bs - 1, 0, sizeI);
        }
    }

    /// Specialized for the symmetric tridiagonal trailing update.
    ///
    /// C = C + A<sup>T</sup> \* B
    ///
    /// Only the lower block region is written (block rows from C.row0 + blockLength, columns j ≥ i),
    /// since symmetry fills in the rest.
    public static void multPlusTransA_symm( int blockLength,
                                            DSubmatrixD1 A, DSubmatrixD1 B,
                                            DSubmatrixD1 C ) {
        int heightA = Math.min(blockLength, A.row1 - A.row0);

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(C.row0 + blockLength, C.row1, blockLength, i -> {
        for (int i = C.row0 + blockLength; i < C.row1; i += blockLength) {
            int heightC = Math.min(blockLength, C.row1 - i);

            int indexA = A.row0*A.original.numCols + (i - C.row0 + A.col0)*heightA;

            for (int j = i; j < C.col1; j += blockLength) {
                int widthC = Math.min(blockLength, C.col1 - j);

                int indexC = i*C.original.numCols + j*heightC;
                int indexB = B.row0*B.original.numCols + (j - C.col0 + B.col0)*heightA;

                TileMultiplication_F64.tileMultPlusTransA(A.original.data, B.original.data, C.original.data,
                        heightA, heightC, widthC, indexA, indexB, indexC);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Computes the householder vector that is used to create a reflector for the column.
    /// The results are stored in the original matrix.
    ///
    /// The householder vector 'u' is computed as follows:
    ///
    /// ```
    /// u(1) = 1
    /// u(i) = x(i)/(τ + x(1))
    /// ```
    ///
    /// The first element is implicitly assumed to be one (on the column's main diagonal) and not written.
    ///
    /// @return If there are any problems or not. true = no problem.
    public static boolean computeHouseholderCol( final int blockLength, final DSubmatrixD1 Y,
                                                 final double[] gamma, final int i ) {
        double max = findMaxCol(blockLength, Y, i);

        if (max == 0.0) {
            return false;
        } else {
            // computes tau and normalizes u by max
            double tau = computeTauAndDivideCol(blockLength, Y, i, max);

            // divide u by u_0
            double u_0 = Y.get(i, i) + tau;
            divideElementsCol(blockLength, Y, i, u_0);

            gamma[Y.col0 + i] = u_0/tau;
            tau *= max;

            // after the reflector is applied the column would be all zeros but be -tau in the first element
            Y.set(i, i, -tau);
        }
        return true;
    }

    /// Computes the householder vector from the specified row
    ///
    /// The householder vector 'u' is computed as follows:
    ///
    /// ```
    /// u(1) = 1
    /// u(i) = x(i)/(τ + x(1))
    /// ```
    ///
    /// The first element is implicitly assumed to be one (on the super-diagonal, at column i+1) and not written.
    ///
    /// @return If there are any problems or not. true = no problem.
    public static boolean computeHouseholderRow( final int blockLength, final DSubmatrixD1 Y,
                                                 final double[] gamma, final int i ) {
        double max = findMaxRow(blockLength, Y, i, i + 1);

        if (max == 0.0) {
            return false;
        } else {
            // computes tau and normalizes u by max
            double tau = computeTauAndDivideRow(blockLength, Y, i, i + 1, max);

            // divide u by u_0
            double u_0 = Y.get(i, i + 1) + tau;
            VectorOps_DDRB.div_row(blockLength, Y, i, u_0, Y, i, i + 1, Y.col1 - Y.col0);

            gamma[Y.row0 + i] = u_0/tau;

            // after the reflector is applied the column would be all zeros but be -tau in the first element
            Y.set(i, i + 1, -tau*max);
        }
        return true;
    }

    /// Applies a householder reflector stored in column 'col' to the remainder of the columns
    /// in the block after it. Takes into account leading zeros and one.
    ///
    /// A = (I - γ\*u\*u<sup>T</sup>)\*A
    ///
    /// @param A at most one block wide
    public static void rank1UpdateMultR_Col( final int blockLength,
                                             final DSubmatrixD1 A, final int col, final double gamma ) {
        final int width = Math.min(blockLength, A.col1 - A.col0);

        final double[] dataA = A.original.data;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(col + 1, width, j -> {
        for (int j = col + 1; j < width; j++) {

            // total = U^T * A(:,j)
            double total = innerProdCol(blockLength, A, col, width, j, width);

            total *= gamma;
            // A(:,j) - gamma*U*total

            for (int i = A.row0; i < A.row1; i += blockLength) {
                int height = Math.min(blockLength, A.row1 - i);

                int indexU = i*A.original.numCols + height*A.col0 + col;
                int indexA = i*A.original.numCols + height*A.col0 + j;

                if (i == A.row0) {
                    indexU += width*(col + 1);
                    indexA += width*col;

                    dataA[indexA] -= total;

                    indexA += width;

                    for (int k = col + 1; k < height; k++, indexU += width, indexA += width) {
                        dataA[indexA] -= total*dataA[indexU];
                    }
                } else {
                    int endU = indexU + width*height;
                    // for( int k = 0; k < height; k++
                    for (; indexU != endU; indexU += width, indexA += width) {
                        dataA[indexA] -= total*dataA[indexU];
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Applies a householder reflector stored in column 'col' to the top block row (excluding
    /// the first block-column) of A. Takes into account leading zeros and one.
    ///
    /// A = (I - γ\*u\*u<sup>T</sup>)\*A
    ///
    /// @param A at most one block wide
    public static void rank1UpdateMultR_TopRow( final int blockLength,
                                                final DSubmatrixD1 A, final int col, final double gamma ) {
        final double[] dataA = A.original.data;

        final int widthCol = Math.min(blockLength, A.col1 - col);

        // step through columns in top block, skipping over the first block
        for (int colStartJ = A.col0 + blockLength; colStartJ < A.col1; colStartJ += blockLength) {
            final int widthJ = Math.min(blockLength, A.col1 - colStartJ);

            for (int j = 0; j < widthJ; j++) {
                // total = U^T * A(:,j) * gamma
                double total = innerProdCol(blockLength, A, col, widthCol, (colStartJ - A.col0) + j, widthJ)*gamma;

                // A(:,j) - gamma*U*total
                // just update the top most block
                int i = A.row0;
                int height = Math.min(blockLength, A.row1 - i);

                int indexU = i*A.original.numCols + height*A.col0 + col;
                int indexA = i*A.original.numCols + height*colStartJ + j;

                // take in account zeros and one
                indexU += widthCol*(col + 1);
                indexA += widthJ*col;

                dataA[indexA] -= total;

                indexA += widthJ;

                for (int k = col + 1; k < height; k++, indexU += widthCol, indexA += widthJ) {
                    dataA[indexA] -= total*dataA[indexU];
                }
            }
        }
    }

    /// Applies a householder reflector stored in row 'row' to the remainder of the rows
    /// in the block after it. Takes into account leading zeros and one.
    ///
    /// A = A\*(I - γ\*u\*u<sup>T</sup>)
    ///
    /// @param colStart First index in 'u' that the reflector starts at
    public static void rank1UpdateMultL_Row( final int blockLength,
                                             final DSubmatrixD1 A,
                                             final int row, final int colStart, final double gamma ) {
        final int height = Math.min(blockLength, A.row1 - A.row0);

        final double[] dataA = A.original.data;

        int zeroOffset = colStart - row;

        for (int i = row + 1; i < height; i++) {
            // total = U^T * A(i,:)
            double total = innerProdRow(blockLength, A, row, A, i, zeroOffset);

            total *= gamma;
            // A(i,:) - gamma*U*total

            for (int j = A.col0; j < A.col1; j += blockLength) {
                int width = Math.min(blockLength, A.col1 - j);

                int indexU = A.row0*A.original.numCols + height*j + row*width;
                int indexA = A.row0*A.original.numCols + height*j + i*width;

                if (j == A.col0) {
                    indexU += colStart + 1;
                    indexA += colStart;

                    dataA[indexA++] -= total;

                    for (int k = colStart + 1; k < width; k++) {
                        dataA[indexA++] -= total*dataA[indexU++];
                    }
                } else {
                    for (int k = 0; k < width; k++) {
                        dataA[indexA++] -= total*dataA[indexU++];
                    }
                }
            }
        }
    }

    /// Computes the inner product of column vector 'colA' against column vector 'colB' while taking into account leading zeros and one.
    ///
    /// ret = a<sup>T</sup>\*b
    ///
    /// Column A is assumed to be a householder vector. Element at 'colA' is one and previous elements are zero.
    ///
    /// @param widthA how wide the column block that colA is inside of.
    /// @param widthB how wide the column block that colB is inside of.
    /// @return dot product of the two vectors.
    public static double innerProdCol( int blockLength, DSubmatrixD1 A,
                                       int colA, int widthA,
                                       int colB, int widthB ) {
        double total = 0;

        final double[] data = A.original.data;
        // first column in the blocks
        final int colBlockA = A.col0 + colA - colA%blockLength;
        final int colBlockB = A.col0 + colB - colB%blockLength;
        colA = colA%blockLength;
        colB = colB%blockLength;

        // compute dot product down column vectors
        for (int i = A.row0; i < A.row1; i += blockLength) {

            int height = Math.min(blockLength, A.row1 - i);

            int indexA = i*A.original.numCols + height*colBlockA + colA;
            int indexB = i*A.original.numCols + height*colBlockB + colB;

            if (i == A.row0) {
                // handle leading zeros
                indexA += widthA*(colA + 1);
                indexB += widthB*colA;

                // handle leading one
                total = data[indexB];

                indexB += widthB;

                // standard vector dot product
                int endA = indexA + (height - colA - 1)*widthA;
                for (; indexA != endA; indexA += widthA, indexB += widthB) {
//                    for( int k = col+1; k < height; k++ , indexU += width, indexA += width ) {
                    total += data[indexA]*data[indexB];
                }
            } else {
                // standard vector dot product
                int endA = indexA + widthA*height;
//                    for( int k = 0; k < height; k++ ) {
                for (; indexA != endA; indexA += widthA, indexB += widthB) {
                    total += data[indexA]*data[indexB];
                }
            }
        }
        return total;
    }

    /// Computes the inner product of row vector 'rowA' against row vector 'rowB' while taking into account leading zeros and one.
    ///
    /// ret = a<sup>T</sup>\*b
    ///
    /// Row A is assumed to be a householder vector. The element at rowA + zeroOffset is one and earlier elements are zero.
    ///
    /// @return dot product of the two vectors.
    public static double innerProdRow( int blockLength,
                                       DSubmatrixD1 A,
                                       int rowA,
                                       DSubmatrixD1 B,
                                       int rowB, int zeroOffset ) {
        int offset = rowA + zeroOffset;
        if (offset + B.col0 >= B.col1)
            return 0;

        // take in account the one in 'A'
        double total = B.get(rowB, offset);

        total += VectorOps_DDRB.dot_row(blockLength, A, rowA, B, rowB, offset + 1, A.col1 - A.col0);

        return total;
    }

    /// Inner product of two rows where B is a symmetric matrix.
    ///
    /// ret = a<sup>T</sup>\*b
    ///
    /// Like [#innerProdRow], except B's stored upper triangle is read symmetrically: B(k,rowB) is used in
    /// place of B(rowB,k) for k below the diagonal. Row 'rowA' of A is a reflector with an implicit leading one.
    public static double innerProdRow_symm( int blockLength,
                                            DSubmatrixD1 A,
                                            int rowA,
                                            DSubmatrixD1 B,
                                            int rowB, int zeroOffset ) {
        int offset = rowA + zeroOffset;
        if (offset + B.col0 >= B.col1)
            return 0;

        if (offset < rowB) {
            // take in account the one in 'A'
            double total = B.get(offset, rowB);

            total += VectorOps_DDRB.dot_row_col(blockLength, A, rowA, B, rowB, offset + 1, rowB);
            total += VectorOps_DDRB.dot_row(blockLength, A, rowA, B, rowB, rowB, A.col1 - A.col0);

            return total;
        } else {
            // take in account the one in 'A'
            double total = B.get(rowB, offset);

            total += VectorOps_DDRB.dot_row(blockLength, A, rowA, B, rowB, offset + 1, A.col1 - A.col0);

            return total;
        }
    }

    /// Adds two reflector rows: C(rowC) = α\*A(rowA) + β\*B(rowB).
    ///
    /// A(rowA) is a stored householder vector, so its implicit leading one (at rowA + zeroOffset) is added
    /// explicitly and the leading zeros are skipped.
    ///
    /// @param zeroOffset How far off the diagonal the first element of the reflector in A is.
    /// @param end Index at which the vectors end.
    public static void addRow( final int blockLength,
                               DSubmatrixD1 A, int rowA, double alpha,
                               DSubmatrixD1 B, int rowB, double beta,
                               DSubmatrixD1 C, int rowC,
                               int zeroOffset, int end ) {
        int offset = rowA + zeroOffset;

        if (C.col0 + offset >= C.col1)
            return;
        // handle leading one
        C.set(rowC, offset, alpha + B.get(rowB, offset)*beta);

        VectorOps_DDRB.add_row(blockLength, A, rowA, alpha, B, rowB, beta, C, rowC, offset + 1, end);
    }

    /// Divides a stored-reflector column by 'val' (implicit leading zeros and one skipped).
    public static void divideElementsCol( final int blockLength,
                                          final DSubmatrixD1 Y, final int col, final double val ) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);

        final double[] dataY = Y.original.data;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(Y.row0, Y.row1, blockLength, i -> {
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            int height = Math.min(blockLength, Y.row1 - i);

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if (i == Y.row0) {
                index += width*(col + 1);

                for (int k = col + 1; k < height; k++, index += width) {
                    dataY[index] /= val;
                }
            } else {
                int endIndex = index + width*height;
                //for( int k = 0; k < height; k++
                for (; index != endIndex; index += width) {
                    dataY[index] /= val;
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Scales a stored-reflector row by 'val', handling the implicit leading one and zeros.
    ///
    /// W = val \* Y
    ///
    /// @param zeroOffset How far off the diagonal is the first element in the vector.
    public static void scaleRow( final int blockLength,
                                 final DSubmatrixD1 Y,
                                 final DSubmatrixD1 W,
                                 final int row,
                                 final int zeroOffset,
                                 final double val ) {


        int offset = row + zeroOffset;

        if (offset >= W.col1 - W.col0)
            return;

        // handle the one
        W.set(row, offset, val);

        // scale rest of the vector
        VectorOps_DDRB.scale_row(blockLength, Y, row, val, W, row, offset + 1, Y.col1 - Y.col0);
    }

    /// From the specified column of Y tau is computed and each element is divided by 'max'.
    /// See code below:
    ///
    /// ```
    /// for i=col:Y.numRows
    ///   Y[i][col] = u[i][col] / max
    ///   tau = tau + u[i][col]*u[i][col]
    /// end
    /// tau = sqrt(tau)
    /// if( Y[col][col] < 0 )
    ///    tau = -tau;
    /// ```
    public static double computeTauAndDivideCol( final int blockLength,
                                                 final DSubmatrixD1 Y,
                                                 final int col, final double max ) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);

        final double[] dataY = Y.original.data;

        double top = 0;
        double norm2 = 0;

        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            int height = Math.min(blockLength, Y.row1 - i);

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if (i == Y.row0) {
                index += width*col;
                // save this value so that the sign can be determined later on
                top = dataY[index] /= max;
                norm2 += top*top;
                index += width;

                for (int k = col + 1; k < height; k++, index += width) {
                    double val = dataY[index] /= max;
                    norm2 += val*val;
                }
            } else {
                for (int k = 0; k < height; k++, index += width) {
                    double val = dataY[index] /= max;
                    norm2 += val*val;
                }
            }
        }

        norm2 = Math.sqrt(norm2);

        if (top < 0)
            norm2 = -norm2;

        return norm2;
    }

    /// From the specified row of Y tau is computed and each element is divided by 'max'.
    /// See code below:
    ///
    /// ```
    /// for j=row:Y.numCols
    ///   Y[row][j] = u[row][j] / max
    ///   tau = tau + u[row][j]*u[row][j]
    /// end
    /// tau = sqrt(tau)
    /// if( Y[row][row] < 0 )
    ///    tau = -tau;
    /// ```
    ///
    /// @param colStart The first column that computation of tau will start at
    /// @param max Largest |element| in the row; scales elements so norm can't overflow.
    public static double computeTauAndDivideRow( final int blockLength,
                                                 final DSubmatrixD1 Y,
                                                 final int row, int colStart, final double max ) {
        final int height = Math.min(blockLength, Y.row1 - Y.row0);

        final double[] dataY = Y.original.data;

        double top = 0;
        double norm2 = 0;

        int startJ = Y.col0 + colStart - colStart%blockLength;
        colStart = colStart%blockLength;

        for (int j = startJ; j < Y.col1; j += blockLength) {
            int width = Math.min(blockLength, Y.col1 - j);

            int index = Y.row0*Y.original.numCols + height*j + row*width;

            if (j == startJ) {
                index += colStart;
                // save this value so that the sign can be determined later on
                top = dataY[index] /= max;
                norm2 += top*top;
                index++;

                for (int k = colStart + 1; k < width; k++) {
                    double val = dataY[index++] /= max;
                    norm2 += val*val;
                }
            } else {
                for (int k = 0; k < width; k++) {
                    double val = dataY[index++] /= max;
                    norm2 += val*val;
                }
            }
        }

        norm2 = Math.sqrt(norm2);

        if (top < 0)
            norm2 = -norm2;

        return norm2;
    }

    /// Finds the element in the column with the largest absolute value. The offset
    /// from zero is automatically taken into account based on the column.
    public static double findMaxCol( final int blockLength, final DSubmatrixD1 Y, final int col ) {
        final int width = Math.min(blockLength, Y.col1 - Y.col0);

        final double[] dataY = Y.original.data;

        double max = 0;

        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            int height = Math.min(blockLength, Y.row1 - i);

            int index = i*Y.original.numCols + height*Y.col0 + col;

            if (i == Y.row0) {
                index += width*col;
                for (int k = col; k < height; k++, index += width) {
                    double v = Math.abs(dataY[index]);
                    if (v > max) {
                        max = v;
                    }
                }
            } else {
                for (int k = 0; k < height; k++, index += width) {
                    double v = Math.abs(dataY[index]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
        }

        return max;
    }

    /// Finds the element in the row with the largest absolute value. The offset
    /// from zero is automatically taken into account based on the row.
    public static double findMaxRow( final int blockLength,
                                     final DSubmatrixD1 Y,
                                     final int row, final int colStart ) {
        final int height = Math.min(blockLength, Y.row1 - Y.row0);

        final double[] dataY = Y.original.data;

        double max = 0;

        for (int j = Y.col0; j < Y.col1; j += blockLength) {
            int width = Math.min(blockLength, Y.col1 - j);

            int index = Y.row0*Y.original.numCols + height*j + row*width;

            if (j == Y.col0) {
                index += colStart;

                for (int k = colStart; k < width; k++) {
                    double v = Math.abs(dataY[index++]);
                    if (v > max) {
                        max = v;
                    }
                }
            } else {
                for (int k = 0; k < width; k++) {
                    double v = Math.abs(dataY[index++]);
                    if (v > max) {
                        max = v;
                    }
                }
            }
        }

        return max;
    }
}
