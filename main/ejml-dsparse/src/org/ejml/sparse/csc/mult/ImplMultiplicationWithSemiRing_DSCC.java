/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
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
import org.ejml.masks.Mask;
import org.ejml.ops.DSemiRing;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;

/**
 * based on ImplSparseSparseGraphMult_DSCC
 */
public class ImplMultiplicationWithSemiRing_DSCC {

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param mask (Optional) Mask for specifying which entries should be overwritten
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DSemiRing semiRing,
                            @Nullable Mask mask, @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
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

            // as colB mask is accessed for each colB entry
            if (mask != null) {
                mask.setIndexColumn(colB);
            }

            // C(:,j) = sum_k A(:,k)*B(k,j)
            for (int bi = idx0; bi < idx1; bi++) {
                int rowB = B.nz_rows[bi];
                double valB = B.nz_values[bi];  // B(k,j)  k=rowB j=colB

                multAddColA(A, rowB, valB, C, colB + 1, semiRing, mask, x, w);
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
                                    DSemiRing semiRing,
                                    @Nullable Mask mask, double[] x, int[] w ) {
        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA + 1];

        for (int j = idxA0; j < idxA1; j++) {
            int row = A.nz_rows[j];

            if (mask == null || mask.isSet(row, mark - 1)) {
                if (w[row] < mark) {
                    if (C.nz_length >= C.nz_rows.length) {
                        int growToLength = C.nz_length*2 + 1;
                        if (mask != null) {
                            growToLength = Math.min(growToLength, mask.maxMaskedEntries());
                        }
                        C.growMaxLength(growToLength, true);
                    }

                    w[row] = mark;
                    C.nz_rows[C.nz_length] = row;
                    C.col_idx[mark] = ++C.nz_length;
                    x[row] = semiRing.mult.func.apply(A.nz_values[j], alpha);
                } else {
                    x[row] = semiRing.add.func.apply(x[row], semiRing.mult.func.apply(A.nz_values[j], alpha));
                }
            }
        }
    }

    public static void mult(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        C.fill(semiRing.add.id);
        multAdd(A, B, C, semiRing);
    }

    public static void multAdd(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        // C(i,j) = sum_k A(i,k) * B(k,j)
        for (int k = 0; k < A.numCols; k++) {
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k + 1];

            for (int indexA = idx0; indexA < idx1; indexA++) {
                int i = A.nz_rows[indexA];
                double valueA = A.nz_values[indexA];

                int indexB = k * B.numCols;
                int indexC = i * C.numCols;
                int end = indexB + B.numCols;

//                for (int j = 0; j < B.numCols; j++) {
                while (indexB < end) {
                    C.data[indexC++] = semiRing.add.func.apply(C.data[indexC++], semiRing.mult.func.apply(valueA, B.data[indexB++]));
                }
            }
        }
    }

    public static void multTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {

        // C(i,j) = sum_k A(k,i) * B(k,j)
        for (int j = 0; j < B.numCols; j++) {

            for (int i = 0; i < A.numCols; i++) {
                int idx0 = A.col_idx[i];
                int idx1 = A.col_idx[i + 1];

                double sum = semiRing.add.id;
                for (int indexA = idx0; indexA < idx1; indexA++) {
                    int rowK = A.nz_rows[indexA];
                    sum = semiRing.add.func.apply(sum, semiRing.mult.func.apply(A.nz_values[indexA], B.data[rowK * B.numCols + j]));
                }

                C.data[i * C.numCols + j] = sum;
            }
        }
    }

    public static void multAddTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        // C(i,j) = sum_k A(k,i) * B(k,j)
        for (int j = 0; j < B.numCols; j++) {

            for (int i = 0; i < A.numCols; i++) {
                int idx0 = A.col_idx[i];
                int idx1 = A.col_idx[i + 1];

                double sum = semiRing.add.id;
                for (int indexA = idx0; indexA < idx1; indexA++) {
                    int rowK = A.nz_rows[indexA];
                    sum = semiRing.add.func.apply(sum, semiRing.mult.func.apply(A.nz_values[indexA], B.data[rowK * B.numCols + j]));
                }

                C.data[i * C.numCols + j] = semiRing.add.func.apply(C.data[i * C.numCols + j], sum);
            }
        }
    }

    public static void multTransB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        C.zero();
        multAddTransB(A, B, C, semiRing);
    }

    public static void multAddTransB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {

        // C(i,j) = sum_k A(i,k) * B(j,k)
        for (int k = 0; k < A.numCols; k++) {
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k + 1];
            for (int indexA = idx0; indexA < idx1; indexA++) {
                for (int j = 0; j < B.numRows; j++) {
                    int i = A.nz_rows[indexA];
                    C.data[i * C.numCols + j] = semiRing.add.func.apply(
                            C.data[i * C.numCols + j],
                            semiRing.mult.func.apply(
                                    A.nz_values[indexA],
                                    B.data[j * B.numCols + k]
                            )
                    );
                }
            }
        }
    }

    public static void multTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        C.zero();
        multAddTransAB(A, B, C, semiRing);
    }

    public static void multAddTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        // C(i,j) = sum_k A(k,i) * B(j,K)
        for (int i = 0; i < A.numCols; i++) {
            int idx0 = A.col_idx[i];
            int idx1 = A.col_idx[i + 1];

            for (int indexA = idx0; indexA < idx1; indexA++) {
                for (int j = 0; j < B.numRows; j++) {
                    int indexB = j * B.numCols;

                    int k = A.nz_rows[indexA];

                    C.data[i * C.numCols + j] = semiRing.add.func.apply(
                            C.data[i * C.numCols + j],
                            semiRing.mult.func.apply(
                                    A.nz_values[indexA],
                                    B.data[indexB + k]
                            )
                    );
                }
            }
        }
    }
}
