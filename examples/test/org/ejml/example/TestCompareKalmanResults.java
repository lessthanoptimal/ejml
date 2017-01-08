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

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Make sure all the filters produce exactly the same results.
 *
 * @author Peter Abeles
 */
public class TestCompareKalmanResults {

    private static final double T = 0.5;

    /**
     * See if all the filters produce the same reslts.
     */
    @Test
    public void checkIdentical() {
        KalmanFilterSimple simple = new KalmanFilterSimple();

        List<KalmanFilter> all = new ArrayList<KalmanFilter>();
        all.add( new KalmanFilterOperations() );
        all.add( new KalmanFilterEquation() );
        all.add( simple );

        RowMatrix_F64 priorX = new RowMatrix_F64(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
        RowMatrix_F64 priorP = CommonOps_D64.identity(9);

        RowMatrix_F64 F = BenchmarkKalmanPerformance.createF(T);
        RowMatrix_F64 Q = BenchmarkKalmanPerformance.createQ(T,0.1);
        RowMatrix_F64 H = BenchmarkKalmanPerformance.createH();


        for( KalmanFilter f : all ) {
            f.configure(F,Q,H);
            f.setState(priorX,priorP);
            f.predict();
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }

        RowMatrix_F64 z = new RowMatrix_F64(H.numRows,1);
        RowMatrix_F64 R = CommonOps_D64.identity(H.numRows);

        for( KalmanFilter f : all ) {
            f.update(z,R);
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }
    }

    private void compareFilters( KalmanFilter a, KalmanFilter b ) {
            RowMatrix_F64 testX = b.getState();
            RowMatrix_F64 testP = b.getCovariance();

            RowMatrix_F64 X = a.getState();
            RowMatrix_F64 P = a.getCovariance();

            EjmlUnitTests.assertEquals(testX,X,UtilEjml.TEST_F64);
            EjmlUnitTests.assertEquals(testP,P,UtilEjml.TEST_F64);
    }
}