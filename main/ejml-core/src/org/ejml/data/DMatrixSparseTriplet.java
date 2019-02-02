/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.FancyPrint;
import org.ejml.ops.MatrixIO;

/**
 * TODO describe
 *
 * Internally, memory is allocated in fixed sized blocked. Items are added to a block until it has been filled
 * up then a new block is created. This results in a reasonable trade off between memory efficiency and
 * computation speed as elements are added one at a time. If the initial length was set correctly then this
 * would not be needed but its not always known or people don't use that option.
 *
 * @author Peter Abeles
 */
public class DMatrixSparseTriplet implements DMatrixSparse
{
    // Number of elements in a block. A block is the smallest amount of memory allocated
    public final int blockSize = 1024;

    /**
     * Storage for row and column coordinate for non-zero elements
     */
    public final IBlockArray nz_rowcol = new IBlockArray(blockSize*2);
    /**
     * Storage for value of a non-zero element
     */
    public final DBlockArray nz_value = new DBlockArray(blockSize);

    public int nz_length;
    public int numRows;
    public int numCols;

    public DMatrixSparseTriplet() {
    }

    /**
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @param initLength Initial maximum length of data array.
     */
    public DMatrixSparseTriplet(int numRows, int numCols, int initLength ) {
        // declare the memory
        nz_rowcol.resize(initLength*2);
        nz_value.resize(initLength);
        // set it back to zero since it is empty
        nz_rowcol.clear();
        nz_value.clear();

        this.numRows = numRows;
        this.numCols = numCols;
    }

    public DMatrixSparseTriplet(DMatrixSparseTriplet orig ) {
        set(orig);
    }

    public void reset() {
        nz_rowcol.clear();
        nz_value.clear();
        nz_length = 0;
        numRows = 0;
        numCols = 0;
    }

    public void reshape( int numRows , int numCols ) {
        this.nz_rowcol.clear();
        this.nz_value.clear();
        this.numRows = numRows;
        this.numCols = numCols;
        this.nz_length = 0;
    }

    @Override
    public void reshape(int numRows, int numCols, int arrayLength) {
        nz_rowcol.resize(arrayLength*2);
        nz_value.resize(arrayLength);
        reshape(numRows, numCols);
    }

    public void addItem(int row , int col , double value ) {
        // manually manage allocated blocks for sake of speed
        int block0 = nz_value.length/blockSize;
        int index0 = nz_value.length%blockSize;
        if( block0 >= nz_value.blockCount ) {
            nz_value.resize( nz_value.length+1);
            nz_rowcol.resize( nz_rowcol.length+2);
        }
        nz_value.data[block0][index0] = value;
        int[] data = nz_rowcol.data[block0];
        data[index0*2  ] = row;
        data[index0*2+1] = col;

        nz_length++;
        nz_value.length = nz_length;
        nz_rowcol.length = 2*nz_length;
    }

    @Override
    public void set( int row , int col , double value ) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        unsafe_set(row,col,value);
    }

    @Override
    public void unsafe_set(int row, int col, double value) {
        int index = nz_index(row,col);
        if( index < 0 )
            addItem( row,col,value);
        else {
            nz_value.set(index,value);
        }
    }

    @Override
    public int getNumElements() {
        return nz_length;
    }

    @Override
    public double get( int row , int col ) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        return unsafe_get(row,col);
    }

    @Override
    public double unsafe_get(int row, int col) {
        int index = nz_index(row,col);
        if( index < 0 )
            return 0;
        else
            return nz_value.get(index);
    }

    public void get( int index , Triplet value ) {
        value.value = nz_value.get(index);
        value.row = nz_rowcol.get(index*2);
        value.col = nz_rowcol.get(index*2+1);
    }

    public int nz_index(int row , int col ) {
        int blockSize =  nz_rowcol.getBlockSize();
        for (int bidx = 0; bidx < nz_rowcol.blockCount; bidx++) {
            int[] block = nz_rowcol.getBlock(bidx);
            int index0 = blockSize*bidx;
            int N =nz_rowcol.getBlockLength(bidx);
            for (int i = 0; i < N; i += 2) {
                int r = block[i];
                int c = block[i+1];
                if( r == row && c == col )
                    return (index0+i)/2;
            }
        }

        return -1;
    }

    public int getLength() {
        return nz_length;
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
        return (T)new DMatrixSparseTriplet(this);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new DMatrixSparseTriplet(numRows,numCols, nz_length);
    }

    @Override
    public void set(Matrix original) {
        DMatrixSparseTriplet orig = (DMatrixSparseTriplet)original;
        reshape(orig.numRows,orig.numCols);
        this.nz_rowcol.set(orig.nz_rowcol);
        this.nz_value.set(orig.nz_value);
        this.nz_length = orig.nz_length;
    }

    @Override
    public void shrinkArrays() {
        nz_rowcol.shrink();
        nz_value.shrink();
    }

    @Override
    public void remove(int row, int col) {
        int where = nz_index(row,col);
        if( where >= 0 ) {
            nz_length -= 1;
            nz_value.remove(where,where);
//            nz_rowcol.remove(2*where,2*where+1);
        }
    }

    @Override
    public boolean isAssigned(int row, int col) {
        return nz_index(row,col) >= 0;
    }

    @Override
    public void zero() {
        nz_length = 0;
    }

    @Override
    public int getNonZeroLength() {
        return nz_length;
    }

    @Override
    public void print() {
        print(MatrixIO.DEFAULT_FLOAT_FORMAT);
    }

    @Override
    public void print( String format ) {
        System.out.println("Type = "+getClass().getSimpleName()+" , rows = "+numRows+" , cols = "+numCols
                +" , nz_length = "+ nz_length);
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int index = nz_index(row,col);
                if( index >= 0 )
                    System.out.printf(format,nz_value.get(index));
                else
                    System.out.print("   *  ");
                if( col != numCols-1 )
                    System.out.print(" ");
            }
            System.out.println();
        }
    }

    @Override
    public void printNonZero() {
        FancyPrint fancy = new FancyPrint();
        System.out.println("Type = "+getClass().getSimpleName()+" , rows = "+numRows+" , cols = "+numCols
                +" , nz_length = "+ nz_length);

        for (int i = 0; i < nz_length; i++) {
            int row = nz_rowcol.get(i*2);
            int col = nz_rowcol.get(i*2+1);
            double value = nz_value.get(i);
            System.out.printf("%d %d %s\n",row,col,fancy.s(value));
        }
    }

    @Override
    public MatrixType getType() {
        return MatrixType.DTRIPLET;
    }

    public static class Triplet {
        public int row,col;
        public double value;
    }
}
