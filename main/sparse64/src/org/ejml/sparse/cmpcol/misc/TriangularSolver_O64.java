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

package org.ejml.sparse.cmpcol.misc;

import org.ejml.data.SMatrixCmpC_F64;

/**
 * @author Peter Abeles
 */
public class TriangularSolver_O64 {

    /**
     * Solves for a lower triangular matrix against a dense matrix. L*x = b
     *
     * @param L Lower triangular matrix.  Diagonal elements are assumed to be non-zero
     * @param x (Input) Solution matrix 'b'.  (Output) matrix 'x'
     */
    public static void solveL(SMatrixCmpC_F64 L , double []x )
    {
        final int N = L.numCols;

        int idx0 = L.col_idx[0];
        for (int col = 0; col < N; col++) {

            int idx1 = L.col_idx[col+1];
            double x_j = x[col] /= L.nz_values[idx0];

            for (int i = idx0+1; i < idx1; i++) {
                int row = L.nz_rows[i];
                x[row] -=  L.nz_values[i]*x_j;
            }

            idx0 = idx1;
        }
    }

    /**
     * Solves for an upper triangular matrix against a dense vector. L*x = b
     *
     * @param U Upper triangular matrix.  Diagonal elements are assumed to be non-zero
     * @param x (Input) Solution matrix 'b'.  (Output) matrix 'x'
     */
    public static void solveU(SMatrixCmpC_F64 U , double []x )
    {
        final int N = U.numCols;

        int idx1 = U.col_idx[N];
        for (int col = N-1; col >= 0; col--) {
            int idx0 = U.col_idx[col];
            double x_j = x[col] /= U.nz_values[idx1-1];

            for (int i = idx0; i < idx1-1; i++) {
                int row = U.nz_rows[i];
                x[row] -=  U.nz_values[i]*x_j;
            }

            idx1 = idx0;
        }
    }

    // TODO modify algorithm so that G does not get modified
    // TODO pick a better name
    public static void reach( SMatrixCmpC_F64 G , SMatrixCmpC_F64 B , int k, int xi[] )
    {

    }

    static void reach_dfs( int j , SMatrixCmpC_F64 G , int top , int x[], int pstack[] )
    {

    }
}
