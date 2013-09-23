/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.mult;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkMatrixMultProduct {
    static Random rand = new Random(234234);

    static int TRIALS_MULT = 10000000;

    public static long multTransA( DenseMatrix64F matA ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.multTransA(matA, matA, matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long innerProd_small( DenseMatrix64F matA ,
                                        DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMultProduct.inner_small(matA, matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long innerProd_reorder( DenseMatrix64F matA ,
                                        DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMultProduct.inner_reorder(matA, matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static void performTests( int numRows , int numCols ,
                                     int numTrials )
    {
        System.out.println("M = "+numRows+" N = "+numCols+" trials "+numTrials);
        DenseMatrix64F matA = RandomMatrices.createRandom(numRows, numCols, rand);
        DenseMatrix64F matResult = RandomMatrices.createRandom(numCols,numCols,rand);

        System.out.printf("Mult: %7d  Small %7d  Reord %7d\n",
                0,//multTransA(matA,matResult,numTrials),
                innerProd_small(matA,matResult,numTrials),
                innerProd_reorder(matA,matResult,numTrials));
        System.gc();
    }

    public static void main( String args[] ) {
        int size[] = new int[]{2,4,10,15,20,50,100,200,500,1000,2000,4000,10000};
        int count[] = new int[]{20000000,5000000,500000,150000,100000,5000,500,50,4,1,1,1,1};


        int N = size.length;

        for( int i = 0; i < N; i++ ) {
            System.out.println();

            performTests(2*size[i],size[i],count[i]);
        }
    }
}
