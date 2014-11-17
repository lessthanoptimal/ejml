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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.data.DenseMatrix64F;


/**
 * This code is inspired from what's in numerical recipes.
 *
 * @author Peter Abeles
 */
public class LUDecompositionNR_D64 extends LUDecompositionBase_D64 {

    private static final double TINY = 1.0e-40;


    /**
     * <p>
     * This implementation of LU Decomposition uses the algorithm specified below:
     *
     * "Numerical Recipes The Art of Scientific Computing", Third Edition, Pages 48-55<br>
     * </p>
     *
     * @param orig The matrix that is to be decomposed.  Not modified.
     * @return true If the matrix can be decomposed and false if it can not.  It can
     * return true and still be singular.
     */
    @Override
    public boolean decompose( DenseMatrix64F orig ) {
//        if( orig.numCols != orig.numRows )
//            throw new RuntimeException("Must be square");
        decomposeCommonInit(orig);

        // loop over the rows to get implicit scaling information
        for( int i = 0; i < m; i++ ) {
            double big = 0.0;
            for( int j = 0; j < n; j++ ) {
                double temp = Math.abs(dataLU[i* n +j]);
                if( big < temp ) big = temp;
            }
            // see if it is singular
            if( big == 0.0 ) big = 1.0;
            vv[i] = 1.0/big;
        }

        // outermost kij loop
        for( int k = 0; k < n; k++ ) {
            int imax=-1;

            // start search by row for largest pivot element
            double big = 0.0;
            for( int i=k; i< m; i++ ) {
                double temp = vv[i]* dataLU[i* n +k];
                if( temp < 0 ) temp = -temp;
                if( temp > big ) {
                    big = temp;
                    imax=i;
                }
            }

            // see if it is singular
            if( imax < 0 ) {
                indx[k] = -1;
                return true;
            } else {
                // check to see if rows need to be interchanged
                if( k != imax ) {
                    int imax_n = imax*n;
                    int k_n = k*n;
                    int end = k_n+n;
                    // j=0:n-1
                    for( ; k_n < end; imax_n++,k_n++) {
                        double temp = dataLU[imax_n];
                        dataLU[imax_n] = dataLU[k_n];
                        dataLU[k_n] = temp;
                    }
                    pivsign = -pivsign;
                    vv[imax] = vv[k];

                    int z = pivot[imax]; pivot[imax] = pivot[k]; pivot[k] = z;
                }

                indx[k] = imax;
                // for some applications it is better to have this set to tiny even though
                // it is singular.  see the book
                double element_kk = dataLU[k* n +k];
                if( element_kk == 0.0) {
                    dataLU[k* n +k] = TINY;
                    element_kk = TINY;
                }

                // the large majority of the processing time is spent in the code below
                for( int i =k+1; i < m; i++ ) {
                    int i_n=i*n;

                    // divide the pivot element
                    double temp = dataLU[i_n +k] /= element_kk;

                    int k_n = k*n + k+1;
                    int end = i_n+n;
                    i_n += k+1;
                    // reduce remaining submatrix
                    // j = k+1:n-1
                    for( ; i_n<end; k_n++,i_n++) {
                        // dataLU[i*n +j] -= temp* dataLU[k* n +j];
                        dataLU[i_n] -= temp* dataLU[k_n];
                    }
                }
            }
        }
        return true;
    }
}