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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkHessenberg {


    public static long basic(RowMatrix_F64 orig , int numTrials ) {

        HessenbergSimilarDecomposition_D64 alg = new HessenbergSimilarDecomposition_D64();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

//    public static long alt( RowMatrix_F64 orig , int numTrials ) {
//
//        HessenbergSimilarDecompositionAlt alg = new HessenbergSimilarDecompositionAlt();
//
//        long prev = System.currentTimeMillis();
//
//        for( long i = 0; i < numTrials; i++ ) {
//            if( !alg.decompose(orig) ) {
//                throw new RuntimeException("Bad matrix");
//            }
//        }
//
//        return System.currentTimeMillis() - prev;
//    }

    private static void runAlgorithms(RowMatrix_F64 mat , int numTrials )
    {
        System.out.println("basic            = "+ basic(mat,numTrials));
//        System.out.println("alt              = "+ alt(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000};
        int trials[] = new int[]{(int)2e6,(int)5e5,(int)1e5,400,15,3,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decompositing size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            RowMatrix_F64 mat = RandomMatrices_D64.createRandom(w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }
    }
}