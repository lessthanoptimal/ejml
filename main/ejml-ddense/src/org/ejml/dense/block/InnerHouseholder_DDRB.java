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
import org.ejml.data.DSubmatrixD1;

//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

/// Unblocked, per-reflector Householder operations (LAPACK xLARFG + xLARF analogues): generate a single
/// reflector and apply it to a column or row. These work on a [DSubmatrixD1] and take account of the
/// implicit leading zeros and unit diagonal of a stored reflector, so they are Householder specific rather
/// than generic vector ops.
///
/// Assumptions:
///
///   - All submatrices are aligned along the inner blocks of the [DMatrixRBlock].
///   - Some vectors are stored reflectors with implicit leading zeros and a one. The one sits on the
///     main diagonal for a column-stored reflector (the `…Col` ops, at the reflector's own column) and
///     at row + zeroOffset for a row-stored reflector (the `…Row` ops — the super-diagonal in
///     tridiagonal use; the offset is passed in, not assumed).
///
/// Parameter conventions (ops document only what departs from these):
///
///   - blockLength: inner block size; all ops here work over block-aligned [DSubmatrixD1] views.
///   - [DSubmatrixD1] args (A, B, Y, W, ...) are block-aligned submatrix views; a paired row/col index
///     selects a stored vector (usually a reflector) within one.
///   - The output is the pseudocode's left-hand side (e.g. ret, or A in A = (I - γuu<sup>T</sup>)A); ops whose
///     output or in-place modification isn't evident from the pseudocode say so explicitly.
public class InnerHouseholder_DDRB {

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
