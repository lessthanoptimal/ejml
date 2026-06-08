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
///   - blockLength: inner block size. Ops taking it work over block-aligned [DSubmatrixD1] views; the raw
///     inner-block kernels take heightA/widthA/widthC directly instead.
///   - [DSubmatrixD1] args (A, B, C, Y, W, V, ...) are block-aligned submatrix views; a paired row/col index
///     selects a stored vector (usually a reflector) within one.
///   - Raw kernels follow the [InnerMultiplication_DDRB] convention: dataA/dataB/dataC backing arrays,
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
        InnerHouseholder_DDRB.scaleRow(blockLength, Y, W, 0, 1, -beta[betaIndex++]);

        final int min = Math.min(heightY, W.col1 - W.col0);

        // set up rest of the rows
        for (int i = 1; i < min; i++) {
            // w=-beta*(I + W*Y^T)*u
            double b = -beta[betaIndex++];

            // w = w -beta*W*(Y^T*u)
            for (int j = 0; j < i; j++) {
                double yv = InnerHouseholder_DDRB.innerProdRow(blockLength, Y, i, Y, j, 1);
                VectorOps_DDRB.add_row(blockLength, W, i, 1, W, j, b*yv, W, i, 1, Y.col1 - Y.col0);
            }

            //w=w -beta*u + stuff above
            InnerHouseholder_DDRB.addRow(blockLength, Y, i, b, W, i, 1, W, i, 1, Y.col1 - Y.col0);
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
        final int width = W.col1 - W.col0;
        if (width <= 0)
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
                indexW += width;
                indexY += width;
                for (int k = 1; k < heightW; k++, indexW += width, indexY += width) {
                    dataW[indexW] = -beta*dataY[indexY];
                }
            } else {
                for (int k = 0; k < heightW; k++, indexW += width, indexY += width) {
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
    /// where h is a vector of length 'col' and was computed using [#computeY_t_V].
    /// V is a column in the Y matrix. Z is a column in the W matrix. Both Z and V are
    /// column 'col'.
    ///
    /// @param temp Temporary storage of at least length 'col'.
    public static void computeZ( final int blockLength, final DSubmatrixD1 Y, final DSubmatrixD1 W,
                                 final int col, final double[] temp, final double beta ) {
        final int width = Y.col1 - Y.col0;

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
                for (int k = 0; k < heightW; k++, indexZ += width, indexW += width, indexV += width) {
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
                final int endZ = indexZ + width*heightW;
//                for( int k = 0; k < heightW; k++ ,
                while (indexZ != endZ) {
                    // compute the rows of W * h
                    double total = 0;

                    for (int j = 0; j < col; j++) {
                        total += dataW[indexW + j]*temp[j];
                    }

                    // add the two vectors together and multiply by -beta
                    dataW[indexZ] = beta_neg*(dataY[indexV] + total);

                    indexZ += width;
                    indexW += width;
                    indexV += width;
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
            temp[j] = InnerHouseholder_DDRB.innerProdCol(blockLength, Y, col, widthB, j, widthB);
        }
        //CONCURRENT_ABOVE });
    }

    /// Block multiply-add using Y's implicit zeros and unit diagonal (Y holds the reflectors).
    ///
    /// C = C + Y \* B
    public static void multPlus_TriLL0( final int blockLength,
                                        final DSubmatrixD1 Y, final DSubmatrixD1 B,
                                        final DSubmatrixD1 C ) {
        final int widthY = Y.col1 - Y.col0;

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(Y.row0, Y.row1, blockLength, i -> {
        for (int i = Y.row0; i < Y.row1; i += blockLength) {
            final int heightY = Math.min(blockLength, Y.row1 - i);

            for (int j = B.col0; j < B.col1; j += blockLength) {
                final int widthB = Math.min(blockLength, B.col1 - j);

                int indexC = (i - Y.row0 + C.row0)*C.original.numCols + (j - B.col0 + C.col0)*heightY;

                for (int k = Y.col0; k < Y.col1; k += blockLength) {
                    int indexY = i*Y.original.numCols + k*heightY;
                    int indexB = (k - Y.col0 + B.row0)*B.original.numCols + j*widthY;

                    if (i == Y.row0) {
                        blockMultPlus_TriLL0(Y.original.data, B.original.data, C.original.data,
                                indexY, indexB, indexC, heightY, widthY, widthB);
                    } else {
                        InnerMultiplication_DDRB.blockMultPlus(Y.original.data, B.original.data, C.original.data,
                                indexY, indexB, indexC, heightY, widthY, widthB);
                    }
                }
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Inner-block mult-add over the top block of Y, whose implicit zeros and unit diagonal are baked in.
    ///
    /// C = C + A \* B
    private static void blockMultPlus_TriLL0( double[] dataA, double[] dataB, double[] dataC,
                                              int indexA, int indexB, int indexC,
                                              final int heightA, final int widthA, final int widthC ) {


        for (int i = 0; i < heightA; i++) {
            for (int j = 0; j < widthC; j++) {
                double val = i < widthA ? dataB[i*widthC + j + indexB] : 0;

                int end = Math.min(i, widthA);
                int innerIndexA = i*widthA + indexA;
                int innerOffsetB = j + indexB;
                final int endA = innerIndexA + end;

//                for (int k = 0; k < end; k++) {
                while (innerIndexA != endA) {
                    val += dataA[innerIndexA++]*dataB[innerOffsetB];
                    innerOffsetB += widthC;
                }

                dataC[i*widthC + j + indexC] += val;
            }
        }
    }

    /// multTransAB where A is a one-block-wide column of reflectors (lower-triangular, unit diagonal).
    ///
    /// C = A<sup>T</sup> \* B
    public static void multTransA_TriLL0( final int blockLength,
                                          DSubmatrixD1 A, DSubmatrixD1 B,
                                          DSubmatrixD1 C ) {
        int widthA = A.col1 - A.col0;
        if (widthA > blockLength)
            throw new IllegalArgumentException("A is expected to be at most one block wide.");

        //CONCURRENT_BELOW EjmlConcurrency.loopFor(B.col0, B.col1, blockLength, j -> {
        for (int j = B.col0; j < B.col1; j += blockLength) {
            int widthB = Math.min(blockLength, B.col1 - j);

            int indexC = C.row0*C.original.numCols + (j - B.col0 + C.col0)*widthA;

            for (int k = A.row0; k < A.row1; k += blockLength) {
                int heightA = Math.min(blockLength, A.row1 - k);

                int indexA = k*A.original.numCols + A.col0*heightA;
                int indexB = (k - A.row0 + B.row0)*B.original.numCols + j*heightA;

                if (k == A.row0)
                    blockMultTransA_TriLL0(A.original.data, B.original.data, C.original.data,
                            indexA, indexB, indexC, heightA, widthA, widthB);
                else
                    InnerMultiplication_DDRB.blockMultPlusTransA(A.original.data, B.original.data, C.original.data,
                            indexA, indexB, indexC, heightA, widthA, widthB);
            }
        }
        //CONCURRENT_ABOVE });
    }

    /// Lower-triangular multTransAB with implicit unit diagonal.
    ///
    /// C = A<sup>T</sup> \* B
    private static void blockMultTransA_TriLL0( double[] dataA, double[] dataB, double[] dataC,
                                                int indexA, int indexB, int indexC,
                                                final int heightA, final int widthA, final int widthC ) {
        for (int i = 0; i < widthA; i++) {
            for (int j = 0; j < widthC; j++) {
                double val = i < heightA ? dataB[i*widthC + j + indexB] : 0;

                for (int k = i + 1; k < heightA; k++) {
                    val += dataA[k*widthA + i + indexA]*dataB[k*widthC + j + indexB];
                }

                dataC[i*widthC + j + indexC] = val;
            }
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

                InnerMultiplication_DDRB.blockMultPlusTransA(A.original.data, B.original.data, C.original.data,
                        indexA, indexB, indexC, heightA, heightC, widthC);
            }
        }
        //CONCURRENT_ABOVE });
    }
}
