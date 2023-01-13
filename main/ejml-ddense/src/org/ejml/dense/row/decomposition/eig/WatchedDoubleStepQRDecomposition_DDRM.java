/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decomposition.eig;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigen_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigenvalue_DDRM;
import org.ejml.dense.row.decomposition.eig.watched.WatchedDoubleStepQREigenvector_DDRM;
import org.ejml.dense.row.decomposition.hessenberg.HessenbergSimilarDecomposition_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

/**
 * <p>
 * Finds the eigenvalue decomposition of an arbitrary square matrix using the implicit double-step QR algorithm.
 * Watched is included in its name because it is designed to print out internal debugging information. This
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
@SuppressWarnings("NullAway.Init")
public class WatchedDoubleStepQRDecomposition_DDRM
        implements EigenDecomposition_F64<DMatrixRMaj> {

    HessenbergSimilarDecomposition_DDRM hessenberg;
    WatchedDoubleStepQREigenvalue_DDRM algValue;
    WatchedDoubleStepQREigenvector_DDRM algVector;

    DMatrixRMaj H;

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public WatchedDoubleStepQRDecomposition_DDRM( boolean computeVectors ) {
        this(new HessenbergSimilarDecomposition_DDRM(10),
                new WatchedDoubleStepQREigen_DDRM(),
                computeVectors);
    }

    public WatchedDoubleStepQRDecomposition_DDRM( HessenbergSimilarDecomposition_DDRM hessenberg,
                                                  WatchedDoubleStepQREigen_DDRM eigenQR,
                                                  boolean computeVectors ) {
        this.hessenberg = hessenberg;
        this.algValue = new WatchedDoubleStepQREigenvalue_DDRM(eigenQR);
        this.algVector = new WatchedDoubleStepQREigenvector_DDRM();

        this.computeVectors = computeVectors;
    }

    @Override
    public boolean decompose( DMatrixRMaj A ) {
        if (!hessenberg.decompose(A))
            return false;

        H = hessenberg.getH(null);

        algValue.getImplicitQR().createR = false;
//        algValue.getImplicitQR().setChecks(true,true,true);

        if (!algValue.process(H))
            return false;

//        for( int i = 0; i < A.numRows; i++ ) {
//            System.out.println(algValue.getEigenvalues()[i]);
//        }

        algValue.getImplicitQR().createR = true;

        if (computeVectors)
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
    public Complex_F64 getEigenvalue( int index ) {
        return algValue.getEigenvalues()[index];
    }

    @Override
    public DMatrixRMaj getEigenVector( int index ) {
        return algVector.getEigenvectors()[index];
    }
}
