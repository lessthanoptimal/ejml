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

import org.ejml.alg.dense.linsol.qr.LinearSolverQrBlock64_R64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol_R64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseTran_R64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouse_R64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.factory.LinearSolverFactory_R64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolveOver {
    private static final long SEED = 6;
    private static Random rand = new Random();
    private static DMatrixRow_F64 A;
    private static DMatrixRow_F64 B;

    private static boolean includeSet = false;

    public static long solveBenchmark(LinearSolver<DMatrixRow_F64> solver , int numTrials ) {
        rand.setSeed(SEED);
        DMatrixRow_F64 X = new DMatrixRow_F64(A.numCols,B.numCols);
        RandomMatrices_R64.setRandom(A,rand);
        RandomMatrices_R64.setRandom(B,rand);

        DMatrixRow_F64 B_tmp = new DMatrixRow_F64(B.numRows,B.numCols);

        if( !includeSet ) solver.setA(A);

        DMatrixRow_F64 A_copy = solver.modifiesA() ? A.copy() : A;

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {

            if( solver.modifiesA() ) {
                A_copy.set(A);
            }

            if(includeSet) solver.setA(A_copy);

            if( solver.modifiesB() ) {
                B_tmp.set(B);
                solver.solve(B_tmp,X);
            } else {
                solver.solve(B,X);
            }

        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( int numTrials )
    {
        System.out.println("  solve QR house        = "+ solveBenchmark(
                new LinearSolverQrHouse_R64(),numTrials));
        System.out.println("  solve QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol_R64(),numTrials));
        System.out.println("  solve QR tran        = "+ solveBenchmark(
                new LinearSolverQrHouseTran_R64(),numTrials));
        System.out.println("  solve QR Block64      = "+ solveBenchmark(
                new LinearSolverQrBlock64_R64(),numTrials));
        System.out.println("  Selected              = "+ solveBenchmark(
                LinearSolverFactory_R64.leastSquares(A.numRows, A.numCols),numTrials));
//        System.out.println("  solve PInv            = "+ solveBenchmark(
//                new SolvePseudoInverse(),numTrials));
    }

    public static void main( String args [] ) {
        int trialsWith[] = new int[]{3000000,1000000,200000,400,3,1,1,1,1,1};

        int width[] = new int[]{2,4,10,100,500,1000,2000,5000,10000};

        includeSet = true;
        System.out.println("Solving for least squares fitting type problems with set");
        for( int i = 0; i < width.length; i++ ) {
            int N = width[i]*3;

            System.out.printf("height %d Width = %d   trials = %d\n",N,width[i],trialsWith[i]);
            A = new DMatrixRow_F64(N,width[i]);
            B = new DMatrixRow_F64(N,1);

            runAlgorithms(trialsWith[i]);
        }

        System.out.println();
        includeSet = false;
        System.out.println("Solving for least squares fitting type problems without set");
        for( int i = 0; i < width.length; i++ ) {
            int N = width[i]*3;

            System.out.printf("height %d Width = %d   trials = %d\n",N,width[i],trialsWith[i]);
            A = new DMatrixRow_F64(N,width[i]);
            B = new DMatrixRow_F64(N,1);

            runAlgorithms(trialsWith[i]);
        }

    }
}