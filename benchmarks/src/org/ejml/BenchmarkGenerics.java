/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.D1Matrix64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Checks to see if using generics slows things down.
 *
 * @author Peter Abeles
 */
public class BenchmarkGenerics {


    private static class ImplDense implements ScaleDense
    {
        @Override
        public void scale(double s, DenseMatrix64F mat) {
            for( int i = 0; i < mat.data.length; i++ ) {
                mat.data[i] *= s;
            }
        }
    }

    private static class ImplGeneric implements ScaleGeneric<DenseMatrix64F>
    {
        @Override
        public void scale(double s, DenseMatrix64F mat) {
            for( int i = 0; i < mat.data.length; i++ ) {
                mat.data[i] *= s;
            }
        }
    }

    private static interface ScaleDense
    {
        public void scale( double s , DenseMatrix64F mat );
    }

    private static interface ScaleGeneric<T extends D1Matrix64F>
    {
        public void scale( double s , T mat );
    }

    public static long benchmarkDense( DenseMatrix64F A , double scale , int trials ) {
        ScaleDense s = new ImplDense();

        long before = System.currentTimeMillis();

        for( int i = 0; i < trials; i++ ) {
            s.scale(scale,A);
            s.scale(1.0/scale,A);
        }

        return System.currentTimeMillis() - before;
    }

    public static long benchmarkGeneric( DenseMatrix64F A , double scale , int trials ) {
        ImplGeneric s = new ImplGeneric();

        long before = System.currentTimeMillis();

        for( int i = 0; i < trials; i++ ) {
            s.scale(scale,A);
            s.scale(1.0/scale,A);
        }

        return System.currentTimeMillis() - before;
    }


    public static void main( String []args ) {
        DenseMatrix64F A = RandomMatrices.createRandom(10,10,new Random(234));

        int N = 10000000;

        System.out.println("dense   = "+benchmarkDense(A,2.5,N));
        System.out.println("generic = "+benchmarkGeneric(A,2.5,N));
    }
}
