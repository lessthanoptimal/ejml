/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.misc;

import org.ejml.EjmlParameters;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkTranspose {
    static Random rand = new Random(234);

    public static long square( DenseMatrix64F mat , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            TransposeAlgs.square(mat);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long block( DenseMatrix64F mat , int numTrials , int blockLength ) {
        DenseMatrix64F tran = new DenseMatrix64F(mat.numCols,mat.numRows);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            TransposeAlgs.block(mat,tran,blockLength);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long standard( DenseMatrix64F mat , int numTrials) {
        DenseMatrix64F tran = new DenseMatrix64F(mat.numCols,mat.numRows);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            TransposeAlgs.standard(mat,tran);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long common( DenseMatrix64F mat , int numTrials) {
        DenseMatrix64F tran = new DenseMatrix64F(mat.numCols,mat.numRows);

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.transpose(mat,tran);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }


    public static void main( String args[] ) {

//        evaluateMatrix(3, 50000000);
//        evaluateMatrix(20, 1000000);
//        evaluateMatrix(120, 50000);
//        evaluateMatrix(EjmlParameters.TRANSPOSE_SWITCH+1, 4000);
        evaluateMatrix(2000, 80);
//        evaluateMatrix(10000, 1);
    }

    private static void evaluateMatrix( int length , int n) {
        System.out.println("*** Size "+length);
        DenseMatrix64F A = RandomMatrices.createRandom(length,length,rand);

        System.out.println("---------- Square ----------------");
        System.out.println("In place  : "+square(A, n));
        System.out.println("Block     : "+block(A, n, EjmlParameters.BLOCK_WIDTH));
        System.out.println("Block 15  : "+block(A, n, 15));
        System.out.println("Block 20  : "+block(A, n, 20));
        System.out.println("Block 30  : "+block(A, n, 30));
        System.out.println("Block 50  : "+block(A, n, 50));
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
        System.out.println();
        System.out.println("---------- Tall ----------------");
        A = RandomMatrices.createRandom(2*length,length,rand);
        System.out.println("Block     : "+block(A, n,EjmlParameters.BLOCK_WIDTH));
        System.out.println("Block 20  : "+block(A, n, 20));
        System.out.println("Block 30  : "+block(A, n, 30));
        System.out.println("Block 50  : "+block(A, n, 50));
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
        System.out.println("---------- Wide ----------------");
        A = RandomMatrices.createRandom(length,2*length,rand);
        System.out.println("Block     : "+block(A, n, EjmlParameters.BLOCK_WIDTH));
        System.out.println("Block 20  : "+block(A, n, 20));
        System.out.println("Block 30  : "+block(A, n, 30));
        System.out.println("Block 50  : "+block(A, n, 50));
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
    }
}
