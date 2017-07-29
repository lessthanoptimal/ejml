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
import org.ejml.sparse.csc.misc.ColumnCounts_DSCC;
import org.ejml.sparse.csc.misc.TriangularSolver_DSCC;

import java.util.Arrays;

/**
 * Determines the structure of the QR decomposition. Both R and V (householder vectors) component.
 *
 * <p>Fictional Rows: When there are no non-zero values in a row a fictional row is added to the end. The fictional
 * row will have an element in it that is not zero. It will then be permuted in. The QR decomposition algorithm
 * requires that all rows have a structurally non-zero element in them.</p>
 *
 * <p>NOTE: This class contains the all of or part of cs_sqrt() and cs_vcounts() in csparse</p>
 */
public class QrStructuralCounts_DSCC {

    DMatrixSparseCSC A; // reference to input matrix
    int m,n; // short hand for number of rows and columns in A
    int leftmost[] = new int[0]; // left most column in each row
    int m2; // number of rows for QR after adding fictitious rows
    int pinv[] = new int[0]; // inverse permutation to ensure diagonal elements are all structurally nonzero
                             // this is a row pivot
    int parent[] = new int[0]; // elimination tree
    int post[] = new int[0]; // post ordered tree
    IGrowArray gwork = new IGrowArray(); // generic work space
    int nz_in_V; // number of entries in V
    int nz_in_R; // number of entries in R
    int countsR[] = new int[0]; // column counts in R

    // ----- start location of different sections in work array inside of V
    int next;         // col=next[row] element in the linked list
    int head;         // row=head[col] first row in which col is the first
    int tail;         // row=tail[col] last row in which col is the first
    int nque;         // nque[col] number of elements in column linked list

    ColumnCounts_DSCC columnCounts = new ColumnCounts_DSCC(true);

