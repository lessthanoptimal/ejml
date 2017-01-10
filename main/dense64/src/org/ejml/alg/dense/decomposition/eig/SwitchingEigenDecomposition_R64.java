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

import org.ejml.UtilEjml;
import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.ops.MatrixFeatures_R64;


/**
 * Checks to see what type of matrix is being decomposed and calls different eigenvalue decomposition
 * algorithms depending on the results.  This primarily checks to see if the matrix is symmetric or not.
 *
 *
 * @author Peter Abeles
 */
public class SwitchingEigenDecomposition_R64
        implements EigenDecomposition_F64<DMatrixRow_F64> {
    // tolerance used in deciding if a matrix is symmetric or not
    private double tol;

    EigenDecomposition_F64<DMatrixRow_F64> symmetricAlg;
    EigenDecomposition_F64<DMatrixRow_F64> generalAlg;

    boolean symmetric;
    // should it compute eigenvectors or just eigenvalues?
    boolean computeVectors;

    DMatrixRow_F64 A = new DMatrixRow_F64(1,1);

    /**
     *
     * @param computeVectors
     * @param tol Tolerance for a matrix being symmetric
     */
    public SwitchingEigenDecomposition_R64(int matrixSize , boolean computeVectors , double tol ) {
        symmetricAlg = DecompositionFactory_R64.eig(matrixSize,computeVectors,true);
        generalAlg = DecompositionFactory_R64.eig(matrixSize,computeVectors,false);
        this.computeVectors = computeVectors;
        this.tol = tol;
    }

    public SwitchingEigenDecomposition_R64(int matrixSize ) {
        this(matrixSize,true, UtilEjml.TEST_F64);
    }

    @Override
    public int getNumberOfEigenvalues() {
        return symmetric ? symmetricAlg.getNumberOfEigenvalues() :
                generalAlg.getNumberOfEigenvalues();
    }

    @Override
    public Complex_F64 getEigenvalue(int index) {
        return symmetric ? symmetricAlg.getEigenvalue(index) :
                generalAlg.getEigenvalue(index);
    }

    @Override
    public DMatrixRow_F64 getEigenVector(int index) {
        if( !computeVectors )
            throw new IllegalArgumentException("Configured to not compute eignevectors");

        return symmetric ? symmetricAlg.getEigenVector(index) :
                generalAlg.getEigenVector(index);
    }

    @Override
    public boolean decompose(DMatrixRow_F64 orig) {
        A.set(orig);

        symmetric = MatrixFeatures_R64.isSymmetric(A,tol);

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
