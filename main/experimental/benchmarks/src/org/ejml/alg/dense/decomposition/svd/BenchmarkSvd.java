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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.data.DMatrixRow_F64;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.RandomMatrices_R64;

import java.util.Random;


/**
 * Compare the speed of various algorithms at inverting square matrices
 *
 * @author Peter Abeles
 */
public class BenchmarkSvd {


    public static String evaluate(SingularValueDecomposition<DMatrixRow_F64> alg , DMatrixRow_F64 orig , int numTrials ) {

        long prev = System.currentTimeMillis();

        for( long i = 0; i < numTrials; i++ ) {
            if( !alg.decompose(orig) ) {
                throw new RuntimeException("Bad matrix");
            }
        }

        long diff =  System.currentTimeMillis() - prev;
        return diff+" (ms)  "+(numTrials/(diff/1000.0))+" (ops/sec)";
    }

    private static void runAlgorithms(DMatrixRow_F64 mat , int numTrials )
    {
//        mat.print("%f");
        if( numTrials <= 0 ) return;
        System.out.println("qr               = "+ evaluate(new SvdImplicitQrDecompose_R64(true,true,true,true),mat,numTrials));
//        System.out.println("qr smart         = "+ evaluate(new SvdImplicitQrDecompose_UltimateS(true,true,true),mat,numTrials));
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
        for( int i = 0; i < size.length; i++ ) {
            int w = size[i];

            System.out.printf("Decomposition size %3d for %12d trials\n",w,trials[i]);

            System.out.print("* Creating matrix ");
            DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(w,w,rand);
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
            DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(2*w,w,rand);
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
            DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(w,2*w,rand);
            System.out.println("  Done.");
            runAlgorithms(mat,trials[i]);
        }
    }
}