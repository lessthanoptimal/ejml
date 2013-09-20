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

/**
 * Fixed sized 3 by 3 matrix.  The matrix is stored as class variables for very fast read/write.  aXY is the
 * value of row = X and column = Y.
 *
 * @author Peter Abeles
 */
public class FixedMatrix3x3_64F implements FixedMatrix64F {
    public double a11,a12,a13;
    public double a21,a22,a23;
    public double a31,a32,a33;

    public FixedMatrix3x3_64F() {
    }

    public FixedMatrix3x3_64F(double a11, double a12, double a13,
                              double a21, double a22, double a23,
                              double a31, double a32, double a33)
    {
        this.a11 = a11;
        this.a12 = a12;
        this.a13 = a13;
        this.a21 = a21;
        this.a22 = a22;
        this.a23 = a23;
        this.a31 = a31;
        this.a32 = a32;
        this.a33 = a33;
    }

    public FixedMatrix3x3_64F( FixedMatrix3x3_64F o ) {
        this.a11 = o.a11;
        this.a12 = o.a12;
        this.a13 = o.a13;
        this.a21 = o.a21;
        this.a22 = o.a22;
        this.a23 = o.a23;
        this.a31 = o.a31;
        this.a32 = o.a32;
        this.a33 = o.a33;
    }

    @Override
    public double get(int row, int col) {
        return unsafe_get(row,col);
    }

    @Override
    public double unsafe_get(int row, int col) {
        if( row == 0 ) {
            if( col == 0 ) {
                return a11;
            } else if( col == 1 ) {
                return a12;
            } else if( col == 2 ) {
                return a13;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                return a21;
            } else if( col == 1 ) {
                return a22;
            } else if( col == 2 ) {
                return a23;
            }
        } else if( row == 2 ) {
            if( col == 0 ) {
                return a31;
            } else if( col == 1 ) {
                return a32;
            } else if( col == 2 ) {
                return a33;
            }
        }
        throw new IllegalArgumentException("Row and/or column out of range. "+row+" "+col);
    }

    @Override
    public void set(int row, int col, double val) {
        unsafe_set(row,col,val);
    }

    @Override
    public void unsafe_set(int row, int col, double val) {
        if( row == 0 ) {
            if( col == 0 ) {
                a11 = val; return;
            } else if( col == 1 ) {
                a12 = val; return;
            } else if( col == 2 ) {
                a13 = val; return;
            }
        } else if( row == 1 ) {
            if( col == 0 ) {
                a21 = val; return;
            } else if( col == 1 ) {
                a22 = val; return;
            } else if( col == 2 ) {
                a23 = val; return;
            }
        } else if( row == 2 ) {
            if( col == 0 ) {
                a31 = val; return;
            } else if( col == 1 ) {
                a32 = val; return;
            } else if( col == 2 ) {
                a33 = val; return;
            }
        }
        throw new IllegalArgumentException("Row and/or column out of range. "+row+" "+col);
    }

    @Override
    public int getNumRows() {
        return 3;
    }

    @Override
    public int getNumCols() {
        return 3;
    }

    @Override
    public int getNumElements() {
        return 9;
    }

    @Override
    public <T extends Matrix64F> T copy() {
        return (T)new FixedMatrix3x3_64F(this);
    }

    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }
}
