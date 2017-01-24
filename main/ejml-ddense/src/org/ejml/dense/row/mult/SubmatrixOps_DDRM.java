/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.data.DMatrix1Row;


/**
 * Operations that are performed on a submatrix inside a larger matrix.
 *
 * @author Peter Abeles
 */
public class SubmatrixOps_DDRM {

    public static void setSubMatrix(DMatrix1Row src , DMatrix1Row dst ,
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
