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

import org.ejml.UtilEjml;
import org.ejml.data.SMatrixCmpC_F64;

/**
 * @author Peter Abeles
 */
public class MatrixFeatures_O64 {

    public static boolean isEquals(SMatrixCmpC_F64 a , SMatrixCmpC_F64 b ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.length; i++) {
            if( a.nz_values[i] != b.nz_values[i] || a.nz_rows[i] != b.nz_rows[i])
                return false;
        }
        return true;
    }

    public static boolean isEquals(SMatrixCmpC_F64 a , SMatrixCmpC_F64 b , double tol ) {
        if( !isSameShape(a,b) )
            return false;

        for (int i = 0; i < a.length; i++) {
            if( Math.abs(a.nz_values[i]-b.nz_values[i]) > tol )
                return false;
        }
        return true;
    }

    public static boolean isSameShape(SMatrixCmpC_F64 a , SMatrixCmpC_F64 b) {
        if( a.numRows == b.numRows && a.numCols == b.numCols && a.length == b.length ) {
            for (int i = 0; i <= a.numCols; i++) {
                if( a.col_idx[i] != b.col_idx[i] )
                    return false;
            }
            for (int i = 0; i < a.length; i++) {
                if( a.nz_rows[i] != b.nz_rows[i] )
                    return false;
            }
            return true;
        }
        return false;
    }

    public static boolean hasUncountable( SMatrixCmpC_F64 A ) {
        for( int i = 0; i < A.length; i++ ) {
            if(UtilEjml.isUncountable(A.nz_values[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZeros(SMatrixCmpC_F64 A , double tol ) {
        for( int i = 0; i < A.length; i++ ) {
            if(Math.abs(A.nz_values[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIdentity(SMatrixCmpC_F64 A , double tol ) {
        if( A.numCols != A.numRows )
            return false;

        if( A.length != A.numCols )
            return false;

        for( int i = 1; i <= A.numCols; i++ ) {
            if( A.col_idx[i] != i)
                return false;
            if( Math.abs(A.nz_values[i-1]-1) > tol )
                return false;
        }
        return true;
    }
}
