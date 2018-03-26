/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
 * Sparse representation of a diagonal matrix. All elements but diagonal elements are zeros.
 *
 * @author Peter Abeles
 */
public class DMatrixDiag implements DMatrix, ReshapeMatrix
{
    /**
     * Value of diagonal elements
     */
    public double data[] = new double[0];

    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    public DMatrixDiag() {
    }

    public DMatrixDiag(int numRows, int numCols) {
        reshape(numRows,numCols);
    }

    public DMatrixDiag( DMatrixDiag original ) {
        this(original.numRows,original.numCols);
        System.arraycopy(original.data,0,data,0,Math.min(numRows,numCols));
    }

    public int length() {
        return Math.min(numRows,numCols);
    }

    @Override
    public double get(int row, int col) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds: "+row+" "+col);
        }
        if( row == col )
            return data[row];
        else
            return 0;
    }

    @Override
    public double unsafe_get(int row, int col) {
        if( row == col )
            return data[row];
        else
            return 0;
    }

    @Override
    public void set(int row, int col, double val) {
        if( col < 0 || col >= numCols || row < 0 || row >= numRows ) {
            throw new IllegalArgumentException("Specified element is out of bounds: "+row+" "+col);
        }
        if( row == col )
            data[row] = val;
        else
            throw new IllegalArgumentException("Can't change the value of off diagonal elements."+row+" "+col);
    }

    @Override
    public void unsafe_set(int row, int col, double val) {
        if( row == col )
            data[row] = val;
        else
            throw new IllegalArgumentException("Can't change the value of off diagonal elements."+row+" "+col);
    }

    @Override
    public int getNumElements() {
        return Math.min(numRows,numCols);
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
        return (T)new DMatrixDiag(this);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new DMatrixDiag(numRows,numCols);
    }

    @Override
    public void set(Matrix original) {
        if( !(original instanceof DMatrixDiag) )
            throw new RuntimeException("Must be same type");
        DMatrixDiag o = (DMatrixDiag)original;
        reshape(o.numRows,o.numCols);
        System.arraycopy(o.data,0,data,0,Math.min(numRows,numCols));
    }

    @Override
    public void print() {
        throw new RuntimeException("Implement");
    }

    @Override
    public MatrixType getType() {
        return MatrixType.DSDI;
    }

    @Override
    public void reshape(int numRows, int numCols) {
        int m = Math.min(numRows,numCols);
        if( m > data.length ) {
            data = new double[m];
        }
        this.numRows = numRows;
        this.numCols = numCols;
    }
}
