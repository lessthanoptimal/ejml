/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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
        evaluateMatrix(5000, 5);
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
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
        System.out.println();
        System.out.println("---------- Tall ----------------");
        A = RandomMatrices.createRandom(2*length,length,rand);
        System.out.println("Block     : "+block(A, n,EjmlParameters.BLOCK_WIDTH));
        System.out.println("Block 20  : "+block(A, n, 20));
        System.out.println("Block 30  : "+block(A, n, 30));
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
        System.out.println("---------- Wide ----------------");
        A = RandomMatrices.createRandom(length,2*length,rand);
        System.out.println("Block     : "+block(A, n, EjmlParameters.BLOCK_WIDTH));
        System.out.println("Block 20  : "+block(A, n, 20));
        System.out.println("Block 30  : "+block(A, n, 30));
        System.out.println("Standard  : "+standard(A, n));
        System.out.println("Common    : "+common(A, n));
    }
}
