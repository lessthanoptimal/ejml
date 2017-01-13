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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.SMatrixCmpC_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.NormOps_R64;
import org.ejml.sparse.ConvertSparseMatrix_F64;
import org.ejml.sparse.cmpcol.CommonOps_O64;
import org.ejml.sparse.cmpcol.NormOps_O64;
import org.ejml.sparse.cmpcol.RandomMatrices_O64;

import java.util.Random;

/**
 * Example showing how to construct and solve a linear system using sparse matrices
 *
 * @author Peter Abeles
 */
public class ExampleSparseMatrix {


    public static int ROWS = 100000;
    public static int COLS = 1000;
    public static int N = ROWS*COLS;

    public static void main(String[] args) {
        Random rand = new Random(234);

        // Create a 100000x1000 matrix that is 5% filled
        SMatrixCmpC_F64 A = RandomMatrices_O64.uniform(ROWS,COLS,(int)(N*0.05),rand);
        //          1000x1 matrix that is 70% filled
        SMatrixCmpC_F64 x = RandomMatrices_O64.uniform(COLS,1,(int)(COLS*0.7),rand);

        System.out.println("Done generating random matrices");
        // storage for the initial solution
        SMatrixCmpC_F64 y = new SMatrixCmpC_F64(ROWS,1,0);

        // To demonstration how to perform sparse math let's multiply:
        //                  y=A*x
        // Optional storage is set to null so that it will declare it internally
        long before = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            CommonOps_O64.mult(A,x,y,null,null);
        }
        long after = System.currentTimeMillis();

        System.out.println("norm = "+ NormOps_O64.fastNormF(y)+"  time = "+(after-before)+" ms");

        DMatrixRow_F64 Ad = ConvertSparseMatrix_F64.convert(A,(DMatrixRow_F64)null);
        DMatrixRow_F64 xd = ConvertSparseMatrix_F64.convert(x,(DMatrixRow_F64)null);
        DMatrixRow_F64 yd = new DMatrixRow_F64(y.numRows,y.numCols);

        before = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            CommonOps_R64.mult(Ad, xd, yd);
        }
        after = System.currentTimeMillis();
        System.out.println("norm = "+ NormOps_R64.fastNormF(yd)+"  time = "+(after-before)+" ms");

    }
}
