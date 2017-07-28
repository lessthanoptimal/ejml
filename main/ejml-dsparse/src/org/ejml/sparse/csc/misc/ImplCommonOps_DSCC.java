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

package org.ejml.sparse.csc.misc;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.util.Arrays;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;
import static org.ejml.sparse.csc.mult.ImplSparseSparseMult_DSCC.multAddColA;

/**
 * Implementation class.  Not recommended for direct use.  Instead use {@link CommonOps_DSCC}
 * instead.
 *
 * @author Peter Abeles
 */
public class ImplCommonOps_DSCC {

    /**
     * Performs a matrix transpose.
     *
     * @param A Original matrix.  Not modified.
     * @param C Storage for transposed 'a'.  Reshaped.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     */
    public static void transpose(DMatrixSparseCSC A , DMatrixSparseCSC C , IGrowArray gw ) {
        int []work = adjust(gw,A.numRows,A.numRows);
        C.reshape(A.numCols,A.numRows,A.nz_length);

        // compute the histogram for each row in 'a'
        int idx0 = A.col_idx[0];
        for (int j = 1; j <= A.numCols; j++) {
            int idx1 = A.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                if( A.nz_rows.length <= i)
                    throw new RuntimeException("Egads");
                work[A.nz_rows[i]]++;
            }
            idx0 = idx1;
        }

        // construct col_idx in the transposed matrix
        C.colsum(work);

        // fill in the row indexes
        idx0 = A.col_idx[0];
        for (int j = 1; j <= A.numCols; j++) {
            int col = j-1;
            int idx1 = A.col_idx[j];
            for (int i = idx0; i < idx1; i++) {
                int row = A.nz_rows[i];
                int index = work[row]++;
                C.nz_rows[index] = col;
                C.nz_values[index] = A.nz_values[i];
            }
            idx0 = idx1;
        }
    }

    /**
     * Performs matrix addition:<br>
     * C = &alpha;A + &beta;B
     *
     * @param alpha scalar value multiplied against A
     * @param A Matrix
     * @param beta scalar value multiplied against B
     * @param B Matrix
     * @param C Output matrix.
     * @param gw (Optional) Storage for internal workspace.  Can be null.
     * @param gx (Optional) Storage for internal workspace.  Can be null.
     */
    public static void add(double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B, DMatrixSparseCSC C,
                                IGrowArray gw, DGrowArray gx)
    {
        double []x = adjust(gx,A.numRows);
        int []w = adjust(gw,A.numRows,A.numRows);

        C.indicesSorted = false;
        C.nz_length = 0;

        for (int col = 0; col < A.numCols; col++) {
            C.col_idx[col] = C.nz_length;

            multAddColA(A,col,alpha,C,col+1,x,w);
            multAddColA(B,col,beta,C,col+1,x,w);

            // take the values in the dense vector 'x' and put them into 'C'
            int idxC0 = C.col_idx[col];
            int idxC1 = C.col_idx[col+1];

            for (int i = idxC0; i < idxC1; i++) {
                C.nz_values[i] = x[C.nz_rows[i]];
            }
        }
    }

    /**
     * Adds the results of adding a column in A and B as a new column in C.<br>
     * C(:,end+1) = &alpha;*A(:,colA) + &beta;*B(:,colB)
     *
     * @param alpha scalar
     * @param A matrix
     * @param colA column in A
     * @param beta scalar
     * @param B matrix
     * @param colB column in B
     * @param C Column in C
     * @param gw workspace
     */
    public static void addColAppend(double alpha, DMatrixSparseCSC A, int colA, double beta, DMatrixSparseCSC B, int colB,
                                    DMatrixSparseCSC C, IGrowArray gw)
    {
        if( A.numRows != B.numRows || A.numRows != C.numRows)
            throw new IllegalArgumentException("Number of rows in A, B, and C do not match");

        int idxA0 = A.col_idx[colA];
        int idxA1 = A.col_idx[colA+1];
        int idxB0 = B.col_idx[colB];
        int idxB1 = B.col_idx[colB+1];

        C.growMaxColumns(++C.numCols,true);
        C.growMaxLength(C.nz_length+idxA1-idxA0+idxB1-idxB0,true);

        int []w = adjust(gw,A.numRows);
        Arrays.fill(w,0,A.numRows,-1);

        for (int i = idxA0; i < idxA1; i++) {
            int row = A.nz_rows[i];
            C.nz_rows[C.nz_length] = row;
            C.nz_values[C.nz_length] = alpha*A.nz_values[i];
            w[row] = C.nz_length++;
        }

        for (int i = idxB0; i < idxB1; i++) {
            int row = B.nz_rows[i];
            if( w[row] != -1 ) {
                C.nz_values[w[row]] += beta*B.nz_values[i];
            } else {
                C.nz_values[C.nz_length] = beta*B.nz_values[i];
                C.nz_rows[C.nz_length++] = row;
            }
        }
        C.col_idx[C.numCols] = C.nz_length;
    }
}
