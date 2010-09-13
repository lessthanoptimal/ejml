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

import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * An interface for performing matrix decompositions on a {@link org.ejml.data.BlockMatrix64F}.
 * </p>
 *
 * <p>
 * For more information on decompositions see {@link org.ejml.alg.dense.decomposition.DecompositionInterface}.
 * </p>
 *
 * @author Peter Abeles
 */
public interface BlockDecompositionInterface {

    /**
     * Computes the decomposition of the input matrix.  Depending on the implementation
     * the input matrix might be stored internally or modified.  If it is modified then
     * the function {@link #modifyInput()} will return true.
     *
     * @param orig The matrix which is being decomposed.  Modification is implementation dependent.
     * @return Returns if it was able to decompose the matrix.
     */
    public boolean decompose( DenseMatrix64F orig );

    /**
     * Is the input matrix modified.
     *
     * @return true if the input matrix is modified.
     */
    public boolean modifyInput();
}
