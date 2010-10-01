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

package org.ejml;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Test to see how well set and get are inlined in DenseMatrix64F
 *
 * @author Peter Abeles
 */
public class BenchmarkInliningGetSet {

    public static long wrapped( DenseMatrix64F A , int n ) {

        long before = System.currentTimeMillis();

        double total = 0;

        for( int iter = 0; iter < n; iter++ ) {

            for( int i = 0; i < A.numRows; i++ ) {
                for( int j = 0; j < A.numCols; j++ ) {
                    total += A.get(i,j);
                }
            }
        }

        long after = System.currentTimeMillis();

        // print to ensure that ensure that an overly smart compiler does not optimize out
        // the whole function and to show that both produce the same results.
        System.out.println(total);

        return after-before;
    }

    public static long inlined( DenseMatrix64F A , int n ) {

        long before = System.currentTimeMillis();

        double total = 0;

        for( int iter = 0; iter < n; iter++ ) {

            for( int i = 0; i < A.numRows; i++ ) {
                for( int j = 0; j < A.numCols; j++ ) {
                    total += A.data[i*A.numCols + j];
                }
            }
        }

        long after = System.currentTimeMillis();

        // print to ensure that ensure that an overly smart compiler does not optimize out
        // the whole function and to show that both produce the same results.
        System.out.println(total);

        return after-before;
    }

    public static void main( String args[] ) {
        DenseMatrix64F A = RandomMatrices.createRandom(1000,1000,new Random());

        int N = 2000;

//        long timeWrapped = wrapped(A,N);
//        long timeInlined = inlined(A,N);

        long timeInlined = inlined(A,N);
        long timeWrapped = wrapped(A,N);

        System.out.println("Wrapped = "+timeWrapped+"  Inlined "+timeInlined);
    }
}
