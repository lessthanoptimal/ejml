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

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RealMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkImplCommonOps {

    public static long extract_DenseMatrix64F( DenseMatrix64F src ,  DenseMatrix64F dst ,
                                               int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ImplCommonOps_DenseMatrix64F.extract(src, 0, 0, dst, 0, 0, src.numRows, src.numCols);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long extract_Matrix64F( RealMatrix64F src ,  RealMatrix64F dst , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ImplCommonOps_Matrix64F.extract(src,0,0,dst,0,0, src.getNumRows(), src.getNumCols());
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long extract_Common( DenseMatrix64F src ,  DenseMatrix64F dst , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps.extract(src, 0, src.numRows, 0 , src.numCols, dst, 0, 0 );
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static void benchmark( int N , int trials ) {
        Random rand = new Random(234);

        DenseMatrix64F src = new DenseMatrix64F(N,N);
        DenseMatrix64F dst = new DenseMatrix64F(N,N);

        RandomMatrices.addRandom(src,0,100,rand);

        System.out.println("N = "+N);
        System.out.println("extract DenseMatrix64F = "+extract_DenseMatrix64F(src,dst,trials));
        System.out.println("extract Matrix64F      = "+extract_Matrix64F(src,dst,trials));
        System.out.println("extract Common         = "+extract_Common(src, dst, trials));
    }

    public static void main( String args[] ) {
        benchmark(5,10000000);
        benchmark(100,100000);
        benchmark(1000,500);
        benchmark(2000,75);
    }
}
