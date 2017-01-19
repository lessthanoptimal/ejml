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

package org.ejml;

import org.ejml.data.DMatrixD1;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;

import java.util.Random;


/**
 * Benchmark that tests to see if referring the parent of the class versus the actual class
 * has any performance difference.  The function used internally is matrix multiplication in "ikj" order.
 *
 * @author Peter Abeles
 */
public class BenchmarkInheritanceCall {

    public static void multParent(DMatrixD1 a , DMatrixD1 b , DMatrixD1 c )
    {
        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = dataA[indexA++];

            while( indexB < end ) {
                dataC[indexC++] = valA*dataB[indexB++];
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = dataA[indexA++];

                while( indexB < end ) { // j loop
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void multParent_wrap(DMatrixD1 a , DMatrixD1 b , DMatrixD1 c )
    {
        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.get(indexA++);

            while( indexB < end ) {
                c.set( indexC++ , valA*b.get(indexB++));
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.plus( indexC++ , valA*b.get(indexB++));
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void multChild(DMatrixRMaj a , DMatrixRMaj b , DMatrixRMaj c )
    {
        double dataA[] = a.data;
        double dataB[] = b.data;
        double dataC[] = c.data;

        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign dataC to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = dataA[indexA++];

            while( indexB < end ) {
                dataC[indexC++] = valA*dataB[indexB++];
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = dataA[indexA++];

                while( indexB < end ) { // j loop
                    dataC[indexC++] += valA*dataB[indexB++];
                }
            }
            indexCbase += c.numCols;
        }
    }

    public static void main( String args[] ) {
        Random rand = new Random(23234);

        DMatrixRMaj A = RandomMatrices_DDRM.createRandom(2,2,rand);
        DMatrixRMaj B = RandomMatrices_DDRM.createRandom(2,2,rand);
        DMatrixRMaj C = new DMatrixRMaj(2,2);

        int N = 40000000;

        long before = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            multParent(A,B,C);
        }
        long after = System.currentTimeMillis();

        System.out.println("Parent:       "+(after-before));

        before = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            multParent_wrap(A,B,C);
        }
        after = System.currentTimeMillis();

        System.out.println("Parent func:  "+(after-before));

        before = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            multChild(A,B,C);
        }
        after = System.currentTimeMillis();

        System.out.println("Child:        "+(after-before));
    }
}
