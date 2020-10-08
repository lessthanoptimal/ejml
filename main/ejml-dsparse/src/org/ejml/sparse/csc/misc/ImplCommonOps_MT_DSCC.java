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

package org.ejml.sparse.csc.misc;

import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.concurrency.GrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.mult.Workspace_MT_DSCC;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC.multAddColA;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMult_MT_DSCC.stitchMatrix;

/**
 * Concurrent implementations of {@link ImplCommonOps_DSCC}.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_MT_DSCC {
    /**
     * Performs matrix addition:<br>
     * C = &alpha;A + &beta;B
     *
     * @param alpha scalar value multiplied against A
     * @param A Matrix
     * @param beta scalar value multiplied against B
     * @param B Matrix
     * @param C Output matrix.
     * @param listWork (Optional) Storage for internal workspace.  Can be null.
     */
    public static void add( double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C,
                            @Nullable GrowArray<Workspace_MT_DSCC> listWork ) {
        if (listWork == null)
            listWork = new GrowArray<>(Workspace_MT_DSCC::new);

        // Break the problem up into blocks of columns and process them independently
        EjmlConcurrency.loopBlocks(0, A.numCols, listWork, ( workspace, col0, col1 ) -> {
            DMatrixSparseCSC workC = workspace.mat;
            workC.reshape(A.numRows, col1 - col0, col1 - col0);
            workC.col_idx[0] = 0;

            double[] x = adjust(workspace.gx, A.numRows);
            int[] w = adjust(workspace.gw, A.numRows, A.numRows);

            for (int col = col0; col < col1; col++) {
                int colC = col - col0;
                workC.col_idx[colC] = workC.nz_length;

                multAddColA(A, col, alpha, workC, colC + 1, x, w);
                multAddColA(B, col, beta, workC, colC + 1, x, w);

                // take the values in the dense vector 'x' and put them into 'C'
                int idxC0 = workC.col_idx[colC];
                int idxC1 = workC.col_idx[colC + 1];

                for (int i = idxC0; i < idxC1; i++) {
                    workC.nz_values[i] = x[workC.nz_rows[i]];
                }
            }
            workC.col_idx[col1-col0] = workC.nz_length;
        });

        // Stitch the output back together
        stitchMatrix(C, A.numRows, A.numCols, listWork);
    }
}
