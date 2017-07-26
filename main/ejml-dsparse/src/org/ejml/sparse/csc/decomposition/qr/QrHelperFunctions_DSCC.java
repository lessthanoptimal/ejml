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
import org.ejml.data.IGrowArray;

import static org.ejml.sparse.csc.misc.TriangularSolver_DSCC.adjust;

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

    /**
     * Non-zero counts of Householder vectors and computes a permutation
     * matrix that ensures diagonal entires are allstructurally nonzero.
     *
     * <p>NOTE: This is part of cs_vcounts() in csparse</p>
     *
     * @param A (Input) Matrix QR is to be applied to
     * @param gwork
     */
    public static void countsOfV(DMatrixSparseCSC A , int []countsv,
                                 int parent[], int leftmost[] , IGrowArray gwork )
    {
        int m = A.numRows, n = A.numCols;
        int[] w = adjust(gwork,m+3*n);

        if( countsv.length < A.numCols)
            throw new IllegalArgumentException("countsv.length must be "+A.numCols+" or greater");

        // start location of arrays
        int next = 0, head = m, tail = m + n, nque = m + 2*n;

        int pinv[] = new int[m+n];// todo output
        int lnz,m2;

        for (int k = 0; k < n; k++) {
            w[head+k] = -1; w[tail+k] = -1;  w[nque+k] = 0;
        }

        // scan rows in reverse order
        for (int i = m-1; i >= 0; i--) {
            pinv[i] = -1;
            int k = leftmost[i];
            if( k == -1 )
                continue;
            if( w[nque+k]++ == 0 )
                w[tail+k] = i;
            w[next+i] = w[head+k];
            w[head+k] = i;
        }
        lnz = 0;
        m2 = m;

        for (int k = 0; k < n; k++) {
            int i = w[head+k];        // remove row i from queue k
            lnz++;                    // count V(k,k) as nonzero
            if( i < 0)                // add a fictitious row
                i = m2++;
            pinv[i] = k;              // associate row i with V(:,k)
            if( --w[nque+k] <= 0 )
                continue;
            lnz += w[nque+k];
            int pa;
            if( (pa = parent[k]) != -1 ) { // move all rows to parent of k
                if( w[nque+pa] == 0)
                    w[tail+pa] = w[tail+k];
                w[next+w[tail+k]] = w[head+pa];
                w[head+pa] = w[next+i];
                w[nque+pa] += w[nque+k];
            }
        }
        for (int i = 0, k = n; i < m; i++) {
            if( pinv[i] < 0 )
                pinv[i] = k++;
        }
    }

    /**
     * Computes leftmost[i] =  min(find(A[i,:))
     *
     * <p>NOTE: This is part of cs_vcounts() in csparse</p>
     *
     * @param A (input) matrix being decomposed by QR
     * @param leftmost (output) storage for left most elements
     */
    public static void findMinElementIndexInRows(DMatrixSparseCSC A, int leftmost[] )
    {
        int m = A.numRows, n = A.numCols;
        for (int i = 0; i < m; i++) { // todo use fill instead
            leftmost[i] = -1;
        }

        // leftmost[i] = min(find(A(i,:)))
        for (int k = n-1; k >= 0; k--) {
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k+1];

            for( int p = idx0; p < idx1; p++ ) {
                leftmost[A.nz_rows[p]] = k;
            }
        }
    }
}
