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

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.alg.dense.decomposition.lu.LUDecompositionNR;
import org.ejml.alg.dense.linsol.gj.GaussJordan;
import org.ejml.alg.dense.linsol.gj.GaussJordanNoPivot;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu;
import org.ejml.data.DenseMatrix64F;
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

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        System.out.println("invert GJ No Pivot     = "+ invertBenchmark(
                new GaussJordanNoPivot(),mat,numTrials));
        System.out.println("invert GJ              = "+ invertBenchmark(
                new GaussJordan(mat.numRows),mat,numTrials));
        System.out.println("invert LU              = "+ invertBenchmark(
                new LinearSolverLu(new LUDecompositionAlt()),mat,numTrials));
        System.out.println("invert LU  NR          = "+ invertBenchmark(
                new LinearSolverLu(new LUDecompositionNR()),mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)2e7,(int)5e6,(int)1e6,1000,3,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Inverting size %3d for %12d trials\n",w,trials[i]);
            DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);

            runAlgorithms(mat,trials[i]);
        }

    }
}
