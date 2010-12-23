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
        System.out.println("generic = "+benchmarkDense(A,2.5,N));
    }
}
