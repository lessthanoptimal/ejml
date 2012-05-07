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

package org.ejml.alg.dense.linsol.svd;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.data.DenseMatrix64F;
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

        DenseMatrix64F U_t = svd.getU(true);
        DenseMatrix64F V = svd.getV(false);
        double []S = svd.getSingularValues();
        int N = Math.min(A.numRows,A.numCols);

        // compute the threshold for singular values which are to be zeroed
        double maxSingular = 0;
        for( int i = 0; i < N; i++ ) {
            if( S[i] > maxSingular )
                maxSingular = S[i];
        }

        double tau = UtilEjml.EPS*Math.max(A.numCols,A.numRows)*maxSingular;

        // computer the pseudo inverse of A
        for( int i = 0; i < N; i++ ) {
            double s = S[i];
            if( s < tau )
                S[i] = 0;
            else
                S[i] = 1.0/S[i];
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
}
