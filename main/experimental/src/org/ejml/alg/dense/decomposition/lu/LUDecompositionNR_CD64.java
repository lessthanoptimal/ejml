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

import org.ejml.alg.dense.decompose.lu.LUDecompositionBase_CD64;
import org.ejml.data.CDenseMatrix64F;


/**
 * This code is inspired from what's in numerical recipes.
 *
 * @author Peter Abeles
 */
public class LUDecompositionNR_CD64 extends LUDecompositionBase_CD64 {

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
    public boolean decompose( CDenseMatrix64F orig ) {
//        if( orig.numCols != orig.numRows )
//            throw new RuntimeException("Must be square");
        decomposeCommonInit(orig);

        // loop over the rows to get implicit scaling information
        for( int i = 0; i < m; i++ ) {
            double big = 0;
            double bigReal = 0.0;
            double bigImg = 0.0;
            for( int j = 0; j < n; j++ ) {
                double real = dataLU[i*stride +j*2];
                double img =  dataLU[i*stride +j*2+1];

                double temp = real*real + img*img;
                if( big < temp ) {
                    big = temp;
                    bigReal = real;
                    bigImg = img;
                }
            }
            // see if it is singular
            if( big == 0.0 ) {
                bigReal = 1.0;
                bigImg = 0.0;
            }

            // vv = 1.0/big
            vv[i*2] = bigReal/(big*big);
            vv[i*2+1] = -bigImg/(big*big);
        }

        // outermost kij loop
        for( int k = 0; k < n; k++ ) {
            int imax=-1;

            // start search by row for largest pivot element
            double big = 0.0;
            for( int i=k; i< m; i++ ) {
                double luReal = dataLU[i*stride + k*2];
                double luImg  = dataLU[i*stride + k*2 + 1];

                double vvReal = vv[i*2];
                double vvImg  = vv[i*2+1];

                // double temp = vv[i*2]* dataLU[i* n +k ];
                double tmpReal = luReal*vvReal - luImg*vvImg;
                double tmpImg  = luImg*vvReal + luReal*vvReal;

                double tmpMag = tmpReal*tmpReal + tmpImg*tmpImg;

                if( tmpMag > big ) {
                    big = tmpMag;
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
                    int imax_n = imax*stride;
                    int k_n = k*stride;
                    int end = k_n+n*2;
                    // j=0:n-1
                    for( ; k_n < end; imax_n+=2,k_n+=2) {
                        double tempReal = dataLU[imax_n];
                        double tempImg  = dataLU[imax_n+1];
                        dataLU[imax_n]   = dataLU[k_n];
                        dataLU[imax_n+1] = dataLU[k_n+1];
                        dataLU[k_n]   = tempReal;
                        dataLU[k_n+1] = tempImg;
                    }
                    pivsign = -pivsign;
                    vv[imax*2] = vv[k*2];
                    vv[imax*2+1] = vv[k*2+1];

                    int z = pivot[imax]; pivot[imax] = pivot[k]; pivot[k] = z;
                }

                indx[k] = imax;
                // for some applications it is better to have this set to tiny even though
                // it is singular.  see the book
                double element_kk_real = dataLU[k*stride +k*2];
                double element_kk_img  = dataLU[k*stride +k*2+1];
                double mag = element_kk_real*element_kk_real + element_kk_img*element_kk_img;
                if( mag == 0.0) {
                    dataLU[k*stride +k*2]   = element_kk_real = TINY;
                    dataLU[k*stride +k*2+1] = element_kk_img  = 0;
                }

                double element_kk_norm2 = element_kk_real*element_kk_real + element_kk_img*element_kk_img;

                // the large majority of the processing time is spent in the code below
                for( int i =k+1; i < m; i++ ) {
                    int i_n=i*stride;

                    // divide the pivot element
                    double luReal = dataLU[i_n + k*2];
                    double luImg = dataLU[i_n + k*2+1];

                    double tmpReal = (luReal*element_kk_real + luImg*element_kk_img)/element_kk_norm2;
                    double tmpImg  = (luImg*element_kk_real - luReal*element_kk_img)/element_kk_norm2;

                    dataLU[i_n + k*2] = tmpReal;
                    dataLU[i_n + k*2+1] = tmpImg;

                    int k_n = k*stride + k*2+2;
                    int end = i_n+n*2;
                    i_n += k*2+2;
                    // reduce remaining submatrix
                    // j = k+1:n-1
                    for( ; i_n<end; k_n+=2,i_n+=2) {
                        // dataLU[i*n +j] -= temp* dataLU[k* n +j];
                        luReal = dataLU[k_n];
                        luImg = dataLU[k_n+1];

                        dataLU[i_n]   -= tmpReal*luReal - tmpImg*luImg;
                        dataLU[i_n+1] -= tmpReal*luImg + tmpImg*luReal;
                    }
                }
            }
        }
        return true;
    }
}