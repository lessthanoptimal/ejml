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
import org.ejml.data.FMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestCovarianceRandomDraw_FDRM
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        FMatrixRMaj orig_P = new FMatrixRMaj(new float[][]{{6,-2},{-2,10}});

        CovarianceRandomDraw_FDRM dist = new CovarianceRandomDraw_FDRM(new Random(0xfeed),orig_P);

        FMatrixRMaj draws[] = new FMatrixRMaj[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            FMatrixRMaj x = new FMatrixRMaj(2,1);
            dist.next(x);
            draws[i] = x;
        }

        // compute the statistics
        float raw_comp_x[] = new float[2];

        // find the mean
        for( int i = 0; i < N; i++ ) {
            raw_comp_x[0] += draws[i].get(0,0);
            raw_comp_x[1] += draws[i].get(1,0);
        }

        raw_comp_x[0] /= N;
        raw_comp_x[1] /= N;

        assertEquals(0,raw_comp_x[0],0.1f);
        assertEquals(0.0f,raw_comp_x[1],0.1f);

        // now the covariance
        FMatrixRMaj comp_P = new FMatrixRMaj(2,2);
        FMatrixRMaj temp = new FMatrixRMaj(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps_FDRM.multAddTransB(temp,temp,comp_P);
        }

        CommonOps_FDRM.scale(1.0f/N,comp_P);

        MatrixFeatures_FDRM.isIdentical(comp_P,orig_P,0.3f);
    }

    /**
     * Make sure the input is not modified.
     */
    @Test
    public void modifyInput() {
        FMatrixRMaj orig_P = new FMatrixRMaj(new float[][]{{6,-2},{-2,10}});
        FMatrixRMaj input = orig_P.copy();

        CovarianceRandomDraw_FDRM dist = new CovarianceRandomDraw_FDRM(new Random(0xfeed),input);

        assertTrue(MatrixFeatures_FDRM.isIdentical(input,orig_P, UtilEjml.TEST_F32));
    }

}