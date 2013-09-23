/*
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

/*
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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.EjmlUnitTests;
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

            EjmlUnitTests.assertEquals(testX,X,1e-8);
            EjmlUnitTests.assertEquals(testP,P,1e-8);
    }
}