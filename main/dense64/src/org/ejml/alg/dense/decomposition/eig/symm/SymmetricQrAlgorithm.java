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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Computes the eigenvalues and eigenvectors of a symmetric tridiagonal matrix using the symmetric QR algorithm.
 * </p>
 * <p>
 * This implementation is based on the algorithm is sketched out in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. page 377-385
 * </p>
 * @author Peter Abeles
 */
public class SymmetricQrAlgorithm {

    // performs many of the low level calculations
    private SymmetricQREigenHelper helper;

    // transpose of the orthogonal matrix
    private DenseMatrix64F Q;

    // the eigenvalues previously computed
    private double eigenvalues[];

    private int exceptionalThresh = 15;
    private int maxIterations = exceptionalThresh*15;

    // should it ever analytically compute eigenvalues
    // if this is true then it can't compute eigenvalues at the same time
    private boolean fastEigenvalues;

    // is it following a script or not
    private boolean followingScript;

    public SymmetricQrAlgorithm(SymmetricQREigenHelper helper ) {
        this.helper = helper;
    }

    /**
     * Creates a new SymmetricQREigenvalue class that declares its own SymmetricQREigenHelper.
     */
    public SymmetricQrAlgorithm() {
        this.helper = new SymmetricQREigenHelper();
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public DenseMatrix64F getQ() {
        return Q;
    }

    public void setQ(DenseMatrix64F q) {
        Q = q;
    }

    public void setFastEigenvalues(boolean fastEigenvalues) {
        this.fastEigenvalues = fastEigenvalues;
    }

    /**
     * Returns the eigenvalue at the specified index.
     *
     * @param index Which eigenvalue.
     * @return The eigenvalue.
     */
    public double getEigenvalue( int index ) {
       return helper.diag[index];
    }

    /**
     * Returns the number of eigenvalues available.
     *
     * @return How many eigenvalues there are.
     */
    public int getNumberOfEigenvalues() {
        return helper.N;
    }

    /**
     * Computes the eigenvalue of the provided tridiagonal matrix.  Note that only the upper portion
     * needs to be tridiagonal.  The bottom diagonal is assumed to be the same as the top.
     *
     * @param sideLength Number of rows and columns in the input matrix.
     * @param diag Diagonal elements from tridiagonal matrix. Modified.
     * @param off Off diagonal elements from tridiagonal matrix. Modified.
     * @return true if it succeeds and false if it fails.
     */
    public boolean process( int sideLength,
                            double diag[] ,
                            double off[] ,
                            double eigenvalues[] ) {
        if( diag != null )
            helper.init(diag,off,sideLength);
        if( Q == null )
            Q = CommonOps.identity(helper.N);
        helper.setQ(Q);

        this.followingScript = true;
        this.eigenvalues = eigenvalues;
        this.fastEigenvalues = false;

        return _process();
    }

    public boolean process( int sideLength,
                            double diag[] ,
                            double off[] ) {
        if( diag != null )
            helper.init(diag,off,sideLength);

        this.followingScript = false;
        this.eigenvalues = null;

        return _process();
    }


    private boolean _process() {
        while( helper.x2 >= 0 ) {
            // if it has cycled too many times give up
            if( helper.steps > maxIterations ) {
                return false;
            }

            if( helper.x1 == helper.x2 ) {
//                System.out.println("Steps = "+helper.steps);
                // see if it is done processing this submatrix
                helper.resetSteps();
                if( !helper.nextSplit() )
                    break;
            } else if( fastEigenvalues && helper.x2-helper.x1 == 1 ) {
                // There are analytical solutions to this case. Just compute them directly.
                // TODO might be able to speed this up by doing the 3 by 3 case also
                helper.resetSteps();
                helper.eigenvalue2by2(helper.x1);
                helper.setSubmatrix(helper.x2,helper.x2);
            } else if( helper.steps-helper.lastExceptional > exceptionalThresh ){
                // it isn't a good sign if exceptional shifts are being done here
                helper.exceptionalShift();
            } else {
                performStep();
            }
            helper.incrementSteps();
//            helper.printMatrix();
        }

//        helper.printMatrix();
        return true;
    }

    /**
     * First looks for zeros and then performs the implicit single step in the QR Algorithm.
     */
    public void performStep() {
        // check for zeros
        for( int i = helper.x2-1; i >= helper.x1; i-- ) {
            if( helper.isZero(i) ) {
                helper.splits[helper.numSplits++] = i;
                helper.x1 = i+1;
                return;
            }
        }

        double lambda;

        if( followingScript ) {
            if( helper.steps > 10 ) {
                followingScript = false;
                return;
            } else {
                // Using the true eigenvalues will in general lead to the fastest convergence
                // typically takes 1 or 2 steps
                lambda = eigenvalues[helper.x2];
            }
        } else {
            // the current eigenvalue isn't working so try something else
            lambda = helper.computeShift();
        }

        // similar transforms
        helper.performImplicitSingleStep(lambda,false);
    }
}