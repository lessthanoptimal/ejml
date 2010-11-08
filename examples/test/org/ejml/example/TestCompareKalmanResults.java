package org.ejml.example;/*
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.UtilTestMatrix;
import org.ejml.example.*;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Make sure all the filters produce exactly the same results.
 *
 * @author Peter Abeles
 */
public class TestCompareKalmanResults {

    private static double T = 0.5;

    /**
     * See if all the filters produce the same reslts.
     */
    @Test
    public void checkIdentical() {
        KalmanFilterSimple simple = new KalmanFilterSimple();

        List<KalmanFilter> all = new ArrayList<KalmanFilter>();
        all.add( new KalmanFilterOps() );
        all.add( new KalmanFilterAlg() );
        all.add( simple );

        DenseMatrix64F priorX = new DenseMatrix64F(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
        DenseMatrix64F priorP = CommonOps.identity(9);

        DenseMatrix64F F = BenchmarkKalmanPerformance.createF(T);
        DenseMatrix64F Q = BenchmarkKalmanPerformance.createQ(T,0.1);
        DenseMatrix64F H = BenchmarkKalmanPerformance.createH();


        for( KalmanFilter f : all ) {
            f.configure(F,Q,H);
            f.setState(priorX,priorP);
            f.predict();
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }

        DenseMatrix64F z = new DenseMatrix64F(H.numRows,1);
        DenseMatrix64F R = CommonOps.identity(H.numRows);

        for( KalmanFilter f : all ) {
            f.update(z,R);
        }

        for( KalmanFilter f : all ) {
            compareFilters(simple,f);
        }
    }

    private void compareFilters( KalmanFilter a, KalmanFilter b ) {
            DenseMatrix64F testX = b.getState();
            DenseMatrix64F testP = b.getCovariance();

            DenseMatrix64F X = a.getState();
            DenseMatrix64F P = a.getCovariance();

            UtilTestMatrix.checkEquals(testX,X);
            UtilTestMatrix.checkEquals(testP,P);
    }
}