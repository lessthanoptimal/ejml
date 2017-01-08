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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.data.RowMatrix_F64;
import org.ejml.factory.DecompositionFactory_R64;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkCholeskyDecomposition {


    public static long choleskyL(RowMatrix_F64 orig , int numTrials ) {

        CholeskyDecompositionInner_R64 alg = new CholeskyDecompositionInner_R64(true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyU(RowMatrix_F64 orig , int numTrials ) {

        CholeskyDecompositionInner_R64 alg = new CholeskyDecompositionInner_R64(false);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyL_block(RowMatrix_F64 orig , int numTrials ) {

        CholeskyDecompositionBlock_R64 alg = new CholeskyDecompositionBlock_R64(
                EjmlParameters.BLOCK_WIDTH_CHOL);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }


    public static long choleskyBlockU(RowMatrix_F64 orig , int numTrials ) {

        CholeskyDecomposition_B64_to_R64 alg = new CholeskyDecomposition_B64_to_R64(false);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyBlockL(RowMatrix_F64 orig , int numTrials ) {

        CholeskyDecomposition_B64_to_R64 alg = new CholeskyDecomposition_B64_to_R64(true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig)) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyLDL(RowMatrix_F64 orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        CholeskyDecompositionLDL_R64 alg = new CholeskyDecompositionLDL_R64();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory_R64.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms(RowMatrix_F64 mat , int numTrials )
    {
        System.out.println("Lower            = "+ choleskyL(mat,numTrials));
//        System.out.println("Upper            = "+ choleskyU(mat,numTrials));
        System.out.println("Lower Block      = "+ choleskyL_block(mat,numTrials));
//        System.out.println("LDL              = "+ choleskyLDL(mat,numTrials));
//        System.out.println("Real Block U     = "+ choleskyBlockU(mat,numTrials));
        System.out.println("Real Block L     = "+ choleskyBlockL(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000,10000};
        int trials[] = new int[]{(int)2e7,(int)5e6,(int)1e6,1000,40,3,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 4; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposition size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            RowMatrix_F64 symMat = RandomMatrices_R64.createSymmPosDef(w,rand);
            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}