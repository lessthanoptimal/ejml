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

package org.ejml.ops;

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;

import static org.ejml.ops.CommonOps.multAdd;

/**
 * Generates random vectors based on a zero mean multivariate Gaussian distribution.  The covariance
 * matrix is provided in the contructor.
 */
public class CovarianceRandomDraw {
    private DenseMatrix64F A;
    private Random rand;
    private DenseMatrix64F r;

    /**
     * Creates a random distribution with the specified mean and covariance.  The references
     * to the variables are not saved, their value are copied.
     *
     * @param rand Used to create the random numbers for the draw. Reference is saved.
     * @param cov The covariance of the stribution.  Not modified.
     */
    public CovarianceRandomDraw( Random rand , DenseMatrix64F cov )
    {
        r = new DenseMatrix64F(cov.numRows,1);
        CholeskyDecompositionInner choleky = new CholeskyDecompositionInner( false,true);

        choleky.decompose(cov);

        A = choleky.getT();
        this.rand = rand;
    }

    /**
     * Makes a draw on the distribution.  The results are added to parameter 'x'
     */
    public void next( DenseMatrix64F x )
    {
        for( int i = 0; i < r.numRows; i++ ) {
            r.set(i,0,rand.nextGaussian());
        }

        multAdd(A,r,x);
    }

    /**
     * Computes the likelihood of the random draw
     *
     * @return The likelihood.
     */
    public double computeLikelihoodP() {
        double ret = 1.0;

        for( int i = 0; i < r.numRows; i++ ) {
            double a = r.get(i,0);

            ret *= Math.exp(-a*a/2.0);
        }

        return ret;
    }
}