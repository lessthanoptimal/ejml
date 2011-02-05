/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.data;

import java.io.Serializable;

/**
 * An abstract class for all 64 bit floating point rectangular matrices.
 *
 * @author Peter Abeles
 */
public abstract class Matrix64F implements Serializable {

    private static final long serialVersionUID = 423423451942L;
    
    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    /**
     * Changes the number of rows and columns in a matrix and if possible does not declare new memory.  If saveValues is
     * true and new memory needs to be declared, the value of the old internal data will be copied over into the new internal data.
     *
     * @param numRows The new number of rows the matrix will have.
     * @param numCols The new number of columns the matrix will have.
     * @param saveValues If new memory is declared should it copy the old values over?
     */
    public abstract void reshape(int numRows, int numCols, boolean saveValues);

    /**
     * Returns the value of value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public abstract double get( int row , int col );

    /**
     * Same as {@link #get} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public abstract double unsafe_get( int row , int col );

    /**
     * Sets the value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public abstract void set( int row , int col , double val );


    /**
     * Same as {@link #set} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public abstract void unsafe_set( int row , int col , double val );

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix.  It can be traversed
     * by row or by column.  Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1.  The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
    {
        return new MatrixIterator(this,rowMajor, minRow, minCol, maxRow, maxCol);
    }

    /**
     * Returns the number of rows in this matrix.
     *
     * @return Number of rows.
     */
    public int getNumRows() {
        return numRows;    
    }

    /**
     * Returns the number of columns in this matrix.
     *
     * @return Number of columns.
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * Returns the number of elements in this matrix, which is the number of rows
     * times the number of columns.
     *
     * @return Number of elements in this matrix.
     */
    public abstract int getNumElements();

    /**
     * Assigns the value of 'this' matrix to be the same as 'A'.  The shape of
     * both matrices must be the same.
     *
     * @param A The matrix whose value is to be copied into 'this'.
     */
    public void set( Matrix64F A ) {
        if( A.numRows != numRows )
            throw new IllegalArgumentException("Unexpected number of rows.");

        if( A.numCols != numCols )
            throw new IllegalArgumentException("Unexpected number of columns.");


        for( int i = 0; i < A.numRows; i++ ) {
            for( int j = 0; j < A.numCols; j++ ) {
                set(i,j,A.get(i,j));
            }
        }
    }

    public abstract <T extends Matrix64F> T copy();

    public abstract void print();
}
