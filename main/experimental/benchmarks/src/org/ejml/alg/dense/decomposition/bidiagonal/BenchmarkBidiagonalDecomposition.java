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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.decomposition.BidiagonalDecomposition_F64;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkBidiagonalDecomposition {


    public static long evaluate(BidiagonalDecomposition_F64<RowMatrix_F64> alg , RowMatrix_F64 orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig.copy()) ) {
                throw new RuntimeException("Bad matrix");
            }
//            alg.getU(null,false,false);
//            alg.getV(null,false,false);

        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms(RowMatrix_F64 mat , int numTrials )
    {
        if( numTrials <= 0 ) return;
        System.out.println("row               = "+ evaluate(new BidiagonalDecompositionRow_D64(),mat,numTrials));
        System.out.println("tall              = "+ evaluate(new BidiagonalDecompositionTall_D64(),mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,5000,10000};
        int trials[] = new int[]{(int)4e6,(int)1e6,(int)1e5,200,1,1,1,1,1};
//        int trials[] = new int[]{(int)1e6,(int)2e5,(int)2e4,50,1,1,1,1,1};

        System.out.println("Square matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposition size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            RowMatrix_F64 mat = RandomMatrices_D64.createRandom(w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }

        System.out.println("Tall matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];
            int h = w*3;

            int t = trials[i];

            if( t == 0 ) continue;

            System.out.printf("Decomposition size w=%3d h=%3d for %12d trials\n",w,h,t);

            System.out.print("* Creating matrix ");
            RowMatrix_F64 mat = RandomMatrices_D64.createRandom(h,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,t);
        }
    }
}