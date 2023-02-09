/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
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

    public static void main( String[] args ) {
        Random rand = new Random(234);

        // easy to work with sparse format, but hard to do computations with
        // NOTE: It is very important to you set 'initLength' to the actual number of elements in the final array
        //       If you don't it will be forced to thrash memory as it grows its internal data structures.
        //       Failure to heed this advice will make construction of large matrices 4x slower and use 2x more memory
        var work = new DMatrixSparseTriplet(5, 4, 5);
        work.addItem(0, 1, 1.2);
        work.addItem(3, 0, 3);
        work.addItem(1, 1, 22.21234);
        work.addItem(2, 3, 6);

        // convert into a format that's easier to perform math with
        DMatrixSparseCSC Z = DConvertMatrixStruct.convert(work, (DMatrixSparseCSC)null);

        // print the matrix to standard out in two different formats
        Z.print();
        System.out.println();
        Z.printNonZero();
        System.out.println();

        // Create a large matrix that is 5% filled
        DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(ROWS, COLS, (int)(ROWS*COLS*0.05), rand);
        //          large vector that is 70% filled
        DMatrixSparseCSC x = RandomMatrices_DSCC.rectangle(COLS, XCOLS, (int)(XCOLS*COLS*0.7), rand);

        System.out.println("Done generating random matrices");
        // storage for the initial solution
        var y = new DMatrixSparseCSC(ROWS, XCOLS, 0);
        var z = new DMatrixSparseCSC(ROWS, XCOLS, 0);

        // To demonstration how to perform sparse math let's multiply:
        //                  y=A*x
        // Optional storage is set to null so that it will declare it internally
        long before = System.currentTimeMillis();
        var workA = new IGrowArray(A.numRows);
        var workB = new DGrowArray(A.numRows);
        for (int i = 0; i < 100; i++) {
            CommonOps_DSCC.mult(A, x, y, workA, workB);
            CommonOps_DSCC.add(1.5, y, 0.75, y, z, workA, workB);
        }
        long after = System.currentTimeMillis();

        System.out.println("norm = " + NormOps_DSCC.fastNormF(y) + "  sparse time = " + (after - before) + " ms");

        DMatrixRMaj Ad = DConvertMatrixStruct.convert(A, (DMatrixRMaj)null);
        DMatrixRMaj xd = DConvertMatrixStruct.convert(x, (DMatrixRMaj)null);
        var yd = new DMatrixRMaj(y.numRows, y.numCols);
        var zd = new DMatrixRMaj(y.numRows, y.numCols);

        before = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            CommonOps_DDRM.mult(Ad, xd, yd);
            CommonOps_DDRM.add(1.5, yd, 0.75, yd, zd);
        }
        after = System.currentTimeMillis();
        System.out.println("norm = " + NormOps_DDRM.fastNormF(yd) + "  dense time  = " + (after - before) + " ms");
    }
}
