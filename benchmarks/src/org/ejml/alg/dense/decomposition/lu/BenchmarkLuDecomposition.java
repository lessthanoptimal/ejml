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

package org.ejml.alg.dense.decomposition.lu;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LUDecomposition;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at decomposing square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkLuDecomposition {


    public static void benchmark( LUDecomposition lu , DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();
        for( long i = 0; i < numTrials; i++ ) {
            if( !lu.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        long time = System.currentTimeMillis() - prev;
        System.out.printf(" %20s time = %7d\n",lu.getClass().getSimpleName(),time);
    }


    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        benchmark(new LUDecompositionAlt(),mat,numTrials);
        benchmark(new LUDecompositionNR(),mat,numTrials);
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,4000};
        int trials[] = new int[]{(int)1e7,(int)5e6,(int)1e6,2000,40,3,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposing size %3d for %12d trials\n",w,trials[i]);

//            System.out.print("* Creating matrix ");
            DenseMatrix64F symMat = RandomMatrices.createRandom(w,w,-1,1,rand);
//            System.out.println("  Done.");
            runAlgorithms(symMat,trials[i]);
        }
    }
}