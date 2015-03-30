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

package org.ejml.alg.dense.decompose.chol;

import org.ejml.UtilEjml;
import org.ejml.data.Complex64F;

/**
 * <p>
 * This implementation of a Cholesky decomposition using the inner-product form.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionInner_CD64 extends CholeskyDecompositionCommon_CD64 {

    // tolerance for it being SPD
    double tolerance = UtilEjml.EPS;

    // storage for the square root of a number
    protected double realRoot;
    protected double imagRoot;

    public CholeskyDecompositionInner_CD64() {
        super(true);
    }

    public CholeskyDecompositionInner_CD64(boolean lower) {
        super(lower);
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    protected boolean decomposeLower() {
        if( n == 0 )
            throw new IllegalArgumentException("Cholesky is undefined for 0 by 0 matrix");

        double real_el_ii=0;
        double imag_el_ii=0;
        double norm2_ii=0;

        sqrt(t[0],t[1]);
        double pivmax = Math.sqrt(realRoot*realRoot + imagRoot*imagRoot);

        int stride = n*2;
        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double realSum = t[i*stride+j*2];
                double imagSum = t[i*stride+j*2+1];

                int iEl = i*stride; // row i is inside the lower triangle
                int jEl = j*stride; // row j conjugate transposed upper triangle
                int end = iEl+i*2;
                // k = 0:i-1
                for( ; iEl<end; ) {
//                    sum -= el[i*n+k]*el[j*n+k];
                    double realI = t[iEl++];
                    double imagI = t[iEl++];

                    double realJ = t[jEl++];
                    double imagJ = t[jEl++];

                    // multiply by the complex conjugate of I  TODO look at this more carefully
                    realSum -= realI*realJ + imagI*imagJ;
                    imagSum -= realI*imagJ - realJ*imagI;
                }

                if( i == j ) {
                    sqrt(realSum,imagSum);
                    real_el_ii = realRoot;
                    imag_el_ii = imagRoot;
                    norm2_ii = real_el_ii*real_el_ii + imag_el_ii*imag_el_ii;

                    double norm_ii = Math.sqrt(norm2_ii);

                    // is it positive-definite?
                    if( norm_ii/pivmax <= tolerance )
                        return false;

                    if( norm_ii > pivmax )
                        pivmax = norm_ii;

                    t[i*stride+i*2]   = real_el_ii;
                    t[i*stride+i*2+1] = imag_el_ii;

                } else {
                    // divide the sum by the conjugate of the diagonal element
                    t[j*stride+i*2]   = (realSum*real_el_ii - imagSum*imag_el_ii)/norm2_ii;
                    t[j*stride+i*2+1] = (imagSum*real_el_ii + realSum*imag_el_ii)/norm2_ii;
                }
            }
        }

        // zero the top right corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = i+1; j < n; j++ ) {
                t[i*stride+j*2] = 0.0;
                t[i*stride+j*2+1] = 0.0;
            }
        }

        return true;
    }

    @Override
    protected boolean decomposeUpper() {
        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = t[i*n+j];

                for( int k = 0; k < i; k++ ) {
                    sum -= t[k*n+i]* t[k*n+j];
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0 )
                        return false;

                    // I suspect that the sqrt is slowing this down relative to MTJ
                    el_ii = Math.sqrt(sum);
                    t[i*n+i] = el_ii;
                    div_el_ii = 1.0/el_ii;
                } else {
                    t[i*n+j] = sum*div_el_ii;
                }
            }
        }
        // zero the lower left corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < i; j++ ) {
                t[i*n+j] = 0.0;
            }
        }

        return true;
    }

    /**
     * Computes the square root of the input complex number
     */
    protected void sqrt( double real , double imag ) {
        double r = Math.sqrt(real*real + imag*imag);

        realRoot = Math.sqrt((r+real)/2.0);
        imagRoot = Math.sqrt((r-real)/2.0);
        if( imagRoot < 0 )
            imagRoot = -imagRoot;
    }

    @Override
    public Complex64F computeDeterminant() {
        throw new RuntimeException("IMplement");
    }
}
