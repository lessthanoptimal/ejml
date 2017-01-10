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

package org.ejml.dense.row;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRow_F64;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkEquality {

    public static long equals( DMatrixRow_F64 matA ,
                               DMatrixRow_F64 matB ,
                               int numTrials) {
        boolean args = false;
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            args = MatrixFeatures_R64.isEquals(matA,matB, UtilEjml.TEST_F64);
        }

        long curr = System.currentTimeMillis();
        if( !args )
            throw new RuntimeException("don't optimize me away!");
        return curr-prev;
    }

    public static long identical( DMatrixRow_F64 matA ,
                                  DMatrixRow_F64 matB ,
                                  int numTrials) {

        boolean args = false;
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            args = MatrixFeatures_R64.isIdentical(matA,matB,UtilEjml.TEST_F64);
        }

        long curr = System.currentTimeMillis();
        if( !args )
            throw new RuntimeException("don't optimize me away!");
        return curr-prev;
    }

    public static void main( String args[] ) {
        Random rand = new Random(234234);

        DMatrixRow_F64 A = RandomMatrices_R64.createRandom(1000,2000,rand);
        DMatrixRow_F64 B = A.copy();

        int N = 1000;

        System.out.println("Equals:    "+equals(A,B,N));
        System.out.println("Identical: "+identical(A,B,N));
    }

}
