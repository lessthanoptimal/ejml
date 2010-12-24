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
 * A row-major block matrix declared on to one continuous array.
 *
 * @author Peter Abeles
 */
public class BlockMatrix64F extends D1Matrix64F {
    public int blockLength;

    public BlockMatrix64F( int numRows , int numCols , int blockLength)
    {
        this.data = new double[ numRows * numCols ];
        this.blockLength = blockLength;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public BlockMatrix64F( int numRows , int numCols )
    {
        this(numRows,numCols, EjmlParameters.BLOCK_WIDTH);
    }

    public void set( BlockMatrix64F A ) {
        this.blockLength = A.blockLength;
        this.numRows = A.numRows;
        this.numCols = A.numCols;

        int N = numCols*numRows;

        if( data.length < N )
            data = new double[ N ];

        System.arraycopy(A.data,0,data,0,N);
    }

    @Override
    public double[] getData() {
        return data;
    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues)
    {
        if( numRows*numCols <= data.length  ) {
            this.numRows = numRows;
            this.numCols = numCols;
        } else {
            double[] data = new double[ numRows*numCols ];

            if( saveValues ) {
                System.arraycopy(this.data,0,data,0,getNumElements());
            }

            this.numRows = numRows;
            this.numCols = numCols;
            this.data = data;
        }
    }

    public void reshape(int numRows, int numCols, int blockLength , boolean saveValues) {
        this.blockLength = blockLength;
        this.reshape(numRows,numCols,saveValues);
    }

    @Override
    public int getIndex( int row, int col ) {
        // find the block it is inside
        int blockRow = row / blockLength;
        int blockCol = col / blockLength;

        int localHeight = Math.min(numRows - blockRow*blockLength , blockLength);

        int index = blockRow*blockLength*numCols + blockCol* localHeight * blockLength;

        int localLength = Math.min(numCols - blockLength*blockCol , blockLength);

        row = row % blockLength;
        col = col % blockLength;
        
        return index + localLength * row + col;
    }

    @Override
    public double get( int row, int col) {
        return data[ getIndex(row,col)];
    }

    @Override
    public double unsafe_get( int row, int col) {
        return data[ getIndex(row,col)];
    }

    @Override
    public void set( int row, int col, double val) {
        data[ getIndex(row,col)] = val;
    }

    @Override
    public void unsafe_set( int row, int col, double val) {
        data[ getIndex(row,col)] = val;
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
        MatrixIO.print(System.out,this);
    }

    public BlockMatrix64F copy() {
        BlockMatrix64F A = new BlockMatrix64F(numRows,numCols,blockLength);
        A.set(this);
        return A;
    }
}
