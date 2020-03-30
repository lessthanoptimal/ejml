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

import org.ejml.ops.MatrixIO;

import java.util.Iterator;

/**
 * A sparse matrix format that is designed to act as an intermediate step for other matrix types. Constructing
 * {@link DMatrixSparseCSC} from scratch is difficult, but if a triplet is first defined then it is much easier.
 * Inside this class elements are stored in an unsorted list. Adding an element to the list with {@link #addItem(int, int, double)}
 * is an O(1) operation but reading a specific element is O(N) operation, making it impractical for operations like
 * matrix multiplications.
 *
 * @author Peter Abeles
 */
public class DMatrixSparseTriplet implements DMatrixSparse
{
    /**
     * Storage for row and column coordinate for non-zero elements
     */
    public IGrowArray nz_rowcol = new IGrowArray();
    /**
     * Storage for value of a non-zero element
     */
    public DGrowArray nz_value = new DGrowArray();

    /**
     * Number of non-zero elements in this matrix
     */
    public int nz_length;

    /**
     * Number of rows in the matrix
     */
    public int numRows;
    /**
     * Number of columns in the matrix
     */
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
        nz_rowcol.reshape(initLength*2);
        nz_value.reshape(initLength);
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public DMatrixSparseTriplet(DMatrixSparseTriplet orig ) {
        set(orig);
    }

    public void reset() {
        nz_length = 0;
        numRows = 0;
        numCols = 0;
    }

    public void reshape( int numRows , int numCols ) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.nz_length = 0;
    }

    @Override
    public void reshape(int numRows, int numCols, int arrayLength) {
        reshape(numRows, numCols);
        nz_rowcol.reshape(arrayLength*2);
        nz_value.reshape(arrayLength);
    }

    /**
     * <p>Adds a triplet of (row,vol,value) to the end of the list. This is the preferred way to add elements
     * into this array type as it has a runtime complexity of O(1).</p>
     *
     * One potential problem with using this function instead of {@link #set(int, int, double)} is that it does
     * not check to see if a (row,col) has already been assigned a value. If a (row,col) is defined multiple times
     * how this is handled is not defined.
     *
     * @param row Row the element belongs in
     * @param col Column the element belongs in
     * @param value The value of the element
     */
    public void addItem(int row , int col , double value ) {
        if( nz_length == nz_value.data.length ) {
            int amount = nz_length + 10;
            nz_value.growInternal(amount);
            nz_rowcol.growInternal(amount*2);
        }
        nz_value.data[nz_length] = value;
        nz_rowcol.data[nz_length*2] = row;
        nz_rowcol.data[nz_length*2+1] = col;
        nz_length += 1;
    }

    /**
     * Adds a triplet of (row,vol,value) to the end of the list and performs a bounds check to make
     * sure it is a legal value.
     *
     * @See #addItem(int, int, double)
     *
     * @param row Row the element belongs in
     * @param col Column the element belongs in
     * @param value The value of the element
     */
    public void addItemCheck(int row , int col , double value ) {
        if( row < 0 || col < 0 || row >= numRows || col >= numCols )
            throw new IllegalArgumentException("Out of bounds. ("+row+","+col+") "+numRows+" "+numCols);
        if( nz_length == nz_value.data.length ) {
            int amount = nz_length + 10;
            nz_value.growInternal(amount);
            nz_rowcol.growInternal(amount*2);
        }
        nz_value.data[nz_length] = value;
        nz_rowcol.data[nz_length*2] = row;
        nz_rowcol.data[nz_length*2+1] = col;
        nz_length += 1;
    }

    /**
     * Sets the element's value at (row,col). It first checks to see if the element already has a value and if it
     * does that value is changed. As a result this operation is O(N), where N is the number of elements in the matrix.
     *
     * @see #addItem(int, int, double) For a faster but less "safe" alternative
     *
     * @param row Matrix element's row index.
     * @param col Matrix element's column index.
     * @param value value of element.
     */
    @Override
    public void set( int row , int col , double value ) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        unsafe_set(row,col,value);
    }

    /**
     * Same as {@link #set(int, int, double)} but does not check to see if row and column are within bounds.
     *
     * @param row Matrix element's row index.
     * @param col Matrix element's column index.
     * @param value value of element.
     */
    @Override
    public void unsafe_set(int row, int col, double value) {
        int index = nz_index(row,col);
        if( index < 0 )
            addItem( row,col,value);
        else {
            nz_value.data[index] = value;
        }
    }

    @Override
    public int getNumElements() {
        return nz_length;
    }

    /**
     * Searches the list to see if the element at (row,col) has been assigned. The worst case runtime for this
     * operation is O(N), where N is the number of elements in the matrix.
     *
     * @param row Matrix element's row index.
     * @param col Matrix element's column index.
     * @return Value at (row,col)
     */
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
            return nz_value.data[index];
    }

    public int nz_index(int row , int col ) {
        int end = nz_length*2;
        for (int i = 0; i < end; i += 2) {
            int r = nz_rowcol.data[i];
            int c = nz_rowcol.data[i+1];
            if( r == row && c == col )
                return i/2;
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
    public <T extends Matrix> T create(int numRows, int numCols) {
        return (T)new DMatrixSparseTriplet(numRows,numCols,1);
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
        if( nz_length < nz_value.length ) {
            double vtmp[] = new double[nz_length];
            int rctmp[] = new int[nz_length*2];

            System.arraycopy(this.nz_value.data,0,vtmp,0,vtmp.length);
            System.arraycopy(this.nz_rowcol.data,0,rctmp,0,rctmp.length);

            nz_value.data = vtmp;
            nz_rowcol.data = rctmp;
        }
    }

    @Override
    public void remove(int row, int col) {
        int where = nz_index(row,col);
        if( where >= 0 ) {

            nz_length -= 1;
            for (int i = where; i < nz_length; i++) {
                nz_value.data[i] = nz_value.data[i+1];
            }
            int end = nz_length*2;
            for (int i = where*2; i < end; i += 2) {
                nz_rowcol.data[i] = nz_rowcol.data[i+2];
                nz_rowcol.data[i+1] = nz_rowcol.data[i+3];
            }
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
                    System.out.printf(format,nz_value.data[index]);
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
        System.out.println("Type = "+getClass().getSimpleName()+" , rows = "+numRows+" , cols = "+numCols
                +" , nz_length = "+ nz_length);

        for (int i = 0; i < nz_length; i++) {
            int row = nz_rowcol.data[i*2];
            int col = nz_rowcol.data[i*2+1];
            double value = nz_value.data[i];
            System.out.printf("%d %d %f\n",row,col,value);
        }
    }

    @Override
    public MatrixType getType() {
        return MatrixType.DTRIPLET;
    }

    @Override
    public Iterator<CoordinateRealValue> createCoordinateIterator() {
        return new Iterator<CoordinateRealValue>() {
            CoordinateRealValue coordinate = new CoordinateRealValue();
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < nz_length;
            }

            @Override
            public CoordinateRealValue next() {
                coordinate.row = nz_rowcol.data[index*2];
                coordinate.col = nz_rowcol.data[index*2+1];
                coordinate.value = nz_value.data[index];
                index++;
                return coordinate;
            }
        };
    }
}
