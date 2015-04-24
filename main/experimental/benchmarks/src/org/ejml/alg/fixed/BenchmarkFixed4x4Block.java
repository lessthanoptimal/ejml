/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.fixed;

import org.ejml.alg.dense.mult.Fixed4x4Block;
import org.ejml.alg.dense.mult.MatrixMultFixed4x4Block;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Feeds the algorithms matrices that are closer and closer to being singular
 * and sees at which point they break.
 *
 * @author Peter Abeles
 */
public class BenchmarkFixed4x4Block {
    private static Random rand = new Random(234);

    private static DenseMatrix64F dm32x32_a = new DenseMatrix64F(32,32);
    private static DenseMatrix64F dm32x32_b = new DenseMatrix64F(32,32);
    private static DenseMatrix64F dm32x32_c = new DenseMatrix64F(32,32);

    private static Fixed4x4Block fixed32x32_a = new Fixed4x4Block(32/4,32/4);
    private static Fixed4x4Block fixed32x32_b = new Fixed4x4Block(32/4,32/4);
    private static Fixed4x4Block fixed32x32_c = new Fixed4x4Block(32/4,32/4);

    public static long benchmark(DenseMatrix64F a, DenseMatrix64F b , DenseMatrix64F c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps.mult(a,b,c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmark(Fixed4x4Block a, Fixed4x4Block b , Fixed4x4Block c , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            MatrixMultFixed4x4Block.mult(a, b, c);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long benchmarkConv(DenseMatrix64F a, DenseMatrix64F b , DenseMatrix64F c , int numTrials ) {

        Fixed4x4Block fa = new Fixed4x4Block(a.numRows/4,a.numCols/4);
        Fixed4x4Block fb = new Fixed4x4Block(a.numRows/4,a.numCols/4);
        Fixed4x4Block fc = new Fixed4x4Block(a.numRows/4,a.numCols/4);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            fa.set(a,0,0);
            fb.set(b,0,0);
            MatrixMultFixed4x4Block.mult(fa, fb, fc);
        }

        return System.currentTimeMillis() - prev;
    }



    public static void main( String arg[] ) {
        RandomMatrices.setRandom(dm32x32_a, rand);
        RandomMatrices.setRandom(dm32x32_b, rand);
        RandomMatrices.setRandom(dm32x32_c, rand);

        fixed32x32_a.set(dm32x32_a, 0, 0);
        fixed32x32_b.set(dm32x32_b, 0, 0);
        fixed32x32_c.set(dm32x32_c, 0, 0);

        int numTrials = 150000;

        System.out.println("Dense  32x32 = "+benchmark(dm32x32_a,dm32x32_b,dm32x32_c,numTrials));
        System.out.println("Fixed  32x32 = "+benchmark(fixed32x32_a,fixed32x32_b,fixed32x32_c,numTrials));
        System.out.println("FixedC 32x32 = "+benchmarkConv(dm32x32_a, dm32x32_b, dm32x32_c, numTrials));
    }
}
