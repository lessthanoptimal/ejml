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

import org.ejml.ops.MatrixIO;

/**
 * Dense matrix for complex numbers.  Internally it stores its data in a single row-major array with the real
 * and imaginary components interlaces.  The total number of elements in the array will be numRows*numColumns*2.
 *
 * @author Peter Abeles
 */
public class CDenseMatrix64F extends CD1Matrix64F {

    /**
     * Creates a new {@link org.ejml.data.CDenseMatrix64F} which is a copy of the passed in matrix.
     * @param original Matrix which is to be copied
     */
    public CDenseMatrix64F(CDenseMatrix64F original) {
        this(original.numRows, original.numCols);
        set(original);
    }

    /**
     * Creates a new matrix with the specified number of rows and columns
     *
     * @param numRows number of rows
     * @param numCols number of columns
     */
    public CDenseMatrix64F(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.data = new double[numRows*numCols*2];
    }

    @Override
    public int getIndex(int row, int col) {
        return row*numCols*2 + col*2;
    }

    @Override
    public void reshape( int numRows , int numCols ) {
        int newLength = numRows*numCols*2;

        if( newLength > data.length ) {
            data = new double[newLength];
        }

        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Override
    public void get(int row, int col, Complex64F output) {
        int index = row*col*2 + col*2;
        output.real = data[index];
        output.imaginary = data[index+1];
    }

    @Override
    public void set(int row, int col, double real, double imaginary) {
        int index = row*col*2 + col*2;
        data[index] = real;
        data[index+1] = imaginary;
    }

    @Override
    public double getReal(int row, int col) {
        return data[row*col*2 + col*2];
    }

    @Override
    public void setReal(int row, int col, double val) {
        data[row*col*2 + col*2] = val;
    }

    @Override
    public double getImaginary(int row, int col) {
        return data[row*col*2 + col*2 + 1];
    }

    @Override
    public void setImaginary(int row, int col, double val) {
        data[row*col*2 + col*2 + 1] = val;
    }

    @Override
    public int getDataLength() {
        return numRows*numCols*2;
    }

    public void set( CDenseMatrix64F original ) {
        reshape(original.numRows,original.numCols);
        int columnSize = numCols*2;
        for (int y = 0; y < numRows; y++) {
            int index = y*numCols*2;
            System.arraycopy(original.data,index,data,index,columnSize);
        }
    }

    @Override
    public CDenseMatrix64F copy() {
        return new CDenseMatrix64F(this);
    }

    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }

    /**
     * Number of array elements in the matrix's row.
     */
    public int getRowStride() {
        return numCols*2;
    }
}
