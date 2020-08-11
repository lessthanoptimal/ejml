/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.MatrixIO;

/**
 * Fixed sized 2 by FMatrix2x2 matrix.  The matrix is stored as class variables for very fast read/write.  aXY is the
 * value of row = X and column = Y.
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateMatrixFixedNxN</p>
 *
 * @author Peter Abeles
 */
public class FMatrix2x2 implements FMatrixFixed {

    public float a11,a12;
    public float a21,a22;

    public FMatrix2x2() {
    }

    public FMatrix2x2( float a11, float a12,
                       float a21, float a22)
    {
        this.a11 = a11; this.a12 = a12;
        this.a21 = a21; this.a22 = a22;
    }

    public FMatrix2x2( FMatrix2x2 o ) {
        this.a11 = o.a11; this.a12 = o.a12;
        this.a21 = o.a21; this.a22 = o.a22;
    }

    @Override
    public void zero() {
        a11 = 0.0f; a12 = 0.0f;
        a21 = 0.0f; a22 = 0.0f;
    }

    public void set( float a11, float a12,
                     float a21, float a22)
    {
        this.a11 = a11; this.a12 = a12;
        this.a21 = a21; this.a22 = a22;
    }

    public void set( int offset , float []a ) {
        this.a11 = a[offset + 0]; this.a12 = a[offset + 1];
        this.a21 = a[offset + 2]; this.a22 = a[offset + 3];
    }

    @Override
    public float get(int row, int col) {
        return unsafe_get(row,col);
    }

    @Override
    public float unsafe_get(int row, int col) {
        if( row == 0 ) {
            if( col == 0 ) {
                return a11;
            } else if( col == 1 ) {
                return a12;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                return a21;
            } else if( col == 1 ) {
                return a22;
            }
        }
        throw new IllegalArgumentException("Row and/or column out of range. "+row+" "+col);
    }

    @Override
    public void set(int row, int col, float val) {
        unsafe_set(row,col,val);
    }

    @Override
    public void unsafe_set(int row, int col, float val) {
        if( row == 0 ) {
            if( col == 0 ) {
                a11 = val; return;
            } else if( col == 1 ) {
                a12 = val; return;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                a21 = val; return;
            } else if( col == 1 ) {
                a22 = val; return;
            }
        }
        throw new IllegalArgumentException("Row and/or column out of range. "+row+" "+col);
    }

    @Override
    public void set(Matrix original) {
        if( original.getNumCols() != 2 || original.getNumRows() != 2 )
            throw new IllegalArgumentException("Rows and/or columns do not match");
        FMatrix m = (FMatrix)original;
        
        a11 = m.get(0,0);
        a12 = m.get(0,1);
        a21 = m.get(1,0);
        a22 = m.get(1,1);
    }

    @Override
    public int getNumRows() {
        return 2;
    }

    @Override
    public int getNumCols() {
        return 2;
    }

    @Override
    public int getNumElements() {
        return 4;
    }

    @Override
    public <T extends Matrix> T copy() {
        return (T)new FMatrix2x2(this);
    }

    @Override
    public void print() {
        MatrixIO.printFancy(System.out, this, MatrixIO.DEFAULT_LENGTH);
    }

    @Override
    public void print( String format ) {
        MatrixIO.print(System.out, this, format);
    }

    @Override
    public <T extends Matrix> T createLike() {
        return (T)new FMatrix2x2();
    }

    @Override
    public MatrixType getType() {
        return MatrixType.UNSPECIFIED;
    }}

