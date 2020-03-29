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

package org.ejml.data;

import java.util.Arrays;

/**
 * Dense matrix composed of boolean values
 *
 * @author Peter Abeles
 */
public class BMatrixRMaj implements ReshapeMatrix {
    /**
     * 1D row-major array for storing theboolean matrix
     */
    public boolean[] data;
    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    public BMatrixRMaj(int numRows , int numCols ) {
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

    /**
     * Sets every element in the matrix to the specified value
     * @param value new value of every element
     */
    public void fill( boolean value ) {
        Arrays.fill(data,0,getNumElements(),value);
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

    /**
     * Returns the total number of elements which are true.
     * @return number of elements which are set to true
     */
    public int sum() {
        int total = 0;
        int N = getNumElements();
        for (int i = 0; i < N; i++) {
            if( data[i] )
                total += 1;
        }
        return total;
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
    public void zero() {
        Arrays.fill(data, 0, getNumElements(), false);
    }

    @Override
    public <T extends Matrix> T copy() {
        BMatrixRMaj ret = new BMatrixRMaj(numRows,numCols);
        ret.set(this);
        return (T)ret;
    }

    @Override
    public void set(Matrix original) {
        BMatrixRMaj orig = (BMatrixRMaj)original;

        reshape(original.getNumRows(),original.getNumCols());
        System.arraycopy(orig.data,0,data,0,orig.getNumElements());
    }

    @Override
    public void print() {
       System.out.println("Type = binary , numRows = "+numRows+" , numCols = "+numCols);
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if ( get(row, col)) {
                    System.out.print("+");
                } else {
                    System.out.print("-");
                }
            }
            System.out.println();
        }
    }

    @Override
    public void print( String format ) {
        print();
    }

    @Override
    public BMatrixRMaj createLike() {
        return new BMatrixRMaj(numRows,numCols);
    }

    @Override
    public BMatrixRMaj create(int numRows, int numCols) {
        return new BMatrixRMaj(numRows,numCols);
    }

    @Override
    public MatrixType getType() {
        return MatrixType.UNSPECIFIED;
    }
}
