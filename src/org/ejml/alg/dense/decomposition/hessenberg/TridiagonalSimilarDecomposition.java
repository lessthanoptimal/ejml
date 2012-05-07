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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.alg.dense.decomposition.DecompositionInterface;
import org.ejml.data.Matrix64F;


/**
 * <p>
 * Finds the decomposition of a matrix in the form of:<br>
 * <br>
 * A = O*T*O<sup>T</sup><br>
 * <br>
 * where A is a symmetric m by m matrix, O is an orthogonal matrix, and T is a tridiagonal matrix.
 * </p>
 *
 * @author Peter Abeles
 */
public interface TridiagonalSimilarDecomposition<MatrixType extends Matrix64F>
        extends DecompositionInterface<MatrixType> {

    /**
     * Extracts the tridiagonal matrix found in the decomposition.
     *
     * @param T If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted T matrix.
     */
    public MatrixType getT( MatrixType T );

    /**
     * An orthogonal matrix that has the following property: T = Q<sup>T</sup>AQ
     *
     * @param Q If not null then the results will be stored here.  Otherwise a new matrix will be created.
     * @return The extracted Q matrix.
     */
    public MatrixType getQ( MatrixType Q , boolean transposed );

    /**
     * Extracts the diagonal and off diagonal elements of the decomposed tridiagonal matrix.
     * Since it is symmetric only one off diagonal array is returned.
     *
     * @param diag Diagonal elements. Modified.
     * @param off off diagonal elements. Modified.
     */
    public void getDiagonal( double []diag, double []off );
}
