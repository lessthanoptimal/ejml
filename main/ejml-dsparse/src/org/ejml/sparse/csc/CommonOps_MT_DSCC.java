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
package org.ejml.sparse.csc;

import org.ejml.MatrixDimensionException;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.misc.ImplCommonOps_MT_DSCC;
import org.ejml.sparse.csc.mult.ImplMultiplication_MT_DSCC;
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

        if (listWork == null)
            listWork = new GrowArray<>(Workspace_MT_DSCC::new);

        ImplMultiplication_MT_DSCC.mult(A, B, outputC, listWork);

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

        if (listWork == null)
            listWork = new GrowArray<>(Workspace_MT_DSCC::new);

        ImplCommonOps_MT_DSCC.add(alpha, A, beta, B, outputC, listWork);

        return outputC;
    }

    /**
     * Performs matrix multiplication.  C = A<sup>T</sup>*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param outputC Dense Matrix
     */
    public static DMatrixRMaj mult( DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj outputC,
                                    @Nullable GrowArray<DGrowArray> workArrays ) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        outputC = reshapeOrDeclare(outputC, A.numRows, B.numCols);
        if (workArrays == null)
            workArrays = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.mult(A, B, outputC, workArrays);

        return outputC;
    }

    /**
     * <p>C = C + A<sup>T</sup>*B</p>
     */
    public static void multAdd( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj outputC,
                                @Nullable GrowArray<DGrowArray> workArrays ) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        if (A.numRows != outputC.numRows || B.numCols != outputC.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B, outputC));

        if (workArrays == null)
            workArrays = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.multAdd(A, B, outputC, workArrays);
    }

    /**
     * Performs matrix multiplication.  C = A<sup>T</sup>*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param outputC Dense Matrix
     */
    public static DMatrixRMaj multTransA( DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj outputC,
                                          @Nullable GrowArray<DGrowArray> workArray ) {
        if (A.numRows != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));

        outputC = reshapeOrDeclare(outputC, A.numCols, B.numCols);

        if (workArray == null)
            workArray = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.multTransA(A, B, outputC, workArray);

        return outputC;
    }

    /**
     * <p>C = C + A<sup>T</sup>*B</p>
     */
    public static void multAddTransA( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj outputC,
                                      @Nullable GrowArray<DGrowArray> workArray ) {
        if (A.numRows != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        if (A.numCols != outputC.numRows || B.numCols != outputC.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B, outputC));

        if (workArray == null)
            workArray = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.multAddTransA(A, B, outputC, workArray);
    }

    /**
     * Performs matrix multiplication.  C = A*B<sup>T</sup>
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param outputC Dense Matrix
     */
    public static DMatrixRMaj multTransB( DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj outputC,
                                          @Nullable GrowArray<DGrowArray> workArrays ) {
        if (A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        outputC = reshapeOrDeclare(outputC, A.numRows, B.numRows);

        if (workArrays == null)
            workArrays = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.multTransB(A, B, outputC, workArrays);

        return outputC;
    }

    /**
     * <p>C = C + A*B<sup>T</sup></p>
     */
    public static void multAddTransB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj outputC,
                                      @Nullable GrowArray<DGrowArray> workArrays ) {
        if (A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        if (A.numRows != outputC.numRows || B.numRows != outputC.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B, outputC));

        if (workArrays == null)
            workArrays = new GrowArray<>(DGrowArray::new);

        ImplMultiplication_MT_DSCC.multAddTransB(A, B, outputC, workArrays);
    }

    /**
     * Performs matrix multiplication.  C = A<sup>T</sup>*B<sup>T</sup>
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param outputC Dense Matrix
     */
    public static DMatrixRMaj multTransAB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj outputC ) {
        if (A.numRows != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        outputC = reshapeOrDeclare(outputC, A.numCols, B.numRows);

        ImplMultiplication_MT_DSCC.multTransAB(A, B, outputC);

        return outputC;
    }

    /**
     * <p>C = C + A<sup>T</sup>*B<sup>T</sup></p>
     */
    public static void multAddTransAB( DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj outputC ) {
        if (A.numRows != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        if (A.numCols != outputC.numRows || B.numRows != outputC.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B, outputC));

        ImplMultiplication_MT_DSCC.multAddTransAB(A, B, outputC);
    }
}
