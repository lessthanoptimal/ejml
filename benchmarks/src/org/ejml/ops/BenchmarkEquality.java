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

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkEquality {

    public static long equals( DenseMatrix64F matA ,
                               DenseMatrix64F matB ,
                               int numTrials) {
        boolean args = false;
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            args = MatrixFeatures.isEquals(matA,matB,1e-8);
        }

        long curr = System.currentTimeMillis();
        if( !args )
            throw new RuntimeException("don't optimize me away!");
        return curr-prev;
    }

    public static long identical( DenseMatrix64F matA ,
                                  DenseMatrix64F matB ,
                                  int numTrials) {

        boolean args = false;
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            args = MatrixFeatures.isIdentical(matA,matB,1e-8);
        }

        long curr = System.currentTimeMillis();
        if( !args )
            throw new RuntimeException("don't optimize me away!");
        return curr-prev;
    }

    public static void main( String args[] ) {
        Random rand = new Random(234234);

        DenseMatrix64F A = RandomMatrices.createRandom(1000,2000,rand);
        DenseMatrix64F B = A.copy();

        int N = 1000;

        System.out.println("Equals:    "+equals(A,B,N));
        System.out.println("Identical: "+identical(A,B,N));
    }

}
