/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.ops;

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;

import static org.ejml.ops.CommonOps.multAdd;

/**
 * Generates random vectors based on a zero mean multivariate Gaussian distribution.  The covariance
 * matrix is provided in the constructor.
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
     * @param cov The covariance of the distribution.  Not modified.
     */
    public CovarianceRandomDraw( Random rand , DenseMatrix64F cov )
    {
        r = new DenseMatrix64F(cov.numRows,1);
        CholeskyDecompositionInner_D64 cholesky = new CholeskyDecompositionInner_D64( true);

        if( cholesky.inputModified() )
            cov = cov.copy();
        if( !cholesky.decompose(cov) )
            throw new RuntimeException("Decomposition failed!");

        A = cholesky.getT();
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