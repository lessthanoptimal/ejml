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

package org.ejml.ops;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestCovarianceRandomDraw
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        DenseMatrix64F orig_P = new DenseMatrix64F(new double[][]{{6,-2},{-2,10}});

        CovarianceRandomDraw dist = new CovarianceRandomDraw(new Random(0xfeed),orig_P);

        DenseMatrix64F draws[] = new DenseMatrix64F[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            DenseMatrix64F x = new DenseMatrix64F(2,1);
            dist.next(x);
            draws[i] = x;
        }

        // compute the statistics
        double raw_comp_x[] = new double[2];

        // find the mean
        for( int i = 0; i < N; i++ ) {
            raw_comp_x[0] += draws[i].get(0,0);
            raw_comp_x[1] += draws[i].get(1,0);
        }

        raw_comp_x[0] /= N;
        raw_comp_x[1] /= N;

        assertEquals(0,raw_comp_x[0],0.1);
        assertEquals(0.0,raw_comp_x[1],0.1);

        // now the covariance
        DenseMatrix64F comp_P = new DenseMatrix64F(2,2);
        DenseMatrix64F temp = new DenseMatrix64F(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps.multAddTransB(temp,temp,comp_P);
        }

        CommonOps.scale(1.0/N,comp_P);

        MatrixFeatures.isIdentical(comp_P,orig_P,0.3);
    }

    /**
     * Make sure the input is not modified.
     */
    @Test
    public void modifyInput() {
        DenseMatrix64F orig_P = new DenseMatrix64F(new double[][]{{6,-2},{-2,10}});
        DenseMatrix64F input = orig_P.copy();

        CovarianceRandomDraw dist = new CovarianceRandomDraw(new Random(0xfeed),input);

        assertTrue(MatrixFeatures.isIdentical(input,orig_P,1e-8));
    }

}