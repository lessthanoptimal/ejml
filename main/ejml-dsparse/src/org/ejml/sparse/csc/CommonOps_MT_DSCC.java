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

package org.ejml.sparse.csc;

import org.ejml.MatrixDimensionException;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.misc.ImplCommonOps_MT_DSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMult_MT_DSCC;
import org.ejml.sparse.csc.mult.Workspace_MT_DSCC;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

import static org.ejml.UtilEjml.reshapeOrDeclare;
import static org.ejml.UtilEjml.stringShapes;

/**
 * Concurrent implementations of functions found in {@link CommonOps_DSCC}.
 *
 * @author Peter Abeles
 */
public class CommonOps_MT_DSCC {
    public static DMatrixSparseCSC mult( DMatrixSparseCSC A, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC outputC ) {
        return mult(A, B, outputC, null);
    }

    /**
     * Performs matrix multiplication.  C = A*B. Concurrency workspace is about the same size as the resulting "output"
     * matrix.
     *
     * @param A (Input) Matrix. Not modified.
     * @param B (Input) Matrix. Not modified.
     * @param outputC (Output) Storage for results.  Data length is increased if insufficient.
     * @param listWork (Optional) Storage for internal workspace.  Can be null.
     */
    public static DMatrixSparseCSC mult( DMatrixSparseCSC A, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC outputC,
                                         @Nullable GrowArray<Workspace_MT_DSCC> listWork ) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        outputC = reshapeOrDeclare(outputC, A, A.numRows, B.numCols);

        ImplSparseSparseMult_MT_DSCC.mult(A, B, outputC, listWork);

        return outputC;
    }

    /**
     * Performs matrix addition:<br>
     *
     * C = &alpha;A + &beta;B
     *
     * @param alpha scalar value multiplied against A
     * @param A Matrix
     * @param beta scalar value multiplied against B
     * @param B Matrix
     * @param outputC Output matrix.
     * @param listWork (Optional) Storage for internal workspace.  Can be null.
     */
    public static DMatrixSparseCSC add( double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B,
                                        @Nullable DMatrixSparseCSC outputC,
                                        @Nullable GrowArray<Workspace_MT_DSCC> listWork ) {
        if (A.numRows != B.numRows || A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        outputC = reshapeOrDeclare(outputC, A, A.numRows, A.numCols);

        ImplCommonOps_MT_DSCC.add(alpha, A, beta, B, outputC, listWork);

        return outputC;
    }
}
