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

package org.ejml.dense.fixed;

import org.ejml.UtilEjml;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix3x3;

/**
 * <p>Matrix features for fixed sized matrices which are 3 x 3 or 3 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedFeatures</p>
 *
 * @author Peter Abeles
 */
public class MatrixFeatures_FDF3 {
    public static boolean isIdentical(FMatrix3x3 a , FMatrix3x3 b , float tol ) {
        if( !UtilEjml.isIdentical(a.a11,b.a11,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a12,b.a12,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a13,b.a13,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a21,b.a21,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a22,b.a22,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a23,b.a23,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a31,b.a31,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a32,b.a32,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a33,b.a33,tol))
            return false;
        return true;
    }

    public static boolean isIdentical(FMatrix3 a , FMatrix3 b , float tol ) {
        if( !UtilEjml.isIdentical(a.a1,b.a1,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a2,b.a2,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a3,b.a3,tol))
            return false;
        return true;
    }

    public static boolean hasUncountable(FMatrix3x3 a ) {
        if( UtilEjml.isUncountable(a.a11+ a.a12+ a.a13))
            return true;
        if( UtilEjml.isUncountable(a.a21+ a.a22+ a.a23))
            return true;
        if( UtilEjml.isUncountable(a.a31+ a.a32+ a.a33))
            return true;
        return false;
    }

    public static boolean hasUncountable(FMatrix3 a ) {
        if( UtilEjml.isUncountable(a.a1))
            return true;
        if( UtilEjml.isUncountable(a.a2))
            return true;
        if( UtilEjml.isUncountable(a.a3))
            return true;
        return false;
    }

}

