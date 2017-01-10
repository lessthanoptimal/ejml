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

package org.ejml.dense.row.misc;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.data.Matrix_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkImplCommonOps {

    public static long extract_DMatrixRow_F64(DMatrixRow_F64 src , DMatrixRow_F64 dst ,
                                              int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ImplCommonOps_R64.extract(src, 0, 0, dst, 0, 0, src.numRows, src.numCols);
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long extract_Matrix64F(Matrix_F64 src , Matrix_F64 dst , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            ImplCommonOps_F64.extract(src,0,0,dst,0,0, src.getNumRows(), src.getNumCols());
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static long extract_Common(DMatrixRow_F64 src , DMatrixRow_F64 dst , int numTrials) {

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.extract(src, 0, src.numRows, 0 , src.numCols, dst, 0, 0 );
        }
        long curr = System.currentTimeMillis();

        return curr-prev;
    }

    public static void benchmark( int N , int trials ) {
        Random rand = new Random(234);

        DMatrixRow_F64 src = new DMatrixRow_F64(N,N);
        DMatrixRow_F64 dst = new DMatrixRow_F64(N,N);

        RandomMatrices_R64.addRandom(src,0,100,rand);

        System.out.println("N = "+N);
        System.out.println("extract DMatrixRow_F64 = "+extract_DMatrixRow_F64(src,dst,trials));
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
