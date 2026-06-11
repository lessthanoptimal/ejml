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

//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

public class RankUpdate_DDRB {
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

                TileMultiplication_F64.tileMultPlusTransA(alpha,
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
    public static void symmRankNMinus_U( int blockLength, DSubmatrixD1 A, DSubmatrixD1 B ) {

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
                    TileRankUpdate_F64.tileMultMinusTransA_U(B.original.data, A.original.data,
                            heightB, widthB_i, widthB_j, indexB_i, indexB_j, indexA);
                } else {
                    TileMultiplication_F64.tileMultMinusTransA(B.original.data, B.original.data, A.original.data,
                            heightB, widthB_i, widthB_j, indexB_i, indexB_j, indexA);
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
                    TileRankUpdate_F64.tileMultMinusTransB_L(B.original.data, A.original.data,
                            widthB, heightB_i, widthB_j, indexB_i, indexB_j, indexA);
                } else {
                    TileMultiplication_F64.tileMultMinusTransB(B.original.data, B.original.data, A.original.data,
                            widthB, heightB_i, widthB_j, indexB_i, indexB_j, indexA);
                }
            }
        }
        //CONCURRENT_ABOVE });
    }
}
