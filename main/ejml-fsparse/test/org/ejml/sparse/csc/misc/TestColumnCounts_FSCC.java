/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.UtilEjml;
import org.ejml.data.FMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.sparse.csc.CommonOps_FSCC;
import org.ejml.sparse.csc.RandomMatrices_FSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestColumnCounts_FSCC {
    private Random rand = new Random(234);

    /**
     * Hand constructed test case.
     */
    @Test
    public void process_ata_false() {
        FMatrixSparseCSC A = UtilEjml.parse_FSCC(
                     "1 0 1 1 0 1 0 " +
                        "0 1 0 1 0 0 0 " +
                        "0 0 1 0 1 0 0 " +
                        "0 0 0 1 0 0 0 " +
                        "0 0 0 0 1 0 1 " +
                        "0 0 0 0 0 1 1 " +
                        "0 0 0 0 0 0 1 ",7);

        int parent[] = new int[A.numCols];
        int post[] = new int[A.numCols];
        int counts[] = new int[A.numCols];
        int n = A.numRows;

        TriangularSolver_FSCC.eliminationTree(A,false,parent,null);
        TriangularSolver_FSCC.postorder(parent,n,post,null);

        ColumnCounts_FSCC alg = new ColumnCounts_FSCC(false);
        alg.process(A,parent,post,counts);

        // computed by inspection
        int expected[] = new int[]{4,2,4,3,3,2,1};

        for (int i = 0; i < n; i++) {
            assertEquals(expected[i],counts[i]);
        }
    }

    /**
     * By explicitly computing ATA then compare against the implicit solution
     */
    @Test
    public void process_ata_true() {
        ColumnCounts_FSCC alg = new ColumnCounts_FSCC(false);
        ColumnCounts_FSCC algATA = new ColumnCounts_FSCC(true);

        // recycle the data to add a secondary test of it being cleared
        IGrowArray parent = new IGrowArray();
        IGrowArray post = new IGrowArray();

        for (int mc = 0; mc < 200; mc++) {
            int N = rand.nextInt(16) + 1;

//            System.out.println("mc = "+mc+"  N = "+N);

            parent.reshape(N);
            post.reshape(N);
            FMatrixSparseCSC A = RandomMatrices_FSCC.triangle(true, N, 0.2f, 0.5f, rand);
            FMatrixSparseCSC ATA = new FMatrixSparseCSC(N, N, 0);
            CommonOps_FSCC.multTransA(A, A, ATA, null, null);

            // compute expected results
            int expected[] = new int[A.numCols];
            TriangularSolver_FSCC.eliminationTree(ATA, false, parent.data, null);
            TriangularSolver_FSCC.postorder(parent.data, N, post.data, null);
            alg.process(ATA, parent.data, post.data, expected);

            // Now compute it implicitly
            int found[] = new int[A.numCols];
            TriangularSolver_FSCC.eliminationTree(A, true, parent.data, null);
            TriangularSolver_FSCC.postorder(parent.data, N, post.data, null);
            algATA.process(A, parent.data, post.data, found);

            for (int i = 0; i < N; i++) {
                assertEquals(expected[i], found[i]);
            }
        }
    }

    /**
     *
     */
    @Test
    public void process_monticarlo() {

        ColumnCounts_FSCC alg = new ColumnCounts_FSCC(false);

        for (int i = 0; i < 200; i++) {
            int N = rand.nextInt(16)+1;
            FMatrixSparseCSC A = RandomMatrices_FSCC.triangle(false,N,0.2f,0.5f,rand);
            FMatrixSparseCSC A_t = new FMatrixSparseCSC(N,N,A.nz_length);
            CommonOps_FSCC.transpose(A,A_t,null);


            int parent[] = new int[A.numCols];
            int post[] = new int[A.numCols];
            int counts[] = new int[A.numCols];

            TriangularSolver_FSCC.eliminationTree(A_t,false,parent,null);
            TriangularSolver_FSCC.postorder(parent,N,post,null);

            alg.process(A_t,parent,post,counts);

            // Find a solution the very slow way
            bruteForceFill(A);
            int expected[] = computeColumns(A);

            for (int j = 0; j < N; j++) {
                assertEquals(expected[j],counts[j]);
            }
        }
    }

    private void bruteForceFill( FMatrixSparseCSC A ) {
        for (int i = 0; i < A.numCols; i++) {
            for (int j = 0; j < i; j++) {
                if( A.get(i,j) != 0 ) {
                    for (int k = 0; k < i; k++) {
                        if( A.get(k,j) != 0 ) {
                            A.set(i,k,1);
                        }
                    }
                }
            }
        }
    }

    private int[] computeColumns(FMatrixSparseCSC A ) {
        int[] counts = new int[ A.numCols ];
        for (int i = 0; i < A.numRows; i++) {
            int c = 1;
            for (int j = i+1; j < A.numRows; j++) {
                if( A.get(j,i) != 0 ) {
                    c++;
                }
            }
            counts[i] = c;
        }
        return counts;
    }

    /**
     * Hand constructed test case
     */
    @Test
    public void findFirstDescendant_hand() {
        // set up data structures
        int n = 6;

        int delta[] = new int[n];
        int parent[] = new int[]{2,3,3,4,5,-1};
        int post[] = new int[parent.length];
        TriangularSolver_FSCC.postorder(parent,n,post,null);

        // run the algorithm
        ColumnCounts_FSCC alg = new ColumnCounts_FSCC(false);
        alg.initialize(new FMatrixSparseCSC(n,n,0));
        alg.findFirstDescendant(parent,post,delta);

        // check 'first'
        int expected[] = new int[]{1,0,1,0,0,0};
        int w[] = alg.getW();
        for (int i = 0; i < n; i++) {
            assertEquals(expected[i],w[alg.first+i]);
        }

        // check 'delta'
        expected = new int[]{1,1,0,0,0,0};
        for (int i = 0; i < n; i++) {
            assertEquals(expected[i],delta[i]);
        }
    }

    /**
     * Hand constructed test case
     */
    @Test
    public void isLeaf_hand() {
        // set up data structures
        int n = 6;

        int delta[] = new int[n];
        int parent[] = new int[]{2,3,3,4,5,-1};
        int post[] = new int[parent.length];
        TriangularSolver_FSCC.postorder(parent,n,post,null);

        // run the algorithm
        ColumnCounts_FSCC alg = new ColumnCounts_FSCC(false);
        alg.initialize(new FMatrixSparseCSC(n,n,0));
        for (int i = 0; i < n; i++)  // need to do this here since it isn't done in init
            alg.w[alg.ancestor+i] = i;
        alg.findFirstDescendant(parent,post,delta);

        // test cases in which j is clearly not a leaf
        assertEquals(-1,alg.isLeaf(3,3));
        assertEquals(-1,alg.isLeaf(3,4));

        // test a mixture of positive and negative cases. Note that the internal book keeping needs to be take in
        // account so the order of these tests are very important
        check(alg,1,0,1,1);
        check(alg,2,0,1,2);
        check(alg,2,1,-1,-1);
        check(alg,3,1,1,3);

        // there's some book keeping that isn't done inside of isLeaf() so do it manually
        alg.w[alg.ancestor] = 3;
        alg.w[alg.ancestor+1] = 2;
        alg.w[alg.ancestor+2] = 3;
        check(alg,3,0,2,3);
    }

    private void check( ColumnCounts_FSCC alg , int i , int j, int jleaf , int returned ) {
        assertEquals(returned,alg.isLeaf(i,j));
        if( returned != -1 )
            assertEquals(jleaf,alg.jleaf);
    }
}
