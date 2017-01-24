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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.mult.MatrixMatrixMult_DDRM;

import java.util.Random;



/**
 * @author Peter Abeles
 */
public class BenchmarkMultAndAddOps {

    static Random rand = new Random(234234);

    static int TRIALS_MULT = 4000000;
    static int TRIALS_ADD = 100000000;

    public static long mult(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.mult(matA,matB,results);
//            MatrixMatrixMult.mult_small(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mult_alpha(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.mult(2.0,matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long mult_alt(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            MatrixMatrixMult_DDRM.mult_reorder(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranA(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multTransA(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranA_alpha(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multTransA(2.0,matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranB(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multTransB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multTranAB(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numCols,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multTransAB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAdd(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multAdd(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranA(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numCols,matB.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multAddTransA(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranB(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multAddTransB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long multAddTranAB(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numCols,matB.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.multAddTransAB(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long addEquals(DMatrixRMaj matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.addEquals(results,matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long add(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.add(matA,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long add_a_b(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.add(1.5,matA,3.4,matB,results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long addEqualBeta(DMatrixRMaj matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.addEquals(results,2.5,matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long minusEquals(DMatrixRMaj matA , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.subtractEquals(results, matA);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long minus(DMatrixRMaj matA , DMatrixRMaj matB , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRMaj results = new DMatrixRMaj(matA.numRows,matA.numCols);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_DDRM.subtract(matA, matB, results);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static void performMultTests(DMatrixRMaj matA , DMatrixRMaj matB ,
                                        DMatrixRMaj matC , DMatrixRMaj matD ,
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

    public static void performAddTests(DMatrixRMaj matA , DMatrixRMaj matB ,
                                       DMatrixRMaj matC , DMatrixRMaj matD ,
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
        DMatrixRMaj matA = RandomMatrices_DDRM.rectangle(N,N,rand);
        DMatrixRMaj matB = RandomMatrices_DDRM.rectangle(N,N,rand);
        DMatrixRMaj matC,matD;

        performMultTests(matA,matB,matB,matA,TRIALS_MULT*10);
        performAddTests(matA,matB,matB,matA,TRIALS_ADD);


        System.out.println();
        System.out.println("Large Matrix Results:") ;
        matA = RandomMatrices_DDRM.rectangle(1000,1000,rand);
        matB = RandomMatrices_DDRM.rectangle(1000,1000,rand);

        performMultTests(matA,matB,matB,matA,1);
        performAddTests(matA,matB,matB,matA,500);

        System.out.println();
        System.out.println("Large Not Square Matrix Results:") ;
        matA = RandomMatrices_DDRM.rectangle(600,1000,rand);
        matB = RandomMatrices_DDRM.rectangle(1000,600,rand);
        matC = RandomMatrices_DDRM.rectangle(600,1000,rand);
        matD = RandomMatrices_DDRM.rectangle(1000,600,rand);

        performMultTests(matA,matB,matC,matD,1);
        performAddTests(matA,matB,matC,matD,1000);
    }
}