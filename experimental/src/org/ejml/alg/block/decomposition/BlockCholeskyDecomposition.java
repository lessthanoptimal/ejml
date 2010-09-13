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

import org.ejml.alg.dense.decomposition.DecompositionInterface;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * A Cholesky Decomposition is a special decomposition for positive-definite symmetric matrices
 * that is more efficient than other general purposes decomposition. It decomposes matrices
 * using one of the two following equations:<br>
 * <br>
 * L*L<sup>T</sup>=A<br>
 * R<sup>T</sup>*R=A<br>
 * <br>
 * where L is a lower triangular matrix and R is an upper triangular matrix.<br>
 * </p>
 * @author Peter Abeles
 */
public interface BlockCholeskyDecomposition extends DecompositionInterface {

    /**
     * If true the decomposition was for a lower triangular matrix.
     * If false it was for an upper triangular matrix.
     *
     * @return True if lower, false if upper.
     */
    public boolean isLower();


    /**
     * <p>
     * Returns the triangular matrix from the decomposition.
     * </p>
     *
     * <p>
     * If an input is provided that matrix is used to write the results to.
     * Otherwise a new matrix is created and the results written to it.
     * </p>
     *
     * @param T If not null then the decomposed matrix is written here.
     * @return A lower or upper triangular matrix.
     */
    public DenseMatrix64F getT( DenseMatrix64F T );

}