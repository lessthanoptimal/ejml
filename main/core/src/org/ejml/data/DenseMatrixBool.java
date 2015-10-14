/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

/**
 * Dense matrix composed of boolean values
 *
 * @author Peter Abeles
 */
public class DenseMatrixBool implements ReshapeMatrix {
    /**
     * 1D row-major array for storing theboolean matrix
     */
    public boolean data[];
    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    public DenseMatrixBool( int numRows , int numCols ) {
        data = new boolean[ numRows*numCols ];
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public int getNumElements() {
        return numRows*numCols;
    }

    public int getIndex( int row , int col ) {
        return row * numCols + col;
    }

    public boolean get( int index ) {
        return data[index];
    }

    public boolean get( int row , int col ) {
        if( !isInBounds(row,col))
            throw new IllegalArgumentException("Out of matrix bounds. "+row+" "+col);
        return data[row * numCols + col];
    }

    public void set( int row , int col , boolean value) {
        if( !isInBounds(row,col))
            throw new IllegalArgumentException("Out of matrix bounds. "+row+" "+col);
        data[row * numCols + col] = value;
    }

    public boolean unsafe_get( int row , int col ) {
        return data[row * numCols + col];
    }

    public void unsafe_set( int row , int col , boolean value) {
        data[row * numCols + col] = value;
    }

    /**
     * Determines if the specified element is inside the bounds of the Matrix.
     *
     * @param row The element's row.
     * @param col The element's column.
     * @return True if it is inside the matrices bound, false otherwise.
     */
    public boolean isInBounds( int row  , int col ) {
        return( col >= 0 && col < numCols && row >= 0 && row < numRows );
    }

    @Override
    public void reshape(int numRows, int numCols) {
        int N = numRows*numCols;
        if( data.length < N ) {
           data = new boolean[N];
        }
        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Override
    public int getNumRows() {
        return numRows;
    }

    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public <T extends Matrix> T copy() {
        DenseMatrixBool ret = new DenseMatrixBool(numRows,numCols);
        ret.set(this);
        return (T)ret;
    }

    @Override
    public void set(Matrix original) {
        DenseMatrixBool orig = (DenseMatrixBool)original;

        reshape(original.getNumRows(),original.getNumCols());
        System.arraycopy(orig.data,0,data,0,orig.getNumElements());
    }

    @Override
    public void print() {
        System.out.println("Implement this");
    }
}
