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

package org.ejml.alg.dense.misc;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkDeterminant {

    static int TOTAL_TRIALS = 50000000;


    public static long computeAuto( DenseMatrix64F mat ,int numTrials )
    {
        long before = System.currentTimeMillis();

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            total += UnrolledDeterminantFromMinor.det(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeFixed4x4( DenseMatrix64F mat )
    {
        long before = System.currentTimeMillis();

        double total = 0;

        for( int i = 0; i < TOTAL_TRIALS; i++ ) {
            total += UnrolledDeterminantFromMinor.det4(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeMinor4x4( DenseMatrix64F mat )
    {
        long before = System.currentTimeMillis();

        DeterminantFromMinor minor = new DeterminantFromMinor(4,5);

        double total = 0;

        for( int i = 0; i < TOTAL_TRIALS; i++ ) {
            total += minor.compute(mat);
        }

        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);
        
        return after-before;
    }

    public static long computeLU( DenseMatrix64F mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        LUDecompositionAlt alg = new LUDecompositionAlt();

        double total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            alg.decompose(mat);
            total += alg.computeDeterminant();
        }

//        System.out.println("   total = "+total);
        long after = System.currentTimeMillis();

        if( total == 100 )
            System.out.println(total);

        return after-before;
    }

    public static long computeMinor( DenseMatrix64F mat , int numTrials )
    {
        long before = System.currentTimeMillis();

        DeterminantFromMinor minor = new DeterminantFromMinor(mat.numRows,5);

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

    public static long computeLeibniz( DenseMatrix64F mat , int numTrials )
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

        DenseMatrix64F mat = new DenseMatrix64F(4,4, true, d);

//        System.out.println("Fixed 4x4       = "+computeFixed4x4(mat));
//        System.out.println("Auto 4x4       = "+computeAuto(mat,TOTAL_TRIALS));
////        System.out.println("Minor 4x4       = "+computeMinor4x4(mat));
//        System.out.println("LU alg NR 4x4   = "+computeLU(mat,TOTAL_TRIALS));
////        System.out.println("Leibniz  4x4    = "+computeLeibniz(mat,TOTAL_TRIALS));

        Random rand = new Random(4344535);

        for( int i = 2; i <= 25; i+= 1) {
            int numTrials = TOTAL_TRIALS/(i*i);

            System.out.println("Dimension = "+i+"  trials = "+numTrials);

            mat = RandomMatrices.createRandom(i,i,rand);

            System.out.println("  Auto         = "+computeAuto(mat,numTrials));
//            System.out.println("  Minor         = "+computeMinor(mat,numTrials));
//            System.out.println("  Leibniz         = "+computeLeibniz(mat,numTrials));
            System.out.println("  LU alg NR     = "+ computeLU(mat,numTrials));
        }

//        System.out.println("Recursive 4x4   = "+computeRecursive4x4(mat));
    }
}
