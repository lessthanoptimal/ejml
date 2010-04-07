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
 * A generic abstract class for matrices whose data is stored in a single 1D array of doubles.  The
 * format of the elements in this array is not specified.  For example row major, column major,
 * and block row major are all common formats.
 *
 * @author Peter Abeles
 */
public abstract class D1Matrix64F extends Matrix64F {
    /**
     * Where the raw data for the matrix is stored.  The format is type dependent.
     */
    public double[] data;

    /**
     * Used to get a reference to the internal data.
     *
     * @return Reference to the matrix's data.
     */
    public double[] getData() {
        return data;
    }
}