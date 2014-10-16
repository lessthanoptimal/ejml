/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.complex.mult;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.blockd3.BlockD3MatrixOps;
import org.ejml.alg.dense.mult.CMatrixMatrixMult;
import org.ejml.data.BlockD3Matrix64F;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CCommonOps;
import org.ejml.ops.CRandomMatrices;

import java.util.Random;


/**
 *
 * Some notes:
 *
 * Other libraries implement there multiplication the same as my aux implementation, but theirs run faster.
 * That is because they use 2D arrays, this allows them to only increment one variable in their inner
 * most loop.  While in mine I have to increment two.  Thus there is an additional N^3 addition operations.
 *
 * @author Peter Abeles
 */
public class BenchmarkMatrixMatrixMult {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 10000000;

    public static long multiply( CDenseMatrix64F matA , CDenseMatrix64F matB ,
                             CDenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CCommonOps.mult(matA, matB, matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multSmall( CDenseMatrix64F matA , CDenseMatrix64F matB ,
                                  CDenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CMatrixMatrixMult.mult_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long multAux( DenseMatrix64F matA , DenseMatrix64F matB ,
//                             DenseMatrix64F matResult , int numTrials) {
//        long prev = System.currentTimeMillis();
//
//        for( int i = 0; i < numTrials; i++ ) {
//            MatrixMatrixMult.mult_aux(matA,matB,matResult,null);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static long multReorder( CDenseMatrix64F matA , CDenseMatrix64F matB ,
                                    CDenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CMatrixMatrixMult.mult_reorder(matA, matB, matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multBlockNative( DenseMatrix64F matA , DenseMatrix64F matB ,
                                        DenseMatrix64F matResult , int numTrials) {
        BlockMatrix64F blockA = BlockMatrixOps.convert(matA);
        BlockMatrix64F blockB = BlockMatrixOps.convert(matB);
        BlockMatrix64F blockC = new BlockMatrix64F(matResult.numRows,matResult.numCols);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            BlockMatrixOps.mult(blockA,blockB,blockC);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multBlockD3Native( DenseMatrix64F matA , DenseMatrix64F matB ,
                                          DenseMatrix64F matResult , int numTrials) {
        BlockD3Matrix64F blockA = BlockD3MatrixOps.convert(matA);
        BlockD3Matrix64F blockB = BlockD3MatrixOps.convert(matB);
        BlockD3Matrix64F blockC = new BlockD3Matrix64F(matResult.numRows,matResult.numCols);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            BlockD3MatrixOps.mult(blockA,blockB,blockC);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }


    public static void performTests( int numRows , int numCols , int numK,
                                     int numTrials )
    {
        System.out.println("M = "+numRows+" N = "+numCols+" K = "+numK);
        CDenseMatrix64F matA = CRandomMatrices.createRandom(numRows,numCols,-1,1,rand);
        CDenseMatrix64F matB = CRandomMatrices.createRandom(numCols,numK,-1,1,rand);
        CDenseMatrix64F matResult = CRandomMatrices.createRandom(numRows, numK,-1,1, rand);

        System.out.printf("Mult: %7d  Small %7d  Aux %7d  Reord %7d  Block %7d  BlockD3 %7d\n",
                0,//mult(matA,matB,matResult,numTrials),
                multSmall(matA,matB,matResult,numTrials),
                0,//multAux(matA,matB,matResult,numTrials),
                multReorder(matA,matB,matResult,numTrials),
                0,//multBlockNative(matA,matB,matResult,numTrials),
                0);//multBlockD3Native(matA,matB,matResult,numTrials));
        System.gc();
    }

    public static void main( String args[] ) {
        int size[] = new int[]{2,4,10,15,20,50,100,200,500,1000,2000,4000,10000};
        int count[] = new int[]{40000000,10000000,1000000,300000,100000,10000,1000,100,8,2,1,1,1};

        int sizeTall[] = new int[]{1,2,4,10,20,50,100,200,500,1000,2000,5000,10000};
        int countTall[] = new int[]{3000,2400,1500,1000,200,200,100,50,10,5,2,1,1};

        int N = size.length;

        System.out.println("******* Square:\n");
        for( int i = 2; i < N; i++ ) {
            System.out.println();

            performTests(size[i],size[i],size[i],count[i]);
        }

        N = sizeTall.length;
        System.out.println("\n******* Wide A:");
        for( int i = 0; i < N; i++ ) {
            System.out.println();

            performTests(sizeTall[i],1500,100,countTall[i]);
        }

        System.out.println("\n******* Tall A:");
        for( int i = 7; i < N; i++ ) {
            System.out.println();

            performTests(1500,sizeTall[i],100,countTall[i]);
        }

        System.out.println("\n******* Wide B:");
        for( int i = 7; i < N; i++ ) {
            System.out.println();

            performTests(100,sizeTall[i],1500,countTall[i]);
        }

        System.out.println("\n******* Tall B:");
        for( int i = 7; i < N; i++ ) {
            System.out.println();

            performTests(100,1500,sizeTall[i],countTall[i]);
        }
    }
}