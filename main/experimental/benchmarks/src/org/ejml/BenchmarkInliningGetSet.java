/*
 * Copyright (c) 2009-2016, Peter Abeles. All Rights Reserved.
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

package org.ejml;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;


/**
 * Test to see how well set and get are inlined in DenseMatrix64F.
 *
 * @author Peter Abeles
 */
public class BenchmarkInliningGetSet {

    /**
     * Bounds checks are performed on get(i,j)
     */
    public static long benchGet( DenseMatrix64F A , int n ) {

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

    /**
     * Unsafe version of get(i,j) with no bounds checking
     */
    public static long getUnsafeGet( DenseMatrix64F A , int n ) {

        long before = System.currentTimeMillis();

        double total = 0;

        for( int iter = 0; iter < n; iter++ ) {

            for( int i = 0; i < A.numRows; i++ ) {
                for( int j = 0; j < A.numCols; j++ ) {
                    total += A.unsafe_get(i,j);
                }
            }
        }

        long after = System.currentTimeMillis();

        // print to ensure that ensure that an overly smart compiler does not optimize out
        // the whole function and to show that both produce the same results.
        System.out.println(total);

        return after-before;
    }

    /**
     * Get by index is used here.
     */
    public static long get1D( DenseMatrix64F A , int n ) {

        long before = System.currentTimeMillis();

        double total = 0;

        for( int iter = 0; iter < n; iter++ ) {

            int index = 0;
            for( int i = 0; i < A.numRows; i++ ) {
                int end = index+A.numCols;
                while( index != end ) {
                    total += A.get(index++);
                }
            }
        }

        long after = System.currentTimeMillis();

        // print to ensure that ensure that an overly smart compiler does not optimize out
        // the whole function and to show that both produce the same results.
        System.out.println(total);

        return after-before;
    }

    /**
     * Hand inlined version of get(i,j)
     */
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
        DenseMatrix64F A = RandomMatrices_D64.createRandom(1000,1000,new Random());

        int N = 2000;

        long time1D = get1D(A,N);
        long timeInlined = inlined(A,N);
        long timeGet = benchGet(A,N);
        long timeUnsafeGet = getUnsafeGet(A,N);
        
        System.out.println("get = "+timeGet+"  Inlined "+timeInlined+" unsafe_get "+timeUnsafeGet+" get1D "+time1D);
    }
}
