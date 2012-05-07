/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * @author Peter Abeles
 */
public class SmartRotatorUpdate {

    DenseMatrix64F R;
    int mod[] = new int[ 1 ];

    public SmartRotatorUpdate() {
         
    }

    public DenseMatrix64F getR() {
        return R;
    }

    public void init( DenseMatrix64F R ) {
        this.R = R;
        CommonOps.setIdentity(R);

        int a = Math.min(R.numRows,R.numCols);

        if( mod.length < a ) {
            mod = new int[ a ];
        }

        for( int i = 0; i < a; i++ ) {
            mod[i] = i;
        }
    }

    public void update( int rowA , int rowB , double c , double s )
    {
        int l = Math.max( mod[rowA] , mod[rowB] );
        mod[rowA] = l;
        mod[rowB] = l;

        int indexA = rowA*R.numCols;
        int indexB = rowB*R.numCols;

        for( int i = 0; i < l; i++ , indexA++,indexB++) {
            double a = R.data[indexA];
            double b = R.data[indexB];
            R.data[indexA] = c*a + s*b;
            R.data[indexB] = -s*a + c*b;
        }
    }
}
