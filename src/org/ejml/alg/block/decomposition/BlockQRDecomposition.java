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

package org.ejml.alg.block.decomposition;

import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * A block implementation of QR decomposition.
 * </p>
 *
 * @author Peter Abeles
 */
public interface BlockQRDecomposition extends BlockDecompositionInterface {
    /**
     * <p>
     * Returns the Q matrix from the decomposition.  Should only
     * be called after {@link #decompose(org.ejml.data.BlockMatrix64F)} has
     * been called.
     * </p>
     *
     * <p>
     * If parameter Q is not null, then that matrix is used to store the Q matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param Q If not null then the Q matrix is written to it.  Modified.
     * @param compact If true an m by n matrix is created, otherwise n by n.
     * @return The Q matrix.
     */
    public BlockMatrix64F getQ(BlockMatrix64F Q, boolean compact);

    /**
     * <p>
     * Returns the R matrix from the decomposition.  Should only be
     * called after {@link #decompose(org.ejml.data.BlockMatrix64F)} has been.
     * </p>
     * <p>
     * If setZeros is true then an n &times; m matrix is required and all the elements are set.
     * If setZeros is false then the matrix must be at least m &times; m and only the upper triangular
     * elements are set.
     * </p>
     *
     * <p>
     * If parameter R is not null, then that matrix is used to store the R matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param R If not null then the R matrix is written to it. Modified.
     * @param compact If true only the upper triangular elements are set
     * @return The R matrix.
     */
    public BlockMatrix64F getR(BlockMatrix64F R, boolean compact);

}
