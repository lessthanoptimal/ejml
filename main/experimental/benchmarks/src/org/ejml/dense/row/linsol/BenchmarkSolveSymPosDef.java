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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.CommonOps_R64;
import org.ejml.dense.row.RandomMatrices_R64;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionInner_R64;
import org.ejml.dense.row.decomposition.chol.CholeskyDecompositionLDL_R64;
import org.ejml.dense.row.linsol.chol.LinearSolverCholLDL_R64;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_R64;
import org.ejml.interfaces.linsol.LinearSolver;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkSolveSymPosDef {


    public static long solve(LinearSolver solver , DMatrixRow_F64 A, DMatrixRow_F64 b , int numTrials ) {

        DMatrixRow_F64 x = new DMatrixRow_F64(A.numCols,b.numCols);

        if( !solver.setA(A) ) {
            throw new RuntimeException("Bad matrix");
        }

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            solver.solve(b,x);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms(DMatrixRow_F64 A , DMatrixRow_F64 b , int numTrials )
    {
        System.out.println("Solve Cholesky         = "+solve(
                new LinearSolverChol_R64(new CholeskyDecompositionInner_R64(true)),
                A,b,numTrials));
        System.out.println("Solve Cholesky LDL     = "+solve(
                new LinearSolverCholLDL_R64(new CholeskyDecompositionLDL_R64()),
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
                DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(w,w,rand);
                DMatrixRow_F64 symMat = new DMatrixRow_F64(w,w);
                CommonOps_R64.multTransA(mat,mat,symMat);
                DMatrixRow_F64 b = RandomMatrices_R64.createRandom(w,w*2,rand);

                if(CommonOps_R64.det(symMat) > 0 ) {
                    runAlgorithms(symMat,b,trials[i]);
                    break;
                }
            }
        }

    }
}