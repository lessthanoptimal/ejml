/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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

package org.ejml.dense.row.mult;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;


/**
 *
 * Some notes:
 *
 * Other libraries implement there multiplication the same as my aux implementation, but theirs run faster.
 * That is because they use 2D arrays, this allows them to only increment one variable in their inner
 * most loop. While in mine I have to increment two. Thus there is an additional N^3 addition operations.
 *
 * @author Peter Abeles
 */
public class BenchmarkMatrixMatrixMultAdd {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 10000000;

    public static long mult(DMatrixRMaj matA , DMatrixRMaj matB ,
                            DMatrixRMaj matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multAdd(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multSmall(DMatrixRMaj matA , DMatrixRMaj matB ,
                                 DMatrixRMaj matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_DDRM.multAdd_small(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAux(DMatrixRMaj matA , DMatrixRMaj matB ,
                               DMatrixRMaj matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_DDRM.multAdd_aux(matA,matB,matResult,null);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multReorder(DMatrixRMaj matA , DMatrixRMaj matB ,
                                   DMatrixRMaj matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_DDRM.multAdd_reorder(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }


    public static void performTests( int numRows , int numCols , int numK,
                                     int numTrials )
    {
        DMatrixRMaj matA = RandomMatrices_DDRM.rectangle(numRows,numCols,rand);
        DMatrixRMaj matB = RandomMatrices_DDRM.rectangle(numCols,numK,rand);
        DMatrixRMaj matResult = RandomMatrices_DDRM.rectangle(numRows,numK,rand);

        System.out.printf("Mult: %7d  Small %7d  Aux %7d  Reord %7d\n",
                mult(matA,matB,matResult,numTrials),
                multSmall(matA,matB,matResult,numTrials),
                multAux(matA,matB,matResult,numTrials),
                multReorder(matA,matB,matResult,numTrials));

        System.gc();
    }

    public static void main( String args[] ) {
        int size[] = new int[]{2,4,10,};//20,50,100,200,500,1000};//,2000,4000};
        int count[] = new int[]{40000000,10000000,1000000,100000,10000,1000,100,8,2,1,1};

        int sizeTall[] = new int[]{1,2,4,10,};//20,50,100,200,500,1000};
        int countTall[] = new int[]{3000,2400,1500,1000,200,200,100,50,10,5};

        System.out.println("******* Square:\n");
        for( int i = 0; i < size.length; i++ ) {
            System.out.println("\nWidth = "+size[i]);

            performTests(size[i],size[i],size[i],count[i]);
        }

        System.out.println("\n******* Wide A:");
        for( int i = 0; i < sizeTall.length; i++ ) {
            System.out.println("\nHeight = "+sizeTall[i]);

            performTests(sizeTall[i],1500,100,countTall[i]);
        }

        System.out.println("\n******* Tall A:");
        for( int i = 0; i < sizeTall.length; i++ ) {
            System.out.println("\nWidth = "+sizeTall[i]);

            performTests(1500,sizeTall[i],100,countTall[i]);
        }

        System.out.println("\n******* Wide B:");
        for( int i = 0; i < sizeTall.length; i++ ) {
            System.out.println("\nHeight = "+sizeTall[i]);

            performTests(100,sizeTall[i],1500,countTall[i]);
        }

        System.out.println("\n******* Tall B:");
        for( int i = 0; i < sizeTall.length; i++ ) {
            System.out.println("\nWidth = "+sizeTall[i]);

            performTests(100,1500,sizeTall[i],countTall[i]);
        }
    }
}