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

package org.ejml.sparse.csc;

import org.ejml.data.DMatrixSparseCSC;

/**
 * @author Peter Abeles
 */
public class NormOps_DSCC {

    public static double fastNormF(DMatrixSparseCSC A ) {
        double total = 0;

        for (int i = 0; i < A.nz_length; i++) {
            double x = A.nz_values[i];
            total += x*x;
        }

        return Math.sqrt(total);
    }

    public static double normF(DMatrixSparseCSC A ) {
        double total = 0;
        double max = CommonOps_DSCC.elementMaxAbs(A);

        for (int i = 0; i < A.nz_length; i++) {
            double x = A.nz_values[i]/max;
            total += x*x;
        }

        return max*Math.sqrt(total);
    }
}
