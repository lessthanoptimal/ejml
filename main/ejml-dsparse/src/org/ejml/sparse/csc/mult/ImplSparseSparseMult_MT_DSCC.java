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

package org.ejml.sparse.csc.mult;

import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.concurrency.GrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC.multAddColA;

/**
 * Concurrent matrix multiplication for DSCC matrices.
 *
 * @author Peter Abeles
 */
public class ImplSparseSparseMult_MT_DSCC {
    /**
     * Performs matrix multiplication.  C = A*B. The problem is broken up into as many "blocks" as there are threads
     * available. Each block will process a set of columns independently. After running results from independent
     * blocks are stitched together in the main thread. Extra storage requirements is about the same size as
     * 'C'.
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param listWork (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult( DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C,
                             @Nullable GrowArray<Workspace_MT_DSCC> listWork ) {
        if (listWork == null)
            listWork = new GrowArray<>(Workspace_MT_DSCC::new);

        // Break the problem up into blocks of columns and process them independently
        EjmlConcurrency.loopBlocks(0, B.numCols, listWork, ( workspace, bj0, bj1 ) -> {
            DMatrixSparseCSC workC = workspace.mat;
            workC.reshape(A.numRows, bj1 - bj0, bj1 - bj0);
            workC.col_idx[0] = 0;

            double[] x = adjust(workspace.gx, A.numRows);
            int[] w = adjust(workspace.gw, A.numRows, A.numRows);

            // C(i,j) = sum_k A(i,k) * B(k,j)
            for (int bj = bj0; bj < bj1; bj++) {
                int colC = bj - bj0;
                int idx0 = B.col_idx[bj];
                int idx1 = B.col_idx[bj + 1];
                workC.col_idx[colC + 1] = workC.nz_length;

                if (idx0 == idx1) {
                    continue;
                }

                // C(:,j) = sum_k A(:,k)*B(k,j)
                for (int bi = idx0; bi < idx1; bi++) {
                    int rowB = B.nz_rows[bi];
                    double valB = B.nz_values[bi];  // B(k,j)  k=rowB j=colB

                    multAddColA(A, rowB, valB, workC, colC + 1, x, w);
                }

                // take the values in the dense vector 'x' and put them into 'C'
                int idxC0 = workC.col_idx[colC];
                int idxC1 = workC.col_idx[colC + 1];

                for (int i = idxC0; i < idxC1; i++) {
                    workC.nz_values[i] = x[workC.nz_rows[i]];
                }
            }
        });

        // Stitch the output back together
        stitchMatrix(C, A.numRows, B.numCols, listWork);
    }

    /**
     * Compines results from independent blocks into a single matrix
     */
    public static void stitchMatrix( DMatrixSparseCSC out, int numRows, int numCols,
                                     GrowArray<Workspace_MT_DSCC> listWork ) {
        out.reshape(numRows, numCols);
        out.indicesSorted = false;
        out.nz_length = 0;

        for (int i = 0; i < listWork.size(); i++) {
            out.nz_length += listWork.get(i).mat.nz_length;
        }
        out.growMaxLength(out.nz_length, false);

        out.nz_length = 0;
        out.numCols = 0;
        out.col_idx[0] = 0;
        for (int i = 0; i < listWork.size(); i++) {
            Workspace_MT_DSCC workspace = listWork.get(i);

            System.arraycopy(workspace.mat.nz_rows, 0, out.nz_rows, out.nz_length, workspace.mat.nz_length);
            System.arraycopy(workspace.mat.nz_values, 0, out.nz_values, out.nz_length, workspace.mat.nz_length);

            for (int col = 1; col <= workspace.mat.numCols; col++) {
                out.col_idx[++out.numCols] = out.nz_length + workspace.mat.col_idx[col];
            }

            out.nz_length += workspace.mat.nz_length;
        }
    }

    /**
     * Computes the inner product of A times A and stores the results in B. The inner product is symmetric and this
     * function will only store the lower triangle. If the full matrix is needed then.
     *
     * <p>B = A<sup>T</sup>*A</sup>
     *
     * @param A (Input) Matrix
     * @param B (Output) Storage for output.
     * @param listWork (Optional) Storage for internal workspace.  Can be null.
     */
    public static void innerProductLower(DMatrixSparseCSC A , DMatrixSparseCSC B ,
                                         @Nullable GrowArray<Workspace_MT_DSCC> listWork ) {
        if (listWork == null)
            listWork = new GrowArray<>(Workspace_MT_DSCC::new);

        EjmlConcurrency.loopBlocks(0, A.numCols, listWork, ( workspace, bj0, bj1 ) -> {
            DMatrixSparseCSC workB = workspace.mat;
            workB.reshape(A.numCols, bj1 - bj0, bj1 - bj0);
            workB.col_idx[0] = 0;

            double[] x = adjust(workspace.gx, A.numRows);
            int[] w = adjust(workspace.gw, A.numRows, A.numRows);

            for (int colI = bj0; colI < bj1; colI++) {
                int idx0 = A.col_idx[colI];
                int idx1 = A.col_idx[colI + 1];

                int mark = colI + 1;

                // Sparse copy into dense vector
                for (int i = idx0; i < idx1; i++) {
                    int row = A.nz_rows[i];
                    w[row] = mark;
                    x[row] = A.nz_values[i];
                }

                // Compute dot product along each column
                for (int colJ = colI; colJ < A.numCols; colJ++) {
                    double sum = 0;
                    idx0 = A.col_idx[colJ];
                    idx1 = A.col_idx[colJ + 1];

                    for (int i = idx0; i < idx1; i++) {
                        int row = A.nz_rows[i];
                        if (w[row] == mark) {
                            sum += x[row]*A.nz_values[i];
                        }
                    }

                    if (sum == 0)
                        continue;

                    if (workB.nz_length == workB.nz_values.length) {
                        workB.growMaxLength(workB.nz_length*2 + 1, true);
                    }
                    workB.nz_values[workB.nz_length] = sum;
                    workB.nz_rows[workB.nz_length++] = colJ;
                }
                workB.col_idx[colI + 1 - bj0] = workB.nz_length;
            }
        });

        stitchMatrix(B,A.numCols, A.numCols, listWork);
    }
}
