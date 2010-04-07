/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouse;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolveOver {
    private static long SEED = 6;
    private static Random rand = new Random();
    private static DenseMatrix64F A;
    private static DenseMatrix64F B;

    private static boolean includeSet = false;

    public static long solveBenchmark( LinearSolver solver , int numTrials ) {
        rand.setSeed(SEED);
        DenseMatrix64F X = new DenseMatrix64F(A.numCols,B.numCols);
        RandomMatrices.setRandom(A,rand);
        RandomMatrices.setRandom(B,rand);

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
        System.out.println("solve QR house        = "+ solveBenchmark(
                new LinearSolverQrHouse(),numTrials));
        System.out.println("solve QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol(),numTrials));
        System.out.println("solve PInv            = "+ solveBenchmark(
                new SolvePseudoInverse(),numTrials));
//        System.out.println("solve SVD             = "+ solveBenchmark(
//                new LinearSolverSvd(new SvdNumericalRecipes(A.numRows,A.numCols)),numTrials/8));
    }

    public static void main( String args [] ) {
        int trialsWith[] = new int[]{3000,2000,1500,1000,800,600};
        int trialsWithout[] = new int[]{14000,10000,8000,6000,4000,4000};

        int N = 10000;

        includeSet = true;
        System.out.println("Solving for least squares fitting type problems with set");
        for( int i = 1; i <= 6; i++ ) {

            System.out.printf("DOF = %d\n",i);
            A = new DenseMatrix64F(N,i);
            B = new DenseMatrix64F(N,1);

            runAlgorithms(trialsWith[i-1]/2);
        }

        System.out.println();
        includeSet = false;
        System.out.println("Solving for least squares fitting type problems without set");
        for( int i = 1; i <= 6; i++ ) {

            System.out.printf("DOF = %d\n",i);
            A = new DenseMatrix64F(N,i);
            B = new DenseMatrix64F(N,1);

            runAlgorithms(trialsWithout[i-1]);
        }

    }
}