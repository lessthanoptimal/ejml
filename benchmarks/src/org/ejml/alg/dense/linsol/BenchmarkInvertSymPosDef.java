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

import org.ejml.EjmlParameters;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.alg.dense.linsol.chol.LinearSolverCholBlock64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CovarianceOps;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkInvertSymPosDef {

    public static long invertCovar( DenseMatrix64F orig , int numTrials ) {

        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            CovarianceOps.invert(orig,A);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long invertCholesky( LinearSolver<DenseMatrix64F> alg , DenseMatrix64F orig , int numTrials ) {

        alg = new LinearSolverSafe<DenseMatrix64F>(alg);
        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.setA(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
            alg.invert(A);
        }

        return System.currentTimeMillis() - prev;
    }


    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {

        System.out.println("invert covariance         = "+ invertCovar(mat,numTrials));
//        System.out.println("invert GJ No Pivot     = "+ invertGJ_NoPivot(mat,numTrials));
//        System.out.println("invert GJ               = "+ invertGJ(mat,numTrials));
//        System.out.println("invert LU-NR            = "+ invertLU_nr(mat,numTrials));
//        System.out.println("invert LU-Alt           = "+ invertLU_alt(mat,numTrials));
        System.out.println("invert Cholesky Inner       = "+ invertCholesky(
                new LinearSolverChol(new CholeskyDecompositionInner( true)),
                mat,numTrials));
        System.out.println("invert Cholesky Block Dense = "+ invertCholesky(
                new LinearSolverChol(new CholeskyDecompositionBlock( EjmlParameters.BLOCK_WIDTH_CHOL)),
                mat,numTrials));
//        System.out.println("invert default              = "+ invertCholesky(
//                LinearSolverFactory.symmetric(mat.numRows),
//                mat,numTrials));
//        System.out.println("invert CholeskyLDL          = "+ invertCholesky(
//                new LinearSolverCholLDL(new CholeskyDecompositionLDL()),
//                mat,numTrials));
        System.out.println("invert CholeskyBlock64      = "+ invertCholesky(
                new LinearSolverCholBlock64(),
                mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,6,10,100,1000,2000,4000,8000};
        int trials[] = new int[]{(int)1e7,(int)3e6,(int)1e6,(int)4e5,1000,3,1,1,1};

        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Inverting size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F symMat = RandomMatrices.createSymmPosDef(w,rand);
            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}