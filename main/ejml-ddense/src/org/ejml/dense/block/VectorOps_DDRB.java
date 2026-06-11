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

/// Math operations for inner vectors (row and column) inside of block matrices:
///
/// scale: b<sub>i</sub> = alpha\*a<sub>i</sub>
/// 
/// div:  <sub>i</sub> = a<sub>i</sub>/alpha
/// 
/// add: c<sub>i</sub> = alpha\*a<sub>i</sub> + betaB<sub>i</sub>
/// 
/// dot: c = sum a<sub>i</sub>\*b<sub>i</sub>
///
/// All submatrices must be block aligned. All offsets and end indexes are relative to the beginning of each
/// submatrix.
public class VectorOps_DDRB {

    /// Row vector scale:
    ///
    /// scale: b<sub>i</sub> = alpha\*a<sub>i</sub>
    ///
    /// where 'a' and 'b' are row vectors within the row block vector A and B.
    ///
    /// @param A submatrix. Not modified.
    /// @param rowA which row in 'A' the vector is contained in.
    /// @param alpha scale factor.
    /// @param B submatrix that the results are written to. Modified.
    /// @param offset Index at which the vectors start at.
    /// @param end Index at which the vectors end at.
    public static void scale_row( final int blockLength,
                                  DSubmatrixD1 A, int rowA,
                                  double alpha, DSubmatrixD1 B, int rowB,
                                  int offset, int end ) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;

        // handle the case where offset is more than a block
        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        // handle rows in any block
        int rowBlockA = A.row0 + rowA - rowA%blockLength;
        rowA = rowA%blockLength;
        int rowBlockB = B.row0 + rowB - rowB%blockLength;
        rowB = rowB%blockLength;

        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            int widthB = Math.min(blockLength, B.col1 - B.col0 - i);

            int indexA = rowBlockA*A.original.numCols + (A.col0 + i)*heightA + rowA*widthA;
            int indexB = rowBlockB*B.original.numCols + (B.col0 + i)*heightB + rowB*widthB;

