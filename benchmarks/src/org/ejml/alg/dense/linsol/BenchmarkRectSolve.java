/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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
import org.ejml.alg.dense.linsol.svd.SolvePseudoInverse;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkRectSolve {
    private static final long SEED = 6;
    private static final Random rand = new Random();
    private static DenseMatrix64F A;
    private static DenseMatrix64F B;

    private static final boolean includeSet = true;

    public static long solveBenchmark( LinearSolver<DenseMatrix64F> solver , int numTrials ) {
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

        System.out.println("Pseudo Inverse  = "+ solveBenchmark(
                new SolvePseudoInverse(A.numRows,A.numCols),numTrials));
        System.out.println("QR house        = "+ solveBenchmark(
                new LinearSolverQrHouse(),numTrials));
        System.out.println("QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol(),numTrials));
    }

    public static void main( String args [] ) {
        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)2e6,(int)8e5,(int)3e5,800,3,1};
        int trialsX[] = new int[]{(int)1e5,(int)6e4,(int)1e4,(int)5e3,1000,500};

        System.out.println("Increasing matrix A size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving A size %3d for %12d trials\n",w,trials[i]);
            A = RandomMatrices.createRandom(w*2,w,rand);
            B = new DenseMatrix64F(w*2,2);

            runAlgorithms(trials[i]);
        }

        System.out.println("Increasing matrix B size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving B size %3d for %12d trials\n",w,trialsX[i]);
            A = RandomMatrices.createRandom(200,100,rand);
            B = new DenseMatrix64F(200,w);

            runAlgorithms(trialsX[i]/80);
        }

    }
}