/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
package org.ejml.masks;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;

/**
 * Mask implementation backed by a matrix in CSC format
 */
public class DMaskSparse extends Mask {
    // Matrix to check for Mask.isSet(row, col)
    protected final DMatrixSparseCSC matrix;
    /**
     * Value representing that the entry is not set in the mask
     */
    public final double zeroElement;
    // Corresponding column to rowIndicesInIndexedColumn
    private int indexedColumn = -1;
    // Indexed row indices for the column specified in indexedColumn
    // int[] instead of boolean[] to avoid clearing on multiple setActiveColumns()
    // If the row is non-zero in the indexed column -> rowIndicesInIndexedColumn[row] == nz_index + 1
    private int[] rowIndicesInIndexedColumn;

    public DMaskSparse( DMatrixSparseCSC matrix, boolean negated, double zeroElement, @Nullable IGrowArray gw, boolean indexFirstColumn ) {
        super(negated);
        this.matrix = matrix;
        this.zeroElement = zeroElement;
        this.rowIndicesInIndexedColumn = adjust(gw, matrix.numRows);

        if (indexFirstColumn) {
            setIndexColumn(0);
        }
    }

    @Override
    public boolean isSet( int row, int col ) {
        if (col != indexedColumn) {
            return negated ^ (matrix.unsafe_get(row, col) != zeroElement);
        } else {
            return negated ^ (rowIndicesInIndexedColumn[row] - 1 == col);
        }
    }

    @Override
    public boolean isSet( int idx ) {
        // assuming a column vector
        return isSet(idx, 0);
    }

    @Override
    public int getNumCols() {
        return matrix.numCols;
    }

    @Override
    public int getNumRows() {
        return matrix.numRows;
    }

    @Override
    public void setIndexColumn( int col ) {
        if (indexedColumn != col) {
            this.indexedColumn = col;
            for (int i = matrix.col_idx[col]; i < matrix.col_idx[col + 1]; i++) {
                if (matrix.nz_values[i] != zeroElement) {
                    rowIndicesInIndexedColumn[matrix.nz_rows[i]] = col + 1;
                }
            }
        }
    }

    @Override
    public int maxMaskedEntries() {
        if (negated) {
            return matrix.getNumCols()*matrix.getNumRows() - matrix.nz_length;
        } else {
            return matrix.nz_length;
        }
    }

    /**
     * Utility class to build {@link DMaskSparse}
     */
    public static class Builder extends MaskBuilder<DMaskSparse> {
        private DMatrixSparseCSC matrix;
        private double zeroElement = 0;
        private boolean indexFirstColumn = false;
        private @Nullable IGrowArray gw;

        public Builder( DMatrixSparseCSC matrix ) {
            this.matrix = matrix;
        }

        /**
         * @param zeroElement Value to represent the zero-element in the mask
         */
        public Builder withZeroElement( double zeroElement ) {
            this.zeroElement = zeroElement;
            return this;
        }

        /**
         * @param indexFirstColumn Whether the first column should be indexed on mask construction
         */
        public Builder withIndexFirstColumn( boolean indexFirstColumn ) {
            this.indexFirstColumn = indexFirstColumn;
            return this;
        }

        /**
         * @param gw (Optional) Storage for internal workspace. Can be null.
         */
        public Builder withWorkArray( IGrowArray gw ) {
            this.gw = gw;
            return this;
        }

        @Override
        public DMaskSparse build() {
            return new DMaskSparse(matrix, negated, zeroElement, gw, indexFirstColumn);
        }
    }
}
