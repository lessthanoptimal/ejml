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

package org.ejml.dense.row.misc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.decomposition.lu.LUDecompositionAlt_DDRM;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkDeterminant {

    static int TOTAL_TRIALS = 50000000;


    public static long computeAuto(DMatrixRMaj mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            total += UnrolledDeterminantFromMinor_DDRM.det(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeFixed4x4( DMatrixRMaj mat )
    {
        long before = System.currentTimeMillis();

        double total = 0;

        for( int i = 0; i < TOTAL_TRIALS; i++ ) {
            total += UnrolledDeterminantFromMinor_DDRM.det4(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeMinor4x4( DMatrixRMaj mat )
    {
        long before = System.currentTimeMillis();

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(4,5);

        double total = 0;

        for( int i = 0; i < TOTAL_TRIALS; i++ ) {
            total += minor.compute(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);
        
        return after-before;
    }

    public static long computeLU(DMatrixRMaj mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            alg.decompose(mat);
            total += alg.computeDeterminant().real;
        }

//        System.out.println("   total = "+total);
        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeMinor(DMatrixRMaj mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(mat.numRows,5);

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            total += minor.compute(mat);
        }

//        System.out.println("   total = "+total);
        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeLeibniz(DMatrixRMaj mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            total += NaiveDeterminant.leibniz(mat);
        }

//        System.out.println("   total = "+total);
        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }


    public static void main( String args[] ) {
        double[] d = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        DMatrixRMaj mat = new DMatrixRMaj(4,4, true, d);

//        System.out.println("Fixed 4x4       = "+computeFixed4x4(mat));
//        System.out.println("Auto 4x4       = "+computeAuto(mat,TOTAL_TRIALS));
////        System.out.println("Minor 4x4       = "+computeMinor4x4(mat));
//        System.out.println("LU dense NR 4x4   = "+computeLU(mat,TOTAL_TRIALS));
////        System.out.println("Leibniz  4x4    = "+computeLeibniz(mat,TOTAL_TRIALS));

        Random rand = new Random(4344535);

        for( int i = 2; i <= 25; i+= 1) {
            int numTrials = TOTAL_TRIALS/(i*i);

            System.out.println("Dimension = "+i+"  trials = "+numTrials);

            mat = RandomMatrices_DDRM.createRandom(i,i,rand);

            System.out.println("  Auto         = "+computeAuto(mat,numTrials));
//            System.out.println("  Minor         = "+computeMinor(mat,numTrials));
//            System.out.println("  Leibniz         = "+computeLeibniz(mat,numTrials));
            System.out.println("  LU dense NR     = "+ computeLU(mat,numTrials));
        }

//        System.out.println("Recursive 4x4   = "+computeRecursive4x4(mat));
    }
}
