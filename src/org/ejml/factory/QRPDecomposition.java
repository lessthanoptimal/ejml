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
 * Similar to {@link QRDecomposition} but it can handle the rank deficient case by
 * performing column pivots during the decomposition. The final decomposition has the
 * following structure:<br>
 * A*P=Q*R<br>
 * where A is the original matrix, P is a pivot matrix, Q is an orthogonal matrix, and R is
 * upper triangular.
 * </p>
 *
 * @author Peter Abeles
 */
public interface QRPDecomposition <T extends Matrix64F>
        extends QRDecomposition<T>
{
    /**
     * Returns the rank as determined by the algorithm.  This is dependent upon a fixed threshold
     * and might not be appropriate for some applications.
     *
     * @return Matrix's rank
     */
    public int getRank();

    public int[] getPivots();

    public DenseMatrix64F getPivotMatrix( DenseMatrix64F P );
}
