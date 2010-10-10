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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * An LU decomposition algorithm that originally came from Jama.  In general this is faster than
 * what is in NR since it creates a cache of a column, which makes a big difference in larger
 * matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class LUDecompositionAlt extends LUDecompositionBase {

    /**
     * This is a modified version of what was found in the JAMA package.  The order that it
     * performs its permutations in is the primary difference from NR
     *
     * @param a The matrix that is to be decomposed.  Not modified.
     * @return true If the matrix can be decomposed and false if it can not.
     */
    public boolean decompose( DenseMatrix64F a )
    {
        decomposeCommonInit(a);

        DenseMatrix64F LUcolj = vv;

        for( int j = 0; j < n; j++ ) {

            // make a copy of the column to avoid cache jumping issues
            for( int i = 0; i < m; i++) {
                LUcolj.set( i , LU.unsafe_get(i , j ));
            }

            // Apply previous transformations.
            for( int i = 0; i < m; i++ ) {
                int rowIndex = i*n;

                // Most of the time is spent in the following dot product.
                int kmax = i < j ? i : j;
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LU.get( rowIndex+k )*LUcolj.get(k);
                }

                LUcolj.minus( i , s );
                LU.set( rowIndex+j , LUcolj.get(i) );
            }

            // Find pivot and exchange if necessary.
            int p = j;
            double max = Math.abs(LUcolj.get(p));
            for (int i = j+1; i < m; i++) {
                double v = Math.abs(LUcolj.get(i));
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
                int rowP = p*n;
                int rowJ = j*n;
                int endP = rowP+n;
                for (;rowP < endP; rowP++,rowJ++) {
                    double t = LU.get(rowP);
                    LU.set( rowP , LU.get(rowJ) );
                    LU.set( rowJ , t );
                }
                int k = pivot[p]; pivot[p] = pivot[j]; pivot[j] = k;
                pivsign = -pivsign;
            }
            indx[j] = p;

            // Compute multipliers.
            if (j < m ) {
                double lujj = LU.unsafe_get( j, j );
                if( lujj != 0 ) {
                    for (int i = j+1; i < m; i++) {
                        LU.div( i*n+j , lujj );
                    }
                }
            }
        }

        return true;
    }
}
