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

import org.ejml.EjmlParameters;


/**
 * @author Peter Abeles
 */
public class BlockMatrix64F extends D1Matrix64F {
    public int blockWidth;
    // number of elements in a block
    public int numElements;

    int rowBlocks;
    int colBlocks;
    int blockRowStep;

    int rowRemainder;
    int colRemainder;

    public BlockMatrix64F( int numRows , int numCols , int blockWidth )
    {

        this.blockWidth = blockWidth;
        this.numElements = blockWidth*blockWidth;

        reshape(numRows,numCols, false);

        data = new double[ blockRowStep * rowBlocks ];
    }

    public BlockMatrix64F( int numRows , int numCols )
    {
        this(numRows,numCols, EjmlParameters.BLOCK_WIDTH);
    }

    @Override
    public double[] getData() {
        return data;
    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues)
    {
        this.numRows = numRows;
        this.numCols = numCols;

        rowBlocks = numRows/blockWidth;
        colBlocks = numCols/blockWidth;

        rowRemainder = numRows % blockWidth;
        colRemainder = numCols % blockWidth;

        if( rowRemainder > 0 ) {
            rowBlocks++;
        }

        if( colRemainder > 0 ) {
            colBlocks++;
        }

        blockRowStep = numElements*colBlocks;
    }

    public void set( DenseMatrix64F mat ) {
        if( mat.numRows != numRows || mat.numCols != numCols)
            throw new IllegalArgumentException("Rows and columns must be equal.");

        for( int blockI = 0; blockI < rowBlocks; blockI++ ) {
            int blockRows = blockI < rowBlocks-1 || rowRemainder == 0 ? blockWidth : rowRemainder;

            for( int blockJ = 0; blockJ < colBlocks; blockJ++ ) {
                int base = blockI*blockRowStep + blockJ*blockWidth;

                int blockCols = blockJ < colBlocks-1 || colRemainder == 0 ? blockWidth : colRemainder;

                for( int i = 0; i < blockRows; i++ ) {
                    int indexDst = base + i*blockWidth;
                    int indexSrc = (blockI*blockWidth+i)*mat.numCols + (blockJ*blockWidth);
                    for( int j = 0; j < blockCols; j++ ) {
                        data[indexDst++] = mat.data[indexSrc++];
                    }
                }
            }
        }
    }

    @Override
    public void set( int index , double val ) {
        data[index] = val;
    }

    @Override
    public double get( int index ) {
        return data[index];    
    }

    @Override
    public int getIndex( int row, int col ) {
        int blockI = row/blockWidth;
        int blockJ = col/blockWidth;

        int index = blockI*blockRowStep + blockJ*blockWidth;
        index += (row%blockWidth)*blockWidth + (col%blockWidth);

        return index;
    }

    @Override
    public double get( int row, int col) {
        int blockI = row/blockWidth;
        int blockJ = col/blockWidth;

        int index = blockI*blockRowStep + blockJ*blockWidth;
        index += (row%blockWidth)*blockWidth + (col%blockWidth);
        
        return data[ index ];
    }

    @Override
    public void set( int row, int col, double val) {
        int blockI = row/blockWidth;
        int blockJ = col/blockWidth;

        int index = blockI*blockRowStep + blockJ*blockWidth;
        index += (row%blockWidth)*blockWidth + (col%blockWidth);

        data[ index ] = val;
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
    public int getNumElements() {
        return blockRowStep*numRows;
    }
}
