/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.decompose.chol;

import org.ejml.UtilEjml;

/**
 * <p>
 * This implementation of a Cholesky decomposition using the inner-product form.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionInner_CDRM extends CholeskyDecompositionCommon_CDRM {

    // tolerance for testing to see if diagonal elements are real
    float tolerance = UtilEjml.F_EPS;

    public CholeskyDecompositionInner_CDRM() {
        super(true);
    }

    public CholeskyDecompositionInner_CDRM(boolean lower) {
        super(lower);
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    protected boolean decomposeLower() {
        if( n == 0 )
            throw new IllegalArgumentException("Cholesky is undefined for 0 by 0 matrix");

        float real_el_ii=0;

        int stride = n*2;
        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                float realSum = t[i*stride+j*2  ];
                float imagSum = t[i*stride+j*2+1];

                if( i == j ) {
                    // its easy to prove that for the cholesky decomposition to be valid the original
                    // diagonal elements must be real
                    if( Math.abs(imagSum) > tolerance*Math.abs(realSum) )
                        return false;

                    // This takes advantage of the fact that when you multiply a complex number by
                    // its conjigate the result is a real number
                    int end = i*stride+i*2;
                    for( int index = i*stride; index < end; ) {
                        float real = t[index++];
                        float imag = t[index++];

                        realSum -= real*real + imag*imag;
                    }

                    if( realSum <= 0 ) {
                        return false;
                    }

                    real_el_ii = (float)Math.sqrt(realSum);
                    t[i*stride+i*2]   = real_el_ii;
                    t[i*stride+i*2+1] = 0;
                } else {
                    int iEl = i*stride; // row i is inside the lower triangle
                    int jEl = j*stride; // row j conjugate transposed upper triangle
                    int end = iEl+i*2;
                    // k = 0:i-1
                    for( ; iEl<end; ) {
//                    sum -= el[i*n+k]*el[j*n+k];
                        float realI = t[iEl++];
                        float imagI = t[iEl++];

                        float realJ = t[jEl++];
                        float imagJ = t[jEl++];

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

        float real_el_ii=0;

        int stride = n*2;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                float realSum = t[i*stride+j*2  ];
                float imagSum = t[i*stride+j*2+1];

                if( i == j ) {
                    if( Math.abs(imagSum) > tolerance*Math.abs(realSum) )
                        return false;

                    for (int k = 0; k < i; k++) {
                        float real = t[k*stride+i*2 ];
                        float imag = t[k*stride+i*2+1];

                        realSum -= real*real + imag*imag;
                    }

                    if( realSum <= 0 ) {
                        return false;
                    }

                    real_el_ii = (float)Math.sqrt(realSum);
                    t[i*stride+i*2]   = real_el_ii;
                    t[i*stride+i*2+1] = 0;
                } else {
                    for( int k = 0; k < i; k++ ) {
                        float realI = t[k*stride+i*2  ];
                        float imagI = t[k*stride+i*2+1];

                        float realJ = t[k*stride+j*2  ];
                        float imagJ = t[k*stride+j*2+1];

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
