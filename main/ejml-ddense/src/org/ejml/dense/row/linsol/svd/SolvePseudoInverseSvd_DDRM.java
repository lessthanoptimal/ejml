/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.linsol.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * <p>
 * The pseudo-inverse is typically used to solve over determined system for which there is no unique solution.<br>
 * x=inv(A<sup>T</sup>A)A<sup>T</sup>b<br>
 * where A &isin; &real; <sup>m &times; n</sup> and m &ge; n.
 * </p>
 *
 * <p>
 * This class implements the Moore-Penrose pseudo-inverse using SVD and should never fail.  Alternative implementations
 * can use Cholesky decomposition, but those will fail if the A<sup>T</sup>A matrix is singular.
 * However the Cholesky implementation is much faster.
 * </p>
 *
 * @author Peter Abeles
 */
public class SolvePseudoInverseSvd_DDRM implements LinearSolverDense<DMatrixRMaj> {

    // Used to compute pseudo inverse
    private SingularValueDecomposition_F64<DMatrixRMaj> svd;

    // the results of the pseudo-inverse
    private DMatrixRMaj pinv = new DMatrixRMaj(1, 1);

    // relative threshold used to select singular values
    private double threshold = UtilEjml.EPS;

    // Internal workspace
    private DMatrixRMaj U_t = new DMatrixRMaj(1, 1), V = new DMatrixRMaj(1, 1);

    /**
     * Creates a new solver targeted at the specified matrix size.
     *
     * @param maxRows The expected largest matrix it might have to process.  Can be larger.
     * @param maxCols The expected largest matrix it might have to process.  Can be larger.
     */
    public SolvePseudoInverseSvd_DDRM( int maxRows, int maxCols ) {

        svd = DecompositionFactory_DDRM.svd(maxRows, maxCols, true, true, true);
    }

    /**
     * Creates a solver targeted at matrices around 100x100
     */
    public SolvePseudoInverseSvd_DDRM() {
        this(100, 100);
    }

    @Override
    public boolean setA( DMatrixRMaj A ) {
        pinv.reshape(A.numCols, A.numRows, false);

        if (!svd.decompose(A))
            return false;

        svd.getU(U_t, true);
        svd.getV(V, false);
        double[] S = svd.getSingularValues();
        int N = Math.min(A.numRows, A.numCols);

        // compute the threshold for singular values which are to be zeroed
        double maxSingular = 0;
        for (int i = 0; i < N; i++) {
            if (S[i] > maxSingular)
                maxSingular = S[i];
        }

        double tau = threshold*Math.max(A.numCols, A.numRows)*maxSingular;

        // computer the pseudo inverse of A
        if (maxSingular != 0.0) {
            for (int i = 0; i < N; i++) {
                double s = S[i];
                if (s < tau)
                    S[i] = 0;
                else
                    S[i] = 1.0/S[i];
            }
        }

        // V*W
        for (int i = 0; i < V.numRows; i++) {
            int index = i*V.numCols;
            for (int j = 0; j < V.numCols; j++) {
                V.data[index++] *= S[j];
            }
        }

        // V*W*U^T
        CommonOps_DDRM.mult(V, U_t, pinv);

        return true;
    }

    @Override
    public /**/double quality() {
        double[] S = svd.getSingularValues();
        int N = Math.min(pinv.numRows, pinv.numCols);
        double min = S[0];
        double max = min;
        for (int i = 0; i < N; i++) {
            min = Math.min(min, S[i]);
            max = Math.max(max, S[i]);
        }
        return min/max;
    }

    @Override
    public void solve( DMatrixRMaj b, DMatrixRMaj x ) {
        CommonOps_DDRM.mult(pinv, b, x);
    }

    @Override
    public void invert( DMatrixRMaj A_inv ) {
        A_inv.setTo(pinv);
    }

    @Override
    public boolean modifiesA() {
        return svd.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return false;
    }

    @Override
    public SingularValueDecomposition_F64<DMatrixRMaj> getDecomposition() {
        return svd;
    }

    /**
     * Specify the relative threshold used to select singular values.  By default it's UtilEjml.EPS.
     *
     * @param threshold The singular value threshold
     */
    public void setThreshold( double threshold ) {
        this.threshold = threshold;
    }

    public SingularValueDecomposition<DMatrixRMaj> getDecomposer() {
        return svd;
    }
}
