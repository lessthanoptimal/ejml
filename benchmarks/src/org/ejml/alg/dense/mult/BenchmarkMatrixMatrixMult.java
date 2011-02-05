/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.mult;

import org.ejml.alg.block.BlockMatrixOps;
import org.ejml.alg.blockd3.BlockD3MatrixOps;
import org.ejml.data.BlockD3Matrix64F;
import org.ejml.data.BlockMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

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

    public static long mult( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.mult(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multSmall( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.mult_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAux( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.mult_aux(matA,matB,matResult,null);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multReorder( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.mult_reorder(matA,matB,matResult);
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
        DenseMatrix64F matA = RandomMatrices.createRandom(numRows,numCols,rand);
        DenseMatrix64F matB = RandomMatrices.createRandom(numCols,numK,rand);
        DenseMatrix64F matResult = RandomMatrices.createRandom(numRows,numK,rand);

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
        for( int i = 5; i < N; i++ ) {
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