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

import org.ejml.data.Complex64F;
import org.ejml.data.Matrix64F;


/**
 * <p>
 * This is a generic interface for computing the eigenvalues and eigenvectors of a matrix.
 * Eigenvalues and eigenvectors have the following property:<br>
 * <br>
 * A*v=&lambda;*v<br>
 * <br>
 * where A is a square matrix and v is an eigenvector associated with the eigenvalue &lambda;.
 * </p>
 *
 * <p>
 * In general, both eigenvalues and eigenvectors can be complex numbers.  For symmetric matrices the
 * eigenvalues and eigenvectors are always real numbers.  EJML does not support complex matrices but
 * it does have minimal support for complex numbers.  As a result complex eigenvalues are found, but only
 * the real eigenvectors are computed.
 * </p>
 *
 * <p>
 * To create a new instance of {@link EigenDecomposition} use {@link DecompositionFactory}. If the matrix
 * is known to be symmetric be sure to use the symmetric decomposition, which is much faster and more accurate
 * than the general purpose one.
 * </p>
 * @author Peter Abeles
 */
public interface EigenDecomposition<MatrixType extends Matrix64F>
        extends DecompositionInterface<MatrixType> {

    /**
     * Returns the number of eigenvalues/eigenvectors.  This is the matrix's dimension.
     *
     * @return number of eigenvalues/eigenvectors.
     */
    public int getNumberOfEigenvalues();

    /**
     * <p>
     * Returns an eigenvalue as a complex number.  For symmetric matrices the returned eigenvalue will always be a real
     * number, which means the imaginary component will be equal to zero.
     * </p>
     *
     * <p>
     * NOTE: The order of the eigenvalues is dependent upon the decomposition algorithm used.  This means that they may
     * or may not be ordered by magnitude.  For example the QR algorithm will returns results that are partially
     * ordered by magnitude, but this behavior should not be relied upon.
     * </p>
     * 
     * @param index Index of the eigenvalue eigenvector pair.
     * @return An eigenvalue.
     */
    public Complex64F getEigenvalue( int index );

    /**
     * <p>
     * Used to retrieve real valued eigenvectors.  If an eigenvector is associated with a complex eigenvalue
     * then null is returned instead.
     * </p>
     *
     * @param index Index of the eigenvalue eigenvector pair.
     * @return If the associated eigenvalue is real then an eigenvector is returned, null otherwise.
     */
    public MatrixType getEigenVector( int index );
}
