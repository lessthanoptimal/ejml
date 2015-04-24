/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.mult;

import org.ejml.alg.fixed.FixedOps4;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.FixedMatrix4x4_64F;

/**
 * Set of fixed sized matrices used to do block multiplication
 *
 * @author Peter Abeles
 */
public class Fixed4x4Block {
    int numBlockRows;
    int numBlockCols;

    FixedMatrix4x4_64F blocks[];

    public Fixed4x4Block(int numBlockRows, int numBlockCols) {
        this.numBlockRows = numBlockRows;
        this.numBlockCols = numBlockCols;
        blocks = new FixedMatrix4x4_64F[numBlockRows * numBlockCols];

        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new FixedMatrix4x4_64F();
        }
    }

    public void fill( double value ) {
        for (int i = 0; i < blocks.length; i++) {
            FixedOps4.fill(blocks[i],value);
        }
    }

    public FixedMatrix4x4_64F getBlock( int row , int col ) {
        return blocks[row*numBlockCols+col];
    }

    public double get( int row , int col ) {
        int rowBlock = row/4;
        int colBlock = col/4;

        return blocks[rowBlock*numBlockCols+colBlock].get(row%4,col%4);
    }

    public void set( int row , int col , double value ) {
        int rowBlock = row/4;
        int colBlock = col/4;

        blocks[rowBlock*numBlockCols+colBlock].set(row % 4, col % 4, value);
    }

    public int getNumRows() {
        return numBlockRows*4;
    }

    public int getNumCols() {
        return numBlockCols*4;
    }

    public void set( DenseMatrix64F A , int startRow , int startCol ) {
        for (int i = 0; i < numBlockRows; i++) {
            for (int j = 0; j < numBlockCols; j++) {
                FixedMatrix4x4_64F block = blocks[i* numBlockCols +j];
                setBlock(A,startRow+i*4,startCol+j*4,block);
            }
        }
    }

    protected static void setBlock( DenseMatrix64F A , int startRow , int startCol , FixedMatrix4x4_64F out ) {
        int index = startRow*A.numCols + startCol;
        out.a11 = A.data[index++];
        out.a12 = A.data[index++];
        out.a13 = A.data[index++];
        out.a14 = A.data[index];
        index += A.numCols - 3;
        out.a21 = A.data[index++];
        out.a22 = A.data[index++];
        out.a23 = A.data[index++];
        out.a24 = A.data[index];
        index += A.numCols - 3;
        out.a31 = A.data[index++];
        out.a32 = A.data[index++];
        out.a33 = A.data[index++];
        out.a34 = A.data[index];
        index += A.numCols - 3;
        out.a41 = A.data[index++];
        out.a42 = A.data[index++];
        out.a43 = A.data[index++];
        out.a44 = A.data[index];
    }
}
