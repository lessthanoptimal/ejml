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

/**
 * <p>Compressed Column (CC) sparse matrix format.   Only non-zero elements are stored.</p>
 * <p>
 * Format:<br>
 * Row indexes for column j are stored in rol_idx[col_idx[j]] to rol_idx[col_idx[j+1]-1].  The values
 * for the corresponding elements are stored at data[col_idx[j]] to data[col_idx[j+1]-1].</p>
 *
 *
 * TODO fully describe
 *
 * @author Peter Abeles
 */
public class SMatrixCC_64 implements RealMatrix_F64 {
    /**
     * Storage for non-zero values.  Only valid up to numElements-1.
     */
    public double data[];
    /**
     * Length of data. Number of non-zero elements in the matrix
     */
    public int numElements;
    public int row_idx[];
    public int col_idx[];

    public int numRows;
    public int numCols;

    public SMatrixCC_64(int numRows , int numCols , int numElements ) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.numElements = numElements;

        data = new double[ numElements ];
        col_idx = new int[ numCols+1 ];
        row_idx = new int[ numElements ];
    }

    public SMatrixCC_64(SMatrixCC_64 original ) {
        this(original.numRows, original.numCols, original.numElements);

        set(original);
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
        return (T)new SMatrixCC_64(this);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new SMatrixCC_64(numRows,numCols, numElements);
    }

    @Override
    public void set(Matrix original) {
        SMatrixCC_64 o = (SMatrixCC_64)original;
        reshape(o.numRows, o.numCols, o.numElements);

        System.arraycopy(o.data, 0, data, 0, numElements);
        System.arraycopy(o.row_idx, 0, row_idx, 0, numElements);
        System.arraycopy(o.col_idx, 0, col_idx, 0, numCols);
    }

    @Override
    public void print() {

    }

    @Override
    public double get(int row, int col) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        return unsafe_get(row,col);
    }

    @Override
    public double unsafe_get(int row, int col) {
        int col0 = col_idx[col];
        int col1 = col_idx[col+1];

        for (int i = col0; i < col1; i++) {
            if( row_idx[i] == row ) {
                return data[i];
            }
        }

        return 0;
    }

    @Override
    public void set(int row, int col, double val) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        unsafe_set(row,col,val);
    }

    @Override
    public void unsafe_set(int row, int col, double val) {
        int col0 = col_idx[col];
        int col1 = col_idx[col+1];

        for (int i = col0; i < col1; i++) {
            if( row_idx[i] == row ) {
                data[i] = val;
                return;
            }
        }

        throw new IllegalArgumentException("Setting of zero elements is not currently supported");
    }

    @Override
    public int getNumElements() {
        return numElements;// TODO look at how this is used and decide if it can be re-defined
    }

    public void reshape( int numRows , int numCols , int numElements ) {
        if( numElements > data.length ) {
            data = new double[ numElements ];
            row_idx = new int[ numElements ];
        }
        if( numCols+1 > data.length ) {
            col_idx = new int[ numCols+1 ];
        }
        this.numRows = numRows;
        this.numCols = numCols;
        this.numElements = numElements;
    }
}
