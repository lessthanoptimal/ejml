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

package org.ejml.alg.fixed;

import org.ejml.data.FixedMatrix2_F64;
import org.ejml.data.FixedMatrix2x2_F64;

/**
 * <p>Matrix norm related operations for fixed sized matrices of size 2.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedNormOps</p>
 *
 * @author Peter Abeles
 */
public class FixedNormOps2_D64 {
    public static void normalizeF( FixedMatrix2x2_F64 M ) {
        double val = normF(M);
        FixedOps2_D64.divide(M,val);
    }

    public static void normalizeF( FixedMatrix2_F64 M ) {
        double val = normF(M);
        FixedOps2_D64.divide(M,val);
    }

    public static double fastNormF( FixedMatrix2x2_F64 M ) {
        double sum = 0;

        sum += M.a11*M.a11 + M.a12*M.a12;
        sum += M.a21*M.a21 + M.a22*M.a22;

        return Math.sqrt(sum);
    }

    public static double fastNormF( FixedMatrix2_F64 M ) {
        double sum = M.a1*M.a1 + M.a2*M.a2;
        return Math.sqrt(sum);
    }

    public static double normF( FixedMatrix2x2_F64 M ) {
        double scale = FixedOps2_D64.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a11 = M.a11/scale, a12 = M.a12/scale;
        double a21 = M.a21/scale, a22 = M.a22/scale;

        double sum = 0;
        sum += a11*a11 + a12*a12;
        sum += a21*a21 + a22*a22;

        return scale * Math.sqrt(sum);
    }

    public static double normF( FixedMatrix2_F64 M ) {
        double scale = FixedOps2_D64.elementMaxAbs(M);

        if( scale == 0.0 )
            return 0.0;

        double a1 = M.a1/scale, a2 = M.a2/scale;
        double sum = a1*a1 + a2*a2;

        return scale * Math.sqrt(sum);
    }

}

