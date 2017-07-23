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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestColumnCounts_DSCC {
    private Random rand = new Random(234);

    /**
     * Hand constructed test case.
     */
    @Test
    public void process_ata_false() {
        DMatrixSparseCSC A = UtilEjml.parse_DSCC(
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

        TriangularSolver_DSCC.eliminationTree(A,false,parent,null);
        TriangularSolver_DSCC.postorder(parent,n,post,null);

        ColumnCounts_DSCC alg = new ColumnCounts_DSCC(false);
        alg.process(A,parent,post,counts);

        // computed by inspection
        int expected[] = new int[]{4,2,4,3,3,2,1};

        for (int i = 0; i < n; i++) {
            assertEquals(expected[i],counts[i]);
        }
    }

    /**
     *
     */
    @Test
    public void process_monticarlo() {

        ColumnCounts_DSCC alg = new ColumnCounts_DSCC(false);

        for (int i = 0; i < 200; i++) {
            int N = rand.nextInt(16)+1;
            N = 7;
            DMatrixSparseCSC A = RandomMatrices_DSCC.triangle(false,N,0.2,0.5,rand);
            DMatrixSparseCSC A_t = new DMatrixSparseCSC(N,N,A.nz_length);
            CommonOps_DSCC.transpose(A,A_t,null);


            int parent[] = new int[A.numCols];
            int post[] = new int[A.numCols];
            int counts[] = new int[A.numCols];

            TriangularSolver_DSCC.eliminationTree(A_t,false,parent,null);
            TriangularSolver_DSCC.postorder(parent,N,post,null);

            alg.process(A_t,parent,post,counts);

            // Find a solution the very slow way
            bruteForceFill(A);
            int expected[] = computeColumns(A);

            for (int j = 0; j < N; j++) {
                assertEquals(expected[j],counts[j]);
            }
        }
    }

    private void bruteForceFill( DMatrixSparseCSC A ) {
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

    private int[] computeColumns(DMatrixSparseCSC A ) {
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

    @Ignore
    @Test
    public void process_ata() {
        fail("Implement");
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
        TriangularSolver_DSCC.postorder(parent,n,post,null);

        // run the algorithm
        ColumnCounts_DSCC alg = new ColumnCounts_DSCC(false);
        alg.initialize(new DMatrixSparseCSC(n,n,0));
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
        TriangularSolver_DSCC.postorder(parent,n,post,null);

        // run the algorithm
        ColumnCounts_DSCC alg = new ColumnCounts_DSCC(false);
        alg.initialize(new DMatrixSparseCSC(n,n,0));
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

    private void check( ColumnCounts_DSCC alg , int i , int j, int jleaf , int returned ) {
        assertEquals(returned,alg.isLeaf(i,j));
        if( returned != -1 )
            assertEquals(jleaf,alg.jleaf);
    }
}
