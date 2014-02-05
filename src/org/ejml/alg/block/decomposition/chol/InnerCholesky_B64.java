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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.data.D1Submatrix64F;


/**
 * Performs a cholesky decomposition on an individual inner block.
 *
 *  @author Peter Abeles
 */
// TODO merge with CholeskyBlockHelper
public class InnerCholesky_B64 {

    public static boolean upper( D1Submatrix64F T )
    {
        int n = T.row1-T.row0;
        int indexT = T.row0* T.original.numCols + T.col0*n;

        return upper(T.original.data,indexT,n);
    }

    public static boolean lower( D1Submatrix64F T )
    {
        int n = T.row1-T.row0;
        int indexT = T.row0* T.original.numCols + T.col0*n;

        return lower(T.original.data,indexT,n);
    }

    /**
     * Performs an inline upper Cholesky decomposition on an inner row-major matrix.  Only
     * the upper triangular portion of the matrix is read or written to.
     *
     * @param T  Array containing an inner row-major matrix.  Modified.
     * @param indexT First index of the inner row-major matrix.
     * @param n Number of rows and columns of the matrix.
     * @return If the decomposition succeeded.
     */
    public static boolean upper( double[]T , int indexT , int n ) {
        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = T[ indexT + i*n+j];

                // todo optimize
                for( int k = 0; k < i; k++ ) {
                    sum -= T[ indexT + k*n+i] * T[ indexT + k*n+j];
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0 )
                        return false;

                    el_ii = Math.sqrt(sum);
                    T[ indexT + i*n+i] = el_ii;
                    div_el_ii = 1.0/el_ii;
                } else {
                    T[ indexT + i*n+j] = sum*div_el_ii;
                }
            }
        }

        return true;
    }


    /**
     * Performs an inline lower Cholesky decomposition on an inner row-major matrix.  Only
     * the lower triangular portion of the matrix is read or written to.
     *
     * @param T  Array containing an inner row-major matrix.  Modified.
     * @param indexT First index of the inner row-major matrix.
     * @param n Number of rows and columns of the matrix.
     * @return If the decomposition succeeded.
     */
    public static boolean lower( double[]T , int indexT , int n ) {
        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = T[ indexT + j*n+i];

                // todo optimize
                for( int k = 0; k < i; k++ ) {
                    sum -= T[ indexT + i*n+k] * T[ indexT + j*n+k];
                }

                if( i == j ) {
                    // is it positive-definite?
                    if( sum <= 0.0 )
                        return false;

                    el_ii = Math.sqrt(sum);
                    T[ indexT + i*n+i] = el_ii;
                    div_el_ii = 1.0/el_ii;
                } else {
                    T[ indexT + j*n+i] = sum*div_el_ii;
                }
            }
        }

        return true;
    }
}
