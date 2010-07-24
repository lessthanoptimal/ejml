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
public class BenchmarkElement {
    static Random rand = new Random(234);

    public static void main( String args[] ) {
        long N = 10000000;

        double num = 2.5;

        DenseMatrix64F A = RandomMatrices.createRandom(10,10,rand);

        long timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.div(num,A);
        }
        long timeAfter = System.currentTimeMillis();

        System.out.println("div = "+(timeAfter-timeBefore));

        A = RandomMatrices.createRandom(10,10,rand);

        timeBefore = System.currentTimeMillis();
        for( int i = 0; i < N; i++ ) {
            CommonOps.scale(num,A);
        }
        timeAfter = System.currentTimeMillis();

        System.out.println("scale = "+(timeAfter-timeBefore));
    }
}
