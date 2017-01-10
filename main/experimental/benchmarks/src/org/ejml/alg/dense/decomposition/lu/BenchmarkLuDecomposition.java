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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.LUDecomposition;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at decomposing square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkLuDecomposition {


    public static void benchmark(LUDecomposition<DMatrixRow_F64> lu , DMatrixRow_F64 orig , int numTrials ) {

        long prev = System.currentTimeMillis();
        for( long i = 0; i < numTrials; i++ ) {
            if( !lu.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        long time = System.currentTimeMillis() - prev;
        System.out.printf(" %20s time = %7d\n",lu.getClass().getSimpleName(),time);
    }


    private static void runAlgorithms(DMatrixRow_F64 mat , int numTrials )
    {
        benchmark(new LUDecompositionAlt_R64(),mat,numTrials);
        benchmark(new LUDecompositionNR_R64(),mat,numTrials);
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000};
        int trials[] = new int[]{(int)1e7,(int)5e6,(int)1e6,2000,40,3,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposing size %3d for %12d trials\n",w,trials[i]);

//            System.out.print("* Creating matrix ");
            DMatrixRow_F64 symMat = RandomMatrices_R64.createRandom(w,w,-1,1,rand);
//            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}