/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.triplet;

import org.ejml.data.FMatrixSparseTriplet;

/**
 * @author Peter Abeles
 */
public class MatrixFeatures_FSTL {

    public static boolean isEquals(FMatrixSparseTriplet a , FMatrixSparseTriplet b ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            int arow = a.nz_rowcol.data[i*2];
            int acol = a.nz_rowcol.data[i*2+1];
            float avalue = a.nz_value.data[i];

            int bindex = b.nz_index(arow, acol);
            if( bindex < 0 )
                return false;

            float bvalue = b.nz_value.data[bindex];

            if( avalue != bvalue )
                return false;
        }
        return true;
    }

    public static boolean isEquals(FMatrixSparseTriplet a , FMatrixSparseTriplet b , float tol ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.nz_length; i++) {
            int arow = a.nz_rowcol.data[i*2];
            int acol = a.nz_rowcol.data[i*2+1];
            float avalue = a.nz_value.data[i];

            int bindex = b.nz_index(arow, acol);
            if( bindex < 0 )
                return false;

            float bvalue = b.nz_value.data[bindex];

            if( Math.abs(avalue-bvalue) > tol )
                return false;
        }
        return true;
    }

    public static boolean isSameShape(FMatrixSparseTriplet a , FMatrixSparseTriplet b) {
        return a.numRows == b.numRows && a.numCols == b.numCols && a.nz_length == b.nz_length;
    }
}
