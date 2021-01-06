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

import lombok.Getter;
import org.ejml.MatrixDimensionException;
import org.ejml.data.Matrix;

/**
 * Mask used for specifying which matrix entries should be computed
 */
public abstract class Mask {
    /**
     * Whether the mask entries should be negated.
     * This avoids materializing the actual negated matrix.
     */
    @Getter
    public final boolean negated;

    protected Mask( boolean negated ) {
        this.negated = negated;
    }

    /**
     * @return Whether the matrix entry is set in the mask
     */
    public abstract boolean isSet( int row, int col );

    /**
     * @return Whether the vector entry is set in the mask
     */
    public abstract boolean isSet( int idx );

    /**
     * @return The number of columns of the wrapped matrix
     */
    protected abstract int getNumCols();

    /**
     * @return The number of rows of the wrapped matrix
     */
    protected abstract int getNumRows();

    /**
     * Prints the mask to standard out.
     **/
    public void print() {
        var result = new StringBuilder();
        for (int row = 0; row < getNumRows(); row++) {
            for (int col = 0; col < getNumCols(); col++) {
                result.append(isSet(row, col) ? "+ " : "- ");
            }
            result.append(System.lineSeparator());
        }

        System.out.println(result);
    }

    /**
     * For faster access on a specific column (on at a time)
     * ! Only useful for sparse masks
     *
     * @param column column to index
     */
    public abstract void setIndexColumn( int column );

    /**
     * Checks whether the dimensions of the mask and matrix match
     *
     * @param matrix the mask is applied to
     */
    public void compatible( Matrix matrix ) {
        if (matrix.getNumCols() != getNumCols() || matrix.getNumRows() != getNumRows()) {
            throw new MatrixDimensionException(String.format(
                    "Mask of (%d, %d) cannot be applied for matrix (%d, %d)",
                    getNumRows(), getNumCols(), matrix.getNumCols(), matrix.getNumCols()
            ));
        }
    }
}
