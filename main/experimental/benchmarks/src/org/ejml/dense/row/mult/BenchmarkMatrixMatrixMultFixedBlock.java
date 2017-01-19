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

package org.ejml.dense.row.mult;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkMatrixMatrixMultFixedBlock {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 10000000;

    public static long mult(DMatrixRMaj matA , DMatrixRMaj matB ,
                            DMatrixRMaj matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.mult(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multFixed12_2x6(DMatrixRMaj matA , DMatrixRMaj matB ,
                                       DMatrixRMaj matResult , int numTrials) {

        MatrixMultFixedBlock ops = new MatrixMultFixedBlock();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ops.mult_2x6(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multFixed12_4x3(DMatrixRMaj matA , DMatrixRMaj matB ,
                                       DMatrixRMaj matResult , int numTrials) {

        MatrixMultFixedBlock ops = new MatrixMultFixedBlock();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ops.mult_4x3(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }


    public static void performTests( int size , int numTrials )
    {
        DMatrixRMaj matA = RandomMatrices_DDRM.createRandom(size,size,rand);
        DMatrixRMaj matB = RandomMatrices_DDRM.createRandom(size,size,rand);
        DMatrixRMaj matResult = RandomMatrices_DDRM.createRandom(size,size,rand);

        System.out.printf("12x12 multiply  standard: %7d  fixed6 %7d fixed3 %7d\n",
                mult(matA,matB,matResult,numTrials),
                multFixed12_2x6(matA,matB,matResult,numTrials),
                multFixed12_4x3(matA,matB,matResult,numTrials));
        System.gc();
    }

    public static void main( String args[] ) {

        // 12x12 multiply  standard:    2585  fixed6    1616 fixed3    1582
        // 12x12 multiply  standard:    1929  fixed6    1062 fixed3    1055
        performTests(12,1500000);
    }
}