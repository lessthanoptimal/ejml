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

import org.ejml.data.Matrix64F;


/**
 * <p>
 * LU Decomposition refactors the original matrix such that:<br>
 * <div align=center> P<sup>T</sup>*L*U = A</div>
 * where P is a pivot matrix, L is a lower triangular matrix, U is an upper triangular matrix and A is
 * the original matrix.
 * </p>
 *
 * <p>
 * LU Decomposition is useful since once the decomposition has been performed linear
 * equations can be quickly solved and the original matrix A inverted.  Different algorithms
 * can be selected to perform the decomposition, all will have the same end result.
 * </p>
 * <p>
 * To use this class first specify the size of the matrix that will be decomposed by it in
 * the constructor.  Only square m by m matrices can be decomposed.  Then to decompose a matrix
 * call {@link #decompose}.  If it encounters any problems an exception will be thrown.  After
 * that all the other functions will be available for solving and inverting matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public interface LUDecomposition <T extends Matrix64F>
        extends DecompositionInterface<T> {

    /**
     * <p>
     * Returns the L matrix from the decomposition.  Should only
     * be called after {@link #decompose(org.ejml.data.Matrix64F)} has
     * been called.
     * </p>
     *
     * <p>
     * If parameter 'lower' is not null, then that matrix is used to store the L matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param lower Storage for T matrix. If null then a new matrix is returned.  Modified.
     * @return The L matrix.
     */
    public T getLower( T lower );

    /**
     * <p>
     * Returns the U matrix from the decomposition.  Should only
     * be called after {@link #decompose(org.ejml.data.Matrix64F)}  has
     * been called.
     * </p>
     *
     * <p>
     * If parameter 'upper' is not null, then that matrix is used to store the U matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param upper Storage for U matrix. If null then a new matrix is returned. Modified.
     * @return The U matrix.
     */
    public T getUpper( T upper );

    /**
     * <p>
     * For numerical stability there are often row interchanges.  This computes
     * a pivot matrix that will undo those changes.
     * </p>
     *
     * @param pivot Storage for the pivot matrix. If null then a new matrix is returned. Modified.
     * @return The pivot matrix.
     */
    public T getPivot( T pivot );

    /**
     * Returns true if the decomposition detected a singular matrix.  This check
     * will not work 100% of the time due to machine precision issues.
     *
     * @return True if the matrix is singular and false if it is not.
     */
    // TODO Remove?  If singular decomposition will fail.
    public boolean isSingular();

    /**
     * Computes the matrix's determinant using the LU decomposition.
     *
     * @return The determinant.
     */
    public double computeDeterminant();
}
