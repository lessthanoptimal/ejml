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

package org.ejml.alg.dense.mult;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkMatrixVectorOps {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 40000000;//40000000;

    public static long mm_mult_small(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                                     RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_D64.mult_small(matA,matB,matResult);
//            MatrixMatrixMult.mult_aux(matA,matB,matResult,null);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mm_multTranA_small(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                                          RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_D64.multTransA_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mm_multTranA_large(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                                          RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_D64.multTransA_reorder(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_mult(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                               RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult_D64.mult(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_multTranA_small(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                                          RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult_D64.multTransA_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_multTranA_large(RowMatrix_F64 matA , RowMatrix_F64 matB ,
                                          RowMatrix_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult_D64.multTransA_reorder(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static void performTests( int numRows , int numCols ,
                                     int numTrials )
    {
        RowMatrix_F64 matA = RandomMatrices_D64.createRandom(numRows,numCols,rand);
        RowMatrix_F64 matA_tran = RandomMatrices_D64.createRandom(numCols,numRows,rand);
        RowMatrix_F64 matB = RandomMatrices_D64.createRandom(numCols,1,rand);
        RowMatrix_F64 matResult = RandomMatrices_D64.createRandom(numRows,1,rand);


        System.out.printf("Mult Vec:              %10d\n",
                mv_mult(matA,matB,matResult,numTrials));
        System.out.printf("Mult Tran A Small Vec: %10d\n",
                mv_multTranA_small(matA_tran,matB,matResult,numTrials));
        System.out.printf("Mult Tran A Large Vec: %10d\n",
                mv_multTranA_large(matA_tran,matB,matResult,numTrials));
        System.out.printf("Mult small:            %10d\n",
                mm_mult_small(matA,matB,matResult,numTrials));
        System.out.printf("Mult Tran A small:     %10d\n",
                mm_multTranA_small(matA_tran,matB,matResult,numTrials));
        System.out.printf("Mult Tran A large:     %10d\n",
                mm_multTranA_large(matA_tran,matB,matResult,numTrials));

        System.gc();
    }

    public static void main( String args[] ) {
        System.out.println("Small Matrix Results:") ;
        performTests(4,4,TRIALS_MULT);

        System.out.println();
        System.out.println("Large Matrix Results:") ;

        performTests(1000,1000,2000);

        System.out.println();
        System.out.println("Large Not Square Matrix Results:") ;

        performTests(20,1000,10000);

    }
}