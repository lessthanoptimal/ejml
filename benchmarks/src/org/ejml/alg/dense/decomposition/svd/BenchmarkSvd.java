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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkSvd {


    public static String evaluate( SingularValueDecomposition alg , DenseMatrix64F orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        long diff =  System.currentTimeMillis() - prev;
        return diff+" (ms)  "+(numTrials/(diff/1000.0))+" (ops/sec)";
    }

    private static void runAlgorithms( DenseMatrix64F mat , int numTrials )
    {
        if( numTrials <= 0 ) return;
        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose(true,true,true),mat,numTrials));
        System.out.println("qr smart         = "+ evaluate(new SvdImplicitQrDecompose_UltimateS(true,true,true),mat,numTrials));
        System.out.println("qr separate      = "+ evaluate(new SvdImplicitQrDecompose_Ultimate(true,true,true),mat,numTrials));
//        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose(true,true,true),mat,numTrials));
//        System.out.println("qr no U          = "+ evaluate(new SvdImplicitQrDecompose(true,false,true),mat,numTrials));
//        System.out.println("qr no U and V    = "+ evaluate(new SvdImplicitQrDecompose(true,false,false),mat,numTrials));
//        System.out.println("alt              = "+ evaluate(new SvdNumericalRecipes(),mat,numTrials));
    }

    public static void main( String args [] ) {
        Random rand = new Random(23423);

        int size[] = new int[]{2,4,10,100,500,1000,2000,3000};
        int trials[] = new int[]{(int)7e5,(int)1e5,(int)5e4,100,2,1,1,1};

        System.out.println("Square matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 5; i < size.length; i++ ) {
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

            int t = trials[i]*3/5;

            if( t == 0 ) continue;

            System.out.printf("Decomposition size %3d for %12d trials\n",w,t);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(2*w,w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,t);
        }

        System.out.println("Wide matrix");
        // results vary significantly depending if it starts from a small or large matrix
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            int t = trials[i]*3/5;

            if( t == 0 ) continue;

            System.out.printf("Decomposition size %3d for %12d trials\n",w,t);

            System.out.print("* Creating matrix ");
            DenseMatrix64F mat = RandomMatrices.createRandom(w,2*w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }
    }
}