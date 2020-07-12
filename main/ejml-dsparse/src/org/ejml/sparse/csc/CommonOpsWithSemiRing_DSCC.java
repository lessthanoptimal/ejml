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

package org.ejml.sparse.csc;

import org.ejml.MatrixDimensionException;
import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.ops.DoubleMonoid;
import org.ejml.ops.DoubleSemiRing;
import org.ejml.ops.DoubleUnaryOperator;
import org.ejml.sparse.csc.misc.ImplCommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.mult.ImplSparseSparseMultWithSemiRing_DSCC;

import javax.annotation.Nullable;
import java.util.Arrays;

import static org.ejml.UtilEjml.stringShapes;


public class CommonOpsWithSemiRing_DSCC {

    public static void mult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing) {
        mult(A, B, C, semiRing, null, null);
    }

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A  (Input) Matrix. Not modified.
     * @param B  (Input) Matrix. Not modified.
     * @param C  (Output) Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                            @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numRows, B.numCols);

        ImplSparseSparseMultWithSemiRing_DSCC.mult(A, B, C, semiRing, gw, gx);
    }

    public static void multTransA(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                                  @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numRows != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numCols, B.numCols);

        ImplSparseSparseMultWithSemiRing_DSCC.multTransA(A, B, C, semiRing, gw, gx);
    }

    /**
     * Performs matrix multiplication.  C = A*B<sup>T</sup>. B needs to be sorted and will be sorted if it
     * has not already been sorted.
     *
     * @param A  (Input) Matrix. Not modified.
     * @param B  (Input) Matrix. Value not modified but indicies will be sorted if not sorted already.
     * @param C  (Output) Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void multTransB(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                                  @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numRows, B.numRows);

        if (!B.isIndicesSorted())
            B.sortIndices(null);

        ImplSparseSparseMultWithSemiRing_DSCC.multTransB(A, B, C, semiRing, gw, gx);
    }


    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void mult(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numCols != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numRows, B.numCols);

        ImplSparseSparseMultWithSemiRing_DSCC.mult(A, B, C, semiRing);
    }

    /**
     * <p>C = C + A*B</p>
     */
    public static void multAdd(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numRows != C.numRows || B.numCols != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, C));

        ImplSparseSparseMultWithSemiRing_DSCC.multAdd(A, B, C, semiRing);
    }

    /**
     * Performs matrix multiplication.  C = A<sup>T</sup>*B
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void multTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numRows != B.numRows)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numCols, B.numCols);

        ImplSparseSparseMultWithSemiRing_DSCC.multTransA(A, B, C, semiRing);
    }

    /**
     * <p>C = C + A<sup>T</sup>*B</p>
     */
    public static void multAddTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numCols != C.numRows || B.numCols != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, C));

        ImplSparseSparseMultWithSemiRing_DSCC.multAddTransA(A, B, C, semiRing);
    }

    /**
     * Performs matrix multiplication.  C = A*B<sup>T</sup>
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void multTransB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        // todo: combine with multAdd as only difference is that C is filled with zero before
        if (A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numRows, B.numRows);

        ImplSparseSparseMultWithSemiRing_DSCC.multTransB(A, B, C, semiRing);
    }

    /**
     * <p>C = C + A*B<sup>T</sup></p>
     */
    public static void multAddTransB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numRows != C.numRows || B.numRows != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, C));

        // TODO: ? this is basically the equivalent of graphblas mult with specified accumulator op
        ImplSparseSparseMultWithSemiRing_DSCC.multAddTransB(A, B, C, semiRing);
    }

    /**
     * Performs matrix multiplication.  C = A<sup>T</sup>*B<sup>T</sup>
     *
     * @param A Matrix
     * @param B Dense Matrix
     * @param C Dense Matrix
     */
    public static void multTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        if (A.numRows != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numCols, B.numRows);

        ImplSparseSparseMultWithSemiRing_DSCC.multTransAB(A, B, C, semiRing);
    }


    /**
     * <p>C = C + A<sup>T</sup>*B<sup>T</sup></p>
     */
    public static void multAddTransAB(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj C, DoubleSemiRing semiRing) {
        // TODO: this is basically the equivalent of graphblas mult with specified accumulator op
        if (A.numCols != C.numRows || B.numRows != C.numCols)
            throw new IllegalArgumentException("Inconsistent matrix shapes. " + stringShapes(A, B, C));

        ImplSparseSparseMultWithSemiRing_DSCC.multAddTransAB(A, B, C, semiRing);
    }

    /**
     * Performs matrix addition:<br>
     * C = &alpha;A + &beta;B
     *
     * @param alpha scalar value multiplied against A
     * @param A     Matrix
     * @param beta  scalar value multiplied against B
     * @param B     Matrix
     * @param C     Output matrix.
     * @param gw    (Optional) Storage for internal workspace.  Can be null.
     * @param gx    (Optional) Storage for internal workspace.  Can be null.
     */
    public static void add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                           @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numRows != B.numRows || A.numCols != B.numCols)
            throw new MatrixDimensionException("Inconsistent matrix shapes. " + stringShapes(A, B));
        C.reshape(A.numRows, A.numCols);

        ImplCommonOpsWithSemiRing_DSCC.add(alpha, A, beta, B, C, semiRing, gw, gx);
    }

    /**
     * Performs an element-wise multiplication.<br>
     * C[i,j] = A[i,j]*B[i,j]<br>
     * All matrices must have the same shape.
     *
     * @param A  (Input) Matrix.
     * @param B  (Input) Matrix
     * @param C  (Output) Matrix. data array is grown to min(A.nz_length,B.nz_length), resulting a in a large speed boost.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void elementMult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C, DoubleSemiRing semiRing,
                                   @Nullable IGrowArray gw, @Nullable DGrowArray gx) {
        if (A.numCols != B.numCols || A.numRows != B.numRows)
            throw new MatrixDimensionException("All inputs must have the same number of rows and columns. " + stringShapes(A, B));
        C.reshape(A.numRows, A.numCols);

        ImplCommonOpsWithSemiRing_DSCC.elementMult(A, B, C, semiRing, gw, gx);
    }

    /**
     * Applies the row permutation specified by the vector to the input matrix and save the results
     * in the output matrix.  output[perm[j],:] = input[j,:]
     *
     * @param permInv (Input) Inverse permutation vector.  Specifies new order of the rows.
     * @param input   (Input) Matrix which is to be permuted
     * @param output  (Output) Matrix which has the permutation stored in it.  Is reshaped.
     */
    public static void permuteRowInv(int permInv[], DMatrixSparseCSC input, DMatrixSparseCSC output) {
        CommonOps_DSCC.permuteRowInv(permInv, input, output);
    }

    /**
     * Applies the forward column and inverse row permutation specified by the two vector to the input matrix
     * and save the results in the output matrix. output[permRow[j],permCol[i]] = input[j,i]
     *
     * @param permRowInv (Input) Inverse row permutation vector. Null is the same as passing in identity.
     * @param input      (Input) Matrix which is to be permuted
     * @param permCol    (Input) Column permutation vector. Null is the same as passing in identity.
     * @param output     (Output) Matrix which has the permutation stored in it.  Is reshaped.
     */
    public static void permute(@Nullable int permRowInv[], DMatrixSparseCSC input, @Nullable int permCol[], DMatrixSparseCSC output) {
        CommonOps_DSCC.permute(permRowInv, input, permCol, output);
    }

    /**
     * Extracts a column from A and stores it into out.
     *
     * @param A      (Input) Source matrix. not modified.
     * @param column The column in A
     * @param out    (Output, Optional) Storage for column vector
     * @return The column of A.
     */
    public static DMatrixSparseCSC extractColumn(DMatrixSparseCSC A, int column, @Nullable DMatrixSparseCSC out) {
        return extractColumn(A, column, out);
    }

    /**
     * Creates a submatrix by extracting the specified rows from A. rows = {row0 %le; i %le; row1}.
     *
     * @param A    (Input) matrix
     * @param row0 First row. Inclusive
     * @param row1 Last row+1.
     * @param out  (Output, Option) Storage for output matrix
     * @return The submatrix
     */
    public static DMatrixSparseCSC extractRows(DMatrixSparseCSC A, int row0, int row1, DMatrixSparseCSC out) {
        return CommonOps_DSCC.extractRows(A, row0, row1, out);
    }

    /**
     * <p>
     * Extracts a submatrix from 'src' and inserts it in a submatrix in 'dst'.
     * </p>
     * <p>
     * s<sub>i-y0 , j-x0</sub> = o<sub>ij</sub> for all y0 &le; i &lt; y1 and x0 &le; j &lt; x1 <br>
     * <br>
     * where 's<sub>ij</sub>' is an element in the submatrix and 'o<sub>ij</sub>' is an element in the
     * original matrix.
     * </p>
     *
     * <p>WARNING: This is a very slow operation for sparse matrices. The current implementation is simple but
     * involves excessive memory copying.</p>
     *
     * @param src   The original matrix which is to be copied.  Not modified.
     * @param srcX0 Start column.
     * @param srcX1 Stop column+1.
     * @param srcY0 Start row.
     * @param srcY1 Stop row+1.
     * @param dst   Where the submatrix are stored.  Modified.
     * @param dstY0 Start row in dst.
     * @param dstX0 start column in dst.
     */
    public static void extract(DMatrixSparseCSC src, int srcY0, int srcY1, int srcX0, int srcX1,
                               DMatrixSparseCSC dst, int dstY0, int dstX0) {
        CommonOps_DSCC.extract(src, srcY0, srcY1, srcX0, srcX1, dst, dstY0, dstX0);
    }

    /**
     * This applies a given unary function on every value stored in the matrix
     *
     * @param input  (Input) input matrix. Not modified
     * @param func   Unary function accepting a double
     * @param output (Input/Output) Matrix. Modified.
     */
    public static void apply(DMatrixSparseCSC input, DoubleUnaryOperator func, @Nullable DMatrixSparseCSC output) {
        if (output == null) {
            output = input.createLike();
        } else if (input != output) {
            output.copyStructure(input);
        }

        for (int i = 0; i < input.nz_values.length; i++) {
            output.nz_values[i] = func.apply(input.nz_values[i]);
        }
    }

    public static void apply(DMatrixSparseCSC input, DoubleUnaryOperator func) {
        apply(input, func, input);
    }

    /**
     * This accumulates the matrix values to a scalar value
     *
     * @param input     (Input) input matrix. Not modified
     * @param initValue initial value for accumulator
     * @param monoid    Monoid defining "+" for accumulator +=  cellValue
     * @return accumulated value
     */
    public static double reduceScalar(DMatrixSparseCSC input, double initValue, DoubleMonoid monoid) {
        double result = initValue;

        for (int i = 0; i < input.nz_values.length; i++) {
            result = monoid.func.apply(result, input.nz_values[i]);
        }

        return result;
    }

    public static double reduceScalar(DMatrixSparseCSC input, DoubleMonoid monoid) {
        return reduceScalar(input, 0, monoid);
    }

    /**
     * This accumulates the values per column to a scalar value
     *
     * @param input     (Input) input matrix. Not modified
     * @param initValue initial value for accumulator
     * @param monoid    Monoid defining "+" for accumulator +=  cellValue
     * @param output    output (Output) Vector, where result can be stored in
     * @return a column-vector, where v[i] == values of column i reduced to scalar based on `func`
     */
    public static DMatrixRMaj reduceColumnWise(DMatrixSparseCSC input, double initValue, DoubleMonoid monoid, @Nullable DMatrixRMaj output) {
        if (output == null) {
            output = new DMatrixRMaj(1, input.numCols);
        } else {
            output.reshape(1, input.numCols);
        }

        for (int col = 0; col < input.numCols; col++) {
            int start = input.col_idx[col];
            int end = input.col_idx[col + 1];

            double acc = initValue;
            for (int i = start; i < end; i++) {
                acc = monoid.func.apply(acc, input.nz_values[i]);
            }

            // TODO: allow optional resultAccumulator function (use tmp_result array to save reduce result and than combine arrays f.i. 2nd func)
            output.data[col] = acc;
        }

        return output;
    }

    /**
     * This accumulates the values per row to a scalar value
     *
     * @param input     (Input) input matrix. Not modified
     * @param initValue initial value for accumulator
     * @param monoid    Monoid defining "+" for accumulator +=  cellValue
     * @param output    output (Output) Vector, where result can be stored in
     * @return a row-vector, where v[i] == values of row i reduced to scalar based on `func`
     */
    public static DMatrixRMaj reduceRowWise(DMatrixSparseCSC input, double initValue, DoubleMonoid monoid, @Nullable DMatrixRMaj output) {
        if (output == null) {
            output = new DMatrixRMaj(1, input.numRows);
        } else {
            output.reshape(1, input.numCols);
        }
        // TODO: allow optional resultAccumulator function (use tmp_result array to save reduce result and than combine arrays f.i. 2nd func)
        Arrays.fill(output.data, initValue);

        for (int col = 0; col < input.numCols; col++) {
            int start = input.col_idx[col];
            int end = input.col_idx[col + 1];

            for (int i = start; i < end; i++) {
                output.data[input.nz_rows[i]] = monoid.func.apply(output.data[input.nz_rows[i]], input.nz_values[i]);
            }
        }

        return output;
    }
}

