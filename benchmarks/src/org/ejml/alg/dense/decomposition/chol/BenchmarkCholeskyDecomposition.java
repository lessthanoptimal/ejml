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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.EjmlParameters;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkCholeskyDecomposition {


    public static long choleskyL( DenseMatrix64F orig , int numTrials ) {

        CholeskyDecompositionInner alg = new CholeskyDecompositionInner(true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyU( DenseMatrix64F orig , int numTrials ) {

        CholeskyDecompositionInner alg = new CholeskyDecompositionInner(false);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyL_block( DenseMatrix64F orig , int numTrials ) {

        CholeskyDecompositionBlock alg = new CholeskyDecompositionBlock(
                EjmlParameters.BLOCK_WIDTH_CHOL);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }


    public static long choleskyBlockU( DenseMatrix64F orig , int numTrials ) {

        CholeskyDecompositionBlock64 alg = new CholeskyDecompositionBlock64(false);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyBlockL( DenseMatrix64F orig , int numTrials ) {

        CholeskyDecompositionBlock64 alg = new CholeskyDecompositionBlock64(true);

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig)) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    public static long choleskyLDL( DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        CholeskyDecompositionLDL alg = new CholeskyDecompositionLDL();

        for( long i = 0; i < numTrials; i++ ) {
            if( !DecompositionFactory.decomposeSafe(alg,orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        System.out.println("Lower            = "+ choleskyL(mat,numTrials));
//        System.out.println("Upper            = "+ choleskyU(mat,numTrials));
        System.out.println("Lower Block      = "+ choleskyL_block(mat,numTrials));
//        System.out.println("LDL              = "+ choleskyLDL(mat,numTrials));
//        System.out.println("Real Block U     = "+ choleskyBlockU(mat,numTrials));
        System.out.println("Real Block L     = "+ choleskyBlockL(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000,10000};
        int trials[] = new int[]{(int)2e7,(int)5e6,(int)1e6,1000,40,3,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 4; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposition size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F symMat = RandomMatrices.createSymmPosDef(w,rand);
            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}