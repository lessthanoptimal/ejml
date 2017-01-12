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

import static org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64.checkDeclareRows;

/**
 * @author Peter Abeles
 */
public class ImplSparseSparseMult_O64 {

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param w (Optional) Storage for internal work.  null or array of length A.numRows
     * @param x (Optional) Storage for internal work.  null or array of length A.numRows
     */
    public static void mult(SMatrixCC_F64 A, SMatrixCC_F64 B, SMatrixCC_F64 C,
                            int w[], double x[])
    {
        x = checkDeclareRows(A, x);
        w = checkDeclareRows(A, w, true);

        C.length = 0;

        // C(i,j) = sum_k A(i,k) * B(k,j)
        int idx0 = B.col_idx[0];
        for (int bj = 1; bj <= B.numCols; bj++) {
            int colB = bj-1;
            int idx1 = B.col_idx[bj];
            C.col_idx[bj] = C.length;

            if( idx0 == idx1 ) {
                continue;
            }

            // C(:,j) = sum_k A(:,k)*B(k,j)
            for (int bi = idx0; bi < idx1; bi++) {
                int rowB = B.row_idx[bi];
                double valB = B.data[bi];  // B(k,j)  k=rowB j=colB

                multAddColA(A,rowB,valB,C,colB,x,w);
            }

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[colB];
            int idxC1 = C.col_idx[colB+1];

            for (int i = idxC0; i < idxC1; i++) {
                C.data[i] = x[C.row_idx[i]];
            }

            idx0 = idx1;
        }
    }

    /**
     * Performs the performing operation x = x + A(:,i)*alpha
     */
    public static void multAddColA( SMatrixCC_F64 A , int colA ,
                                     double alpha,
                                     SMatrixCC_F64 C, int colC,
                                     double x[] , int w[] ) {
        int mark = colC+1;

        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA+1];

        for (int j = idxA0; j < idxA1; j++) {
            int row = A.row_idx[j];

            if( w[row] < mark ) {
                if( C.length >= C.row_idx.length ) {
                    C.growMaxLength(C.length*2+1,true);
                }

                w[row] = mark;
                C.row_idx[C.length] = row;
                C.col_idx[mark] = ++C.length;
                x[row] = A.data[j]*alpha;
            } else {
                x[row] += A.data[j]*alpha;
            }
        }
    }
}
