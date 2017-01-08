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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestCovarianceRandomDraw_D64
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        RowMatrix_F64 orig_P = new RowMatrix_F64(new double[][]{{6,-2},{-2,10}});

        CovarianceRandomDraw_D64 dist = new CovarianceRandomDraw_D64(new Random(0xfeed),orig_P);

        RowMatrix_F64 draws[] = new RowMatrix_F64[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            RowMatrix_F64 x = new RowMatrix_F64(2,1);
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
        RowMatrix_F64 comp_P = new RowMatrix_F64(2,2);
        RowMatrix_F64 temp = new RowMatrix_F64(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps_D64.multAddTransB(temp,temp,comp_P);
        }

        CommonOps_D64.scale(1.0/N,comp_P);

        MatrixFeatures_D64.isIdentical(comp_P,orig_P,0.3);
    }

    /**
     * Make sure the input is not modified.
     */
    @Test
    public void modifyInput() {
        RowMatrix_F64 orig_P = new RowMatrix_F64(new double[][]{{6,-2},{-2,10}});
        RowMatrix_F64 input = orig_P.copy();

        CovarianceRandomDraw_D64 dist = new CovarianceRandomDraw_D64(new Random(0xfeed),input);

        assertTrue(MatrixFeatures_D64.isIdentical(input,orig_P, UtilEjml.TEST_F64));
    }

}