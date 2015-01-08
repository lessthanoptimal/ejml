/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import java.util.Iterator;


/**
 * This is a matrix iterator for traversing through a submatrix.  For speed it is recommended
 * that you directly access the elements in the matrix, but there are some situations where this
 * can be a better design.
 *
 * @author Peter Abeles
 */
public class MatrixIterator32F implements Iterator<Float> {
    // the matrix which is being iterated through
    private D1Matrix32F a;

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
    public MatrixIterator32F(D1Matrix32F a, boolean rowMajor,
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
    public Float next() {
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
    public void set( float value ) {
        a.set(subRow+minRow,subCol+minCol,value);
    }
}
