/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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
