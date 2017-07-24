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

package org.ejml.sparse.csc.mult;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;

/**
 * @author Peter Abeles
 */
public class ImplSparseSparseMult_DSCC {

    /**
     * Performs matrix multiplication.  C = A*B
     *
     * @param A Matrix
     * @param B Matrix
     * @param C Storage for results.  Data length is increased if increased if insufficient.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void mult(DMatrixSparseCSC A, DMatrixSparseCSC B, DMatrixSparseCSC C,
                            IGrowArray gw, DGrowArray gx )
    {
        double []x = adjust(gx, A.numRows);
        int []w = adjust(gw, A.numRows, A.numRows);

        C.indicesSorted = false;
        C.nz_length = 0;

        // C(i,j) = sum_k A(i,k) * B(k,j)
        int idx0 = B.col_idx[0];
        for (int bj = 1; bj <= B.numCols; bj++) {
            int colB = bj-1;
            int idx1 = B.col_idx[bj];
            C.col_idx[bj] = C.nz_length;

            if( idx0 == idx1 ) {
                continue;
            }

            // C(:,j) = sum_k A(:,k)*B(k,j)
            for (int bi = idx0; bi < idx1; bi++) {
                int rowB = B.nz_rows[bi];
                double valB = B.nz_values[bi];  // B(k,j)  k=rowB j=colB

                multAddColA(A,rowB,valB,C,colB,x,w);
            }

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[colB];
            int idxC1 = C.col_idx[colB+1];

            for (int i = idxC0; i < idxC1; i++) {
                C.nz_values[i] = x[C.nz_rows[i]];
            }

            idx0 = idx1;
        }

    }


    /**
     * Performs the performing operation x = x + A(:,i)*alpha
     */
    public static void multAddColA(DMatrixSparseCSC A , int colA ,
                                   double alpha,
                                   DMatrixSparseCSC C, int colC,
                                   double x[] , int w[] ) {
        int mark = colC+1;

        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA+1];

        for (int j = idxA0; j < idxA1; j++) {
            int row = A.nz_rows[j];

            if( w[row] < mark ) {
                if( C.nz_length >= C.nz_rows.length ) {
                    C.growMaxLength(C.nz_length *2+1,true);
                }

                w[row] = mark;
                C.nz_rows[C.nz_length] = row;
                C.col_idx[mark] = ++C.nz_length;
                x[row] = A.nz_values[j]*alpha;
            } else {
                x[row] += A.nz_values[j]*alpha;
            }
        }
    }

    public static void mult(DMatrixSparseCSC A , DMatrixRMaj B , DMatrixRMaj C ) {

        C.zero();

        // C(i,j) = sum_k A(i,k) * B(k,j)
        for (int k = 0; k < A.numCols; k++) {
            int idx0 = A.col_idx[k  ];
            int idx1 = A.col_idx[k+1];

            for (int indexA = idx0; indexA < idx1; indexA++) {
                int i = A.nz_rows[indexA];
                double valueA = A.nz_values[indexA];

                int indexB = k*B.numCols;
                int indexC = i*C.numCols;
                int end = indexB + B.numCols;

//                for (int j = 0; j < B.numCols; j++) {
                while (indexB < end ) {
                    C.data[indexC++] += valueA*B.data[indexB++];
                }
            }
        }
    }
}
