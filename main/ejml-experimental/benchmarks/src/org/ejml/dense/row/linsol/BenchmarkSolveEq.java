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

package org.ejml.dense.row.linsol;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLuKJI_DDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouse_DDRM;
import org.ejml.dense.row.linsol.svd.SolvePseudoInverseSvd_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkSolveEq {
    private static final long SEED = 6;
    private static final Random rand = new Random();
    private static DMatrixRMaj A;
    private static DMatrixRMaj B;

    private static boolean includeSet = false;

    public static long solveBenchmark(LinearSolver<DMatrixRMaj> solver , int numTrials ) {
        rand.setSeed(SEED);
        DMatrixRMaj X = new DMatrixRMaj(B.numRows,B.numCols);
        RandomMatrices_DDRM.fillUniform(A,rand);
        RandomMatrices_DDRM.fillUniform(B,rand);

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
        System.out.println("solve LU A            = "+ solveBenchmark(
                new LinearSolverLu_DDRM(new LUDecompositionAlt_DDRM()),numTrials));
        System.out.println("solve LU B            = "+ solveBenchmark(
                new LinearSolverLuKJI_DDRM(new LUDecompositionAlt_DDRM()),numTrials));
        System.out.println("solve QR house        = "+ solveBenchmark(
                new LinearSolverQrHouse_DDRM(),numTrials));
        System.out.println("solve QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol_DDRM(),numTrials));
        System.out.println("solve PInv            = "+ solveBenchmark(
                new SolvePseudoInverseSvd_DDRM(),numTrials));
//        System.out.println("solve SVD             = "+ solveBenchmark(
//                new LinearSolverSvd(new SvdNumericalRecipes(A.numRows,A.numCols)),numTrials/8));
    }

    public static void main( String args [] ) {
        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)1e7,(int)5e6,(int)1e6,2000,8,3};
        int trialsX[] = new int[]{(int)5e5,(int)4e5,(int)2e5,(int)7e4,4000,2000};

        System.out.println("Increasing matrix A size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving A size %3d for %12d trials\n",w,trials[i]);
            A = RandomMatrices_DDRM.rectangle(w,w,rand);
            B = new DMatrixRMaj(w,2);

            runAlgorithms(trials[i]);
        }

        System.out.println("Increasing matrix B size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving B size %3d for %12d trials\n",w,trialsX[i]);
            A = RandomMatrices_DDRM.rectangle(100,100,rand);
            B = new DMatrixRMaj(100,w);

            runAlgorithms(trialsX[i]/80);
        }

    }
}