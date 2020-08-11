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

import org.ejml.data.FMatrix2;
import org.ejml.data.FMatrix2x2;

/**
 * <p>Matrix norm related operations for fixed sized matrices of size 2.</p>
 * <p>DO NOT MODIFY.  Automatically generated code created by GenerateFixedNormOps</p>
 *
 * @author Peter Abeles
 */
public class NormOps_FDF2 {
    public static void normalizeF( FMatrix2x2 M ) {
        float val = normF(M);
        CommonOps_FDF2.divide(M,val);
    }

    public static void normalizeF( FMatrix2 M ) {
        float val = normF(M);
        CommonOps_FDF2.divide(M,val);
    }

    public static float fastNormF( FMatrix2x2 M ) {
        float sum = 0;

        sum += M.a11*M.a11 + M.a12*M.a12;
        sum += M.a21*M.a21 + M.a22*M.a22;

        return (float)Math.sqrt(sum);
    }

    public static float fastNormF( FMatrix2 M ) {
        float sum = M.a1*M.a1 + M.a2*M.a2;
        return (float)Math.sqrt(sum);
    }

    public static float normF( FMatrix2x2 M ) {
        float scale = CommonOps_FDF2.elementMaxAbs(M);

        if( scale == 0.0f )
            return 0.0f;

        float a11 = M.a11/scale, a12 = M.a12/scale;
        float a21 = M.a21/scale, a22 = M.a22/scale;

        float sum = 0;
        sum += a11*a11 + a12*a12;
        sum += a21*a21 + a22*a22;

        return scale * (float)Math.sqrt(sum);
    }

    public static float normF( FMatrix2 M ) {
        float scale = CommonOps_FDF2.elementMaxAbs(M);

        if( scale == 0.0f )
            return 0.0f;

        float a1 = M.a1/scale, a2 = M.a2/scale;
        float sum = a1*a1 + a2*a2;

        return scale * (float)Math.sqrt(sum);
    }

}

