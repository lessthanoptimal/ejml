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
package org.ejml.masks;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.jetbrains.annotations.Nullable;

import static org.ejml.UtilEjml.adjust;

/**
 * Mask implementation which checks if the entry is assigned in the sparse matrix.
 * The actual stored values are disregarded.
 */
public class DMaskSparseStructural extends Mask {
    // TODO use a data-type-independent MatrixSparseCSC class
    private final DMatrixSparseCSC matrix;
    // Corresponding column to rowIndicesInIndexedColumn
    private int indexedColumn = -1;
    // int[] instead of boolean[] to avoid clearing on multiple setActiveColumns()
    // If the row entry is non-zero in indexed column -> rowIndicesInIndexedColumn[row] == col
    private int[] rowIndicesInIndexedColumn;

    public DMaskSparseStructural( DMatrixSparseCSC matrix, boolean negated, @Nullable IGrowArray gw, boolean indexFirstColumn ) {
        super(negated);
        this.matrix = matrix;
        this.rowIndicesInIndexedColumn = adjust(gw, matrix.numRows);
        if (indexFirstColumn) {
            setIndexColumn(0);
        }
    }

    @Override
    public boolean isSet( int row, int col ) {
        if (col != indexedColumn) {
            return negated ^ matrix.isAssigned(row, col);
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
        return matrix.getNumCols();
    }

    @Override
    public int getNumRows() {
        return matrix.getNumRows();
    }

    @Override
    public void setIndexColumn( int col ) {
        if (indexedColumn != col) {
            this.indexedColumn = col;
            for (int i = matrix.col_idx[col]; i < matrix.col_idx[col + 1]; i++) {
                rowIndicesInIndexedColumn[matrix.nz_rows[i]] = col + 1;
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
     * Utility class to build {@link DMaskSparseStructural}
     */
    public static class Builder extends MaskBuilder<DMaskSparseStructural> {
        private DMatrixSparseCSC matrix;
        private boolean indexFirstColumn;
        private @Nullable IGrowArray gw;

        public Builder( DMatrixSparseCSC matrix ) {
            this.matrix = matrix;
        }

        /**
         * @param indexFirstColumn Whether the first column should be indexed on mask construction
         */
        public Builder withIndexFirstColumn( boolean indexFirstColumn ) {
            this.indexFirstColumn = indexFirstColumn;
            return this;
        }

        /**
         * @param gw (Optional) Storage for internal workspace.  Can be null.
         */
        public Builder withWorkArray( IGrowArray gw ) {
            this.gw = gw;
            return this;
        }

        @Override
        public DMaskSparseStructural build() {
            return new DMaskSparseStructural(matrix, negated, gw, indexFirstColumn);
        }
    }
}
