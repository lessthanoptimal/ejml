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

import static org.ejml.sparse.cmpcol.misc.ImplCommonOps_O64.checkDeclare;

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

    /**
     * <p>Determines which elements in 'X' will be non-zero when the system below is solved for.</p>
     * G*X = B
     *
     * <p>xi will contain a list of ordered row indexes in B which will be modified starting at xi[top] to xi[n-1].  top
     * is the value returned by this function.</p>
     *
     * <p>See cs_reach in dsparse library to understand the algorithm.  This code follow the spirit but not
     * the details because of differences in the contract.</p>
     *
     * @param G (Input) Lower triangular system matrix.  Diagonal elements are assumed to be not zero.  Not modified.
     * @param B (Input) Matrix B. Not modified.
     * @param colB Column in B being solved for
     * @param xi (Output) List of row indices in B which are non-zero in graph order.  Must have length B.numRows
     * @param w (Optional) Work space array used internally.  Must be of size B.numRows*2 or null
     * @return Returns the index of the first element in the xi list.  Also known as top.
     */
    public static int searchNzRowsInB( SMatrixCmpC_F64 G , SMatrixCmpC_F64 B , int colB, int xi[] , int w[])
    {
        if( xi.length < B.numRows )
            throw new IllegalArgumentException("xi must be at least this long: "+B.numRows);
        w = checkDeclare(B.numRows*2,w,B.numRows);

        // use 'w' as a marker to know which rows in B have been examined.  0 = unexamined and 1 = examined
        int idx0 = B.col_idx[colB];
        int idx1 = B.col_idx[colB+1];

        int top = G.numRows;
        for (int i = idx0; i < idx1; i++) {
            int rowB = B.nz_rows[i];

            if( w[rowB] == 0 ) {
                top = searchNzRowsInB_DFS(rowB,G,top,xi,w);
            }
        }

        return top;
    }

    /**
     * Given the first row in B it performs a DFS seeing which elements in 'B' will be not zero.  A row=i in 'B' will
     * be not zero if any element in row=(j < i) in G is not zero
     */
    private static int searchNzRowsInB_DFS( int rowB , SMatrixCmpC_F64 G , int top , int xi[], int w[] )
    {
        int N = G.numRows;
        int head = 0; // put the selected row into the FILO stack
        xi[head] = rowB; // use the head of xi to store where the stack it's searching.  The tail is where
                         // the graph ordered list of rows in B is stored.
        while( head >= 0 ) {
            // the column in G being examined
            int G_col = xi[head];

            if( w[G_col] == 0) {
                w[G_col] = 1;
                w[N+head] = G.col_idx[G_col]; // mark which child in the loop below it's examining
            }

            // See if there are any children which have yet to be examined
            boolean done = true;

            int idx0 = w[N+head];
            int idx1 = G.col_idx[G_col+1];

            for (int j = idx0; j < idx1; j++) {
                int jrow = G.nz_rows[j];
                if( w[jrow] == 0 ) {
                    w[N+head] = j+1; // mark that it has processed up to this point
                    xi[++head] = jrow;
                    done = false;
                    break;          // It's a DFS so break and continue down
                }
            }

            if( done ) {
                head--;
                xi[--top] = G_col;
            }

        }
        return top;
    }


}
