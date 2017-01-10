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
public class SMatrixTriplet_64 {

    public Element[] data = new Element[0];
    public int length;
    public int numRows;
    public int numCols;

    public SMatrixTriplet_64(int maxLength) {
        growData(maxLength);
    }

    public SMatrixTriplet_64(int numRows, int numCols, int maxLength ) {
        this(maxLength);
        this.numRows = numRows;
        this.numCols = numCols;
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

    public void add(int row , int col , double value ) {
        if( length == data.length ) {
            growData(( length*2 + 10 ));
        }
        data[length++].set(row,col, value);
    }

    public void set( int row , int col , double value ) {
        Element e = find(row,col);
        if( e == null )
            add( row,col,value);
        else
            e.value = value;
    }

    public double get( int row , int col ) {
        Element e = find(row,col);
        if( e == null )
            return 0;
        else
            return e.value;
    }

    public Element find( int row , int col ) {
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

    public static class Element {
        public int row,col;
        public double value;

        public void set( int row , int col , double value ) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }
}
