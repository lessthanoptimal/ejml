/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL_D64;
import org.ejml.alg.dense.linsol.chol.LinearSolverCholLDL_D64;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol_D64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkSolveSymPosDef {


    public static long solve( LinearSolver solver , DenseMatrix64F A, DenseMatrix64F b , int numTrials ) {

        DenseMatrix64F x = new DenseMatrix64F(A.numCols,b.numCols);

        if( !solver.setA(A) ) {
            throw new RuntimeException("Bad matrix");
        }

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            solver.solve(b,x);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F A , DenseMatrix64F b ,int numTrials )
    {
        System.out.println("Solve Cholesky         = "+solve(
                new LinearSolverChol_D64(new CholeskyDecompositionInner_D64(true)),
                A,b,numTrials));
        System.out.println("Solve Cholesky LDL     = "+solve(
                new LinearSolverCholLDL_D64(new CholeskyDecompositionLDL_D64()),
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
                DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);
                DenseMatrix64F symMat = new DenseMatrix64F(w,w);
                CommonOps.multTransA(mat,mat,symMat);
                DenseMatrix64F b = RandomMatrices.createRandom(w,w*2,rand);

                if(CommonOps.det(symMat) > 0 ) {
                    runAlgorithms(symMat,b,trials[i]);
                    break;
                }
            }
        }

    }
}