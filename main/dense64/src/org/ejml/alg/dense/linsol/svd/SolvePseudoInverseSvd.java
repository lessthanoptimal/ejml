/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol.svd;

import org.ejml.UtilEjml;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;


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
public class SolvePseudoInverseSvd implements LinearSolver<DenseMatrix64F> {

    // Used to compute pseudo inverse
    private SingularValueDecomposition<DenseMatrix64F> svd;

    // the results of the pseudo-inverse
    private DenseMatrix64F pinv = new DenseMatrix64F(1,1);

    // relative threshold used to select singular values
    private double threshold = UtilEjml.EPS;

    /**
     * Creates a new solver targeted at the specified matrix size.
     *
     * @param maxRows The expected largest matrix it might have to process.  Can be larger.
     * @param maxCols The expected largest matrix it might have to process.  Can be larger.
     */
    public SolvePseudoInverseSvd(int maxRows, int maxCols) {

        svd = DecompositionFactory.svd(maxRows,maxCols,true,true,true);
    }

    /**
     * Creates a solver targeted at matrices around 100x100
     */
    public SolvePseudoInverseSvd() {
        this(100,100);
    }

    @Override
    public boolean setA(DenseMatrix64F A) {
        pinv.reshape(A.numCols,A.numRows,false);

        if( !svd.decompose(A) )
            return false;

        DenseMatrix64F U_t = svd.getU(null,true);
        DenseMatrix64F V = svd.getV(null,false);
        double []S = svd.getSingularValues();
        int N = Math.min(A.numRows,A.numCols);

        // compute the threshold for singular values which are to be zeroed
        double maxSingular = 0;
        for( int i = 0; i < N; i++ ) {
            if( S[i] > maxSingular )
                maxSingular = S[i];
        }

        double tau = threshold*Math.max(A.numCols,A.numRows)*maxSingular;

        // computer the pseudo inverse of A
        if( maxSingular != 0.0 ) {
            for (int i = 0; i < N; i++) {
                double s = S[i];
                if (s < tau)
                    S[i] = 0;
                else
                    S[i] = 1.0 / S[i];
            }
        }

        // V*W
        for( int i = 0; i < V.numRows; i++ ) {
            int index = i*V.numCols;
            for( int j = 0; j < V.numCols; j++ ) {
                V.data[index++] *= S[j];
            }
        }

        // V*W*U^T
        CommonOps.mult(V,U_t, pinv);

        return true;
    }

    @Override
    public double quality() {
        throw new IllegalArgumentException("Not supported by this solver.");
    }

    @Override
    public void solve( DenseMatrix64F b, DenseMatrix64F x) {
        CommonOps.mult(pinv,b,x);
    }

    @Override
    public void invert(DenseMatrix64F A_inv) {
        A_inv.set(pinv);
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
    public SingularValueDecomposition<DenseMatrix64F> getDecomposition() {
        return svd;
    }

    /**
     * Specify the relative threshold used to select singular values.  By default it's UtilEjml.EPS.
     * @param threshold The singular value threshold
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public SingularValueDecomposition<DenseMatrix64F> getDecomposer() {
        return svd;
    }
}
