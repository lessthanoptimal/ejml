/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkMatrixVectorOps {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 40000000;//40000000;

    public static long mm_mult_small( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.mult_small(matA,matB,matResult);
//            MatrixMatrixMult.mult_aux(matA,matB,matResult,null);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mm_multTranA_small( DenseMatrix64F matA , DenseMatrix64F matB ,
                                  DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.multTransA_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mm_multTranA_large( DenseMatrix64F matA , DenseMatrix64F matB ,
                                  DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult.multTransA_reorder(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_mult( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult.mult(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_multTranA_small( DenseMatrix64F matA , DenseMatrix64F matB ,
                             DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult.multTransA_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mv_multTranA_large( DenseMatrix64F matA , DenseMatrix64F matB ,
                                        DenseMatrix64F matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixVectorMult.multTransA_reorder(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static void performTests( int numRows , int numCols ,
                                     int numTrials )
    {
        DenseMatrix64F matA = RandomMatrices.createRandom(numRows,numCols,rand);
        DenseMatrix64F matA_tran = RandomMatrices.createRandom(numCols,numRows,rand);
        DenseMatrix64F matB = RandomMatrices.createRandom(numCols,1,rand);
        DenseMatrix64F matResult = RandomMatrices.createRandom(numRows,1,rand);


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