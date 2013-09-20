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
import org.ejml.data.ReshapeMatrix64F;


/**
 * <p>
 * Cholesky decomposition for {@link DenseMatrix64F}.
 * <p>
 * <p>
 * A Cholesky decomposition decomposes positive-definite symmetric matrices into either upper or
 * lower triangles:<br>
 * <br>
 * L*L<sup>T</sup>=A<br>
 * R<sup>T</sup>*R=A<br>
 * <br>
 * where L is a lower triangular matrix and R is an upper triangular matrix.  This is typically 
 * used to invert matrices, such as a covariance matrix.<br>
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL
 *
 * @author Peter Abeles
 */
public interface CholeskyDecomposition <MatrixType extends ReshapeMatrix64F>
        extends DecompositionInterface<MatrixType> {

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
    public MatrixType getT( MatrixType T  );

}