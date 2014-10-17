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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.data.CDenseMatrix64F;


/**
 * <p>
 * An LU decomposition algorithm that originally came from Jama.  In general this is faster than
 * what is in NR since it creates a cache of a column, which makes a big difference in larger
 * matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class LUDecompositionAlt_CD64 extends LUDecompositionBase_CD64 {

    /**
     * This is a modified version of what was found in the JAMA package.  The order that it
     * performs its permutations in is the primary difference from NR
     *
     * @param a The matrix that is to be decomposed.  Not modified.
     * @return true If the matrix can be decomposed and false if it can not.
     */
    public boolean decompose( CDenseMatrix64F a )
    {
        decomposeCommonInit(a);

        double LUcolj[] = vv;

        for( int j = 0; j < n; j++ ) {

            // make a copy of the column to avoid cache jumping issues
            for( int i = 0; i < m; i++) {
                LUcolj[i*2] = dataLU[i*stride + j*2];
                LUcolj[i*2+1] = dataLU[i*stride + j*2+1];
            }

            // Apply previous transformations.
            for( int i = 0; i < m; i++ ) {
                int rowIndex = i*stride;

                // Most of the time is spent in the following dot product.
                int kmax = i < j ? i : j;
                double realS = 0.0;
                double imgS = 0.0;

                for (int k = 0; k < kmax; k++) {
                    double realD = dataLU[rowIndex+k*2];
                    double imgD  = dataLU[rowIndex+k*2+1];

                    double realCol = LUcolj[k*2];
                    double imgCol  = LUcolj[k*2+1];

                    realS += realD*realCol - imgD*imgCol;
                    imgS += realD*imgCol + imgD*realCol;
                }

                dataLU[rowIndex+j*2]   = LUcolj[i*2]   -= realS;
                dataLU[rowIndex+j*2+1] = LUcolj[i*2+1] -= imgS;
            }

            // Find pivot and exchange if necessary.
            int p = j;
            double max = mag(LUcolj, p * 2);
            for (int i = j+1; i < m; i++) {
                double v = mag(LUcolj, i * 2);
                if ( v > max) {
                    p = i;
                    max = v;
                }
            }

            if (p != j) {
                // swap the rows
//                for (int k = 0; k < n; k++) {
//                    double t = dataLU[p*n + k];
//                    dataLU[p*n + k] = dataLU[j*n + k];
//                    dataLU[j*n + k] = t;
//                }
                int rowP = p*stride;
                int rowJ = j*stride;
                int endP = rowP+stride;
                for (;rowP < endP; rowP++,rowJ++) {
                    double t = dataLU[rowP];
                    dataLU[rowP]   = dataLU[rowJ];
                    dataLU[rowJ]   = t;
                }
                int k = pivot[p]; pivot[p] = pivot[j]; pivot[j] = k;
                pivsign = -pivsign;
            }
            indx[j] = p;

            // Compute multipliers.
            if (j < m ) {
                double realLujj = dataLU[j*stride+j*2];
                double imgLujj  = dataLU[j*stride+j*2+1];

                double magLujj = realLujj*realLujj + imgLujj*imgLujj;

                if( realLujj != 0 || imgLujj != 0) {
                    for (int i = j+1; i < m; i++) {

                        double realLU = dataLU[i*stride+j*2];
                        double imagLU = dataLU[i*stride+j*2+1];

                        dataLU[i*stride+j*2]   = (realLU*realLujj + imagLU*imgLujj)/magLujj;
                        dataLU[i*stride+j*2+1] = (imagLU*realLujj - realLU*imgLujj)/magLujj;
                    }
                }
            }
        }

        return true;
    }

    private static double mag( double d[] , int index ) {
        double r = d[index];
        double i = d[index+1];
        return r*r + i*i;
    }
}
