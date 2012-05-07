/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * EJML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EJML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EJML.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ejml.alg.dense.linsol;

import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolveOver {
    private static final long SEED = 6;
    private static Random rand = new Random();
    private static DenseMatrix64F A;
    private static DenseMatrix64F B;

    private static boolean includeSet = false;

    public static long solveBenchmark( LinearSolver<DenseMatrix64F> solver , int numTrials ) {
        rand.setSeed(SEED);
        DenseMatrix64F X = new DenseMatrix64F(A.numCols,B.numCols);
        RandomMatrices.setRandom(A,rand);
        RandomMatrices.setRandom(B,rand);

        DenseMatrix64F B_tmp = new DenseMatrix64F(B.numRows,B.numCols);

        if( !includeSet ) solver.setA(A);

        DenseMatrix64F A_copy = solver.modifiesA() ? A.<DenseMatrix64F>copy() : A;

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
//        System.out.println("  solve QR house        = "+ solveBenchmark(
//                new LinearSolverQrHouse(),numTrials));
        System.out.println("  solve QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol(),numTrials));
//        System.out.println("  solve QR tran        = "+ solveBenchmark(
//                new LinearSolverQrHouseTran(),numTrials));
//        System.out.println("  solve QR Block64      = "+ solveBenchmark(
//                new LinearSolverQrBlock64(),numTrials));
        System.out.println("  Selected              = "+ solveBenchmark(
                LinearSolverFactory.leastSquares(A.numRows,A.numCols),numTrials));
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
            A = new DenseMatrix64F(N,width[i]);
            B = new DenseMatrix64F(N,1);

            runAlgorithms(trialsWith[i]);
        }

        System.out.println();
        includeSet = false;
        System.out.println("Solving for least squares fitting type problems without set");
        for( int i = 0; i < width.length; i++ ) {
            int N = width[i]*3;

            System.out.printf("height %d Width = %d   trials = %d\n",N,width[i],trialsWith[i]);
            A = new DenseMatrix64F(N,width[i]);
            B = new DenseMatrix64F(N,1);

            runAlgorithms(trialsWith[i]);
        }

    }
}