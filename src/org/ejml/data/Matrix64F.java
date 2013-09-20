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

import java.io.Serializable;

/**
 * Interface for all 64 bit floating point rectangular matrices.
 *
 * @author Peter Abeles
 */
public interface Matrix64F extends Serializable {

    /**
     * Returns the value of value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public double get( int row , int col );

    /**
     * Same as {@link #get} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public double unsafe_get( int row , int col );

    /**
     * Sets the value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public void set( int row , int col , double val );


    /**
     * Same as {@link #set} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public void unsafe_set( int row , int col , double val );

    /**
     * Returns the number of rows in this matrix.
     *
     * @return Number of rows.
     */
    public int getNumRows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return Number of columns.
     */
    public int getNumCols();

    /**
     * Returns the number of elements in this matrix, which is the number of rows
     * times the number of columns.
     *
     * @return Number of elements in this matrix.
     */
    public int getNumElements();


    public <T extends Matrix64F> T copy();

    public void print();
}
