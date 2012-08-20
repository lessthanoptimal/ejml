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

package org.ejml.factory;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;

/**
 * <p>
 * Gauss-Jordan decomposition converts a matrix into reduced row echelon form using elementary
 * row operations.  Reduced row echelon will have the diagonal elements all equal to one with off diagonal elements
 * equal to zero, for a square matrix.
 * </p>
 *
 * <p>
 * Rarely used in practice any more because LU decomposition is faster.  There are a few highly specialized uses
 * for reduced echelon form.
 * </p>
 *
 * @author Peter Abeles
 */
public interface GjPivotDecomposition<T extends Matrix64F>
        extends DecompositionInterface<T>
{

    /**
     * Specifies the tolerance for singular matrix.  A reasonable value is:
     * tol = EPS/max(||tol||).
     *
     * @param tol Tolerance for detecting a singular matrix.
     */
    public void setTolerance( double tol );

    /**
     * <p>
     * The order in which rows are processed.  The value of pivot[i] indicates that row 'i' was processed
     * as the specified value.
     * </p>
     *
     * @return The pivot matrix.
     */
    public int[] getRowPivots();

    /**
     * Decomposed matrix where elements are the multipliers.
     *
     * @return Decomposed matrix
     */
    public DenseMatrix64F getDecomposition();
}
