/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

/**
 * <p>
 * Unblocked, per-reflector Householder operations (LAPACK xLARFG + xLARF analogues): generate a single
 * reflector and apply it to a column or row. These work on a {@link DSubmatrixD1} and take account of the
 * implicit leading zeros and unit diagonal of a stored reflector, so they are Householder specific rather
 * than generic vector ops.
 * </p>
 *
 * <p>
 * Assumptions:
 * <ul>
 *  <li> All submatrices are aligned along the inner blocks of the {@link DMatrixRBlock}.
 *  <li> Some times vectors are assumed to have leading zeros and a one.
 * </ul>
 *
 * @author Peter Abeles
 */
public class InnerHouseholder_DDRB {

    /**
     * <p>
     * Computes the householder vector that is used to create reflector for the column.
     * The results are stored in the original matrix.
     * </p>
     *
     * <p>
     * The householder vector 'u' is computed as follows:<br>
     * <br>
     * u(1) = 1 <br>
     * u(i) = x(i)/(&tau; + x(1))<br>
     * </p>
     *
     * The first element is implicitly assumed to be one and not written.
     *
     * @return If there was any problems or not. true = no problem.
     */
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

    /**
     * <p>
     * Computes the householder vector from the specified row
     * </p>
     *
     * <p>
     * The householder vector 'u' is computed as follows:<br>
     * <br>
     * u(1) = 1 <br>
     * u(i) = x(i)/(&tau; + x(1))<br>
     * </p>
     *
     * The first element is implicitly assumed to be one and not written.
     *
     * @return If there was any problems or not. true = no problem.
     */
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

    /**
     * <p>
     * Applies a householder reflector stored in column 'col' to the remainder of the columns
     * in the block after it. Takes in account leading zeros and one.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     *
     * @param A submatrix that is at most one block wide and aligned along inner blocks
     * @param col The column in A containing 'u'
     */
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

    /**
     * <p>
     * Applies a householder reflector stored in column 'col' to the top block row (excluding
     * the first column) of A. Takes in account leading zeros and one.<br>
     * <br>
     * A = (I - &gamma;*u*u<sup>T</sup>)*A<br>
     * </p>
     *
     * @param A submatrix that is at most one block wide and aligned along inner blocks
     * @param col The column in A containing 'u'
     */
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

    /**
     * <p>
     * Applies a householder reflector stored in row 'row' to the remainder of the row
     * in the block after it. Takes in account leading zeros and one.<br>
     * <br>
     * A = A*(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     *
     * @param A submatrix that is block aligned
     * @param row The row in A containing 'u'
     * @param colStart First index in 'u' that the reflector starts at
     */
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

    /**
     * <p>
     * Applies a householder reflector stored in row 'row' to the left column block.
     * Takes in account leading zeros and one.<br>
     * <br>
     * A = A*(I - &gamma;*u*u<sup>T</sup>)<br>
     * </p>
     *
     * @param A submatrix that is block aligned
     * @param row The row in A containing 'u'
     * @param zeroOffset How far off the diagonal is the first element in 'u'
     */
    public static void rank1UpdateMultL_LeftCol( final int blockLength,
                                                 final DSubmatrixD1 A,
                                                 final int row, final double gamma, int zeroOffset ) {
        final int heightU = Math.min(blockLength, A.row1 - A.row0);
        final int width = Math.min(blockLength, A.col1 - A.col0);

        final double[] data = A.original.data;

        for (int blockStart = A.row0 + blockLength; blockStart < A.row1; blockStart += blockLength) {
            final int heightA = Math.min(blockLength, A.row1 - blockStart);

            for (int i = 0; i < heightA; i++) {

                // total = U^T * A(i,:)
                double total = innerProdRow(blockLength, A, row, A, i + (blockStart - A.row0), zeroOffset);

                total *= gamma;

                // A(i,:) - gamma*U*total
//                plusScale_row(blockLength,);

                int indexU = A.row0*A.original.numCols + heightU*A.col0 + row*width;
                int indexA = blockStart*A.original.numCols + heightA*A.col0 + i*width;

                // skip over zeros and assume first element in U is 1
                indexU += zeroOffset + 1;
                indexA += zeroOffset;

                data[indexA++] -= total;

                for (int k = zeroOffset + 1; k < width; k++) {
                    data[indexA++] -= total*data[indexU++];
                }
            }
        }
    }

    /**
     * <p>
     * Computes the inner product of column vector 'colA' against column vector 'colB' while taking account leading zeros and one.<br>
     * <br>
     * ret = a<sup>T</sup>*b
     * </p>
     *
     * <p>
     * Column A is assumed to be a householder vector. Element at 'colA' is one and previous ones are zero.
     * </p>
     *
     * @param A block aligned submatrix.
     * @param colA Column inside the block of first column vector.
     * @param widthA how wide the column block that colA is inside of.
     * @param colB Column inside the block of second column vector.
     * @param widthB how wide the column block that colB is inside of.
     * @return dot product of the two vectors.
     */
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

    /**
     * <p>
     * Computes the inner product of row vector 'rowA' against row vector 'rowB' while taking account leading zeros and one.<br>
     * <br>
     * ret = a<sup>T</sup>*b
     * </p>
     *
     * <p>
     * Row A is assumed to be a householder vector. Element at 'colStartA' is one and previous elements are zero.
     * </p>
     *
     * @param A block aligned submatrix.
     * @param rowA Row index inside the sub-matrix of first row vector has zeros and ones..
     * @param rowB Row index inside the sub-matrix of second row vector.
     * @return dot product of the two vectors.
     */
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

    public static void add_row( final int blockLength,
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

    /**
     * Divides the elements at the specified column by 'val'. Takes in account
     * leading zeros and one.
     */
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

    /**
     * Scales the elements in the specified row starting at element colStart by 'val'.<br>
     * W = val*Y
     *
     * Takes in account zeros and leading one automatically.
     *
     * @param zeroOffset How far off the diagonal is the first element in the vector.
     */
    public static void scale_row( final int blockLength,
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

    /**
     * <p>
     * From the specified column of Y tau is computed and each element is divided by 'max'.
     * See code below:
     * </p>
     *
     * <pre>
     * for i=col:Y.numRows
     *   Y[i][col] = u[i][col] / max
     *   tau = tau + u[i][col]*u[i][col]
     * end
     * tau = sqrt(tau)
     * if( Y[col][col] &lt; 0 )
     *    tau = -tau;
     * </pre>
     */
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

    /**
     * <p>
     * From the specified row of Y tau is computed and each element is divided by 'max'.
     * See code below:
     * </p>
     *
     * <pre>
     * for j=row:Y.numCols
     *   Y[row][j] = u[row][j] / max
     *   tau = tau + u[row][j]*u[row][j]
     * end
     * tau = sqrt(tau)
     * if( Y[row][row] &lt; 0 )
     *    tau = -tau;
     * </pre>
     *
     * @param row Which row in the block will be processed
     * @param colStart The first column that computation of tau will start at
     * @param max used to normalize and prevent buffer over flow
     */
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

    /**
     * Finds the element in the column with the largest absolute value. The offset
     * from zero is automatically taken in account based on the column.
     */
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

    /**
     * Finds the element in the column with the largest absolute value. The offset
     * from zero is automatically taken in account based on the column.
     */
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