    /**
     * Examins the structure of A for QR decomposition
     * @param A matrix which is to be decomposed
     * @return true if the solution is valid or false if the decomposition can't be performed (i.e. requires column pivots)
     */
    public boolean process( DMatrixSparseCSC A ) {
        init(A);

        TriangularSolver_DSCC.eliminationTree(A,true,parent,gwork);

        countNonZeroInR(parent);
        countNonZeroInV(parent);

        // if more columns than rows it's possible that Q*R != A. That's because a householder
        // would need to be created that's outside the  m by m Q matrix. In reality it has
        // a partial solution. Column pivot are needed.
        if( m < n ) {
            for (int row = 0; row <m; row++) {
                if( gwork.data[head+row] < 0 ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Initializes data structures
     */
    void init( DMatrixSparseCSC A ) {
        this.A = A;
        this.m = A.numRows;
        this.n = A.numCols;

        this.next = 0;
        this.head = m;
        this.tail = m + n;
        this.nque = m + 2*n;

        if( parent.length < n || leftmost.length < m) {
            parent = new int[n];
            post = new int[n];
            pinv = new int[m+n];
            countsR = new int[n];
            leftmost = new int[m];
        }
        gwork.reshape(m+3*n);
    }


    /**
     * Count the number of non-zero elements in R
     */
    void countNonZeroInR( int[] parent ) {
        TriangularSolver_DSCC.postorder(parent,n,post,gwork);
        columnCounts.process(A,parent,post,countsR);
        nz_in_R = 0;
        for (int k = 0; k < n; k++) {
            nz_in_R += countsR[k];
        }
        if( nz_in_R < 0)
            throw new RuntimeException("Too many elements. Numerical overflow in R counts");
    }

    /**
     * Count the number of non-zero elements in V
     */
    void countNonZeroInV( int []parent ) {
        int []w = gwork.data;
        findMinElementIndexInRows(leftmost);
        createRowElementLinkedLists(leftmost,w);
        countNonZeroUsingLinkedList(parent,w);
    }

    /**
     * Non-zero counts of Householder vectors and computes a permutation
     * matrix that ensures diagonal entires are all structurally nonzero.
     *
     * @param parent elimination tree
     * @param ll linked list for each row that specifies elements that are not zero
     */
    void countNonZeroUsingLinkedList( int parent[] , int ll[] ) {

        Arrays.fill(pinv,0,m,-1);
        nz_in_V = 0;
        m2 = m;

        for (int k = 0; k < n; k++) {
            int i = ll[head+k];           // remove row i from queue k
            nz_in_V++;                    // count V(k,k) as nonzero
            if( i < 0)                    // add a fictitious row since there are no nz elements
                i = m2++;
            pinv[i] = k;                  // associate row i with V(:,k)
            if( --ll[nque+k] <= 0 )
                continue;
            nz_in_V += ll[nque+k];
            int pa;
            if( (pa = parent[k]) != -1 ) { // move all rows to parent of k
                if( ll[nque+pa] == 0)
                    ll[tail+pa] = ll[tail+k];
                ll[next+ll[tail+k]] = ll[head+pa];
                ll[head+pa] = ll[next+i];
                ll[nque+pa] += ll[nque+k];
            }
        }
        for (int i = 0, k = n; i < m; i++) {
            if( pinv[i] < 0 )
                pinv[i] = k++;
        }

        if( nz_in_V < 0)
            throw new RuntimeException("Too many elements. Numerical overflow in V counts");
    }

    /**
     * Constructs a linked list in w that specifies which elements in each row are not zero (nz)
     * @param leftmost index first elements in each row
     * @param w work space array
     */
    void createRowElementLinkedLists( int leftmost[] , int w[]) {
        for (int k = 0; k < n; k++) {
            w[head+k] = -1; w[tail+k] = -1;  w[nque+k] = 0;
        }

        // scan rows in reverse order creating a linked list of nz element indexes in each row
        for (int i = m-1; i >= 0; i--) {
            int k = leftmost[i];      // 'k' = left most column in row 'i'
            if( k == -1 )             // row 'i' is empty
                continue;
            if( w[nque+k]++ == 0 )
                w[tail+k] = i;
            w[next+i] = w[head+k];
            w[head+k] = i;
        }
    }

//    private void printLinkedList(int w[]) {
//        System.out.print("head [");
//        for (int k = 0; k < n; k++) {
//            System.out.printf(" %2d",w[head+k]);
//        }
//        System.out.println(" ]");
//        System.out.print("tail [");
//        for (int k = 0; k < n; k++) {
//            System.out.printf(" %2d",w[tail+k]);
//        }
//        System.out.println(" ]");
//        System.out.print("nque [");
//        for (int k = 0; k < n; k++) {
//            System.out.printf(" %2d",w[nque+k]);
//        }
//        System.out.println(" ]");
//        System.out.print("next [");
//        for (int k = 0; k < m; k++) {
//            System.out.printf(" %2d",w[next+k]);
//        }
//        System.out.println(" ]");
//    }

    /**
     * Computes leftmost[i] =  min(find(A[i,:))
     * *
     * @param leftmost (output) storage for left most elements
     */
    void findMinElementIndexInRows(int leftmost[] )
    {
        Arrays.fill(leftmost,0,m,-1);

        // leftmost[i] = min(find(A(i,:)))
        for (int k = n-1; k >= 0; k--) {
            int idx0 = A.col_idx[k];
            int idx1 = A.col_idx[k+1];

            for( int p = idx0; p < idx1; p++ ) {
                leftmost[A.nz_rows[p]] = k;
            }
        }
    }

    public void setGwork(IGrowArray gwork) {
        this.gwork = gwork;
    }

    public int getFicticousRowCount() {
        return m2;
    }

    public int[] getLeftMost() {
        return leftmost;
    }

    public int[] getParent() {
        return parent;
    }

    public int[] getPinv() {
        return pinv;
    }

    public int getM2() {
        return m2;
    }
}
