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
 * Fixed sized vector with 3 elements.  Can represent a a 3 x 1 or 1 x 3 matrix, context dependent.
 *
 * @author Peter Abeles
 */
public class FixedMatrix3_64F implements FixedMatrix64F {
    public double a1,a2,a3;

    public FixedMatrix3_64F() {
    }

    public FixedMatrix3_64F(double a1, double a2, double a3)
    {
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }

    public FixedMatrix3_64F(FixedMatrix3_64F o) {
        this.a1 = o.a1;
        this.a2 = o.a2;
        this.a3 = o.a3;
    }

    @Override
    public double get(int row, int col) {
        return unsafe_get(row,col);
    }

    @Override
    public double unsafe_get(int row, int col) {
        if( row != 0 && col != 0 )
            throw new IllegalArgumentException("Row or column must be zero since this is a vector");

        int w = Math.max(row,col);

        if( w == 0 ) {
            return a1;
        } else if( w == 1 ) {
            return a2;
        } else if( w == 2 ) {
            return a3;
        }

        throw new IllegalArgumentException("Out of range.  "+w);
    }

    @Override
    public void set(int row, int col, double val) {
        unsafe_set(row,col,val);
    }

    @Override
    public void unsafe_set(int row, int col, double val) {
        if( row != 0 && col != 0 )
            throw new IllegalArgumentException("Row or column must be zero since this is a vector");

        int w = Math.max(row,col);

        if( w == 0 ) {
            a1 = val;
        } else if( w == 1 ) {
            a1 = val;
        } else if( w == 2 ) {
            a1 = val;
        }

        throw new IllegalArgumentException("Out of range.  "+w);
    }

    @Override
    public int getNumRows() {
        return 3;
    }

    @Override
    public int getNumCols() {
        return 1;
    }

    @Override
    public int getNumElements() {
        return 3;
    }

    @Override
    public <T extends Matrix64F> T copy() {
        return (T)new FixedMatrix3_64F(this);
    }

    @Override
    public void print() {
        MatrixIO.print(System.out, this);
    }
}
