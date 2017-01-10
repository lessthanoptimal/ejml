/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.eig;

import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvalue_R64;
import org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvector_R64;
import org.ejml.alg.dense.decomposition.hessenberg.HessenbergSimilarDecomposition_R64;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;


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
public class WatchedDoubleStepQRDecomposition_R64
        implements EigenDecomposition_F64<DMatrixRow_F64> {

    HessenbergSimilarDecomposition_R64 hessenberg;
    WatchedDoubleStepQREigenvalue_R64 algValue;
    WatchedDoubleStepQREigenvector_R64 algVector;

    DMatrixRow_F64 H;

    // should it compute eigenvectors or just eigenvalues
    boolean computeVectors;

    public WatchedDoubleStepQRDecomposition_R64(boolean computeVectors) {
        hessenberg = new HessenbergSimilarDecomposition_R64(10);
        algValue = new WatchedDoubleStepQREigenvalue_R64();
        algVector = new WatchedDoubleStepQREigenvector_R64();

        this.computeVectors = computeVectors;
    }

    @Override
    public boolean decompose(DMatrixRow_F64 A) {

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
    public Complex_F64 getEigenvalue(int index) {
        return algValue.getEigenvalues()[index];
    }

    @Override
    public DMatrixRow_F64 getEigenVector(int index) {
        return algVector.getEigenvectors()[index];
    }
}
