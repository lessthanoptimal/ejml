/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.simple;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;


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
        eig = DecompositionFactory.eig(mat.numCols,true);
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

    /**
     * Returns the index of the eigenvalue which has the largest magnitude.
     *
     * @return index of the largest magnitude eigen value.
     */
    public int getIndexMax() {
        int indexMax = 0;
        double max = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m > max ) {
                max = m;
                indexMax = i;
            }
        }

        return indexMax;
    }

    /**
     * Returns the index of the eigenvalue which has the smallest magnitude.
     *
     * @return index of the smallest magnitude eigen value.
     */
    public int getIndexMin() {
        int indexMin = 0;
        double min = getEigenvalue(0).getMagnitude2();

        final int N = getNumberOfEigenvalues();
        for( int i = 1; i < N; i++ ) {
            double m = getEigenvalue(i).getMagnitude2();
            if( m < min ) {
                min = m;
                indexMin = i;
            }
        }

        return indexMin;
    }
}