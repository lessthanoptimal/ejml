/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.misc;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DoubleSemiRing;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.ejml.UtilEjml.adjust;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMultWithSemiRing_DSCC.multAddColA;

/**
 * based on ImplCommonOps_DSCC
 */
public class ImplCommonOpsWithSemiRing_DSCC {

    /**
     * Performs a matrix transpose.
     *
     * @param A  Original matrix.  Not modified.
     * @param C  Storage for transposed 'a'.  Reshaped.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     */
    public static void transpose(DMatrixSparseCSC A, DMatrixSparseCSC C, @Nullable IGrowArray gw) {
        ImplCommonOps_DSCC.transpose(A, C, gw);
    }

    /**
     * Performs matrix addition:<br>
     * C = A + B
     *
     * @param A  Matrix
     * @param B  Matrix
     * @param C  Output matrix.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                           @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        double[] x = adjust(gx, A.numRows);
        int[] w = adjust(gw, A.numRows, A.numRows);

        C.indicesSorted = false;
        C.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            C.col_idx[col] = C.nz_length;

            multAddColA(A, col, alpha, C, col + 1, semiRing, x, w);
            multAddColA(B, col, beta, C, col + 1, semiRing, x, w);

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[col];
            int idxC1 = C.col_idx[col + 1];

            for (int i = idxC0; i < idxC1; i++) {
                C.nz_values[i] = x[C.nz_rows[i]];
            }
        }
        C.col_idx[A.numCols] = C.nz_length;
    }

    /**
     * Adds the results of adding a column in A and B as a new column in C.<br>
     * C(:,end+1) = A(:,colA) + B(:,colB)
     *
     * @param A    matrix
     * @param colA column in A
     * @param B    matrix
     * @param colB column in B
     * @param C    Column in C
     * @param gw   workspace
     */
    public static void addColAppend(DMatrixSparseCSC A, int colA, DMatrixSparseCSC B, int colB,
                                    DMatrixSparseCSC C, DoubleSemiRing semiRing, @Nullable IGrowArray gw) {
        if (A.numRows != B.numRows || A.numRows != C.numRows)
            throw new IllegalArgumentException("Number of rows in A, B, and C do not match");

        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA + 1];
        int idxB0 = B.col_idx[colB];
        int idxB1 = B.col_idx[colB + 1];

        C.growMaxColumns(++C.numCols, true);
        C.growMaxLength(C.nz_length + idxA1 - idxA0 + idxB1 - idxB0, true);

        int[] w = adjust(gw, A.numRows);
        Arrays.fill(w, 0, A.numRows, -1);

        for (int i = idxA0; i < idxA1; i++) {
            int row = A.nz_rows[i];
            C.nz_rows[C.nz_length] = row;
            C.nz_values[C.nz_length] = A.nz_values[i];
            w[row] = C.nz_length++;
        }

        for (int i = idxB0; i < idxB1; i++) {
            int row = B.nz_rows[i];
            if (w[row] != -1) {
                C.nz_values[w[row]] = semiRing.add.func.apply(C.nz_values[w[row]], B.nz_values[i]);
            } else {
                C.nz_values[C.nz_length] = B.nz_values[i];
                C.nz_rows[C.nz_length++] = row;
            }
        }
        C.col_idx[C.numCols] = C.nz_length;
    }

    /**
     * Performs element-wise multiplication:<br>
     * C_ij = A_ij * B_ij
     *
     * @param A  (Input) Matrix
     * @param B  (Input) Matrix
     * @param C  (Output) Matrix.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void elementMult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                                   @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        double[] x = adjust(gx, A.numRows);
        int[] w = adjust(gw, A.numRows);
        Arrays.fill(w, 0, A.numRows, -1); // fill with -1. This will be a value less than column

        C.growMaxLength(Math.min(A.nz_length, B.nz_length), false);
        C.indicesSorted = false; // Hmm I think if B is storted then C will be sorted...
        C.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            int idxA0 = A.col_idx[col];
            int idxA1 = A.col_idx[col + 1];
            int idxB0 = B.col_idx[col];
            int idxB1 = B.col_idx[col + 1];

            // compute the maximum number of elements that there can be in this row
            int maxInRow = Math.min(idxA1 - idxA0, idxB1 - idxB0);

            // make sure there are enough non-zero elements in C
            if (C.nz_length + maxInRow > C.nz_values.length)
                C.growMaxLength(C.nz_values.length + maxInRow, true);

            // update the structure of C
            C.col_idx[col] = C.nz_length;

            // mark the rows that appear in A and save their value
            for (int i = idxA0; i < idxA1; i++) {
                int row = A.nz_rows[i];
                w[row] = col;
                x[row] = A.nz_values[i];
            }

            // If a row appears in A and B, multiply and set as an element in C
            for (int i = idxB0; i < idxB1; i++) {
                int row = B.nz_rows[i];
                if (w[row] == col) {
                    C.nz_values[C.nz_length] = semiRing.mult.func.apply(x[row], B.nz_values[i]);
                    C.nz_rows[C.nz_length++] = row;
                }
            }
        }
        C.col_idx[C.numCols] = C.nz_length;
    }
}