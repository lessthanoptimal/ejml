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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkMatrixMatrixMultFixedBlock {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 10000000;

    public static long mult(DMatrixRow_F64 matA , DMatrixRow_F64 matB ,
                            DMatrixRow_F64 matResult , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.mult(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multFixed12_2x6(DMatrixRow_F64 matA , DMatrixRow_F64 matB ,
                                       DMatrixRow_F64 matResult , int numTrials) {

        MatrixMultFixedBlock ops = new MatrixMultFixedBlock();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ops.mult_2x6(matA,matB,matResult);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multFixed12_4x3(DMatrixRow_F64 matA , DMatrixRow_F64 matB ,
                                       DMatrixRow_F64 matResult , int numTrials) {

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
        DMatrixRow_F64 matA = RandomMatrices_R64.createRandom(size,size,rand);
        DMatrixRow_F64 matB = RandomMatrices_R64.createRandom(size,size,rand);
        DMatrixRow_F64 matResult = RandomMatrices_R64.createRandom(size,size,rand);

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