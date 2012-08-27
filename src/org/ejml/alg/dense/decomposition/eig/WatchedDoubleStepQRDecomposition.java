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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvalue;
import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvector;
import org.ejml.alg.dense.decomposition.hessenberg.HessenbergSimilarDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.EigenDecomposition;


/**
 * <p>
 * Finds the eigenvalue decomposition of an arbitrary square matrix using the implicit double-step QR algorithm.
 * Watched is included in its name because it is designed to print out internal debugging information.  This
 * class is still underdevelopment and has yet to be optimized.
 * </p>
 *
 * <p>
 * Based off the description found in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations." Second Edition.
 * </p>
 *
 * @author Peter Abeles
 */
//TODO looks like there might be some pointless copying of arrays going on
public class WatchedDoubleStepQRDecomposition
        implements EigenDecomposition<DenseMatrix64F> {

    HessenbergSimilarDecomposition hessenberg;
    WatchedDoubleStepQREigenvalue algValue;
    WatchedDoubleStepQREigenvector algVector;

    DenseMatrix64F H;

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public WatchedDoubleStepQRDecomposition( boolean computeVectors ) {
        hessenberg = new HessenbergSimilarDecomposition(10);
        algValue = new WatchedDoubleStepQREigenvalue();
        algVector = new WatchedDoubleStepQREigenvector();

        this.computeVectors = computeVectors;
    }

    @Override
    public boolean decompose(DenseMatrix64F A) {

        if( !hessenberg.decompose(A) )
            return false;

        H = hessenberg.getH(null);

        algValue.getImplicitQR().createR = false;
//        algValue.getImplicitQR().setChecks(true,true,true);

        if( !algValue.process(H) )
            return false;

//        for( int i = 0; i < A.numRows; i++ ) {
//            System.out.println(algValue.getEigenvalues()[i]);
//        }

        algValue.getImplicitQR().createR = true;

        if( computeVectors )
            return algVector.process(algValue.getImplicitQR(), H, hessenberg.getQ(null));
        else
            return true;
    }

    @Override
    public boolean inputModified() {
        return hessenberg.inputModified();
    }

    @Override
    public int getNumberOfEigenvalues() {
        return algValue.getEigenvalues().length;
    }

    @Override
    public Complex64F getEigenvalue(int index) {
        return algValue.getEigenvalues()[index];
    }

    @Override
    public DenseMatrix64F getEigenVector(int index) {
        return algVector.getEigenvectors()[index];
    }
}
