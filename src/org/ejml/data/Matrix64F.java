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


/**
 * An abstract class for all 64 bit floating point rectangular matices.
 *
 * @author Peter Abeles
 */
public abstract class Matrix64F {

    /**
     * Number of columns in the matrix.
     */
    public int numCols;
    /**
     * Number of rows in the matrix.
     */
    public int numRows;

    public abstract void reshape(int numRows, int numCols, boolean saveValues);

    public abstract int getIndex( int row, int col );

    public abstract double get( int index );

    public abstract void set( int index , double val );

    public abstract double get( int row , int col );

    public abstract void set( int row , int col , double val );

    public int getNumRows() {
        return numRows;    
    }

    public int getNumCols() {
        return numCols;
    }

    public abstract int getNumElements();
}