            if (i == startI) {
                indexA += offset;
                indexB += offset;

                for (int j = offset; j < segment; j++) {
                    dataB[indexB++] = alpha*dataA[indexA++];
                }
            } else {
                for (int j = 0; j < segment; j++) {
                    dataB[indexB++] = alpha*dataA[indexA++];
                }
            }
        }
    }

    /// Row vector divide:
    ///
    /// div: b<sub>i</sub> = a<sub>i</sub>/alpha
    ///
    /// where 'a' and 'b' are row vectors within the row block vector A and B.
    ///
    /// @param A submatrix. Not modified.
    /// @param rowA which row in A the vector is contained in.
    /// @param alpha scale factor.
    /// @param B submatrix that the results are written to. Modified.
    /// @param offset Index at which the vectors start at.
    /// @param end Index at which the vectors end at.
    public static void div_row( final int blockLength,
                                DSubmatrixD1 A, int rowA,
                                double alpha, DSubmatrixD1 B, int rowB,
                                int offset, int end ) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;

        // handle the case where offset is more than a block
        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        // handle rows in any block
        int rowBlockA = A.row0 + rowA - rowA%blockLength;
        rowA = rowA%blockLength;
        int rowBlockB = B.row0 + rowB - rowB%blockLength;
        rowB = rowB%blockLength;

        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            int widthB = Math.min(blockLength, B.col1 - B.col0 - i);

            int indexA = rowBlockA*A.original.numCols + (A.col0 + i)*heightA + rowA*widthA;
            int indexB = rowBlockB*B.original.numCols + (B.col0 + i)*heightB + rowB*widthB;

            if (i == startI) {
                indexA += offset;
                indexB += offset;

                for (int j = offset; j < segment; j++) {
                    dataB[indexB++] = dataA[indexA++]/alpha;
                }
            } else {
                for (int j = 0; j < segment; j++) {
                    dataB[indexB++] = dataA[indexA++]/alpha;
                }
            }
        }
    }

    /// Row vector add:
    ///
    /// add: c<sub>i</sub> = alpha\*a<sub>i</sub> + betaB<sub>i</sub>
    ///
    /// where 'a', 'b', and 'c' are row vectors within the row block vectors of A, B, and C respectively.
    ///
    /// @param blockLength Length of each inner matrix block.
    /// @param A submatrix. Not modified.
    /// @param rowA which row in A the vector is contained in.
    /// @param alpha scale factor of A
    /// @param B submatrix. Not modified.
    /// @param rowB which row in B the vector is contained in.
    /// @param beta scale factor of B
    /// @param C submatrix where the results are written to. Modified.
    /// @param rowC which row in C is the vector contained.
    /// @param offset Index at which the vectors start at.
    /// @param end Index at which the vectors end at.
    public static void add_row( final int blockLength,
                                DSubmatrixD1 A, int rowA, double alpha,
                                DSubmatrixD1 B, int rowB, double beta,
                                DSubmatrixD1 C, int rowC,
                                int offset, int end ) {
        // handle rows in any block
        int rowBlockA = A.row0 + rowA - rowA%blockLength;
        rowA = rowA%blockLength;
        int rowBlockB = B.row0 + rowB - rowB%blockLength;
        rowB = rowB%blockLength;
        int rowBlockC = C.row0 + rowC - rowC%blockLength;
        rowC = rowC%blockLength;

        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);
        final int heightC = Math.min(blockLength, C.row1 - rowBlockC);

        // handle the case where offset is more than a block
        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        final double[] dataC = C.original.data;

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            int widthB = Math.min(blockLength, B.col1 - B.col0 - i);
            int widthC = Math.min(blockLength, C.col1 - C.col0 - i);

            int indexA = rowBlockA*A.original.numCols + (A.col0 + i)*heightA + rowA*widthA;
            int indexB = rowBlockB*B.original.numCols + (B.col0 + i)*heightB + rowB*widthB;
            int indexC = rowBlockC*C.original.numCols + (C.col0 + i)*heightC + rowC*widthC;

            if (i == startI) {
                indexA += offset;
                indexB += offset;
                indexC += offset;

                for (int j = offset; j < segment; j++) {
                    dataC[indexC++] = alpha*dataA[indexA++] + beta*dataB[indexB++];
                }
            } else {
                for (int j = 0; j < segment; j++) {
                    dataC[indexC++] = alpha*dataA[indexA++] + beta*dataB[indexB++];
                }
            }
        }
    }

    /// Row vector dot/inner product:
    ///
    /// dot: c = sum a<sub>i</sub>\*b<sub>i</sub>
    ///
    /// where 'a' and 'b' are row vectors within the row block vector A and B, and 'c' is a scalar.
    ///
    /// @param A submatrix. Not modified.
    /// @param rowA which row in `A` the vector is contained in.
    /// @param B submatrix. Not modified.
    /// @param rowB which row in B the vector is contained in.
    /// @param offset Index at which the vectors start at.
    /// @param end Index at which the vectors end at.
    /// @return Results of the dot product.
    public static double dot_row( final int blockLength,
                                  DSubmatrixD1 A, int rowA,
                                  DSubmatrixD1 B, int rowB,
                                  int offset, int end ) {


        // handle the case where offset is more than a block
        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;

        double total = 0;

        // handle rows in any block
        int rowBlockA = A.row0 + rowA - rowA%blockLength;
        rowA = rowA%blockLength;
        int rowBlockB = B.row0 + rowB - rowB%blockLength;
        rowB = rowB%blockLength;

        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int heightB = Math.min(blockLength, B.row1 - rowBlockB);

        if (A.col1 - A.col0 != B.col1 - B.col0)
            throw new RuntimeException();

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            int widthB = Math.min(blockLength, B.col1 - B.col0 - i);

            int indexA = rowBlockA*A.original.numCols + (A.col0 + i)*heightA + rowA*widthA;
            int indexB = rowBlockB*B.original.numCols + (B.col0 + i)*heightB + rowB*widthB;

            if (i == startI) {
                indexA += offset;
                indexB += offset;

                for (int j = offset; j < segment; j++) {
                    total += dataB[indexB++]*dataA[indexA++];
                }
            } else {
                for (int j = 0; j < segment; j++) {
                    total += dataB[indexB++]*dataA[indexA++];
                }
            }
        }

        return total;
    }

    /// vector dot/inner product from one row vector and one column vector:
    ///
    /// dot: c = sum a<sub>i</sub>\*b<sub>i</sub>
    ///
    /// where 'a' is a row vector 'b' is a column vectors within the row block vector A and B, and 'c' is a scalar.
    ///
    /// @param A block row vector. Not modified.
    /// @param rowA which row in `A` the vector is contained in.
    /// @param B block column vector. Not modified.
    /// @param colB which column in B is the vector contained in.
    /// @param offset Index at which the vectors start at.
    /// @param end Index at which the vectors end at.
    /// @return Results of the dot product.
    public static double dot_row_col( final int blockLength,
                                      DSubmatrixD1 A, int rowA,
                                      DSubmatrixD1 B, int colB,
                                      int offset, int end ) {


        // handle the case where offset is more than a block
        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;

        double total = 0;

        // handle rows in any block
        int rowBlockA = A.row0 + rowA - rowA%blockLength;
        rowA = rowA%blockLength;
        int colBlockB = B.col0 + colB - colB%blockLength;
        colB = colB%blockLength;

        final int heightA = Math.min(blockLength, A.row1 - rowBlockA);
        final int widthB = Math.min(blockLength, B.col1 - colBlockB);

        if (A.col1 - A.col0 != B.col1 - B.col0)
            throw new RuntimeException();

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int widthA = Math.min(blockLength, A.col1 - A.col0 - i);
            int heightB = Math.min(blockLength, B.row1 - B.row0 - i);

            int indexA = rowBlockA*A.original.numCols + (A.col0 + i)*heightA + rowA*widthA;
            int indexB = (B.row0 + i)*B.original.numCols + colBlockB*heightB + colB;

            if (i == startI) {
                indexA += offset;
                indexB += offset*widthB;

                for (int j = offset; j < segment; j++, indexB += widthB) {
                    total += dataB[indexB]*dataA[indexA++];
                }
            } else {
                for (int j = 0; j < segment; j++, indexB += widthB) {
                    total += dataB[indexB]*dataA[indexA++];
                }
            }
        }

        return total;
    }

    /// Column vector scale: `b = alpha*a`, where `a` and `b` are column vectors within the block column
    /// vectors A and B.
    ///
    /// @param offset Row index at which the vectors start.
    /// @param end Row index at which the vectors end.
    public static void scale_col( final int blockLength,
                                  DSubmatrixD1 A, int colA,
                                  double alpha, DSubmatrixD1 B, int colB,
                                  int offset, int end ) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;

        // handle columns in any block
        int colBlockA = A.col0 + colA - colA%blockLength;
        colA = colA%blockLength;
        int colBlockB = B.col0 + colB - colB%blockLength;
        colB = colB%blockLength;

        final int widthA = Math.min(blockLength, A.col1 - colBlockA);
        final int widthB = Math.min(blockLength, B.col1 - colBlockB);

        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int heightA = Math.min(blockLength, A.row1 - A.row0 - i);
            int heightB = Math.min(blockLength, B.row1 - B.row0 - i);

            int indexA = (A.row0 + i)*A.original.numCols + colBlockA*heightA + colA;
            int indexB = (B.row0 + i)*B.original.numCols + colBlockB*heightB + colB;

            int rowStart = 0;
            if (i == startI) {
                indexA += offset*widthA;
                indexB += offset*widthB;
                rowStart = offset;
            }
            for (int j = rowStart; j < segment; j++, indexA += widthA, indexB += widthB) {
                dataB[indexB] = alpha*dataA[indexA];
            }
        }
    }

    /// Column vector add: `c = alpha*a + beta*b`, where `a`, `b`, and `c` are column vectors within the block
    /// column vectors A, B, and C.
    ///
    /// @param offset Row index at which the vectors start.
    /// @param end Row index at which the vectors end.
    public static void add_col( final int blockLength,
                                DSubmatrixD1 A, int colA, double alpha,
                                DSubmatrixD1 B, int colB, double beta,
                                DSubmatrixD1 C, int colC,
                                int offset, int end ) {
        final double[] dataA = A.original.data;
        final double[] dataB = B.original.data;
        final double[] dataC = C.original.data;

        // handle columns in any block
        int colBlockA = A.col0 + colA - colA%blockLength;
        colA = colA%blockLength;
        int colBlockB = B.col0 + colB - colB%blockLength;
        colB = colB%blockLength;
        int colBlockC = C.col0 + colC - colC%blockLength;
        colC = colC%blockLength;

        final int widthA = Math.min(blockLength, A.col1 - colBlockA);
        final int widthB = Math.min(blockLength, B.col1 - colBlockB);
        final int widthC = Math.min(blockLength, C.col1 - colBlockC);

        int startI = offset - offset%blockLength;
        offset = offset%blockLength;

        for (int i = startI; i < end; i += blockLength) {
            int segment = Math.min(blockLength, end - i);

            int heightA = Math.min(blockLength, A.row1 - A.row0 - i);
            int heightB = Math.min(blockLength, B.row1 - B.row0 - i);
            int heightC = Math.min(blockLength, C.row1 - C.row0 - i);

            int indexA = (A.row0 + i)*A.original.numCols + colBlockA*heightA + colA;
            int indexB = (B.row0 + i)*B.original.numCols + colBlockB*heightB + colB;
            int indexC = (C.row0 + i)*C.original.numCols + colBlockC*heightC + colC;

            int rowStart = 0;
            if (i == startI) {
                indexA += offset*widthA;
                indexB += offset*widthB;
                indexC += offset*widthC;
                rowStart = offset;
            }
            for (int j = rowStart; j < segment; j++, indexA += widthA, indexB += widthB, indexC += widthC) {
                dataC[indexC] = alpha*dataA[indexA] + beta*dataB[indexB];
            }
        }
    }
}
