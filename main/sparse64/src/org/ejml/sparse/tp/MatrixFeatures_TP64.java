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

package org.ejml.sparse.tp;

import org.ejml.data.SMatrixTriplet_F64;

/**
 * @author Peter Abeles
 */
public class MatrixFeatures_TP64 {

    public static boolean isEquals(SMatrixTriplet_F64 a , SMatrixTriplet_F64 b ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.length; i++) {
            SMatrixTriplet_F64.Element ea = a.data[i];
            SMatrixTriplet_F64.Element eb = b.findItem(ea.row, ea.col);

            if( eb == null || ea.value != eb.value )
                return false;
        }
        return true;
    }

    public static boolean isEquals(SMatrixTriplet_F64 a , SMatrixTriplet_F64 b , double tol ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.length; i++) {
            SMatrixTriplet_F64.Element ea = a.data[i];
            SMatrixTriplet_F64.Element eb = b.findItem(ea.row, ea.col);

            if( eb == null || Math.abs(ea.value-eb.value) > tol )
                return false;
        }
        return true;
    }

    public static boolean isSameShape(SMatrixTriplet_F64 a , SMatrixTriplet_F64 b) {
        return a.numRows == b.numRows && a.numCols == b.numCols && a.length == b.length;
    }
}
