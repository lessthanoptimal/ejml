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

import org.ejml.data.DMatrix6;
import org.ejml.data.DMatrix6x6;

/**
 * <p>Matrix norm related operations for fixed sized matrices of size 6.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedNormOps</p>
 *
 * @author Peter Abeles
 */
public class NormOps_DDF6 {
    public static void normalizeF( DMatrix6x6 M ) {
        double val = normF(M);
        CommonOps_DDF6.divide(M,val);
    }

    public static void normalizeF( DMatrix6 M ) {
        double val = normF(M);
        CommonOps_DDF6.divide(M,val);
    }

    public static double fastNormF( DMatrix6x6 M ) {
        double sum = 0;

        sum += M.a11*M.a11 + M.a12*M.a12 + M.a13*M.a13 + M.a14*M.a14 + M.a15*M.a15 + M.a16*M.a16;
        sum += M.a21*M.a21 + M.a22*M.a22 + M.a23*M.a23 + M.a24*M.a24 + M.a25*M.a25 + M.a26*M.a26;
        sum += M.a31*M.a31 + M.a32*M.a32 + M.a33*M.a33 + M.a34*M.a34 + M.a35*M.a35 + M.a36*M.a36;
        sum += M.a41*M.a41 + M.a42*M.a42 + M.a43*M.a43 + M.a44*M.a44 + M.a45*M.a45 + M.a46*M.a46;
        sum += M.a51*M.a51 + M.a52*M.a52 + M.a53*M.a53 + M.a54*M.a54 + M.a55*M.a55 + M.a56*M.a56;
        sum += M.a61*M.a61 + M.a62*M.a62 + M.a63*M.a63 + M.a64*M.a64 + M.a65*M.a65 + M.a66*M.a66;

        return Math.sqrt(sum);
    }

    public static double fastNormF( DMatrix6 M ) {
        double sum = M.a1*M.a1 + M.a2*M.a2 + M.a3*M.a3 + M.a4*M.a4 + M.a5*M.a5 + M.a6*M.a6;
        return Math.sqrt(sum);
    }

    public static double normF( DMatrix6x6 M ) {
        double scale = CommonOps_DDF6.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a11 = M.a11/scale, a12 = M.a12/scale, a13 = M.a13/scale, a14 = M.a14/scale, a15 = M.a15/scale, a16 = M.a16/scale;
        double a21 = M.a21/scale, a22 = M.a22/scale, a23 = M.a23/scale, a24 = M.a24/scale, a25 = M.a25/scale, a26 = M.a26/scale;
        double a31 = M.a31/scale, a32 = M.a32/scale, a33 = M.a33/scale, a34 = M.a34/scale, a35 = M.a35/scale, a36 = M.a36/scale;
        double a41 = M.a41/scale, a42 = M.a42/scale, a43 = M.a43/scale, a44 = M.a44/scale, a45 = M.a45/scale, a46 = M.a46/scale;
        double a51 = M.a51/scale, a52 = M.a52/scale, a53 = M.a53/scale, a54 = M.a54/scale, a55 = M.a55/scale, a56 = M.a56/scale;
        double a61 = M.a61/scale, a62 = M.a62/scale, a63 = M.a63/scale, a64 = M.a64/scale, a65 = M.a65/scale, a66 = M.a66/scale;

        double sum = 0;
        sum += a11*a11 + a12*a12 + a13*a13 + a14*a14 + a15*a15 + a16*a16;
        sum += a21*a21 + a22*a22 + a23*a23 + a24*a24 + a25*a25 + a26*a26;
        sum += a31*a31 + a32*a32 + a33*a33 + a34*a34 + a35*a35 + a36*a36;
        sum += a41*a41 + a42*a42 + a43*a43 + a44*a44 + a45*a45 + a46*a46;
        sum += a51*a51 + a52*a52 + a53*a53 + a54*a54 + a55*a55 + a56*a56;
        sum += a61*a61 + a62*a62 + a63*a63 + a64*a64 + a65*a65 + a66*a66;

        return scale * Math.sqrt(sum);
    }

    public static double normF( DMatrix6 M ) {
        double scale = CommonOps_DDF6.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a1 = M.a1/scale, a2 = M.a2/scale, a3 = M.a3/scale, a4 = M.a4/scale, a5 = M.a5/scale, a6 = M.a6/scale;
        double sum = a1*a1 + a2*a2 + a3*a3 + a4*a4 + a5*a5 + a6*a6;

        return scale * Math.sqrt(sum);
    }

}

