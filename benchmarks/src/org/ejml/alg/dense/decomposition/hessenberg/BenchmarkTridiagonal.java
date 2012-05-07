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

package org.ejml.alg.dense.decomposition.hessenberg;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkTridiagonal {


    public static long basic( DenseMatrix64F orig , int numTrials ) {

        TridiagonalDecompositionHouseholder alg = new TridiagonalDecompositionHouseholder();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                alg.decompose(orig.<DenseMatrix64F>copy());
            else
                alg.decompose(orig);

            alg.getQ(null,false);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long alt( DenseMatrix64F orig , int numTrials ) {

        TridiagonalDecompositionHouseholderOrig alg = new TridiagonalDecompositionHouseholderOrig();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            alg.decompose(orig);
        }

        return System.currentTimeMillis() - prev;
    }

    public static long block( DenseMatrix64F orig , int numTrials ) {


        TridiagonalDecompositionBlock alg = new TridiagonalDecompositionBlock();

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( alg.inputModified())
                alg.decompose(orig.<DenseMatrix64F>copy());
            else
                alg.decompose(orig);

            alg.getQ(null,false);
        }

        return System.currentTimeMillis() - prev;
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        System.out.println("basic            = "+ basic(mat,numTrials));
//        System.out.println("alt              = "+ alt(mat,numTrials));
        System.out.println("block            = "+ block(mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,5000};
        int trials[] = new int[]{(int)8e6,(int)2e6,(int)2e5,600,12,3,1,1,1};

        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 3; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Processing size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }
    }
}