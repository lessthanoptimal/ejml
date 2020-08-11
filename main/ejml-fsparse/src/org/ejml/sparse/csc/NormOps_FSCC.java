/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc;

import org.ejml.data.FMatrixSparseCSC;

/**
 * @author Peter Abeles
 */
public class NormOps_FSCC {

    public static float fastNormF(FMatrixSparseCSC A ) {
        float total = 0;

        for (int i = 0; i < A.nz_length; i++) {
            float x = A.nz_values[i];
            total += x*x;
        }

        return (float)Math.sqrt(total);
    }

    public static float normF(FMatrixSparseCSC A ) {
        float total = 0;
        float max = CommonOps_FSCC.elementMaxAbs(A);

        for (int i = 0; i < A.nz_length; i++) {
            float x = A.nz_values[i]/max;
            total += x*x;
        }

        return max * (float)Math.sqrt(total);
    }
}
