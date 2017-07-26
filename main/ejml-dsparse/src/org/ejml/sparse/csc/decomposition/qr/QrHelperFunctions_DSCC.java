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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.data.DMatrixSparseCSC;

/**
 * Functions used with a sparse QR decomposition
 *
 * @author Peter Abeles
 */
public class QrHelperFunctions_DSCC {

    /**
     * <p>Applies a sparse Householder vector to a dense vector.</p>
     * <pre>
     *     x = x - v*(beta*(v'*x))</pre>
     *
     * <P>NOTE: This is the same as cs_happly() in csparse</P>
     *
     * @param V (Input) Matrix containing the Householder
     * @param colV Column in V with the Householder vector
     * @param beta scalar
     * @param x (Input and Output) vector that the Householder is applied to. Modified.
     */
    public static void applyHouseholder(DMatrixSparseCSC V , int colV, double beta ,
                                        double []x) {
        int idx0 = V.col_idx[colV];
        int idx1 = V.col_idx[colV+1];

        // Compute tau = v'*x
        double tau = 0;
        for (int p = idx0; p < idx1; p++) {
            tau += V.nz_values[p]*x[V.nz_rows[p]];
        }
        tau *= beta;

        // x = x - v*tau
        for (int p = idx0; p < idx1; p++) {
            x[V.nz_rows[p]] -= V.nz_values[p]*tau;
        }
    }
}
