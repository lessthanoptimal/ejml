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
import org.ejml.ops.MatrixIO;


/**
 * Row-major block matrix declared using 3D array.
 *
 * @author Peter Abeles
 */
public class BlockD3Matrix64F extends Matrix64F {
    public int blockLength;
    public double[][][] blocks;

    public BlockD3Matrix64F( int numRows , int numCols , int blockLength)
    {
        this.blockLength = blockLength;

        reshape(numRows,numCols,false);
    }

    public BlockD3Matrix64F( int numRows , int numCols )
    {
        this(numRows,numCols, EjmlParameters.BLOCK_WIDTH);
    }


    public double[][][] getData() {
        return blocks;
    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues)
    {
        this.numRows = numRows;
        this.numCols = numCols;


        int blockM = numRows / blockLength;
        int blockN = numCols / blockLength;

        if( numRows % blockLength >  0) blockM++;
        if( numCols % blockLength >  0) blockN++;

        this.blocks = new double[blockM][blockN][];

        for( int i = 0; i < numRows; i += blockLength ) {
            int ii = i/blockLength;

            for( int j = 0; j < numCols; j += blockLength ) {
                int jj = j/blockLength;

                blocks[ii][jj] = new double[ blockLength*blockLength ];
            }
        }
    }

    @Override
    public double get( int row, int col) {
        int blockM = row / blockLength;
        int blockN = col / blockLength;

        int m = row % blockLength;
        int n = col % blockLength;

        int index = m*blockLength + n;

        return blocks[blockM][blockN][index];
    }

    @Override
    public void set( int row, int col, double val) {
        int blockM = row / blockLength;
        int blockN = col / blockLength;

        int m = row % blockLength;
        int n = col % blockLength;

        int index = m*blockLength + n;

        blocks[blockM][blockN][index] = val;
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
    public int getNumRows() {
        return numRows;
    }

    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public int getNumElements() {
        return numRows*numCols;
    }

    @Override
    public void print() {
        MatrixIO.print(this);
    }

    @Override
    public <T extends Matrix64F> T copy() {
        return null;
    }
}