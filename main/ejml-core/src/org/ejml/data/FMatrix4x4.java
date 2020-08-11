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
 * Fixed sized 4 by FMatrix4x4 matrix.  The matrix is stored as class variables for very fast read/write.  aXY is the
 * value of row = X and column = Y.
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateMatrixFixedNxN</p>
 *
 * @author Peter Abeles
 */
public class FMatrix4x4 implements FMatrixFixed {

    public float a11,a12,a13,a14;
    public float a21,a22,a23,a24;
    public float a31,a32,a33,a34;
    public float a41,a42,a43,a44;

    public FMatrix4x4() {
    }

    public FMatrix4x4( float a11, float a12, float a13, float a14,
                       float a21, float a22, float a23, float a24,
                       float a31, float a32, float a33, float a34,
                       float a41, float a42, float a43, float a44)
    {
        this.a11 = a11; this.a12 = a12; this.a13 = a13; this.a14 = a14;
        this.a21 = a21; this.a22 = a22; this.a23 = a23; this.a24 = a24;
        this.a31 = a31; this.a32 = a32; this.a33 = a33; this.a34 = a34;
        this.a41 = a41; this.a42 = a42; this.a43 = a43; this.a44 = a44;
    }

    public FMatrix4x4( FMatrix4x4 o ) {
        this.a11 = o.a11; this.a12 = o.a12; this.a13 = o.a13; this.a14 = o.a14;
        this.a21 = o.a21; this.a22 = o.a22; this.a23 = o.a23; this.a24 = o.a24;
        this.a31 = o.a31; this.a32 = o.a32; this.a33 = o.a33; this.a34 = o.a34;
        this.a41 = o.a41; this.a42 = o.a42; this.a43 = o.a43; this.a44 = o.a44;
    }

    @Override
    public void zero() {
        a11 = 0.0f; a12 = 0.0f; a13 = 0.0f; a14 = 0.0f;
        a21 = 0.0f; a22 = 0.0f; a23 = 0.0f; a24 = 0.0f;
        a31 = 0.0f; a32 = 0.0f; a33 = 0.0f; a34 = 0.0f;
        a41 = 0.0f; a42 = 0.0f; a43 = 0.0f; a44 = 0.0f;
    }

    public void set( float a11, float a12, float a13, float a14,
                     float a21, float a22, float a23, float a24,
                     float a31, float a32, float a33, float a34,
                     float a41, float a42, float a43, float a44)
    {
        this.a11 = a11; this.a12 = a12; this.a13 = a13; this.a14 = a14;
        this.a21 = a21; this.a22 = a22; this.a23 = a23; this.a24 = a24;
        this.a31 = a31; this.a32 = a32; this.a33 = a33; this.a34 = a34;
        this.a41 = a41; this.a42 = a42; this.a43 = a43; this.a44 = a44;
    }

    public void set( int offset , float []a ) {
        this.a11 = a[offset + 0]; this.a12 = a[offset + 1]; this.a13 = a[offset + 2]; this.a14 = a[offset + 3];
        this.a21 = a[offset + 4]; this.a22 = a[offset + 5]; this.a23 = a[offset + 6]; this.a24 = a[offset + 7];
        this.a31 = a[offset + 8]; this.a32 = a[offset + 9]; this.a33 = a[offset + 10]; this.a34 = a[offset + 11];
        this.a41 = a[offset + 12]; this.a42 = a[offset + 13]; this.a43 = a[offset + 14]; this.a44 = a[offset + 15];
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
            } else if( col == 2 ) {
                return a13;
            } else if( col == 3 ) {
                return a14;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                return a21;
            } else if( col == 1 ) {
                return a22;
            } else if( col == 2 ) {
                return a23;
            } else if( col == 3 ) {
                return a24;
            }
        } else if( row == 2 ) {
            if( col == 0 ) {
                return a31;
            } else if( col == 1 ) {
                return a32;
            } else if( col == 2 ) {
                return a33;
            } else if( col == 3 ) {
                return a34;
            }
        } else if( row == 3 ) {
            if( col == 0 ) {
                return a41;
            } else if( col == 1 ) {
                return a42;
            } else if( col == 2 ) {
                return a43;
            } else if( col == 3 ) {
                return a44;
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
            } else if( col == 2 ) {
                a13 = val; return;
            } else if( col == 3 ) {
                a14 = val; return;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                a21 = val; return;
            } else if( col == 1 ) {
                a22 = val; return;
            } else if( col == 2 ) {
                a23 = val; return;
            } else if( col == 3 ) {
                a24 = val; return;
            }
        } else if( row == 2 ) {
            if( col == 0 ) {
                a31 = val; return;
            } else if( col == 1 ) {
                a32 = val; return;
            } else if( col == 2 ) {
                a33 = val; return;
            } else if( col == 3 ) {
                a34 = val; return;
            }
        } else if( row == 3 ) {
            if( col == 0 ) {
                a41 = val; return;
            } else if( col == 1 ) {
                a42 = val; return;
            } else if( col == 2 ) {
                a43 = val; return;
            } else if( col == 3 ) {
                a44 = val; return;
            }
        }
        throw new IllegalArgumentException("Row and/or column out of range. "+row+" "+col);
    }

    @Override
    public void set(Matrix original) {
        if( original.getNumCols() != 4 || original.getNumRows() != 4 )
            throw new IllegalArgumentException("Rows and/or columns do not match");
        FMatrix m = (FMatrix)original;
        
        a11 = m.get(0,0);
        a12 = m.get(0,1);
        a13 = m.get(0,2);
        a14 = m.get(0,3);
        a21 = m.get(1,0);
        a22 = m.get(1,1);
        a23 = m.get(1,2);
        a24 = m.get(1,3);
        a31 = m.get(2,0);
        a32 = m.get(2,1);
        a33 = m.get(2,2);
        a34 = m.get(2,3);
        a41 = m.get(3,0);
        a42 = m.get(3,1);
        a43 = m.get(3,2);
        a44 = m.get(3,3);
    }

    @Override
    public int getNumRows() {
        return 4;
    }

    @Override
    public int getNumCols() {
        return 4;
    }

    @Override
    public int getNumElements() {
        return 16;
    }

    @Override
    public <T extends Matrix> T copy() {
        return (T)new FMatrix4x4(this);
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
        return (T)new FMatrix4x4();
    }

    @Override
    public MatrixType getType() {
        return MatrixType.UNSPECIFIED;
    }}

