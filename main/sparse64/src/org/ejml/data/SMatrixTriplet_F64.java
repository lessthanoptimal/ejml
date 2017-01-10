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
 * TODO describe
 *
 * @author Peter Abeles
 */
public class SMatrixTriplet_F64 implements Matrix_F64
{
    public Element[] data = new Element[0];
    public int length;
    public int numRows;
    public int numCols;

    public SMatrixTriplet_F64() {
    }

    /**
     *
     * @param numRows Number of rows in the matrix
     * @param numCols Number of columns in the matrix
     * @param initLength Initial maximum length of data array.
     */
    public SMatrixTriplet_F64(int numRows, int numCols, int initLength ) {
        growData(initLength);
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public SMatrixTriplet_F64( SMatrixTriplet_F64 orig ) {
        set(orig);
    }

    public void reset() {
        length = 0;
        numRows = 0;
        numCols = 0;
    }

    public void reshape( int numRows , int numCols ) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.length = 0;
    }

    public void addItem(int row , int col , double value ) {
        if( length == data.length ) {
            growData(( length*2 + 10 ));
        }
        data[length++].set(row,col, value);
    }

    @Override
    public void set( int row , int col , double value ) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        unsafe_set(row,col,value);
    }

    @Override
    public void unsafe_set(int row, int col, double value) {
        Element e = findItem(row,col);
        if( e == null )
            addItem( row,col,value);
        else
            e.value = value;
    }

    @Override
    public int getNumElements() {
        return length;
    }

    @Override
    public double get( int row , int col ) {
        if( row < 0 || row >= numRows || col < 0 || col >= numCols )
            throw new IllegalArgumentException("Outside of matrix bounds");

        return unsafe_get(row,col);
    }

    @Override
    public double unsafe_get(int row, int col) {
        Element e = findItem(row,col);
        if( e == null )
            return 0;
        else
            return e.value;
    }

    public Element findItem(int row , int col ) {
        for (int i = 0; i < length; i++) {
            Element e = data[i];
            if( e.row == row && e.col == col )
                return e;
        }
        return null;
    }

    /**
     * Will resize the array and keep all the old data
     * @param maxLength New maximum length of data
     */
    public void growData( int maxLength ) {
        if( data.length < maxLength ) {
            Element[] tmp = new Element[maxLength];
            System.arraycopy(data,0,tmp,0,data.length);
            for (int i = data.length; i < maxLength; i++) {
                tmp[i] = new Element();
            }
            data = tmp;
        }
    }

    public int getLength() {
        return length;
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
        return (T)new SMatrixTriplet_F64(this);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new SMatrixTriplet_F64(numRows,numCols,length);
    }

    @Override
    public void set(Matrix original) {
        SMatrixTriplet_F64 orig = (SMatrixTriplet_F64)original;
        reshape(orig.numRows,orig.numCols);
        growData(orig.length);

        for (int i = 0; i < length; i++) {
            data[i].set(orig.data[i]);
        }
    }

    @Override
    public void print() {

    }

    public static class Element {
        public int row,col;
        public double value;

        public void set( int row , int col , double value ) {
            this.row = row;
            this.col = col;
            this.value = value;
        }

        public void set( Element e ) {
            row = e.row;
            col = e.col;
            value = e.value;
        }
    }
}
