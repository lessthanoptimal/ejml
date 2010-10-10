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

package org.ejml.alg.dense.decomposition.chol;

/**
 * <p>
 * This implementation of a Cholesky decomposition using the inner-product form.
 * For large matrices a block implementation is better.  On larger matrices the lower triangular
 * decomposition is significantly faster.  This is faster on smaller matrices than {@link CholeskyDecompositionBlock}
 * but much slower on larger matices.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionInner extends CholeskyDecompositionCommon {

    public CholeskyDecompositionInner() {
        super(false,true);
    }

    public CholeskyDecompositionInner( boolean decomposeOrig, boolean lower) {
        super(decomposeOrig, lower);
    }

    @Override
    protected boolean decomposeLower() {
        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = T.unsafe_get( i , j );

                int iEl = i*n;
                int jEl = j*n;
                int end = iEl+i;
                // k = 0:i-1
                for( ; iEl<end; iEl++,jEl++ ) {
//                    sum -= el[i*n+k]*el[j*n+k];
                    sum -= T.get(iEl)* T.get(jEl);
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0 )
                        return false;

                    el_ii = Math.sqrt(sum);
                    T.unsafe_set( i , i , el_ii );
                    div_el_ii = 1.0/el_ii;
                } else {
                    T.unsafe_set( j , i , sum*div_el_ii );
                }
            }
        }
        // zero the top right corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = i+1; j < n; j++ ) {
                T.unsafe_set( i , j , 0.0 );
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
                double sum = T.unsafe_get( i , j );

                for( int k = 0; k < i; k++ ) {
                    sum -= T.unsafe_get( k , i )* T.unsafe_get( k , j );
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0 )
                        return false;

                    // I suspect that the sqrt is slowing this down relative to MTJ
                    el_ii = Math.sqrt(sum);
                    T.unsafe_set( i , i , el_ii );
                    div_el_ii = 1.0/el_ii;
                } else {
                    T.unsafe_set( i , j , sum*div_el_ii );
                }
            }
        }
        // zero the lower left corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < i; j++ ) {
                T.unsafe_set( i  , j , 0.0 );
            }
        }

        return true;
    }
}
