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

package org.ejml.example;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.ops.ConvertDMatrixSparse;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.NormOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;

import java.util.Random;

/**
 * Example showing how to construct and solve a linear system using sparse matrices
 *
 * @author Peter Abeles
 */
public class ExampleSparseMatrix {


    public static int ROWS = 100000;
    public static int COLS = 1000;
    public static int XCOLS = 1;
    public static int N = ROWS*COLS;

    public static void main(String[] args) {
        Random rand = new Random(234);

        DMatrixSparseCSC Z = RandomMatrices_DSCC.rectangle(20,5,20,rand);
        Z.print();
        Z.printNonZero();

        // Create a 100000x1000 matrix that is 5% filled
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(ROWS,COLS,(int)(N*0.05),rand);
        //          1000x1 matrix that is 70% filled
        DMatrixSparseCSC x = RandomMatrices_DSCC.rectangle(COLS,XCOLS,(int)(XCOLS*COLS*0.7),rand);

        System.out.println("Done generating random matrices");
        // storage for the initial solution
        DMatrixSparseCSC y = new DMatrixSparseCSC(ROWS,XCOLS,0);

        // To demonstration how to perform sparse math let's multiply:
        //                  y=A*x
        // Optional storage is set to null so that it will declare it internally
        long before = System.currentTimeMillis();
        int []workA = new int[A.numRows];
        double []workB = new double[A.numRows];
        for (int i = 0; i < 100; i++) {
            CommonOps_DSCC.mult(A,x,y,workA,workB);
        }
        long after = System.currentTimeMillis();

        System.out.println("norm = "+ NormOps_DSCC.fastNormF(y)+"  time = "+(after-before)+" ms");

        DMatrixRMaj Ad = ConvertDMatrixSparse.convert(A,(DMatrixRMaj)null);
        DMatrixRMaj xd = ConvertDMatrixSparse.convert(x,(DMatrixRMaj)null);
        DMatrixRMaj yd = new DMatrixRMaj(y.numRows,y.numCols);

        before = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            CommonOps_DDRM.mult(Ad, xd, yd);
        }
        after = System.currentTimeMillis();
        System.out.println("norm = "+ NormOps_DDRM.fastNormF(yd)+"  time = "+(after-before)+" ms");

    }
}
