/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.mult;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DOperatorBinary;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static org.ejml.UtilEjml.adjust;

/**
 * @author Peter Abeles
 */
public class ImplMultiplication_DSCC {

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C,
                             @Nullable IGrowArray gw, @Nullable DGrowArray gx ) {
        double[] x = adjust(gx, A.numRows);
        int[] w = adjust(gw, A.numRows, A.numRows);

        C.growMaxLength(A.nz_length + B.nz_length, false);
        C.indicesSorted = false;
        C.nz_length = 0;

        // C(i,j) = sum_k A(i,k) * B(k,j)
        int idx0 = B.col_idx[0];
        for (int bj = 1; bj <= B.numCols; bj++) {
            int colB = bj - 1;
            int idx1 = B.col_idx[bj];
            C.col_idx[bj] = C.nz_length;

            if (idx0 == idx1) {
                continue;
            }

            // C(:,j) = sum_k A(:,k)*B(k,j)
            for (int bi = idx0; bi < idx1; bi++) {
                int rowB = B.nz_rows[bi];
                double valB = B.nz_values[bi];  // B(k,j)  k=rowB j=colB

                multAddColA(A, rowB, valB, C, colB + 1, x, w);
            }

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[colB];
            int idxC1 = C.col_idx[colB + 1];

            for (int i = idxC0; i < idxC1; i++) {
                C.nz_values[i] = x[C.nz_rows[i]];
            }

            idx0 = idx1;
        }
    }

    /**
     * Performs the operation x = x + A(:,i)*alpha
     *
     * <p>NOTE: This is the same as cs_scatter() in csparse.</p>
     */
    public static void multAddColA( DMatrixSparseCSC A, int colA,
                                    double alpha,
                                    DMatrixSparseCSC C, int mark,
                                    double[] x, int[] w ) {
        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA + 1];

        for (int j = idxA0; j < idxA1; j++) {
            int row = A.nz_rows[j];

            if (w[row] < mark) {
                if (C.nz_length >= C.nz_rows.length) {
                    C.growMaxLength(C.nz_length*2 + 1, true);
                }

                w[row] = mark;
                C.nz_rows[C.nz_length] = row;
                C.col_idx[mark] = ++C.nz_length;
                x[row] = A.nz_values[j]*alpha;
            } else {
                x[row] += A.nz_values[j]*alpha;
            }
        }
    }

    /**
     * Adds rows to C[*,colC] that are in A[*,colA] as long as they are marked in w. This is used to grow C
     * and colC must be the last filled in column in C.
     *
     * <p>NOTE: This is the same as cs_scatter if x is null.</p>
     *
     * @param A Matrix
     * @param colA The column in A that is being examined
     * @param C Matrix
     * @param colC Column in C that rows in A are being added to.
     * @param w An array used to indicate if a row in A should be added to C. if w[i] < colC AND i is a row
     * in A[*,colA] then it will be added.
     */
    public static void addRowsInAInToC( DMatrixSparseCSC A, int colA,
                                        DMatrixSparseCSC C, int colC,
                                        int[] w ) {
        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA + 1];

        for (int j = idxA0; j < idxA1; j++) {
            int row = A.nz_rows[j];

            if (w[row] < colC) {
                if (C.nz_length >= C.nz_rows.length) {
                    C.growMaxLength(C.nz_length*2 + 1, true);
                }

                w[row] = colC;
                C.nz_rows[C.nz_length++] = row;
            }
        }
        C.col_idx[colC + 1] = C.nz_length;
    }

    public static void mult( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C ) {
        C.zero();
        multAdd(A, B, C);
    }

    public static void multAdd( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C ) {
        // C(i,j) = sum_k A(i,k) * B(k,j)
        for (int k = 0; k < A.numCols; k++) {
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k + 1];

            for (int indexA = idx0; indexA < idx1; indexA++) {
                int i = A.nz_rows[indexA];
                double valueA = A.nz_values[indexA];

                int indexB = k*B.numCols;
                int indexC = i*C.numCols;
                int end = indexB + B.numCols;

//                for (int j = 0; j < B.numCols; j++) {
                while (indexB < end) {
                    C.data[indexC++] += valueA*B.data[indexB++];
                }
            }
        }
    }

    public static void multTransA( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DGrowArray workArray ) {
        multTransA(A, B, C, workArray, ( a, b ) -> b);
    }

    public static void multAddTransA( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DGrowArray workArray ) {
        multTransA(A, B, C, workArray, Double::sum);
    }

    public static void multTransA( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DGrowArray workArray,
                                   DOperatorBinary op ) {
        double[] work = workArray.reshape(B.numRows).data;

        // C(i,j) = sum_k A(k,i) * B(k,j)
        for (int j = 0; j < B.numCols; j++) {
            // local copy of row to avoid cache misses
            for (int k = 0; k < B.numRows; k++) {
                work[k] = B.data[k*B.numCols + j];
            }

            for (int i = 0; i < A.numCols; i++) {
                int idx0 = A.col_idx[i];
                int idx1 = A.col_idx[i + 1];

                double sum = 0;
                for (int indexA = idx0; indexA < idx1; indexA++) {
                    int k = A.nz_rows[indexA];
                    sum += A.nz_values[indexA]*work[k];
                }

                C.data[i*C.numCols + j] = op.apply(C.data[i*C.numCols + j], sum);
            }
        }
    }

    public static void multTransB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DGrowArray workArray ) {
        C.zero();
        multAddTransB(A, B, C, workArray);
    }

    public static void multAddTransB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DGrowArray workArray ) {
        double[] work = workArray.reshape(B.numRows).data;

        // C(i,j) = sum_k A(i,k) * B(j,k)
        for (int k = 0; k < A.numCols; k++) {
            // local copy of row to avoid cache misses
            for (int j = 0; j < B.numRows; j++) {
                work[j] = B.data[j*B.numCols + k];
            }

            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k + 1];
            for (int indexA = idx0; indexA < idx1; indexA++) {
                for (int j = 0; j < B.numRows; j++) {
                    int i = A.nz_rows[indexA];
                    C.data[i*C.numCols + j] += A.nz_values[indexA]*work[j];
                }
            }
        }
    }

    public static void multTransAB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C ) {
        // C(i,j) = sum_k A(k,i) * B(j,K)
        for (int j = 0; j < B.numRows; j++) {
            for (int i = 0; i < A.numCols; i++) {
                int idx0 = A.col_idx[i];
                int idx1 = A.col_idx[i + 1];

                final int indexRowB = j*B.numCols;

                double sum = 0;
                for (int indexA = idx0; indexA < idx1; indexA++) {
                    int k = A.nz_rows[indexA];
                    sum += A.nz_values[indexA]*B.data[indexRowB + k];
                }

                C.data[i*C.numCols + j] = sum;
            }
        }
    }

    public static void multAddTransAB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C ) {
        // C(i,j) = sum_k A(k,i) * B(j,K)
        for (int j = 0; j < B.numRows; j++) {
            for (int i = 0; i < A.numCols; i++) {
                int idx0 = A.col_idx[i];
                int idx1 = A.col_idx[i + 1];

                final int indexRowB = j*B.numCols;

                double sum = 0;
                for (int indexA = idx0; indexA < idx1; indexA++) {
                    int k = A.nz_rows[indexA];
                    sum += A.nz_values[indexA]*B.data[indexRowB + k];
                }

                C.data[i*C.numCols + j] += sum;
            }
        }
    }

    /**
     * Computes the inner product of two column vectors taken from the input matrices.
     *
     * <p>dot = A(:,colA)'*B(:,colB)</p>
     *
     * @param A Matrix
     * @param colA Column in A
     * @param B Matrix
     * @param colB Column in B
     * @return Dot product
     */
    public static double dotInnerColumns( DMatrixSparseCSC A, int colA, DMatrixSparseCSC B, int colB,
                                          @Nullable IGrowArray gw, @Nullable DGrowArray gx ) {
        if (A.numRows != B.numRows)
            throw new IllegalArgumentException("Number of rows must match.");

        int[] w = adjust(gw, A.numRows);
        Arrays.fill(w, 0, A.numRows, -1);
        double[] x = adjust(gx, A.numRows);

        int length = 0;

        int idx0 = A.col_idx[colA];
        int idx1 = A.col_idx[colA + 1];
        for (int i = idx0; i < idx1; i++) {
            int row = A.nz_rows[i];
            x[length] = A.nz_values[i];
            w[row] = length++;
        }

        double dot = 0;

        idx0 = B.col_idx[colB];
        idx1 = B.col_idx[colB + 1];
        for (int i = idx0; i < idx1; i++) {
            int row = B.nz_rows[i];
            if (w[row] != -1) {
                dot += x[w[row]]*B.nz_values[i];
            }
        }

        return dot;
    }
}
