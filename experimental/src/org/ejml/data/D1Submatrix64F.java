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
 * <p>
 * Describes a rectangular submatrix inside of a {@link D1Matrix64F}.
 * </p>
 *
 * <p>
 * Rows are row0 <= i < row1 and Columns are col0 <= j < col1
 * </p>
 * 
 * @author Peter Abeles
 */
public class D1Submatrix64F {
    public D1Matrix64F original;

    // bounding rows and columns
    public int row0,col0;
    public int row1,col1;

    public D1Submatrix64F() {
    }

    public D1Submatrix64F(D1Matrix64F original,
                          int row0, int col0, int row1, int col1) {
        this.original = original;
        this.row0 = row0;
        this.col0 = col0;
        this.row1 = row1;
        this.col1 = col1;
    }

    public D1Submatrix64F(BlockMatrix64F original) {
        this.original = original;
    }
}
