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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.ComputePermutation;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.jetbrains.annotations.Nullable;

/**
 * Applies the fill reduction permutation to the input matrix to reduce fill in during decomposition/solve.
 *
 * The permuted matrix is P*A*Q, where P is the row permutation and Q is the column permutation provided
 * by the {@link ComputePermutation ordering}. If the ordering does not provide one of the two permutations
 * it is treated as identity. In symmetric mode the row permutation is applied to both sides, P*A*P<sup>T</sup>,
 * and the column permutation is ignored.
 *
 * @author Peter Abeles
 */
public class ApplyFillReductionPermutation_DSCC {
    // fill reduction permutation
    private @Nullable ComputePermutation<DMatrixSparseCSC> fillReduce;

    // storage for permuted A matrix
    DMatrixSparseCSC Aperm = new DMatrixSparseCSC(1, 1, 0);
    int[] p = new int[0]; // row pivots
    int[] pinv = new int[1]; // inverse row pivots
    int[] q = new int[0]; // column pivots

    // storage for identity permutations when the ordering doesn't provide one
    private int[] identRow = new int[0];
    private int[] identCol = new int[0];

    IGrowArray gw = new IGrowArray();

    boolean symmetric;

    public ApplyFillReductionPermutation_DSCC( @Nullable ComputePermutation<DMatrixSparseCSC> fillReduce,
                                               boolean symmetric ) {
        this.fillReduce = fillReduce;
        this.symmetric = symmetric;
    }

    /**
     * Computes and applies the fill reduction permutation. Either A is returned (unmodified) or the permutated
     * version of A.
     *
     * @param A Input matrix. unmodified.
     * @return A permuted matrix. Might be A or a different matrix.
     */
    public DMatrixSparseCSC apply( DMatrixSparseCSC A ) {
        if (fillReduce == null)
            return A;
        fillReduce.process(A);

        IGrowArray gp = fillReduce.getRow();
        IGrowArray gq = fillReduce.getColumn();

        p = gp != null ? gp.data : identity(true, A.numRows);
        if (pinv.length < A.numRows)
            pinv = new int[A.numRows];
        CommonOps_DSCC.permutationInverse(p, pinv, A.numRows);

        if (symmetric) {
            // rows and columns are permuted by the same permutation
            q = p;
            CommonOps_DSCC.permuteSymmetric(A, pinv, Aperm, gw);
        } else {
            q = gq != null ? gq.data : identity(false, A.numCols);
            CommonOps_DSCC.permute(pinv, A, q, Aperm);
        }
        return Aperm;
    }

    private int[] identity( boolean row, int length ) {
        int[] storage = row ? identRow : identCol;
        if (storage.length < length) {
            storage = new int[length];
            for (int i = 0; i < length; i++) {
                storage[i] = i;
            }
            if (row)
                identRow = storage;
            else
                identCol = storage;
        }
        return storage;
    }

    /**
     * Inverse of the row permutation. Only valid after {@link #apply} has been called. null if no fill reduction.
     */
    public @Nullable int[] getArrayPinv() {
        return fillReduce == null ? null : pinv;
    }

    /**
     * Row permutation. Only valid after {@link #apply} has been called. null if no fill reduction.
     */
    public @Nullable int[] getArrayP() {
        return fillReduce == null ? null : p;
    }

    /**
     * Column permutation. Only valid after {@link #apply} has been called. null if no fill reduction.
     */
    public @Nullable int[] getArrayQ() {
        return fillReduce == null ? null : q;
    }

    /**
     * <p>Returns the inverse row permutation to apply to a right hand side before the triangular solves, formed by
     * composing this fill reduction's row permutation with the decomposition's own (numeric or structural) row
     * pivots. The result is used the same way in every solver:</p>
     *
     * <ul>
     *     <li>dense: {@code CommonOps_DSCC.permuteInv(rowPinv, b, x, n)}</li>
     *     <li>sparse: {@code CommonOps_DSCC.permute(rowPinv, B, null, Bp)}</li>
     * </ul>
     *
     * <p>This is the single location where the direction and composition of the row permutation is decided, so no
     * solver has to reason about it. For decompositions with no numeric row pivots (e.g. Cholesky) use
     * {@link #fillRowPermInv()} instead.</p>
     *
     * @param numericRowPinv (Input) The decomposition's inverse row pivots.
     * @param work (Optional) Storage for the composed permutation. Only used when a composition is required.
     * @return The inverse row permutation, or {@code numericRowPinv} unchanged if there is no fill reduction.
     */
    public int[] rowPermInv( int[] numericRowPinv, @Nullable IGrowArray work ) {
        if (fillReduce == null)
            return numericRowPinv;
        // pinv is grow-only, so its length is only an upper bound; the current matrix's row count is Aperm.numRows
        int n = Aperm.numRows;
        int[] combined = UtilEjml.adjust(work, n);
        for (int i = 0; i < n; i++) {
            combined[i] = numericRowPinv[pinv[i]];
        }
        return combined;
    }

    /**
     * Variant of {@link #rowPermInv(int[], IGrowArray)} for decompositions with no numeric row pivots to compose
     * with, e.g. Cholesky. Returns just the fill reduction's inverse row permutation, or null if there is none.
     */
    public @Nullable int[] fillRowPermInv() {
        return fillReduce == null ? null : pinv;
    }

    /**
     * Undoes the fill reduction column permutation on a dense solution. {@code out[q[i]] = t[i]}. If there is no
     * fill reduction 't' is copied into 'out' unchanged. Callers that manage their own buffers should guard this
     * with {@link #isApplied()} to avoid the copy.
     */
    public void undoColumnPermutation( double[] t, double[] out, int n ) {
        if (fillReduce == null)
            System.arraycopy(t, 0, out, 0, n);
        else
            CommonOps_DSCC.permuteInv(q, t, out, n);
    }

    /**
     * Undoes the fill reduction column permutation on a sparse solution. Row {@code i} of 't' becomes row
     * {@code q[i]} of 'out'. If there is no fill reduction 't' is copied into 'out' unchanged.
     */
    public void undoColumnPermutation( DMatrixSparseCSC t, DMatrixSparseCSC out ) {
        if (fillReduce == null)
            out.setTo(t);
        else
            CommonOps_DSCC.permuteRowInv(q, t, out);
    }

    public IGrowArray getGw() {
        return gw;
    }

    public void setGw( IGrowArray gw ) {
        this.gw = gw;
    }

    public @Nullable ComputePermutation<DMatrixSparseCSC> getFillReduce() {
        return fillReduce;
    }

    public boolean isApplied() {
        return fillReduce != null;
    }
}
