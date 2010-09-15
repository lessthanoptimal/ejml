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

import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.alg.dense.linsol.chol.LinearSolverCholLDL;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkInvertSymPosDef {


    public static long invertCholesky( LinearSolver alg , DenseMatrix64F orig , int numTrials ) {

        DenseMatrix64F A = new DenseMatrix64F(orig.numRows,orig.numCols);

        if( !alg.setA(orig) ) {
            throw new RuntimeException("Bad matrix");
        }

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            alg.invert(A);
        }

        return System.currentTimeMillis() - prev;
    }


    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {

//        System.out.println("invert GJ No Pivot     = "+ invertGJ_NoPivot(mat,numTrials));
//        System.out.println("invert GJ               = "+ invertGJ(mat,numTrials));
//        System.out.println("invert LU-NR            = "+ invertLU_nr(mat,numTrials));
//        System.out.println("invert LU-Alt           = "+ invertLU_alt(mat,numTrials));
        System.out.println("invert Cholesky         = "+ invertCholesky(
                new LinearSolverChol(new CholeskyDecompositionInner( false,true)),
                mat,numTrials));
        System.out.println("invert CholeskyLDL        = "+ invertCholesky(
                new LinearSolverCholLDL(new CholeskyDecompositionLDL()),
                mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,1000,2000,4000};
        int trials[] = new int[]{(int)2e7,(int)5e6,(int)1e6,1000,3,1,1};

        for( int i = 3; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Inverting size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F symMat = RandomMatrices.createSymmPosDef(w,rand);
            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}