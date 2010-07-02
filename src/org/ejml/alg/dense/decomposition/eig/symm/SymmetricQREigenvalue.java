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


/**
 * <p>
 * Computes the eigenvalues of a symmetric tridiagonal matrix using the symmetric QR algorithm.  To
 * compute the eigenvalues of a general real symmetric matrix this needs to be used in conjunction
 * with {@link org.ejml.alg.dense.decomposition.hessenberg.TridiagonalSimilarDecomposition}.
 * </p>
 * <p>
 * The symmetry is taken advantage of to minimize memory and cache misses by storing the
 * matrix in two arrays along the diagonals.  Rotators are used to compute the eigenvalues.
 * </p>
 * <p>
 * This implementation is based on the algorithm is sketched out in:<br>
 * David S. Watkins, "Fundamentals of Matrix Computations," Second Edition. page 377-385
 * </p>
 * @author Peter Abeles
 */
public class SymmetricQREigenvalue {

    private SymmetricQREigen helper;

    private int exceptionalThresh = 15;
    private int maxIterations = exceptionalThresh*4;

    // should it ever analytically compute eigenvalues
    // if this is true then it can't compute eigenvalues at the same time
    private boolean fastEigenvalues = true;


    public SymmetricQREigenvalue(SymmetricQREigen helper ) {
        this.helper = helper;
    }

    /**
     * Creates a new SymmetricQREigenvalue class that declares its own SymmetricQREigen.
     */
    public SymmetricQREigenvalue() {
        this.helper = new SymmetricQREigen();
    }

    public void setFastEigenvalues(boolean fastEigenvalues) {
        this.fastEigenvalues = fastEigenvalues;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
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
     * @param T A tridiagonal matrix.  Not modified.
     * @return true if it successeds and false if it fails.
     */
    public boolean process( DenseMatrix64F T ) {
        // if T is null then assume init was called outside of this function
        if( T != null )
            helper.init(T);
        // TODO Clean up.  make similar to SVD

        while( helper.x2 >= 0 ) {
            // if it has cycled too many times give up
            if( helper.steps > maxIterations ) {
                return false;
            }

            if( helper.x1 == helper.x2 ) {
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
                helper.exceptionalShift();
            } else {
                performStep();
            }
            helper.incrementSteps();
//            helper.printMatrix();
        }

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

        // similar transforms
        helper.performImplicitSingleStep(helper.diag[helper.x2],false);
    }

    public double[] getValues() {
        return helper.diag;
    }
}