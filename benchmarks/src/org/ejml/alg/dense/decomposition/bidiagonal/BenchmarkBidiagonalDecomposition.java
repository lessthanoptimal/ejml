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

package org.ejml.alg.dense.decomposition.bidiagonal;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkBidiagonalDecomposition {


    public static long evaluate( BidiagonalDecomposition<DenseMatrix64F> alg , DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig.copy()) ) {
                throw new RuntimeException("Bad matrix");
            }
//            alg.getU(null,false,false);
//            alg.getV(null,false,false);

        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        if( numTrials <= 0 ) return;
        System.out.println("row               = "+ evaluate(new BidiagonalDecompositionRow(),mat,numTrials));
        System.out.println("tall              = "+ evaluate(new BidiagonalDecompositionTall(),mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,5000,10000};
        int trials[] = new int[]{(int)4e6,(int)1e6,(int)1e5,200,1,1,1,1,1};
//        int trials[] = new int[]{(int)1e6,(int)2e5,(int)2e4,50,1,1,1,1,1};

        System.out.println("Square matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposition size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }

        System.out.println("Tall matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];
            int h = w*3;

            int t = trials[i];

            if( t == 0 ) continue;

            System.out.printf("Decomposition size w=%3d h=%3d for %12d trials\n",w,h,t);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(h,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,t);
        }
    }
}