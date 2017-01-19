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

import org.ejml.data.DMatrix4;
import org.ejml.data.DMatrix4x4;

/**
 * <p>Matrix norm related operations for fixed sized matrices of size 4.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedNormOps</p>
 *
 * @author Peter Abeles
 */
public class NormOps_DDF4 {
    public static void normalizeF( DMatrix4x4 M ) {
        double val = normF(M);
        CommonOps_DDF4.divide(M,val);
    }

    public static void normalizeF( DMatrix4 M ) {
        double val = normF(M);
        CommonOps_DDF4.divide(M,val);
    }

    public static double fastNormF( DMatrix4x4 M ) {
        double sum = 0;

        sum += M.a11*M.a11 + M.a12*M.a12 + M.a13*M.a13 + M.a14*M.a14;
        sum += M.a21*M.a21 + M.a22*M.a22 + M.a23*M.a23 + M.a24*M.a24;
        sum += M.a31*M.a31 + M.a32*M.a32 + M.a33*M.a33 + M.a34*M.a34;
        sum += M.a41*M.a41 + M.a42*M.a42 + M.a43*M.a43 + M.a44*M.a44;

        return Math.sqrt(sum);
    }

    public static double fastNormF( DMatrix4 M ) {
        double sum = M.a1*M.a1 + M.a2*M.a2 + M.a3*M.a3 + M.a4*M.a4;
        return Math.sqrt(sum);
    }

    public static double normF( DMatrix4x4 M ) {
        double scale = CommonOps_DDF4.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a11 = M.a11/scale, a12 = M.a12/scale, a13 = M.a13/scale, a14 = M.a14/scale;
        double a21 = M.a21/scale, a22 = M.a22/scale, a23 = M.a23/scale, a24 = M.a24/scale;
        double a31 = M.a31/scale, a32 = M.a32/scale, a33 = M.a33/scale, a34 = M.a34/scale;
        double a41 = M.a41/scale, a42 = M.a42/scale, a43 = M.a43/scale, a44 = M.a44/scale;

        double sum = 0;
        sum += a11*a11 + a12*a12 + a13*a13 + a14*a14;
        sum += a21*a21 + a22*a22 + a23*a23 + a24*a24;
        sum += a31*a31 + a32*a32 + a33*a33 + a34*a34;
        sum += a41*a41 + a42*a42 + a43*a43 + a44*a44;

        return scale * Math.sqrt(sum);
    }

    public static double normF( DMatrix4 M ) {
        double scale = CommonOps_DDF4.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a1 = M.a1/scale, a2 = M.a2/scale, a3 = M.a3/scale, a4 = M.a4/scale;
        double sum = a1*a1 + a2*a2 + a3*a3 + a4*a4;

        return scale * Math.sqrt(sum);
    }

}

