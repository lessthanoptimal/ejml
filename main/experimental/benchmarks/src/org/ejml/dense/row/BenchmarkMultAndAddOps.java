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

package org.ejml.dense.row;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.mult.MatrixMatrixMult_R64;

import java.util.Random;



/**
 * @author Peter Abeles
 */
public class BenchmarkMultAndAddOps {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 4000000;
    static int TRIALS_ADD = 100000000;

    public static long mult(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.mult(matA,matB,results);
//            MatrixMatrixMult.mult_small(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mult_alpha(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.mult(2.0,matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mult_alt(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_R64.mult_reorder(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranA(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multTransA(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranA_alpha(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multTransA(2.0,matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranB(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multTransB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranAB(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numCols,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multTransAB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAdd(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multAdd(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranA(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multAddTransA(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranB(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multAddTransB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranAB(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numCols,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.multAddTransAB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long addEquals(DMatrixRow_F64 matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.addEquals(results,matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long add(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.add(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long add_a_b(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.add(1.5,matA,3.4,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long addEqualBeta(DMatrixRow_F64 matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.addEquals(results,2.5,matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long minusEquals(DMatrixRow_F64 matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.subtractEquals(results, matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long minus(DMatrixRow_F64 matA , DMatrixRow_F64 matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 results = new DMatrixRow_F64(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.subtract(matA, matB, results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static void performMultTests(DMatrixRow_F64 matA , DMatrixRow_F64 matB ,
                                        DMatrixRow_F64 matC , DMatrixRow_F64 matD ,
                                        int numTrials )
    {
        System.out.printf("Mult:                  = %10d\n",
                mult(matA,matB,numTrials));
        System.out.printf("Mult Alpha:            = %10d\n",
                mult_alpha(matA,matB,numTrials));
        System.out.printf("Tran A Mult:           = %10d\n",
                multTranA(matD,matB,numTrials));
        System.out.printf("Tran A Mult Alpha:     = %10d\n",
                multTranA_alpha(matD,matB,numTrials));
        System.out.printf("Tran B Mult:           = %10d\n",
                multTranB(matA,matC,numTrials));
        System.out.printf("Tran AB Mult:          = %10d\n",
                multTranAB(matD,matC,numTrials));
        System.out.printf("Mult Add:              = %10d\n",
                multAdd(matA,matB,numTrials));
        System.out.printf("Tran A Mult Add:       = %10d\n",
                multAddTranA(matD,matB,numTrials));
        System.out.printf("Tran B Mult Add:       = %10d\n",
                multAddTranB(matA,matC,numTrials));
        System.out.printf("Tran AB Mult Add:      = %10d\n",
                multAddTranAB(matD,matC,numTrials));
    }

    public static void performAddTests(DMatrixRow_F64 matA , DMatrixRow_F64 matB ,
                                       DMatrixRow_F64 matC , DMatrixRow_F64 matD ,
                                       int numTrials )
    {
        System.out.printf("Add Equal:             = %10d\n",
                addEquals(matA,numTrials));
        System.out.printf("Add Equals b:          = %10d\n",
                addEqualBeta(matA,numTrials));
        System.out.printf("Add:                   = %10d\n",
                add(matA,matC,numTrials));
        System.out.printf("Add a b:               = %10d\n",
                add(matA,matC,numTrials));
        System.out.printf("Minus Equal:           = %10d\n",
                minusEquals(matA,numTrials));
        System.out.printf("Minus:                 = %10d\n",
                minus(matA,matC,numTrials));
    }

    public static void main( String args[] ) {
        System.out.println("Small Matrix Results:") ;
        int N = 2;
        DMatrixRow_F64 matA = RandomMatrices_R64.createRandom(N,N,rand);
        DMatrixRow_F64 matB = RandomMatrices_R64.createRandom(N,N,rand);
        DMatrixRow_F64 matC,matD;

        performMultTests(matA,matB,matB,matA,TRIALS_MULT*10);
        performAddTests(matA,matB,matB,matA,TRIALS_ADD);


        System.out.println();
        System.out.println("Large Matrix Results:") ;
        matA = RandomMatrices_R64.createRandom(1000,1000,rand);
        matB = RandomMatrices_R64.createRandom(1000,1000,rand);

        performMultTests(matA,matB,matB,matA,1);
        performAddTests(matA,matB,matB,matA,500);

        System.out.println();
        System.out.println("Large Not Square Matrix Results:") ;
        matA = RandomMatrices_R64.createRandom(600,1000,rand);
        matB = RandomMatrices_R64.createRandom(1000,600,rand);
        matC = RandomMatrices_R64.createRandom(600,1000,rand);
        matD = RandomMatrices_R64.createRandom(1000,600,rand);

        performMultTests(matA,matB,matC,matD,1);
        performAddTests(matA,matB,matC,matD,1000);
    }
}