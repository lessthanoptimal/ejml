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

import org.ejml.alg.dense.decomposition.qr.QRColPivDecompositionHouseholderColumn;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrpHouseCol;
import org.ejml.alg.dense.linsol.qr.SolvePseudoInverseQrp;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolver;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolvePseudoInverse {
    private static final long SEED = 6;
    private static final Random rand = new Random();
    private static DenseMatrix64F A;
    private static DenseMatrix64F B;

    private static boolean includeSet = true;

    public static long solveBenchmark( LinearSolver<DenseMatrix64F> solver , int numTrials ) {
        rand.setSeed(SEED);
        DenseMatrix64F X = new DenseMatrix64F(B.numRows,B.numCols);

        solver = new LinearSolverSafe<DenseMatrix64F>(solver);

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
                new SolvePseudoInverseQrp(new QRColPivDecompositionHouseholderColumn(),false),numTrials));
        System.out.println("solve Gen QRP        = "+ solveBenchmark(
                new SolvePseudoInverseQrp(new QRColPivDecompositionHouseholderColumn(),true),numTrials));
        System.out.println("solve QRP Col Basic  = "+ solveBenchmark(
                new LinearSolverQrpHouseCol(new QRColPivDecompositionHouseholderColumn(),false),numTrials));
        System.out.println("solve QRP Col        = "+ solveBenchmark(
                new LinearSolverQrpHouseCol(new QRColPivDecompositionHouseholderColumn(),true),numTrials));
    }

    public static void main( String args [] ) {
        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)1e6,(int)5e5,(int)1e5,500,2,1};
        int trialsX[] = new int[]{(int)5e5,(int)4e5,(int)2e5,(int)7e4,4000,2000};

        System.out.println("Increasing matrix A size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            // create a singular matrix
            double singularValues[] = new double[w];
            for( int j = 0; j < w-1; j++ )
                singularValues[j] = 10+w-j;

            System.out.printf("Solving A size %3d for %12d trials\n",w,trials[i]);
            A = RandomMatrices.createSingularValues(w, w, rand, singularValues);
            B = new DenseMatrix64F(w,2);

            runAlgorithms(trials[i]);
        }

        System.out.println("Increasing matrix B size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving B size %3d for %12d trials\n",w,trialsX[i]);
            A = RandomMatrices.createRandom(100,100,rand);
            B = new DenseMatrix64F(100,w);

            runAlgorithms(trialsX[i]/80);
        }

    }
}