/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.MatrixOps_MT_DDRB;
import org.ejml.dense.row.CommonOps_MT_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;

/**
 * For many operations EJML provides block matrix support. These block or tiled matrices are designed to reduce
 * the number of cache misses which can kill performance when working on large matrices. A critical tuning parameter
 * is the block size and this is system specific. The example below shows you how this parameter can be optimized.
 *
 * @author Peter Abeles
 */
public class OptimizingLargeMatrixPerformance {

    public static void main( String[] args ) {
        // Create larger matrices to experiment with
        var rand = new Random(0xBEEF);
        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(3000,3000,-1,1,rand);
        DMatrixRMaj B = A.copy();
        DMatrixRMaj C = A.createLike();

        // Since we are dealing with larger matrices let's use the concurrent implementation. By default
        printTime("Row-Major Multiplication:",()-> CommonOps_MT_DDRM.mult(A,B,C));

        // Converts A into a block matrix and creates a new matrix while leaving A unmodified
        DMatrixRBlock Ab = MatrixOps_DDRB.convert(A);
        // Converts A into a block matrix, but modifies it's internal array inplace. The returned block matrix
        // will share the same data array as the input. Much more memory efficient, but you need to be careful.
        DMatrixRBlock Bb = MatrixOps_DDRB.convertInplace(B,null,null);
        DMatrixRBlock Cb = Ab.createLike();

        // Since we are dealing with larger matrices let's use the concurrent implementation. By default
        printTime("Block Multiplication:    ",()-> MatrixOps_MT_DDRB.mult(Ab,Bb,Cb));

        // Can we make this faster? Probably by adjusting the block size. This is system dependent so let's
        // try a range of values
        int defaultBlockWidth = EjmlParameters.BLOCK_WIDTH;
        System.out.println("Default Block Size: "+defaultBlockWidth);
        for ( int block : new int[]{10,20,30,50,70,100,140,200,500}) {
            EjmlParameters.BLOCK_WIDTH = block;

            // Need to create the block matrices again since we changed the block size
            DMatrixRBlock Ac = MatrixOps_DDRB.convert(A);
            DMatrixRBlock Bc = MatrixOps_DDRB.convert(B);
            DMatrixRBlock Cc = Ac.createLike();
            printTime("Block "+EjmlParameters.BLOCK_WIDTH+": ",()-> MatrixOps_MT_DDRB.mult(Ac,Bc,Cc));
        }

        // On my system the optimal block size is around 100 and has an improvement of about 5%
        // On some architectures the improvement can be substantial in others the default value is very reasonable

        // Some decompositions will switch to a block format automatically. Matrix multiplication might in the
        // future and others too. The main reason this hasn't happened for it to be memory efficient it would
        // need to modify then undo the modification for input matrices which would be very confusion if you're
        // writing concurrent code.
    }

    public static void printTime( String message, Process timer ) {
        System.out.println("Processing...");
        long time0 = System.nanoTime();
        timer.process();
        long time1 = System.nanoTime();
        System.out.println(message+" "+((time1-time0)*1e-6)+" (ms)");
    }

    private interface Process {
        void process();
    }
}
