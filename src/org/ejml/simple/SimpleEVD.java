/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;


/**
 * Wrapper around EigenDecomposition for SimpleMatrix
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class SimpleEVD <T extends SimpleMatrix>
{
    private EigenDecomposition<DenseMatrix64F> eig;

    DenseMatrix64F mat;

    public SimpleEVD( DenseMatrix64F mat )
    {
        this.mat = mat;
        eig = DecompositionFactory.eig();
        if( !eig.decompose(mat))
            throw new RuntimeException("Eigenvalue Decomposition failed");
    }

    /**
     * Returns the number of eigenvalues/eigenvectors.  This is the matrix's dimension.
     *
     * @return number of eigenvalues/eigenvectors.
     */
    public int getNumberOfEigenvalues() {
        return eig.getNumberOfEigenvalues();
    }

    /**
     * <p>
     * Returns the value of an individual eigenvalue.  The eigenvalue maybe a complex number.  It is
     * a real number of the imaginary component is equal to exactly one.
     * </p>
     *
     * @param index Index of the eigenvalue eigenvector pair.
     * @return An eigenvalue.
     */
    public Complex64F getEigenvalue( int index ) {
        return eig.getEigenvalue(index);
    }

    /**
     * <p>
     * Used to retrieve real valued eigenvectors.  If an eigenvector is associated with a complex eigenvalue
     * then null is returned instead.
     * </p>
     *
     * @param index Index of the eigenvalue eigenvector pair.
     * @return If the associated eigenvalue is real then an eigenvector is returned, null otherwise.
     */
    public T getEigenVector( int index ) {
        return (T)SimpleMatrix.wrap(eig.getEigenVector(index));
    }

    /**
     * <p>
     * Computes the quality of the computed decomposition.  A value close to or less than 1e-15
     * is considered to be within machine precision.
     * </p>
     *
     * <p>
     * This function must be called before the original matrix has been modified or else it will
     * produce meaningless results.
     * </p>
     *
     * @return Quality of the decomposition.
     */
    public double quality() {
        return DecompositionFactory.quality(mat,eig);
    }

    /**
     * Returns the underlying decomposition that this is a wrapper around.
     *
     * @return EigenDecomposition
     */
    public EigenDecomposition getEVD() {
        return eig;
    }
}