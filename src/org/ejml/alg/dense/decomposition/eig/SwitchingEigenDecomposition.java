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

    public SwitchingEigenDecomposition( double tol ) {
        this.tol = tol;
    }

    public SwitchingEigenDecomposition() {
        this(1e-8);
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
        return symmetric ? symmetricAlg.getEigenVector(index) :
                generalAlg.getEigenVector(index);
    }

    @Override
    public boolean decompose(DenseMatrix64F orig) {
        symmetric = MatrixFeatures.isSymmetric(orig,tol);

        if( symmetric ) {
            if( symmetricAlg == null )
                symmetricAlg = new SymmetricQRAlgorithmDecomposition();
        } else if( generalAlg == null ) {
            generalAlg = new WatchedDoubleStepQRDecomposition();
        }

        return symmetric ?
                symmetricAlg.decompose(orig) :
                generalAlg.decompose(orig);

    }

    @Override
    public void setExpectedMaxSize(int numRows, int numCols) {
        if( symmetric )
            symmetricAlg.setExpectedMaxSize(numRows,numCols);
        else
            generalAlg.setExpectedMaxSize(numRows,numCols);
    }
}
