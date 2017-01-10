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

package org.ejml.alg.dense.linsol;

import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn_R64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrpHouseCol_R64;
import org.ejml.alg.dense.linsol.qr.SolvePseudoInverseQrp_R64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.factory.LinearSolverFactory_R64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolvePseudoInverse {
    private static final long SEED = 6;
    private static final Random rand = new Random();
    private static DMatrixRow_F64 A;
    private static DMatrixRow_F64 B;

    private static boolean includeSet = true;

    public static long solveBenchmark(LinearSolver<DMatrixRow_F64> solver , int numTrials ) {
        rand.setSeed(SEED);
        DMatrixRow_F64 X = new DMatrixRow_F64(B.numRows,B.numCols);

        solver = new LinearSolverSafe<DMatrixRow_F64>(solver);

        if( !includeSet ) solver.setA(A);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if(includeSet) solver.setA(A);

            solver.solve(B,X);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( int numTrials )
    {
//        System.out.println("solve SVD            = "+ solveBenchmark(
//                new SolvePseudoInverseSvd(),numTrials));
        System.out.println("solve Gen QRP Basic  = "+ solveBenchmark(
                new SolvePseudoInverseQrp_R64(new QRColPivDecompositionHouseholderColumn_R64(),false),numTrials));
        System.out.println("solve Gen QRP        = "+ solveBenchmark(
                new SolvePseudoInverseQrp_R64(new QRColPivDecompositionHouseholderColumn_R64(),true),numTrials));
        System.out.println("solve QRP Col Basic  = "+ solveBenchmark(
                new LinearSolverQrpHouseCol_R64(new QRColPivDecompositionHouseholderColumn_R64(),false),numTrials));
        System.out.println("solve QRP Col        = "+ solveBenchmark(
                new LinearSolverQrpHouseCol_R64(new QRColPivDecompositionHouseholderColumn_R64(),true),numTrials));
        System.out.println("solve QRP Col        = "+ solveBenchmark(
                LinearSolverFactory_R64.leastSquaresQrPivot(true,false),numTrials));
    }

    public static void main( String args [] ) {
        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)1e6,(int)5e5,(int)1e5,500,2,1};
        int trialsX[] = new int[]{(int)5e5,(int)4e5,(int)2e5,(int)7e4,4000,2000};

        System.out.println("Increasing matrix A size");
        for( int i = 3; i < size.length; i++ ) {
            int w = size[i];

            // create a singular matrix
            double singularValues[] = new double[w];
            for( int j = 0; j < w-1; j++ )
                singularValues[j] = 10+w-j;

            System.out.printf("Solving A size %3d for %12d trials\n",w,trials[i]);
            A = RandomMatrices_R64.createSingularValues(w, w, rand, singularValues);
            B = new DMatrixRow_F64(w,2);

            runAlgorithms(trials[i]);
        }

        System.out.println("Increasing matrix B size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving B size %3d for %12d trials\n",w,trialsX[i]);
            A = RandomMatrices_R64.createRandom(100,100,rand);
            B = new DMatrixRow_F64(100,w);

            runAlgorithms(trialsX[i]/80);
        }

    }
}