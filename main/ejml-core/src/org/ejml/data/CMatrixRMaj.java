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

import org.ejml.UtilEjml;
import org.ejml.ops.MatrixIO;

import java.util.Arrays;

/**
 * Dense matrix for complex numbers.  Internally it stores its data in a single row-major array with the real
 * and imaginary components interlaces, in that order.  The total number of elements in the array will be
 * numRows*numColumns*2.
 *
 * @author Peter Abeles
 */
public class CMatrixRMaj extends CMatrixD1 {

    /**
     * <p>
     * Creates a matrix with the values and shape defined by the 2D array 'data'.
     * It is assumed that 'data' has a row-major formatting:<br>
     *  <br>
     * data[ row ][ column ]
     * </p>
     * @param data 2D array representation of the matrix. Not modified.
     */
    public CMatrixRMaj(float[][] data ) {
        this.numRows = data.length;
        this.numCols = data[0].length/2;

        UtilEjml.checkTooLargeComplex(numRows,numCols);

        this.data = new float[ numRows * numCols * 2 ];

        for (int i = 0; i < numRows; i++) {
            float[] row = data[i];
            if( row.length != numCols*2 )
                throw new IllegalArgumentException("Unexpected row size in input data at row "+i);

            System.arraycopy(row,0,this.data,i*numCols*2,row.length);
        }
    }

    public CMatrixRMaj(int numRows, int numCols, boolean rowMajor, float... data) {
        if( data.length != numRows*numCols*2 )
            throw new RuntimeException("Unexpected length for data");

        this.data = new float[ numRows * numCols * 2 ];

        this.numRows = numRows;
        this.numCols = numCols;

        set(numRows,numCols, rowMajor, data);
    }

    /**
     * Creates a new {@link CMatrixRMaj} which is a copy of the passed in matrix.
     * @param original Matrix which is to be copied
     */
    public CMatrixRMaj(CMatrixRMaj original) {
        this(original.numRows, original.numCols);
        set(original);
    }

    /**
     * Creates a new matrix with the specified number of rows and columns
     *
     * @param numRows number of rows
     * @param numCols number of columns
     */
    public CMatrixRMaj(int numRows, int numCols) {
        UtilEjml.checkTooLargeComplex(numRows,numCols);
        this.numRows = numRows;
        this.numCols = numCols;
        this.data = new float[numRows*numCols*2];
    }

    @Override
    public int getIndex(int row, int col) {
        return row*numCols*2 + col*2;
    }

    @Override
    public void reshape( int numRows , int numCols ) {
        UtilEjml.checkTooLargeComplex(numRows,numCols);
        int newLength = numRows*numCols*2;

        if( newLength > data.length ) {
            data = new float[newLength];
        }

        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Override
    public void get(int row, int col, Complex_F32 output) {
        int index = row*numCols*2 + col*2;
        output.real = data[index];
        output.imaginary = data[index+1];
    }

    @Override
    public void set(int row, int col, float real, float imaginary) {
        int index = row*numCols*2 + col*2;
        data[index] = real;
        data[index+1] = imaginary;
    }

    public float getReal( int element ) {
        return data[element*2];
    }

    public float getImag( int element ) {
        return data[element*2+1];
    }

    @Override
    public float getReal(int row, int col) {
        return data[(row*numCols + col)*2];
    }

    @Override
    public void setReal(int row, int col, float val) {
        data[(row*numCols + col)*2] = val;
    }

    @Override
    public float getImag(int row, int col) {
        return data[(row*numCols + col)*2 + 1];
    }

    @Override
    public void setImag(int row, int col, float val) {
        data[(row*numCols + col)*2 + 1] = val;
    }

    @Override
    public int getDataLength() {
        return numRows*numCols*2;
    }

    public void set( CMatrixRMaj original ) {
        reshape(original.numRows,original.numCols);
        int columnSize = numCols*2;
        for (int y = 0; y < numRows; y++) {
            int index = y*numCols*2;
            System.arraycopy(original.data,index,data,index,columnSize);
        }
    }

    @Override
    public CMatrixRMaj copy() {
        return new CMatrixRMaj(this);
    }

    @Override
    public void set(Matrix original) {
        reshape(original.getNumRows(),original.getNumCols());

        CMatrix n = (CMatrix)original;

        Complex_F32 c = new Complex_F32();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                n.get(i,j,c);
                set(i,j,c.real,c.imaginary);
            }
        }
    }

    @Override
    public void print() {
        MatrixIO.printFancy(System.out,this,MatrixIO.DEFAULT_LENGTH);
    }

    @Override
    public void print( String format ) {
        MatrixIO.print(System.out, this, format);
    }

    /**
     * Number of array elements in the matrix's row.
     */
    public int getRowStride() {
        return numCols*2;
    }

    /**
     * Sets this matrix equal to the matrix encoded in the array.
     *
     * @param numRows The number of rows.
     * @param numCols The number of columns.
     * @param rowMajor If the array is encoded in a row-major or a column-major format.
     * @param data The formatted 1D array. Not modified.
     */
    public void set(int numRows, int numCols, boolean rowMajor, float ...data)
    {
        reshape(numRows,numCols);
        int length = numRows*numCols*2;

        if( length > data.length )
            throw new RuntimeException("Passed in array not long enough");

        if( rowMajor ) {
            System.arraycopy(data,0,this.data,0,length);
        } else {
            int index = 0;
            int stride = numRows*2;
            for( int i = 0; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    this.data[index++] = data[j*stride+i*2];
                    this.data[index++] = data[j*stride+i*2+1];
                }
            }
        }
    }

    /**
     * Sets all the elements in the matrix to zero
     */
    public void zero() {
        Arrays.fill(data, 0, numCols*numRows*2, 0.0f);
    }

    @Override
    public CMatrixRMaj createLike() {
        return new CMatrixRMaj(numRows,numCols);
    }

    @Override
    public CMatrixRMaj create(int numRows, int numCols) {
        return new CMatrixRMaj(numRows,numCols);
    }

    @Override
    public MatrixType getType() {
        return MatrixType.CDRM;
    }
}
