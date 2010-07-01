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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * This is a generic interface for computing the eigenvalues and eigenvectors of a matrix.
 * Eigenvalues and eigenvectors have the following property:<br>
 * <br>
 * Av=&lambda;v<br>
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
 * To create a new instance of {@link EigenDecomposition} use either {@link DecompositionFactory} or
 * {@link org.ejml.ops.EigenOps}.  {@link org.ejml.ops.EigenOps} contains options that allows customized
 * algorithms to be called, avoided unnecessary computations.
 * </p>
 * @author Peter Abeles
 */
public interface EigenDecomposition extends DecompositionInterface {

    /**
     * Returns the number of eigenvalues/eigenvectors.  This is the matrix's dimension.
     *
     * @return number of eigenvalues/eigenvectors.
     */
    public int getNumberOfEigenvalues();

    /**
     * <p>
     * Returns the value of an individual eigenvalue.  The eigenvalue maybe a complex number.  It is
     * a real number of the imaginary component is equal to exactly one.
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
    public DenseMatrix64F getEigenVector( int index );
}
