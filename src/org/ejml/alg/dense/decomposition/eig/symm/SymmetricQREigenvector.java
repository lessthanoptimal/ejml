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

package org.ejml.alg.dense.decomposition.eig.symm;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * Computes the eigenvectors of a symmetric tridiagonal matrix using the symmetric QR algorithm.
 * </p>
 * <p>
 * This implementation is based on the algorithm is sketched out in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. page 377-385
 * </p>
 * @author Peter Abeles
 */
public class SymmetricQREigenvector {

    // performs many of the low level calculations
    private SymmetricQREigen helper;

    // transpose of the orthogonal matrix
    private DenseMatrix64F Q;

    // the eigenvalues previously computed
    private double eigenvalues[];
    // which eigenvalue is currently being used
    private int whichEigen;

    private int exceptionalThresh = 15;
    private int maxIterations = exceptionalThresh*3;

    public SymmetricQREigenvector(SymmetricQREigen helper ) {
        this.helper = helper;
    }

    /**
     * Creates a new SymmetricQREigenvalue class that declares its own SymmetricQREigen.
     */
    public SymmetricQREigenvector() {
        this.helper = new SymmetricQREigen();
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

    /**
     * Computes the eigenvalue of the provided tridiagonal matrix.  Note that only the upper portion
     * needs to be tridiagonal.  The bottom diagonal is assumed to be the same as the top.
     *
     * @param T A tridiagonal matrix.  Not modified.
     * @return true if it succeeds and false if it fails.
     */
    public boolean process( DenseMatrix64F T , double eigenvalues[] ) {
        if( T != null )
            helper.init(T);
        if( Q == null )
            Q = CommonOps.identity(helper.N);
        helper.setQ(Q);

        this.eigenvalues = eigenvalues;
        whichEigen = helper.N-1;

        while( whichEigen >= 0 ) {
            // if it has cycled too many times give up
            if( helper.steps > maxIterations ) {
                return false;
            }

            if( helper.x1 == helper.x2 ) {
//                System.out.println("Steps = "+helper.steps);
                // see if it is done processing this submatrix
                helper.resetSteps();
                whichEigen--;
                if( !helper.nextSplit() )
                    break;
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

        if( helper.steps > 10 ) {
            // the current eigenvalue isn't working so try something else
            lambda = helper.diag[helper.x2];
        } else {
            // Using the true eigenvalues will in general lead to the fastest convergence
            // typically takes 1 or 2 steps
            lambda = eigenvalues[whichEigen];
        }

        // similar transforms
        helper.performImplicitSingleStep(lambda,false);
    }
}