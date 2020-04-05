/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestCovarianceRandomDraw_DDRM
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        DMatrixRMaj orig_P = new DMatrixRMaj(new double[][]{{6,-2},{-2,10}});

        CovarianceRandomDraw_DDRM dist = new CovarianceRandomDraw_DDRM(new Random(0xfeed),orig_P);

        DMatrixRMaj draws[] = new DMatrixRMaj[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            DMatrixRMaj x = new DMatrixRMaj(2,1);
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
        DMatrixRMaj comp_P = new DMatrixRMaj(2,2);
        DMatrixRMaj temp = new DMatrixRMaj(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps_DDRM.multAddTransB(temp,temp,comp_P);
        }

        CommonOps_DDRM.scale(1.0/N,comp_P);

        MatrixFeatures_DDRM.isIdentical(comp_P,orig_P,0.3);
    }

    /**
     * Make sure the input is not modified.
     */
    @Test
    public void modifyInput() {
        DMatrixRMaj orig_P = new DMatrixRMaj(new double[][]{{6,-2},{-2,10}});
        DMatrixRMaj input = orig_P.copy();

        CovarianceRandomDraw_DDRM dist = new CovarianceRandomDraw_DDRM(new Random(0xfeed),input);

        assertTrue(MatrixFeatures_DDRM.isIdentical(input,orig_P, UtilEjml.TEST_F64));
    }

}