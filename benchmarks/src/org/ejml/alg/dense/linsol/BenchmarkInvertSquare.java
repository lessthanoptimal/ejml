/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.linsol;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionNR;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
// TODO generate random stuff per trial
public class BenchmarkInvertSquare {


    public static long invertBenchmark( LinearSolver solver , DenseMatrix64F orig , int numTrials ) {
        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        solver.setA(orig);

        for( long i = 0; i < numTrials; i++ ) {
            solver.invert(A);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long invertUnrolledBenchmark( DenseMatrix64F orig , int numTrials ) {
        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            UnrolledInverseFromMinor.inv(orig,A);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long invertOpsBenchmark( DenseMatrix64F orig , int numTrials ) {
        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CommonOps.invert(orig,A);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
//        System.out.println("invert GJ No Pivot     = "+ invertBenchmark(
//                new GaussJordanNoPivot(),mat,numTrials));
//        System.out.println("invert GJ              = "+ invertBenchmark(
//                new GaussJordan(mat.numRows),mat,numTrials));
        System.out.println("invert LU              = "+ invertBenchmark(
                new LinearSolverLu(new LUDecompositionAlt()),mat,numTrials));
        System.out.println("invert LU  NR          = "+ invertBenchmark(
                new LinearSolverLu(new LUDecompositionNR()),mat,numTrials));
        System.out.println("invert Ops             = "+
                invertOpsBenchmark(mat,numTrials));
//        System.out.println("unrolled               = "+
//                invertUnrolledBenchmark(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,6,10,100,1000,2000,5000,10000};
        int trials[] = new int[]{(int)2e7,(int)5e6,(int)2e6,(int)1e6,1000,3,1,1,1};

        for( int i = 2; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Inverting size %3d for %12d trials\n",w,trials[i]);
            DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);

            runAlgorithms(mat,trials[i]);
        }

    }
}
