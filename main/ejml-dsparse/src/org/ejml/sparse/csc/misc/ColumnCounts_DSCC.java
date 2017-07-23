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

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.util.Arrays;

/**
 * Computes the column counts of the upper triangular portion of A.
 *
 * <p>See cs_counts() on page 55</p>
 *
 * @author Peter Abeles
 */
public class ColumnCounts_DSCC {

    // See contructor comments
    private boolean ata;

    // transpose of input matrix
    private DMatrixSparseCSC At = new DMatrixSparseCSC(1,1,1);

    // workspace array
    private int w[] = new int[1];

    // shape of input matrix
    private int m,n; // (row,col)

    //--------------indices in workspace
    private int ancestor;
    private int maxfirst;
    private int prevleaf;
    private int first;
    private int head,next;

    // output from isLeaf()
    private int jleaf;

    /**
     * Configures column count algorithm.
     *
     * @param ata flag used to indicate if the cholesky factor of A or A<sup>T</sup>A is to be computed.
     */
    public ColumnCounts_DSCC( boolean ata ) {
        this.ata = ata;
    }

    /**
     * Initializes class data structures and parameters
     */
    void initialize(DMatrixSparseCSC A, int[] counts) {
        if( counts.length < A.numCols )
            throw new IllegalArgumentException("counts must be at least of length A.numCols");

        m = A.numRows;
        n = A.numCols;
        int s = 4*n + (ata ? (n+m+1) : 0);

        // check and declare workspace
        if( w.length < s )
            w = new int[s];
        Arrays.fill(w,-1,0,s); // assign all values in workspace to -1

        ancestor = 0;
        maxfirst = n;
        prevleaf = 2*n;
        first    = 3*n;

        // compute the transpose of A
        At.reshape(A.numCols,A.numRows,A.nz_length);
        CommonOps_DSCC.transpose(A,At,w);
    }

    /**
     * Processes and computes column counts of A
     *
     * @param A (Input) Upper triangular matrix
     * @param parent (Input) Elimination tree.
     * @param post (Input) Post ordering of elimination tree
     * @param counts (Output) Storage for column counts.
     */
    public void process(DMatrixSparseCSC A , int parent[], int post[], int counts[] ) {
        initialize(A, counts);

        int delta[] = counts;

        // find first 'j'
        int j;
        for (int k = 0; k < n; k++) {
            j = post[k];
            delta[j] = (w[first+j] == -1) ? 1 : 0;
            for(; j != -1 && w[first+j]==-1;j=parent[j]) {
                w[first+j] = k;
            }
        }

        if( ata ) {
            init_ata(post);
        }

        int[] ATp = At.col_idx; int []ATi = At.nz_rows;

        for (int i = 0; i < n; i++) {
            w[ancestor] = i;
        }

        for (int k = 0; k < n; k++) {
            j = post[k];
            if( parent[j] != -1 )
                delta[parent[j]]--; // j is not a root
            for (int J = HEAD(k,j); J != -1; J = NEXT(J)) {
                for (int p = ATp[J]; p < ATp[J+1]; p++) {
                    int i = ATi[p];
                    int q = isLeaf(i,j);
                    if( jleaf >= 1)
                        delta[j]++;
                    if( jleaf == 2 )
                        delta[q]--;
                }
            }
            if( parent[j] != -1 )
                w[ancestor+j] = parent[j];
        }

        // sum up delta's of each child
        for ( j = 0; j < n; j++) {
            if( parent[j] != -1)
                counts[parent[j]] += counts[j];
        }
    }

    private int HEAD(int k , int j ) {
        return ata ? w[head+k] : j;
    }

    private int NEXT( int J ) {
        return ata ? w[next+J] : -1;
    }

    private void init_ata( int post[]) {
        int[] ATp = At.col_idx; int []ATi = At.nz_rows;

        head = 4*n;
        next = 5*n+1;

        for (int k = 0; k < n; k++) {
            w[post[k]] = k;
        }
        for (int i = 0; i < m; i++) {
            int k,p;
            for (k = n, p = ATp[i]; p < ATp[i+1]; p++) {
                k = Math.min(k,w[ATi[p]]);
            }
            w[next+i] = w[head+k];
            w[head+k] = i;
        }
    }


    /**
     * <p>Determines if j is a leaf in the ith row subtree of T^t. If it is then it finds the least-common-ancestor
     * of the previously found leaf in T^i (jprev) and node j.</p>
     *
     * <ul>
     * <li>jleaf == 0 then j is not a leaf
     * <li>jleaf == 1 then 1st leaf. q = root of ith subtree
     * <li>jleaf == 2 then j is a subsequent leaf
     * </ul>
     *
     * <p>See cs_leaf on page 51</p>
     *
     * @param i Specifies which row subtree in T
     * @param j node in subtree
     * @return The least common ancestor (jprev,j)
     */
    public int isLeaf( int i , int j ) {
        jleaf = 0;

        // see j is not a leaf
        if( i <= j || w[first+j] <= w[maxfirst+i] )
            return -1;

        w[maxfirst+i] = w[first+j]; // update the max first[j] seen so far
        int jprev = w[prevleaf+i];
        w[prevleaf+i]=j;

        jleaf = (jprev == -1) ? 1 : 2;
        if( jleaf == 1 )
            return i;

        int q,sparent;
        for( q = jprev; q != w[ancestor+q]; q = w[ancestor+q]){}
        for( int s = jprev; s != q; s = sparent )
        {
            sparent = w[ancestor+s];
            w[ancestor+s] = q;
        }
        return q;
    }
}
