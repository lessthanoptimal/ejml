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

package org.ejml.alg.dense.linsol;

import org.ejml.data.DenseMatrix64F;

/**
 * In many situations solutions to linear systems that share many of the same data points are needed.
 * This can happen when solving using the most recent data or when rejecting outliers.  In these situations
 * it is possible to solve these related systems much faster than solving the entire data set again.
 *
 * @see LinearSolver
 *
 * @author Peter Abeles
 */
public interface AdjustableLinearSolver extends LinearSolver<DenseMatrix64F> {


    /**
     * Adds a row to A.  This has the same effect as creating a new A and calling {@link #setA}.
     *
     * @param A_row The row in A.
     * @param rowIndex Where the row appears in A.
     * @return if it succeeded or not.
     */
    public boolean addRowToA( double []A_row , int rowIndex );

    /**
     * Removes a row from A.  This has the same effect as creating a new A and calling {@link #setA}.
     *
     * @param index which row is removed from A.
     * @return If it succeeded or not.
     */
    public boolean removeRowFromA( int index );
}
