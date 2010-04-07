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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;


/**
 * Operations that are performed on a submatrix inside a larger matrix.
 *
 * @author Peter Abeles
 */
public class SubmatrixOps {

    public static void setSubMatrix( DenseMatrix64F src , DenseMatrix64F dst ,
                                     int srcRow , int srcCol , int dstRow , int dstCol ,
                                     int numSubRows, int numSubCols )
    {
        for( int i = 0; i < numSubRows; i++ ) {
            for( int j = 0; j < numSubCols; j++ ) {
                double val = src.get(i+srcRow,j+srcCol);
                dst.set(i+dstRow,j+dstCol,val);
            }
        }
    }
}
