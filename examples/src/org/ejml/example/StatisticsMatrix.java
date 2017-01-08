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

package org.ejml.example;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.RandomMatrices_D64;
import org.ejml.simple.SimpleBase;

import java.util.Random;


/**
 * Example of how to extend "SimpleMatrix" and add your own functionality.  In this case
 * two basic statistic operations are added.  Since SimpleBase is extended and StatisticsMatrix
 * is specified as the generics type, all "SimpleMatrix" operations return a matrix of
 * type StatisticsMatrix, ensuring strong typing.
 *
 * @author Peter Abeles
 */
public class StatisticsMatrix extends SimpleBase<StatisticsMatrix> {

    public StatisticsMatrix( int numRows , int numCols ) {
        super(numRows,numCols);
    }

    protected StatisticsMatrix(){}

    /**
     * Wraps a StatisticsMatrix around 'm'.  Does NOT create a copy of 'm' but saves a reference
     * to it.
     */
    public static StatisticsMatrix wrap( RowMatrix_F64 m ) {
        StatisticsMatrix ret = new StatisticsMatrix();
        ret.mat = m;

        return ret;
    }

    /**
     * Computes the mean or average of all the elements.
     *
     * @return mean
     */
    public double mean() {
        double total = 0;

        final int N = getNumElements();
        for( int i = 0; i < N; i++ ) {
            total += get(i);
        }

        return total/N;
    }

    /**
     * Computes the unbiased standard deviation of all the elements.
     *
     * @return standard deviation
     */
    public double stdev() {
        double m = mean();

        double total = 0;

        final int N = getNumElements();
        if( N <= 1 )
            throw new IllegalArgumentException("There must be more than one element to compute stdev");


        for( int i = 0; i < N; i++ ) {
            double x = get(i);

            total += (x - m)*(x - m);
        }

        total /= (N-1);

        return Math.sqrt(total);
    }

    /**
     * Returns a matrix of StatisticsMatrix type so that SimpleMatrix functions create matrices
     * of the correct type.
     */
    @Override
    protected StatisticsMatrix createMatrix(int numRows, int numCols) {
        return new StatisticsMatrix(numRows,numCols);
    }

    public static void main( String args[] ) {
        Random rand = new Random(24234);

        int N = 500;

        // create two vectors whose elements are drawn from uniform distributions
        StatisticsMatrix A = StatisticsMatrix.wrap(RandomMatrices_D64.createRandom(N,1,0,1,rand));
        StatisticsMatrix B = StatisticsMatrix.wrap(RandomMatrices_D64.createRandom(N,1,1,2,rand));

        // the mean should be about 0.5
        System.out.println("Mean of A is               "+A.mean());
        // the mean should be about 1.5
        System.out.println("Mean of B is               "+B.mean());

        StatisticsMatrix C = A.plus(B);

        // the mean should be about 2.0
        System.out.println("Mean of C = A + B is       "+C.mean());

        System.out.println("Standard deviation of A is "+A.stdev());
        System.out.println("Standard deviation of B is "+B.stdev());
        System.out.println("Standard deviation of C is "+C.stdev());
    }
}
