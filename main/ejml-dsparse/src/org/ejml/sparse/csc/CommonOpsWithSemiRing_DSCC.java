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
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DSemiRing;
import org.ejml.sparse.csc.misc.ImplCommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.mult.ImplMultiplicationWithSemiRing_DSCC;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.reshapeOrDeclare;
import static org.ejml.UtilEjml.stringShapes;


public class CommonOpsWithSemiRing_DSCC {

    public static DMatrixSparseCSC mult(DMatrixSparseCSC A, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC output, DSemiRing semiRing) {
        return mult(A, B, output, semiRing, null, null);
    }

    /**
     * Performs matrix multiplication.  output = A*B
     *
     * @param A        (Input) Matrix. Not modified.
     * @param B        (Input) Matrix. Not modified.
     * @param output   (Output) Storage for results.  Data length is increased if insufficient.
     * @param semiRing Semi-Ring to define + and *
     * @param gw       (Optional) Storage for internal workspace.  Can be null.
     * @param gx       (Optional) Storage for internal workspace.  Can be null.
     */
    public static DMatrixSparseCSC mult(DMatrixSparseCSC A, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC output, DSemiRing semiRing,
                                        @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        output = reshapeOrDeclare(output, A, A.numRows, B.numCols);

        ImplMultiplicationWithSemiRing_DSCC.mult(A, B, output, semiRing, gw, gx);

        return output;
    }

    /**
     * Performs matrix multiplication.  output = A*B
     *
     * @param A        Matrix
     * @param B        Dense Matrix
     * @param semiRing Semi-Ring to define + and *
     * @param output   Dense Matrix
     */
    public static DMatrixRMaj mult(DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));

        output = reshapeOrDeclare(output, A.numRows, B.numCols);

        ImplMultiplicationWithSemiRing_DSCC.mult(A, B, output, semiRing);

        return output;
    }

    /**
     * <p>output = output + A*B</p>
     */
    public static void multAdd(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numRows != output.numRows || B.numCols != output.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, output));

        ImplMultiplicationWithSemiRing_DSCC.multAdd(A, B, output, semiRing);
    }

    /**
     * Performs matrix multiplication.  output = A<sup>T</sup>*B
     *
     * @param A        Matrix
     * @param B        Dense Matrix
     * @param output   Dense Matrix
     * @param semiRing Semi-Ring to define + and *
     */
    public static DMatrixRMaj multTransA(DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numRows != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));

        output = reshapeOrDeclare(output, A.numCols, B.numCols);

        ImplMultiplicationWithSemiRing_DSCC.multTransA(A, B, output, semiRing);

        return output;
    }

    /**
     * <p>output = output + A<sup>T</sup>*B</p>
     */
    public static void multAddTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numCols != output.numRows || B.numCols != output.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, output));

        ImplMultiplicationWithSemiRing_DSCC.multAddTransA(A, B, output, semiRing);
    }

    /**
     * Performs matrix multiplication.  output = A*B<sup>T</sup>
     *
     * @param A        Matrix
     * @param B        Dense Matrix
     * @param output   Dense Matrix
     * @param semiRing Semi-Ring to define + and *
     */
    public static DMatrixRMaj multTransB(DMatrixSparseCSC A, DMatrixRMaj B, @Nullable DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        output = reshapeOrDeclare(output, A.numRows, B.numRows);

        ImplMultiplicationWithSemiRing_DSCC.multTransB(A, B, output, semiRing);

        return output;
    }

    /**
     * <p>output = output + A*B<sup>T</sup></p>
     */
    public static void multAddTransB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numRows != output.numRows || B.numRows != output.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, output));

        ImplMultiplicationWithSemiRing_DSCC.multAddTransB(A, B, output, semiRing);
    }

    /**
     * Performs matrix multiplication.  output = A<sup>T</sup>*B<sup>T</sup>
     *
     * @param A        Matrix
     * @param B        Dense Matrix
     * @param output   Dense Matrix
     * @param semiRing Semi-Ring to define + and *
     */
    public static DMatrixRMaj multTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output, DSemiRing semiRing) {
        if (A.numRows != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        output = reshapeOrDeclare(output, A.numCols, B.numRows);

        ImplMultiplicationWithSemiRing_DSCC.multTransAB(A, B, output, semiRing);

        return output;
    }


    /**
     * <p>C = C + A<sup>T</sup>*B<sup>T</sup></p>
     */
    public static void multAddTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DSemiRing semiRing) {
        if (A.numCols != C.numRows || B.numRows != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, C));

        ImplMultiplicationWithSemiRing_DSCC.multAddTransAB(A, B, C, semiRing);
    }

    /**
     * Performs matrix addition:<br>
     * output = &alpha;A + &beta;B
     *
     * @param alpha    scalar value multiplied against A
     * @param A        Matrix
     * @param beta     scalar value multiplied against B
     * @param B        Matrix
     * @param output   (Optional)    Output matrix.
     * @param semiRing Semi-Ring to define + and *
     * @param gw       (Optional) Storage for internal workspace.  Can be null.
     * @param gx       (Optional) Storage for internal workspace.  Can be null.
     */
    public static DMatrixSparseCSC add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC output, DSemiRing semiRing,
                                       @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numRows != B.numRows || A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        output = reshapeOrDeclare(output, A, A.numRows, A.numCols);

        ImplCommonOpsWithSemiRing_DSCC.add(alpha, A, beta, B, output, semiRing, gw, gx);

        return output;
    }

    /**
     * Performs an element-wise multiplication.<br>
     * output[i,j] = A[i,j]*B[i,j]<br>
     * All matrices must have the same shape.
     *
     * @param A        (Input) Matrix.
     * @param B        (Input) Matrix
     * @param output   (Output) Matrix. data array is grown to min(A.nz_length,B.nz_length), resulting a in a large speed boost.
     * @param semiRing Semi-Ring to define + and *
     * @param gw       (Optional) Storage for internal workspace.  Can be null.
     * @param gx       (Optional) Storage for internal workspace.  Can be null.
     */
    public static DMatrixSparseCSC elementMult(DMatrixSparseCSC A, DMatrixSparseCSC B, @Nullable DMatrixSparseCSC output, DSemiRing semiRing,
                                               @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numCols != B.numCols || A.numRows != B.numRows)
            throw new MatrixDimensionException("All inputs must have the same number of rows and columns. " + stringShapes(A, B));
        output = reshapeOrDeclare(output, A, A.numRows, A.numCols);

        ImplCommonOpsWithSemiRing_DSCC.elementMult(A, B, output, semiRing, gw, gx);

        return output;
    }
}
