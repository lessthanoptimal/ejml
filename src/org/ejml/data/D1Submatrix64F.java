/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.MatrixIO;
import org.ejml.simple.SimpleMatrix;

/**
 * <p>
 * Describes a rectangular submatrix inside of a {@link D1Matrix64F}.
 * </p>
 *
 * <p>
 * Rows are row0 <= i < row1 and Columns are col0 <= j < col1
 * </p>
 * 
 * @author Peter Abeles
 */
public class D1Submatrix64F {
    public D1Matrix64F original;

    // bounding rows and columns
    public int row0,col0;
    public int row1,col1;

    public D1Submatrix64F() {
    }

    public D1Submatrix64F(D1Matrix64F original,
                          int row0, int row1, int col0, int col1) {
        this.original = original;
        this.row0 = row0;
        this.col0 = col0;
        this.row1 = row1;
        this.col1 = col1;
    }

    public D1Submatrix64F(D1Matrix64F original) {
        this.original = original;
        row1 = original.numRows;
        col1 = original.numCols;
    }

    public int getRows() {
        return row1 - row0;
    }

    public int getCols() {
        return col1 - col0;
    }

    public double get(int row, int col ) {
        return original.get(row+row0,col+col0);
    }

    public void set(int row, int col, double value) {
        original.set(row+row0,col+col0,value);
    }

    public SimpleMatrix extract() {
        SimpleMatrix ret = new SimpleMatrix(row1-row0,col1-col0);

        for( int i = 0; i < ret.numRows(); i++ ) {
            for( int j = 0; j < ret.numCols(); j++ ) {
                ret.set(i,j,get(i,j));
            }
        }

        return ret;
    }

    public void print() {
        MatrixIO.print(System.out,original,"%6.3f",row0,row1,col0,col1);
    }
}
