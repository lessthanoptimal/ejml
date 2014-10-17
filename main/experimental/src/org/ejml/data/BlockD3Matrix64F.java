/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlParameters;
import org.ejml.ops.MatrixIO;


/**
 * Row-major block matrix declared using 3D array.
 *
 * @author Peter Abeles
 */
public class BlockD3Matrix64F implements ReshapeMatrix, RealMatrix64F {
    public int blockLength;
    public double[][][] blocks;

    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    public BlockD3Matrix64F( int numRows , int numCols , int blockLength)
    {
        this.blockLength = blockLength;

        reshape(numRows,numCols);
    }

    public BlockD3Matrix64F( int numRows , int numCols )
    {
        this(numRows,numCols, EjmlParameters.BLOCK_WIDTH);
    }


    public double[][][] getData() {
        return blocks;
    }

    @Override
    public void reshape(int numRows, int numCols )
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
        MatrixIO.print(System.out,this);
    }

    @Override
    public <T extends Matrix> T copy() {
        return null;
    }

    @Override
    public void set(Matrix original) {
        throw new RuntimeException("Not supported yet");
    }
}