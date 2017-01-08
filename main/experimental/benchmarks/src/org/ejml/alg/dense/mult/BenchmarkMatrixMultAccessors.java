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

package org.ejml.alg.dense.mult;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_D64;

import java.util.Random;

/**
 * Compares different implementations of a reordered matrix multiplication that use different accessor functions.
 * but identical algorithms.
 *
 * @author Peter Abeles
 */
public class BenchmarkMatrixMultAccessors {

    /**
     * All reads/writes have been inline by hand
     */
    public static long inlined(RowMatrix_F64 a , RowMatrix_F64 b , RowMatrix_F64 c )
    {
        long timeBefore = System.currentTimeMillis();

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

        return System.currentTimeMillis() - timeBefore;
    }

    /**
     * Wrapper functions with no bounds checking are used to access matrix internals
     */
    public static long wrapped(RowMatrix_F64 a , RowMatrix_F64 b , RowMatrix_F64 c )
    {
        long timeBefore = System.currentTimeMillis();
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

        return System.currentTimeMillis() - timeBefore;
    }

    /**
     * Only sets and gets that are by row and column are used.
     */
    public static long access2d(RowMatrix_F64 a , RowMatrix_F64 b , RowMatrix_F64 c )
    {
        long timeBefore = System.currentTimeMillis();

        for( int i = 0; i < a.numRows; i++ ) {

            for( int j = 0; j < b.numCols; j++ ) {
                c.set(i,j,a.get(i,0)*b.get(0,j));
            }

            for( int k = 1; k < b.numRows; k++ ) {
                for( int j = 0; j < b.numCols; j++ ) {
//                    c.set(i,j, c.get(i,j) + a.get(i,k)*b.get(k,j));
                    c.data[i*b.numCols+j] +=a.get(i,k)*b.get(k,j);
                }
            }
        }

        return System.currentTimeMillis() - timeBefore;
    }

    public static void main( String args[] ) {
        Random rand = new Random(9234243);

        int N = 1000;

        RowMatrix_F64 A = RandomMatrices_D64.createRandom(N,N,rand);
        RowMatrix_F64 B = RandomMatrices_D64.createRandom(N,N,rand);
        RowMatrix_F64 C = new RowMatrix_F64(N,N);

        long timeInlined = inlined(A,B,C);
        long timeWrapped = wrapped(A,B,C);
        long time2D = access2d(A,B,C);

        System.out.println("inlined "+timeInlined+" wrapped "+timeWrapped+" access2d "+time2D);
    }
}
