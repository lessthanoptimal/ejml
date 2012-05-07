/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.data;

/**
 * Checks to see if having a return statement that isn't used makes any different or not
 *
 * @author Peter Abeles
 */
public class BenchmarkFunctionReturn {

    double data[] = new double[ 1000 ];

    private void resetData() {
        for( int i = 0; i < data.length; i++ ) {
            data[i] = i + 1;
        }
    }


    public double funcA( int i , double b) {
        return data[i] *= b;
    }

    public void funcB( int i , double b) {
        data[i] *= b;
    }

    public long benchmarkA( int numTrials ) {

        resetData();

        long timeBefore = System.currentTimeMillis();
        for( int i = 0; i < numTrials; i++ ) {
            for( int j = 0; j < 50; j++ ) {
                for( int k = j; k < data.length; k++ ) {
                    funcA(k,1.1);
                }
            }
        }
        long timeAfter = System.currentTimeMillis();

        return timeAfter-timeBefore;
    }

    public long benchmarkB( int numTrials ) {

        resetData();

        long timeBefore = System.currentTimeMillis();
        for( int i = 0; i < numTrials; i++ ) {
            for( int j = 0; j < 50; j++ ) {
                for( int k = j; k < data.length; k++ ) {
                    funcB(k,1.1);
                }
            }
        }
        long timeAfter = System.currentTimeMillis();

        return timeAfter-timeBefore;
    }

    public static void main( String args[] ) {
        BenchmarkFunctionReturn app = new BenchmarkFunctionReturn();
        int N = 100000;

        System.out.println("With return = "+app.benchmarkA(N));
        System.out.println("No return   = "+app.benchmarkB(N));
    }
}
