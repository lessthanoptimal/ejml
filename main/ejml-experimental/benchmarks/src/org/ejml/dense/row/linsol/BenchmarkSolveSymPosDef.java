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
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionInner_DDRM;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionLDL_DDRM;
import org.ejml.dense.row.linsol.chol.LinearSolverCholLDL_DDRM;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkSolveSymPosDef {


    public static long solve(LinearSolverDense solver , DMatrixRMaj A, DMatrixRMaj b , int numTrials ) {

        DMatrixRMaj x = new DMatrixRMaj(A.numCols,b.numCols);

        if( !solver.setA(A) ) {
            throw new RuntimeException("Bad matrix");
        }

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            solver.solve(b,x);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms(DMatrixRMaj A , DMatrixRMaj b , int numTrials )
    {
        System.out.println("Solve Cholesky         = "+solve(
                new LinearSolverChol_DDRM(new CholeskyDecompositionInner_DDRM(true)),
                A,b,numTrials));
        System.out.println("Solve Cholesky LDL     = "+solve(
                new LinearSolverCholLDL_DDRM(new CholeskyDecompositionLDL_DDRM()),
                A,b,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,1000};
        int trials[] = new int[]{(int)6e6,(int)1e6,(int)2e5,500,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Matrix A size %3d for %12d trials\n",w,trials[i]);

            while( true ) {
                DMatrixRMaj mat = RandomMatrices_DDRM.rectangle(w,w,rand);
                DMatrixRMaj symMat = new DMatrixRMaj(w,w);
                CommonOps_DDRM.multTransA(mat,mat,symMat);
                DMatrixRMaj b = RandomMatrices_DDRM.rectangle(w,w*2,rand);

                if(CommonOps_DDRM.det(symMat) > 0 ) {
                    runAlgorithms(symMat,b,trials[i]);
                    break;
                }
            }
        }

    }
}