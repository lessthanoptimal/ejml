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

package org.ejml.sparse.cmpcol;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCC_F64;
import org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64;

/**
 * @author Peter Abeles
 */
public class CommonOps_O64 {

//    public static void orderRowIndexes( SMatrixCC_F64 a ) {
//        int idx0 = a.col_idx[0];
//        for (int j = 1; j <= a.numCols; j++) {
//            int idx1 = a.col_idx[j];
//
//            Arrays.sort();
//        }
//    }

    /**
     * Perform matrix transpose
     *
     * @param a Input matrix.  Not modified
     * @param a_t Storage for transpose of 'a'.  Must be correct shape.  data length might be adjusted.
     * @param work Optional work matrix.  null or of length a.numRows
     */
    public static void transpose(SMatrixCC_F64 a , SMatrixCC_F64 a_t , int work[] ) {
        if( a_t.numRows != a.numCols || a_t.numCols != a.numRows )
            throw new IllegalArgumentException("Unexpected shape for transpose matrix");

        a_t.growMaxLength(a.length);
        a_t.length = a.length;

        ImplCommonOps_O64.transpose(a, a_t, work);
    }

    public static void mult(SMatrixCC_F64 a , DMatrixRow_F64 b , DMatrixRow_F64 c ) {

    }
}
