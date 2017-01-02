/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps_D64;


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
        CommonOps_D64.setIdentity(R);

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
