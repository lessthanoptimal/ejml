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
import org.ejml.sparse.csc.RandomMatrices_DSCC;
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
     * Make sure it has the expected results when called multiple times
     */
    @Test
    public void processRepeatCalls() {
        fail("Implement");
    }

    @Test
    public void process() {
        fail("Implement");
    }

    @Test
    public void process_ata() {
        fail("Implement");
    }


    /**
     * Hand constructed test case
     */
    @Test
    public void findFirstDescendant_hand() {
        int n = 6;

        int delta[] = new int[n];
        int parent[] = new int[]{2,3,3,4,5,-1};
        int post[] = new int[parent.length];
        TriangularSolver_DSCC.postorder(parent,n,post,null);

        ColumnCounts_DSCC alg = new ColumnCounts_DSCC(false);
        alg.initialize(new DMatrixSparseCSC(n,n,0));
        alg.findFirstDescendant(parent,post,delta);

        int expected[] = new int[]{0,1,0,0,0,0};
        int w[] = alg.getW();
        for (int i = 0; i < n; i++) {
            assertEquals(expected[i],w[alg.first+i]);
        }

        // todo check delta
    }

    @Test
    public void findFirstDescendant_random() {

        fail("Implement");
    }

    /**
     * Hand constructed test case
     */
    @Test
    public void isLeaf_hand() {
        fail("Implement");
    }

    /**
     * Test against randomly generated matrices using brute force checks
     */
    @Test
    public void isLeaf_random() {
        fail("Implement");
    }

    private void performRandomizedCheck( TestFunction checker ) {
        for (int i = 0; i < 200; i++) {
            // select the matrix size
            int N = rand.nextInt(16)+1;
            // select number of non-zero elements in the matrix. diagonal elements are always filled
            int nz = (int)(((N-1)*(N-1)/2)*(rand.nextDouble()*0.8+0.2))+N;
            DMatrixSparseCSC A = RandomMatrices_DSCC.triangleUpper(N,0,nz,-1,1,rand);

            // compute the elimination tree
            int parent[] = new int[A.numCols];
            TriangularSolver_DSCC.eliminationTree(A,false,parent,null);

            // compute the post ordering
            int post[] = new int[A.numCols];
            TriangularSolver_DSCC.postorder(parent,N,post,null);

            // now perform the test
            checker.performTest(A,parent,post);
        }
    }

    private interface TestFunction {
        void performTest( DMatrixSparseCSC A , int []parents, int []post );
    }
}
