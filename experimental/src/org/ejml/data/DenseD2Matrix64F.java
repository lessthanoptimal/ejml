/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.data;

import java.io.Serializable;


/**
 * A dense matrix where the array is stored as a 2D array.
 *
 * @author Peter Abeles
 */
public class DenseD2Matrix64F extends Matrix64F implements Serializable {

    /**
     * Where the raw data for the matrix is stored.  The format is type dependent.
     */
    public double[][] data;

    public DenseD2Matrix64F( int numRows , int numCols ) {
        data = new double[ numRows ][numCols];
        this.numRows = numRows;
        this.numCols = numCols;
    }

//    public double[][] getData() {
//        return data;
//    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues) {
        if( numRows <= data.length ) {
            this.numRows = numRows;
        } else {
            throw new IllegalArgumentException("Requested number of rows is too great.");
        }

        if( numCols <= data[0].length ) {
            this.numCols = numCols;
        } else {
            throw new IllegalArgumentException("Requested number of columns is too great.");
        }
    }

    @Override
    public double get(int row, int col) {
        return data[row][col];
    }

    @Override
    public void set(int row, int col, double val) {
        data[row][col] = val;
    }

    @Override
    public double unsafe_get( int row, int col) {
        return get(row,col);
    }

    @Override
    public void unsafe_set( int row, int col, double val) {
        set(row,col,val);
    }

    @Override
    public int getNumElements() {
        return numRows*numCols;
    }

    @Override
    public void print() {
    }
}