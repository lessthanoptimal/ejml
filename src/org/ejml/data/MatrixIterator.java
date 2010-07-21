/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

import java.util.Iterator;


/**
 * This is a matrix iterator for traversing through a submatrix.  For speed it is recommended
 * that you directly access the elements in the matrix, but there are some situations where this
 * can be a better design.
 *
 * @author Peter Abeles
 */
public class MatrixIterator implements Iterator<Double> {
    // the matrix which is being iterated through
    private Matrix64F a;

    // should it iterate through by row or by column
    private boolean rowMajor;

    // the first row and column it returns
    private int minCol;
    private int minRow;

    // where in the iteration it is
    private int index = 0;
    // how many elements inside will it return
    private int size;

    // how wide the submatrix is
    private int submatrixStride;

    // the current element
    int subRow,subCol;

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix.  It can be traversed
     * by row or by column.  Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1.  The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param a the matrix it is iterating through
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    public MatrixIterator(Matrix64F a, boolean rowMajor,
                          int minRow, int minCol, int maxRow, int maxCol
    ) {
        if( maxCol < minCol )
            throw new IllegalArgumentException("maxCol has to be more than or equal to minCol");
        if( maxRow < minRow )
            throw new IllegalArgumentException("maxRow has to be more than or equal to minCol");
        if( maxCol >= a.numCols)
            throw new IllegalArgumentException("maxCol must be < numCols");
        if( maxRow >= a.numRows)
            throw new IllegalArgumentException("maxRow must be < numCRows");



        this.a = a;
        this.rowMajor = rowMajor;
        this.minCol = minCol;
        this.minRow = minRow;

        size = (maxCol-minCol+1)*(maxRow-minRow+1);

        if( rowMajor )
            submatrixStride = maxCol-minCol+1;
        else
            submatrixStride = maxRow-minRow+1;
    }

    @Override
    public boolean hasNext() {
        return index < size;
    }

    @Override
    public Double next() {
        if( rowMajor ) {
            subRow = index / submatrixStride;
            subCol = index % submatrixStride;
        } else {
            subRow = index % submatrixStride;
            subCol = index / submatrixStride;
        }
        index++;
        return a.get(subRow+minRow,subCol+minCol);
    }

    @Override
    public void remove() {
        throw new RuntimeException("Operation not supported");
    }

    /**
     * Which element in the submatrix was returned by next()
     *
     * @return Submatrix element's index.
     */
    public int getIndex() {
        return index-1;
    }

    /**
     * True if it is iterating through the matrix by rows and false if by columns.
     * @return row major or column major
     */
    public boolean isRowMajor() {
        return rowMajor;
    }

    /**
     * Sets the value of the current element.
     *
     * @param value The element's new value.
     */
    public void set( double value ) {
        a.set(subRow+minRow,subCol+minCol,value);
    }
}
