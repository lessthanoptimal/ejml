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

import org.ejml.data.FGrowArray;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.FScalar;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.misc.ImplCommonOps_FSCC;

/**
 * Functions used with a sparse QR decomposition
 *
 * @author Peter Abeles
 */
public class QrHelperFunctions_FSCC {

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
    public static void applyHouseholder(FMatrixSparseCSC V , int colV, float beta ,
                                        float []x) {
        int idx0 = V.col_idx[colV];
        int idx1 = V.col_idx[colV+1];

        // Compute tau = v'*x
        float tau = 0;
        for (int p = idx0; p < idx1; p++) {
            tau += V.nz_values[p]*x[V.nz_rows[p]];
        }
        tau *= beta;

        // x = x - v*tau
        for (int p = idx0; p < idx1; p++) {
            x[V.nz_rows[p]] -= V.nz_values[p]*tau;
        }
    }

    /**
     * <p>
     * Performs a rank-1 update operation on the submatrix specified by V with the multiply on the right.<br>
     * <br>
     * C = (I - &gamma;*v*v<sup>T</sup>)*A<br>
     * </p>
     * <p>
     * The order that matrix multiplies are performed has been carefully selected
     * to minimize the number of operations.
     * </p>
     *
     * <p>
     * Before this can become a truly generic operation the submatrix specification needs
     * to be made more generic.
     * </p>
     */
    public static void rank1UpdateMultR(FMatrixSparseCSC V , int colV, float gamma ,
                                        FMatrixSparseCSC A , FMatrixSparseCSC C,
                                        IGrowArray gw , FGrowArray gx )
    {
        if( V.numRows != A.numRows )
            throw new IllegalArgumentException("Number of rows in V and A must match");

        C.nz_length = 0;
        C.numRows = V.numRows;
        C.numCols = 0;

        for (int i = 0; i < A.numCols; i++) {
            float tau = CommonOps_FSCC.dotInnerColumns(V,colV,A,i,gw,gx);
            ImplCommonOps_FSCC.addColAppend(1.0f,A,i,-gamma*tau,V,colV,C,gw);
        }
    }

    /**
     * Creates a householder reflection.
     *
     * (I-gamma*v*v')*x = tau*e1
     *
     * <p>NOTE: Same as cs_house in csparse</p>
     * @param x (Input) Vector x (Output) Vector v. Modified.
     * @param xStart First index in X that is to be processed
     * @param xEnd Last + 1 index in x that is to be processed.
     * @param gamma (Output) Storage for computed gamma
     * @return variable tau
     */
    public static float computeHouseholder(float []x , int xStart , int xEnd , float max , FScalar gamma ) {
        float tau = 0;
        for (int i = xStart; i < xEnd ; i++) {
            float val = x[i] /= max;
            tau += val*val;
        }
        tau = (float)Math.sqrt(tau);
        if( x[xStart] < 0 ) {
            tau = -tau;
        }
        float u_0 = x[xStart] + tau;
        gamma.value = u_0/tau;
        x[xStart] = 1;
        for (int i = xStart+1; i < xEnd ; i++) {
            x[i] /= u_0;
        }

        return -tau*max;
    }
}
