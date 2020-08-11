/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

import org.ejml.MatrixDimensionException;


/**
 * A generic abstract class for matrices whose data is stored in a single 1D array of floats.  The
 * format of the elements in this array is not specified.  For example row major, column major,
 * and block row major are all common formats.
 *
 * @author Peter Abeles
 */
public abstract class CMatrixD1 implements CMatrix, ReshapeMatrix{
    /**
     * Where the raw data for the matrix is stored.  The format is type dependent.
     */
    public float[] data;

    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    /**
     * Used to get a reference to the internal data.
     *
     * @return Reference to the matrix's data.
     */
    public float[] getData() {
        return data;
    }

    /**
     * Changes the internal array reference.
     */
    public void setData( float[] data ) {
        this.data = data;
    }

    /**
     * Returns the internal array index for the specified row and column.
     *
     * @param row Row index.
     * @param col Column index.
     * @return Internal array index.
     */
    public abstract int getIndex( int row, int col );

    /**
     * Sets the value of this matrix to be the same as the value of the provided matrix.  Both
     * matrices must have the same shape:<br>
     * <br>
     * a<sub>ij</sub> = b<sub>ij</sub><br>
     * <br>
     *
     * @param b The matrix that this matrix is to be set equal to.
     */
    public void set( CMatrixD1 b )
    {
        if( numRows != b.numRows || numCols != b.numCols ) {
            throw new MatrixDimensionException("The two matrices do not have compatible shapes.");
        }

        int dataLength = b.getDataLength();

        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumRows() {
        return numRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumCols() {
        return numCols;
    }

    /**
     * Sets the number of rows.
     *
     * @param numRows Number of rows
     */
    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    /**
     * Sets the number of columns.
     *
     * @param numCols Number of columns
     */
    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    public int getNumElements() {
        return numRows*numCols;
    }
}