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

package org.ejml.sparse.cmpcol.mult;

import org.ejml.data.SMatrixCC_F64;

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class ImplSparseSparseMult_O64 {

    public static void mult( SMatrixCC_F64 A , SMatrixCC_F64 B , SMatrixCC_F64 C , double x[] )
    {
        if( x == null )
            x = new double[A.numRows];
        else if( x.length < A.numRows )
            throw new IllegalArgumentException("x needs to at least be as long as A.numRows");

        C.length = 0;

        // C(i,j) = sum_k A(i,k) * B(j,k)
        int idx0 = B.col_idx[0];
        for (int bj = 1; bj <= B.numCols; bj++) {
            int colB = bj-1;
            int idx1 = B.col_idx[bj];

            // C(:,j) = sum_k A(:,k)*B(j,k)
            Arrays.fill(x,0,A.numRows,0);
            for (int bi = idx0; bi < idx1; bi++) {
                int rowB = B.row_idx[bi];
                double valB = B.data[bi];  // B(j,k)  j=rowB k=colB

                multAddColA(A,colB,valB,C,rowB,x);
            }

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[colB];
            int idxC1 = C.col_idx[bj];

            for (int i = idxC0; i < idxC1; i++) {
                C.data[i] = x[C.row_idx[i]];
            }

            idx0 = idx1;
        }
    }

    public static void multAddColA( SMatrixCC_F64 A , int colA ,
                                    double beta,
                                    SMatrixCC_F64 C, int colC,
                                    double x[] ) {
        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA+1];

        int idxC0 = C.col_idx[colC];

        for (int j = idxA0; j < idxA1; j++) {
            int jc = idxC0 + j-idxA0;
            int row = A.row_idx[j];

            if( jc >= C.length ) {
                C.length = jc+1;
                C.row_idx[jc] = row;
            }

            x[row] += A.data[j]*beta;
        }
    }
}
