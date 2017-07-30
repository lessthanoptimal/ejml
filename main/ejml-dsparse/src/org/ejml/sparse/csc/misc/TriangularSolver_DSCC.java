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

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class TriangularSolver_DSCC {

    /**
     * Solves for a lower triangular matrix against a dense matrix. L*x = b
     *
     * @param L Lower triangular matrix.  Diagonal elements are assumed to be non-zero
     * @param x (Input) Solution matrix 'b'.  (Output) matrix 'x'
     */
    public static void solveL(DMatrixSparseCSC L , double []x )
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
     * Solves for the transpose of a lower triangular matrix against a dense matrix. L<sup>T</sup>*x = b
     *
     * @param L Lower triangular matrix.  Diagonal elements are assumed to be non-zero
     * @param x (Input) Solution matrix 'b'.  (Output) matrix 'x'
     */
    public static void solveTranL(DMatrixSparseCSC L , double []x )
    {
        final int N = L.numCols;

        for (int j = N-1; j >= 0; j--) {
            int idx0 = L.col_idx[j];
            int idx1 = L.col_idx[j+1];

            for (int p = idx0+1; p < idx1; p++) {
                x[j] -= L.nz_values[p]*x[L.nz_rows[p]];
            }
            x[j] /= L.nz_values[idx0];
        }
    }

    /**
     * Solves for an upper triangular matrix against a dense vector. U*x = b
     *
     * @param U Upper triangular matrix.  Diagonal elements are assumed to be non-zero
     * @param x (Input) Solution matrix 'b'.  (Output) matrix 'x'
     */
    public static void solveU(DMatrixSparseCSC U , double []x )
    {
        final int N = U.numCols;

        int idx1 = U.col_idx[N];
        for (int col = N-1; col >= 0; col--) {
            int idx0 = U.col_idx[col];
            double x_j = x[col] /= U.nz_values[idx1-1];

            for (int i = idx0; i < idx1 - 1; i++) {
                int row = U.nz_rows[i];
                x[row] -= U.nz_values[i] * x_j;
            }

            idx1 = idx0;
        }
    }

    /**
     * Computes the solution to the triangular system.
     *
     * @param G     (Input) Lower or upper triangular matrix.  diagonal elements must be non-zero.  Not modified.
     * @param lower true for lower triangular and false for upper
     * @param B     (Input) Matrix.  Not modified.
     * @param X     (Output) Solution
     * @param g_x   (Optional) Storage for workspace.
     * @param g_xi  (Optional) Storage for workspace.
     * @param g_w   (Optional) Storage for workspace.
     */
    public static void solve(DMatrixSparseCSC G, boolean lower,
                             DMatrixSparseCSC B, DMatrixSparseCSC X,
                             DGrowArray g_x, IGrowArray g_xi, IGrowArray g_w)
    {
        double[] x = adjust(g_x,G.numRows);
        if( g_xi == null ) g_xi = new IGrowArray();
        int[] xi = adjust(g_xi,G.numRows);

        X.nz_length = 0;
        X.col_idx[0] = 0;
        X.indicesSorted = false;

        for (int colB = 0; colB < B.numCols; colB++) {
            int top = solve(G,lower,B,colB, x, null,g_xi, g_w);

            int nz_count = X.numRows-top;
            if( X.nz_values.length < X.nz_length + nz_count) {
                X.growMaxLength(X.nz_length*2 + nz_count,true);
            }

            for (int p = top; p < X.numRows; p++,X.nz_length++) {
                X.nz_rows[X.nz_length] = xi[p];
                X.nz_values[X.nz_length] = x[xi[p]];
            }
            X.col_idx[colB+1] = X.nz_length;
        }
    }

    /**
     * Computes the solution to a triangular system with (optional) pivots.  Only a single column in B is solved for. Diagonals
     * in G are assumed to filled in and either the first or last entry for lower or upper triangle, respectively.
     *
     * @param G     (Input) Lower or upper triangular matrix.  diagonal elements must be non-zero and last
     *              or first entry in a column.  Not modified.
     * @param lower true for lower triangular and false for upper
     * @param B     (Input) Matrix.  Not modified.
     * @param colB  The column in B which is solved for
     * @param x     (Output) Storage for dense solution.  length = G.numRows
     * @param pinv  (Input, Optional) Permutation vector. Maps col j to G. Null if no pivots.
     * @param g_xi  (Optional) Storage for workspace.
     * @param g_w   (Optional) Storage for workspace.
     * @return Return number of zeros in 'x', ignoring cancellations.
     */
    public static int solve(DMatrixSparseCSC G, boolean lower,
                            DMatrixSparseCSC B, int colB, double x[], int pinv[], IGrowArray g_xi, IGrowArray g_w) {
        int[] xi = adjust(g_xi,G.numRows);
        int top = searchNzRowsInB(G, B, colB, pinv, xi, g_w);

        // sparse clear of x
        for( int p = top; p < G.numRows; p++ )
            x[xi[p]] = 0;

        // copy B into X
        int idxB0 = B.col_idx[colB];
        int idxB1 = B.col_idx[colB+1];
        for( int p = idxB0; p < idxB1; p++ )
            x[B.nz_rows[p]] = B.nz_values[p];

        for (int px = top; px < G.numRows; px++) {
            int j = xi[px];
            int J = pinv != null ? pinv[j] : j;
            if( J < 0 )
                continue;
            int p,q;
            if( lower ) {
                x[j] /= G.nz_values[G.col_idx[J]];
                p = G.col_idx[J]+1;
                q = G.col_idx[J+1];
            } else {
                x[j] /= G.nz_values[G.col_idx[J+1]-1];
                p = G.col_idx[J];
                q = G.col_idx[J+1]-1;
            }
            for(;p<q;p++) {
                x[G.nz_rows[p]] -= G.nz_values[p]*x[j];
            }
        }

        return top;
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
     * @param G    (Input) Lower triangular system matrix.  Diagonal elements are assumed to be not zero.  Not modified.
     * @param B    (Input) Matrix B. Not modified.
     * @param colB Column in B being solved for
     * @param pinv (Input, Optional) Column pivots in G. Null if no pivots.
     * @param xi   (Output) List of row indices in B which are non-zero in graph order.  Must have length B.numRows
     * @param gwork workspace array used internally. Can be null.
     * @return Returns the index of the first element in the xi list.  Also known as top.
     */
    public static int searchNzRowsInB(DMatrixSparseCSC G, DMatrixSparseCSC B, int colB, int pinv[],
                                      int xi[], IGrowArray gwork) {
        if (xi.length < B.numRows)
            throw new IllegalArgumentException("xi must be at least this long: " + B.numRows);

        // this is a change from csparse. CSparse marks an entry by modifying G then reverting it. This can cause
        // weird unexplained behavior when people start using threads...
        int[] w = adjust(gwork,B.numRows * 2,B.numRows);

        // use 'w' as a marker to know which rows in B have been examined.  0 = unexamined and 1 = examined
        int idx0 = B.col_idx[colB];
        int idx1 = B.col_idx[colB+1];

        int top = G.numRows;
        for (int i = idx0; i < idx1; i++) {
            int rowB = B.nz_rows[i];

            if( w[rowB] == 0 ) {
                top = searchNzRowsInB_DFS(rowB,G,top,pinv,xi,w);
            }
        }

        return top;
    }

    /**
     * Given the first row in B it performs a DFS seeing which elements in 'B' will be not zero.  A row=i in 'B' will
     * be not zero if any element in row=(j < i) in G is not zero
     */
    private static int searchNzRowsInB_DFS(int rowB , DMatrixSparseCSC G , int top , int pinv[], int xi[], int w[] )
    {
        int N = G.numRows;
        int head = 0; // put the selected row into the FILO stack
        xi[head] = rowB; // use the head of xi to store where the stack it's searching.  The tail is where
                         // the graph ordered list of rows in B is stored.
        while( head >= 0 ) {
            // the column in G being examined
            int G_col = xi[head];
            int G_col_new = pinv != null ? pinv[G_col] : G_col;
            if( w[G_col] == 0) {
                w[G_col] = 1;
                // mark which child in the loop below it's examining
                w[N+head] = G_col_new < 0 ? 0 : G.col_idx[G_col_new];
            }

            // See if there are any children which have yet to be examined
            boolean done = true;

            int idx0 = w[N+head];
            int idx1 = G_col_new < 0 ? 0 : G.col_idx[G_col_new+1];

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

    /**
     * <p>If ata=false then it computes the elimination tree for sparse lower triangular square matrix
     * generated from Cholesky decomposition. If ata=true then it computes the elimination tree of
     * A<sup>T</sup>A without forming A<sup>T</sup>A explicitly. In an elimination tree the parent of
     * node 'i' is 'j', where the first off-diagonal non-zero in column 'i' has row index 'j'; j &gt; i
     * for which l[k,i] != 0.</p>
     *
     * <p>This tree encodes the non-zero elements in L given A, e.g. L*L' = A, and enables faster to compute solvers
     * than the general purpose implementations.</p>
     *
     * <p>Functionally identical to cs_etree in csparse</p>
     *
     * @param A      (Input) M by N sparse upper triangular matrix.  If ata is false then M=N otherwise M &ge; N
     * @param ata    If true then it computes elimination treee of A'A without forming A'A otherwise computes elimination
     *               tree for cholesky factorization
     * @param parent (Output) Parent of each node in tree. This is the elimination tree.  -1 if no parent.  Size N.
     * @param gwork  (Optional) Internal workspace.  Can be null.
     */
    public static void eliminationTree(DMatrixSparseCSC A, boolean ata, int parent[], IGrowArray gwork) {
        int m = A.numRows;
        int n = A.numCols;

        if (parent.length < n)
            throw new IllegalArgumentException("parent must be of length N");

        int[] work = adjust(gwork, n + (ata ? m : 0));

        int ancestor = 0; // reference to index in work array
        int previous = n; // reference to index in work array

        if( ata ) {
            for (int i = 0; i < m; i++) {
                work[previous+i] = -1;
            }
        }

        // step through each column
        for (int k = 0; k < n; k++) {
            parent[k] = -1;
            work[ancestor+k] = -1;

            int idx0 = A.col_idx[k];   // node k has no parent
            int idx1 = A.col_idx[k+1]; // node k has no ancestor

            for (int p = idx0; p < idx1; p++) {

                int nz_row_p = A.nz_rows[p];

                int i = ata ? work[previous+nz_row_p] : nz_row_p;

                int inext;
                while( i != -1 && i < k ) {
                    inext = work[ancestor+i];
                    work[ancestor+i] = k;
                    if( inext == -1 ) {
                        parent[i] = k;
                        break;
                    } else {
                        i = inext;
                    }
                }

                if( ata ) {
                    work[previous+nz_row_p] = k;
                }
            }
        }
    }

    /**
     * <p>Sorts an elimination tree {@link #eliminationTree} into postorder. In a postoredered tree, the d proper
     * descendants of any node k are numbered k-d through k-1.  Non-recursive implementation for better performance.</p>
     *
     * <p>post[k] = i means node 'i' of the original tree is node 'k' in the postordered tree.</p>
     *
     * <p>See page 44</p>
     *
     * @param parent (Input) The elimination tree.
     * @param N      Number of elements in parent
     * @param post   (Output) Postordering permutation.
     * @param gwork  (Optional) Internal workspace. Can be null
     */
    public static void postorder(int parent[], int N, int post[], IGrowArray gwork) {
        if (parent.length < N)
            throw new IllegalArgumentException("parent must be at least of length N");
        if (post.length < N)
            throw new IllegalArgumentException("post must be at least of length N");

        int[] w = adjust(gwork, 3*N);

        // w[0] to w[N-1] is initialized to the youngest child of node 'j'
        // w[N] to w[2N-1] is initialized to the second youngest child of node 'j'
        // w[2N] to w[3N-1] is the stacked of nodes to be examined in the dfs
        final int next = N;

        // specify the linked list as being empty initially
        for (int j = 0; j < N; j++) {
            w[j] = -1;
        }
        // traverse nodes in reverse order
        for (int j = N-1; j >= 0; j--) {
            // skip if j has no parent, i.e. is a root node
            if( parent[j] == -1 )
                continue;
            // add j to the list of parents
            w[next+j] = w[parent[j]];
            w[parent[j]] = j;
        }

        // perform the DFS on each root node
        int k = 0;
        for (int j = 0; j < N; j++) {
            if( parent[j] != -1 )
                continue;

            k = postorder_dfs(j,k,w,post,N);
        }
    }

    /**
     * Depth First Search used inside of {@link #postorder}.
     */
    protected static int postorder_dfs( int j , int k , int []w, int[] post, int N ) {
        final int next = N;
        final int stack = 2*N;
        int top = 0; // top of the stack
        w[stack+top] = j;
        while( top >= 0 ) {
            int p = w[stack+top]; // next index in the stack to process
            int i = w[p];         // yongest child of p

            if( i == -1 ) {
                // p has no more unordered children left to process
                top--;
                post[k++] = p;
            } else {
                w[p] = w[next+i];
                top++;
                w[stack + top] = i;
            }
        }
        return k;
    }


    /**
     * <p>Given an elimination tree compute the non-zero elements in the specified row of L given the
     * symmetric A matrix.  This is in general much faster than general purpose algorithms</p>
     *
     * <p>Functionally equivalent to cs_ereach() in csparse</p>
     *
     * @param A Symmetric matrix.
     * @param k Row in A being processed.
     * @param parent elimination tree.
     * @param s (Output) s[top:(n-1)] = pattern of L[k,:].  Must have length A.numCols
     * @param w workspace array used internally.  All elements must be &ge; 0 on input. Must be of size A.numCols
     * @return Returns the index of the first element in the xi list.  Also known as top.
     */
    public static int searchNzRowsElim( DMatrixSparseCSC A , int k , int parent[], int s[], int w[] ) {
        int top = A.numCols;

        // Traversing through the column in A is the same as the row in A since it's symmetric
        int idx0 = A.col_idx[k], idx1 = A.col_idx[k+1];

        w[k] = -w[k]-2;  // makr node k as visited
        for (int p = idx0; p < idx1; p++) {
            int i = A.nz_rows[p];   // A[k,i] is not zero

            if( i > k ) // only consider upper triangular part of A
                continue;

            // move up the elimination tree
            int len = 0;
            for(;w[i]>=0; i = parent[i]) {
                s[len++] = i; // L[k,i] is not zero
                w[i] = -w[i]-2; // mark i as being visited
            }
            while( len > 0 ) {
                s[--top] = s[--len];
            }
        }

        // unmark all nodes
        for( int p = top; p < A.numCols; p++ ) {
            w[s[p]] = -w[s[p]]-2;
        }
        w[k] = -w[k]-2;
        return top;
    }

    /**
     * Resizes the array to ensure that it is at least of length desired and returns its internal array
     */
    public static int[] adjust(IGrowArray gwork, int desired) {
        if (gwork == null) gwork = new IGrowArray();
        gwork.reshape(desired);
        return gwork.data;
    }

    public static int[] adjust(IGrowArray gwork, int desired, int zeroToM) {
       int[] w = adjust(gwork,desired);
       Arrays.fill(w,0,zeroToM,0);
       return w;
    }

    public static int[] adjustClear(IGrowArray gwork, int desired) {
        return adjust(gwork,desired,desired);
    }

    /**
     * Resizes the array to ensure that it is at least of length desired and returns its internal array
     */
    public static double[] adjust(DGrowArray gwork, int desired) {
        if (gwork == null) gwork = new DGrowArray();
        gwork.reshape(desired);
        return gwork.data;
    }

    /**
     * Computes the quality of a triangular matrix, where the quality of a matrix
     * is defined in {@link org.ejml.interfaces.linsol.LinearSolver#quality()}.  In
     * this situation the quality os the absolute value of the product of
     * each diagonal element divided by the magnitude of the largest diagonal element.
     * If all diagonal elements are zero then zero is returned.
     *
     * @param T A matrix.
     * @return the quality of the system.
     */
    public static double qualityTriangular(DMatrixSparseCSC T)
    {
        int N = Math.min(T.numRows,T.numCols);

        double max = T.unsafe_get(0,0);
        for( int i = 1; i < N; i++ ) {
            max = Math.max(max,T.unsafe_get(i,i));
        }

        if( max == 0.0 )
            return 0.0;

        double quality = 1.0;
        for( int i = 0; i < N; i++ ) {
            quality *= T.unsafe_get(i,i)/max;
        }

        return Math.abs(quality);
    }
}

