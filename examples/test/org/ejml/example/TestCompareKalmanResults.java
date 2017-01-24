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
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
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

        DMatrixRMaj priorX = new DMatrixRMaj(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
        DMatrixRMaj priorP = CommonOps_DDRM.identity(9);

        DMatrixRMaj F = BenchmarkKalmanPerformance.createF(T);
        DMatrixRMaj Q = BenchmarkKalmanPerformance.createQ(T,0.1);
        DMatrixRMaj H = BenchmarkKalmanPerformance.createH();


        for( KalmanFilter f : all ) {
            f.configure(F,Q,H);
            f.setState(priorX,priorP);
            f.predict();
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }

        DMatrixRMaj z = new DMatrixRMaj(H.numRows,1);
        DMatrixRMaj R = CommonOps_DDRM.identity(H.numRows);

        for( KalmanFilter f : all ) {
            f.update(z,R);
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }
    }

    private void compareFilters( KalmanFilter a, KalmanFilter b ) {
            DMatrixRMaj testX = b.getState();
            DMatrixRMaj testP = b.getCovariance();

            DMatrixRMaj X = a.getState();
            DMatrixRMaj P = a.getCovariance();

            EjmlUnitTests.assertEquals(testX,X,UtilEjml.TEST_F64);
            EjmlUnitTests.assertEquals(testP,P,UtilEjml.TEST_F64);
    }
}