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

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.alg.dense.linsol.chol.LinearSolverCholLDL;
import org.ejml.data.DenseMatrix64F;
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
                new LinearSolverChol(new CholeskyDecompositionInner(true)),
                A,b,numTrials));
        System.out.println("Solve Cholesky LDL     = "+solve(
                new LinearSolverCholLDL(new CholeskyDecompositionLDL()),
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