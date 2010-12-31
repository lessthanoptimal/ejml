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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;


/**
 * Checks to see what type of matrix is being decomposed and calls different eigenvalue decomposition
 * algorithms depending on the results.  This primarily checks to see if the matrix is symmetric or not.
 *
 *
 * @author Peter Abeles
 */
public class SwitchingEigenDecomposition implements EigenDecomposition {
    // tolerance used in deciding if a matrix is symmetric or not
    private double tol;

    SymmetricQRAlgorithmDecomposition symmetricAlg;
    WatchedDoubleStepQRDecomposition generalAlg;

    boolean symmetric;
    // should it compute eigenvectors or just eigenvalues?
    boolean computeVectors;

    DenseMatrix64F A = new DenseMatrix64F(1,1);

    /**
     *
     * @param computeVectors
     * @param tol Tolerance for a matrix being symmetric
     */
    public SwitchingEigenDecomposition( boolean computeVectors , double tol ) {
        this.computeVectors = computeVectors;
        this.tol = tol;
    }

    public SwitchingEigenDecomposition() {
        this(true,1e-8);
    }

    @Override
    public int getNumberOfEigenvalues() {
        return symmetric ? symmetricAlg.getNumberOfEigenvalues() :
                generalAlg.getNumberOfEigenvalues();
    }

    @Override
    public Complex64F getEigenvalue(int index) {
        return symmetric ? symmetricAlg.getEigenvalue(index) :
                generalAlg.getEigenvalue(index);
    }

    @Override
    public DenseMatrix64F getEigenVector(int index) {
        if( !computeVectors )
            throw new IllegalArgumentException("Configured to not compute eignevectors");

        return symmetric ? symmetricAlg.getEigenVector(index) :
                generalAlg.getEigenVector(index);
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        A.setReshape(orig);

        symmetric = MatrixFeatures.isSymmetric(A,tol);

        if( symmetric ) {
            if( symmetricAlg == null )
                symmetricAlg = new SymmetricQRAlgorithmDecomposition(computeVectors);
        } else if( generalAlg == null ) {
            generalAlg = new WatchedDoubleStepQRDecomposition(computeVectors);
        }

        return symmetric ?
                symmetricAlg.decompose(A) :
                generalAlg.decompose(A);

    }

    @Override
    public boolean inputModified() {
        // since it doesn't know which algorithm will be used until a matrix is provided make a copy
        // of all inputs
        return false;
    }
}
