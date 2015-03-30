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

/**
 * <p>
 * This implementation of a Cholesky decomposition using the inner-product form.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionInner_CD64 extends CholeskyDecompositionCommon_CD64 {

    // tolerance for testing to see if diagonal elements are real
    double tolerance = UtilEjml.EPS;

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

        int stride = n*2;
        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double realSum = t[i*stride+j*2  ];
                double imagSum = t[i*stride+j*2+1];

                if( i == j ) {
                    // its easy to prove that for the cholesky decomposition to be valid the original
                    // diagonal elements must be real
                    if( Math.abs(imagSum) > tolerance*Math.abs(realSum) )
                        return false;

                    // This takes advantage of the fact that when you multiply a complex number by
                    // its conjigate the result is a real number
                    int end = i*stride+i*2;
                    for( int index = i*stride; index < end; ) {
                        double real = t[index++];
                        double imag = t[index++];

                        realSum -= real*real + imag*imag;
                    }

                    if( realSum <= 0 ) {
                        return false;
                    }

                    real_el_ii = Math.sqrt(realSum);
                    t[i*stride+i*2]   = real_el_ii;
                    t[i*stride+i*2+1] = 0;
                } else {
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

                        // multiply by the complex conjugate of I since the triangle being stored
                        // is the conjugate of L
                        realSum -= realI*realJ + imagI*imagJ;
                        imagSum -= realI*imagJ - realJ*imagI;
                    }

                    // divide the sum by the diagonal element, which is always real
                    // Note that it is storing the conjugate of L
                    t[j*stride+i*2]   = realSum/real_el_ii;
                    t[j*stride+i*2+1] = imagSum/real_el_ii;
                }
            }
        }
        // Make it L instead of the conjugate of L
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                t[i*stride+j*2+1] = -t[i*stride+j*2+1];
            }
        }

        return true;
    }


    @Override
    protected boolean decomposeUpper()
    {
        // See code comments in lower for more details on the algorithm

        if( n == 0 )
            throw new IllegalArgumentException("Cholesky is undefined for 0 by 0 matrix");

        double real_el_ii=0;

        int stride = n*2;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double realSum = t[i*stride+j*2  ];
                double imagSum = t[i*stride+j*2+1];

                if( i == j ) {
                    if( Math.abs(imagSum) > tolerance*Math.abs(realSum) )
                        return false;

                    for (int k = 0; k < i; k++) {
                        double real = t[k*stride+i*2 ];
                        double imag = t[k*stride+i*2+1];

                        realSum -= real*real + imag*imag;
                    }

                    if( realSum <= 0 ) {
                        return false;
                    }

                    real_el_ii = Math.sqrt(realSum);
                    t[i*stride+i*2]   = real_el_ii;
                    t[i*stride+i*2+1] = 0;
                } else {
                    for( int k = 0; k < i; k++ ) {
                        double realI = t[k*stride+i*2  ];
                        double imagI = t[k*stride+i*2+1];

                        double realJ = t[k*stride+j*2  ];
                        double imagJ = t[k*stride+j*2+1];

                        realSum -= realI*realJ + imagI*imagJ;
                        imagSum -= realI*imagJ - realJ*imagI;
                    }

                    t[i*stride+j*2]   = realSum/real_el_ii;
                    t[i*stride+j*2+1] = imagSum/real_el_ii;
                }
            }
        }

        return true;
    }
}
