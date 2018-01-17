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
import org.ejml.data.DMatrix2;
import org.ejml.data.DMatrix2x2;

/**
 * <p>Matrix features for fixed sized matrices which are 2 x 2 or 2 element vectors.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedFeatures</p>
 *
 * @author Peter Abeles
 */
public class MatrixFeatures_DDF2 {
    public static boolean isIdentical(DMatrix2x2 a , DMatrix2x2 b , double tol ) {
        if( !UtilEjml.isIdentical(a.a11,b.a11,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a12,b.a12,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a21,b.a21,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a22,b.a22,tol))
            return false;
        return true;
    }

    public static boolean isIdentical(DMatrix2 a , DMatrix2 b , double tol ) {
        if( !UtilEjml.isIdentical(a.a1,b.a1,tol))
            return false;
        if( !UtilEjml.isIdentical(a.a2,b.a2,tol))
            return false;
        return true;
    }

    public static boolean hasUncountable(DMatrix2x2 a ) {
        if( UtilEjml.isUncountable(a.a11+ a.a12))
            return true;
        if( UtilEjml.isUncountable(a.a21+ a.a22))
            return true;
        return false;
    }

    public static boolean hasUncountable(DMatrix2 a ) {
        if( UtilEjml.isUncountable(a.a1))
            return true;
        if( UtilEjml.isUncountable(a.a2))
            return true;
        return false;
    }

}

