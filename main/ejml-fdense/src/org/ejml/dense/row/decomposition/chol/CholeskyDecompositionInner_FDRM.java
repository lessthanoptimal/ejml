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

package org.ejml.dense.row.decomposition.chol;

/**
 * <p>
 * This implementation of a Cholesky decomposition using the inner-product form.
 * For large matrices a block implementation is better.  On larger matrices the lower triangular
 * decomposition is significantly faster.  This is faster on smaller matrices than {@link CholeskyDecompositionBlock_FDRM}
 * but much slower on larger matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionInner_FDRM extends CholeskyDecompositionCommon_FDRM {

    public CholeskyDecompositionInner_FDRM() {
        super(true);
    }

    public CholeskyDecompositionInner_FDRM(boolean lower) {
        super(lower);
    }

    @Override
    protected boolean decomposeLower() {
        float el_ii;
        float div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                float sum = t[i*n+j];

                int iEl = i*n;
                int jEl = j*n;
                int end = iEl+i;
                // k = 0:i-1
                for( ; iEl<end; iEl++,jEl++ ) {
//                    sum -= el[i*n+k]*el[j*n+k];
                    sum -= t[iEl]* t[jEl];
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0f )
                        return false;

                    el_ii = (float)Math.sqrt(sum);
                    t[i*n+i] = el_ii;
                    div_el_ii = 1.0f/el_ii;
                } else {
                    t[j*n+i] = sum*div_el_ii;
                }
            }
        }

        // zero the top right corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = i+1; j < n; j++ ) {
                t[i*n+j] = 0.0f;
            }
        }

        return true;
    }

    @Override
    protected boolean decomposeUpper() {
        float el_ii;
        float div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                float sum = t[i*n+j];

                for( int k = 0; k < i; k++ ) {
                    sum -= t[k*n+i]* t[k*n+j];
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0f )
                        return false;

                    // I suspect that the sqrt is slowing this down relative to MTJ
                    el_ii = (float)Math.sqrt(sum);
                    t[i*n+i] = el_ii;
                    div_el_ii = 1.0f/el_ii;
                } else {
                    t[i*n+j] = sum*div_el_ii;
                }
            }
        }
        // zero the lower left corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < i; j++ ) {
                t[i*n+j] = 0.0f;
            }
        }

        return true;
    }
}
