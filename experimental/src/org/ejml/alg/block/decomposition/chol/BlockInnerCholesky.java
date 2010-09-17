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

package org.ejml.alg.block.decomposition.chol;

import org.ejml.data.D1Submatrix64F;


/**
 * Performs a cholesky decomposition on an individual inner block.
 *
 *  @author Peter Abeles
 */
// TODO add lower
public class BlockInnerCholesky {

    public static boolean upper( D1Submatrix64F T )
    {
        int n = T.row1-T.row0;
        int indexT = T.row0* T.original.numCols + T.col0*n;

        return upper(T.original.data,indexT,n);
    }

    public static boolean upper( double[]T , int indexT , int n ) {
        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = T[ indexT + i*n+j];

                for( int k = 0; k < i; k++ ) {
                    // todo is this optimal?  its traversing by row
                    // todo not sure if this will work if its only the upper portion
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
}
