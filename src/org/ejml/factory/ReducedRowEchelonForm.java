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

/**
 * <p>
 * An augmented system matrix is said to be in reduced row echelon form (RREF) if the following are true:
 * </p>
 *
 * <ol>
 *     <li>If a row has non-zero entries, then the first non-zero entry is 1.  This is known as the leading one.</li>
 *     <li>If a column contains a leading one then all other entries in that column are zero.</li>
 *     <li>If a row contains a leading 1, then each row above contains a leading 1 further to the left.</li>
 * </ol>
 *
 * <p>
 * [1] Page 19 in, Otter Bretscherm "Linear Algebra with Applications" Prentice-Hall Inc, 1997
 * </p>
 *
 * @author Peter Abeles
 */
public interface ReducedRowEchelonForm<T extends DenseMatrix64F> {

    /**
     * Puts the augmented matrix into RREF.  The coefficient matrix is stored in
     * columns less than coefficientColumns.
     *
     *
     * @param A Input: Augmented matrix.  Output: RREF.  Modified.
     * @param coefficientColumns Number of coefficients in the system matrix.
     */
    public void reduce( T A , int coefficientColumns );

    /**
     * Specifies tolerance for determining if the system is singular and it should stop processing.
     * A reasonable value is: tol = EPS/max(||tol||).
     *
     * @param tol Tolerance for singular matrix. A reasonable value is: tol = EPS/max(||tol||). Or just set to zero.
     */
    public void setTolerance(double tol);
}
