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
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouse_DDRM;
import org.ejml.dense.row.linsol.svd.SolvePseudoInverseSvd_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkRectSolve {
    private static final long SEED = 6;
    private static final Random rand = new Random();
    private static DMatrixRMaj A;
    private static DMatrixRMaj B;

    private static final boolean includeSet = true;

    public static long solveBenchmark(LinearSolverDense<DMatrixRMaj> solver , int numTrials ) {
        rand.setSeed(SEED);
        DMatrixRMaj X = new DMatrixRMaj(A.numCols,B.numCols);
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

        System.out.println("Pseudo Inverse  = "+ solveBenchmark(
                new SolvePseudoInverseSvd_DDRM(A.numRows,A.numCols),numTrials));
        System.out.println("QR house        = "+ solveBenchmark(
                new LinearSolverQrHouse_DDRM(),numTrials));
        System.out.println("QR house Col    = "+ solveBenchmark(
                new LinearSolverQrHouseCol_DDRM(),numTrials));
    }

    public static void main( String args [] ) {
        int size[] = new int[]{2,4,10,100,1000,2000};
        int trials[] = new int[]{(int)2e6,(int)8e5,(int)3e5,800,3,1};
        int trialsX[] = new int[]{(int)1e5,(int)6e4,(int)1e4,(int)5e3,1000,500};

        System.out.println("Increasing matrix A size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving A size %3d for %12d trials\n",w,trials[i]);
            A = RandomMatrices_DDRM.rectangle(w*2,w,rand);
            B = new DMatrixRMaj(w*2,2);

            runAlgorithms(trials[i]);
        }

        System.out.println("Increasing matrix B size");
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Solving B size %3d for %12d trials\n",w,trialsX[i]);
            A = RandomMatrices_DDRM.rectangle(200,100,rand);
            B = new DMatrixRMaj(200,w);

            runAlgorithms(trialsX[i]/80);
        }

    }
}