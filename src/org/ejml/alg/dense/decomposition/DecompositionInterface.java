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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.Matrix64F;


/**
 * <p>
 * An interface for performing matrix decompositions on a {@link org.ejml.data.DenseMatrix64F}.
 * </p>
 *
 * <p>
 * A matrix decomposition is an algorithm which decomposes the input matrix into a set of equivalent
 * matrices that store the same information as the original.  Decompositions are useful
 * in that they allow specialized efficient algorithms to be run on generic input
 * matrices.
 * </p>
 *
 * <p>
 * By default most decompositions will modify the input matrix.  This is done to save
 * memory and simply code by reducing the number of cases which need to be tested.
 * </p>
 *
 * @author Peter Abeles
 */
public interface DecompositionInterface <T extends Matrix64F> {

    /**
     * Computes the decomposition of the input matrix.  Depending on the implementation
     * the input matrix might be stored internally or modified.  If it is modified then
     * the function {@link #inputModified()} will return true and the matrix should not be
     * modified until the decomposition is no longer needed.
     *
     * @param orig The matrix which is being decomposed.  Modification is implementation dependent.
     * @return Returns if it was able to decompose the matrix.
     */
    public boolean decompose( T orig );

    /**
     * Is the input matrix to {@link #decompose(org.ejml.data.Matrix64F)} is modified during
     * the decomposition process.
     *
     * @return true if the input matrix to decompose() is modified.
     */
    public boolean inputModified();
}
