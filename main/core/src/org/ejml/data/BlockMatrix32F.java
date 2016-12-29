/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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
 * A row-major block matrix declared on to one continuous array.
 *
 * @author Peter Abeles
 */
public class BlockMatrix32F extends D1Matrix32F {
    public int blockLength;

    public BlockMatrix32F( int numRows , int numCols , int blockLength)
    {
        this.data = new float[ numRows * numCols ];
        this.blockLength = blockLength;
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public BlockMatrix32F( int numRows , int numCols )
    {
        this(numRows,numCols, EjmlParameters.BLOCK_WIDTH);
    }

    public BlockMatrix32F(){}

    public void set( BlockMatrix32F A ) {
        this.blockLength = A.blockLength;
        this.numRows = A.numRows;
        this.numCols = A.numCols;

        int N = numCols*numRows;

        if( data.length < N )
            data = new float[ N ];

        System.arraycopy(A.data,0,data,0,N);
    }

    public static BlockMatrix32F wrap( float data[] , int numRows , int numCols , int blockLength )
    {
        BlockMatrix32F ret = new BlockMatrix32F();
        ret.data = data;
        ret.numRows = numRows;
        ret.numCols = numCols;
        ret.blockLength = blockLength;

        return ret;
    }

    @Override
    public float[] getData() {
        return data;
    }

    @Override
    public void reshape(int numRows, int numCols, boolean saveValues)
    {
        if( numRows*numCols <= data.length  ) {
            this.numRows = numRows;
            this.numCols = numCols;
        } else {
            float[] data = new float[ numRows*numCols ];

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
    public float get( int row, int col) {
        return data[ getIndex(row,col)];
    }

    @Override
    public float unsafe_get( int row, int col) {
        return data[ getIndex(row,col)];
    }

    @Override
    public void set( int row, int col, float val) {
        data[ getIndex(row,col)] = val;
    }

    @Override
    public void unsafe_set( int row, int col, float val) {
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
    public <T extends Matrix> T createLike() {
        return (T)new BlockMatrix32F(numRows,numCols);
    }

    @Override
    public void set(Matrix original) {
        if( original instanceof BlockMatrix32F ) {
            set((BlockMatrix32F)original);
        } else {
            RealMatrix32F m = (RealMatrix32F) original;

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    set(i, j, m.get(i, j));
                }
            }
        }
    }

    @Override
    public int getNumElements() {
        return numRows*numCols;
    }

    @Override
    public void print() {
        MatrixIO.print(System.out,this);
    }

    public BlockMatrix32F copy() {
        BlockMatrix32F A = new BlockMatrix32F(numRows,numCols,blockLength);
        A.set(this);
        return A;
    }
}
