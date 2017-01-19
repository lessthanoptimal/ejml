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
public class SMatrixTriplet_F64 implements SDMatrix
{
    public Element[] nz_data = new Element[0];
    public int nz_length;
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
    public void reshape(int numRows, int numCols, int nz_length) {
        reshape(numRows, numCols);
        growData(nz_length);
    }

    public void addItem(int row , int col , double value ) {
        if( nz_length == nz_data.length ) {
            growData(( nz_length *2 + 10 ));
        }
        nz_data[nz_length++].set(row,col, value);
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
        else
            nz_data[index].value = value;
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
            return nz_data[index].value;
    }

    public int nz_index(int row , int col ) {
        for (int i = 0; i < nz_length; i++) {
            Element e = nz_data[i];
            if( e.row == row && e.col == col )
                return i;
        }
        return -1;
    }

    /**
     * Will resize the array and keep all the old data
     * @param max_nz_length New maximum length of data
     */
    public void growData( int max_nz_length ) {
        if( nz_data.length < max_nz_length ) {
            Element[] tmp = new Element[max_nz_length];
            System.arraycopy(nz_data,0,tmp,0, nz_data.length);
            for (int i = nz_data.length; i < max_nz_length; i++) {
                tmp[i] = new Element();
            }
            nz_data = tmp;
        }
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
        return (T)new SMatrixTriplet_F64(this);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new SMatrixTriplet_F64(numRows,numCols, nz_length);
    }

    @Override
    public void set(Matrix original) {
        SMatrixTriplet_F64 orig = (SMatrixTriplet_F64)original;
        reshape(orig.numRows,orig.numCols);
        growData(orig.nz_length);

        this.nz_length = orig.nz_length;
        for (int i = 0; i < nz_length; i++) {
            nz_data[i].set(orig.nz_data[i]);
        }
    }

    @Override
    public void shrinkArrays() {
        if( nz_length < nz_data.length ) {
            Element tmp_data[] = new Element[nz_length];

            System.arraycopy(this.nz_data,0,tmp_data,0,nz_length);

            this.nz_data = tmp_data;
        }
    }

    @Override
    public void remove(int row, int col) {
        int where = nz_index(row,col);
        if( where >= 0 ) {
            Element e = nz_data[where];
            nz_length -= 1;
            for (int i = where; i < nz_length; i++) {
                nz_data[i] = nz_data[i+1];
            }
            nz_data[nz_length] = e;
        }
    }

    @Override
    public void zero() {
        nz_length = 0;
    }

    @Override
    public void print() {
        System.out.println(getClass().getSimpleName()+"\n , numRows = "+numRows+" , numCols = "+numCols
                +" , nz_length = "+ nz_length);
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int index = nz_index(row,col);
                if( index >= 0 )
                    System.out.printf("%6.3f",nz_data[index].value);
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
        System.out.println(getClass().getSimpleName()+"\n , numRows = "+numRows+" , numCols = "+numCols
                +" , nz_length = "+ nz_length);

        for (int i = 0; i < nz_length; i++) {
            Element e = nz_data[i];
            System.out.printf("%d %d %f\n",e.row,e.col,e.value);
        }
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
