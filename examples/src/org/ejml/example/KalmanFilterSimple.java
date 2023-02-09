/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.ConstMatrix;
import org.ejml.simple.SimpleMatrix;

/**
 * A Kalman filter implemented using SimpleMatrix. The code tends to be easier to
 * read and write, but the performance is degraded due to excessive creation/destruction of
 * memory and the use of more generic algorithms. This also demonstrates how code can be
 * seamlessly implemented using both SimpleMatrix and DMatrixRMaj. This allows code
 * to be quickly prototyped or to be written either by novices or experts.
 *
 * @author Peter Abeles
 */
public class KalmanFilterSimple implements KalmanFilter {
    // kinematics description
    private ConstMatrix<SimpleMatrix> F, Q, H;

    // sytem state estimate
    private SimpleMatrix x, P;

    @Override public void configure( DMatrixRMaj F, DMatrixRMaj Q, DMatrixRMaj H ) {
        this.F = new SimpleMatrix(F);
        this.Q = new SimpleMatrix(Q);
        this.H = new SimpleMatrix(H);
    }

    @Override public void setState( DMatrixRMaj x, DMatrixRMaj P ) {
        this.x = new SimpleMatrix(x);
        this.P = new SimpleMatrix(P);
    }

    @Override public void predict() {
        // x = F x
        x = F.mult(x);

        // P = F P F' + Q
        P = F.mult(P).mult(F.transpose()).plus(Q);
    }

    @Override public void update( DMatrixRMaj _z, DMatrixRMaj _R ) {
        // a fast way to make the matrices usable by SimpleMatrix
        SimpleMatrix z = SimpleMatrix.wrap(_z);
        SimpleMatrix R = SimpleMatrix.wrap(_R);

        // y = z - H x
        ConstMatrix<?> y = z.minus(H.mult(x));

        // S = H P H' + R
        ConstMatrix<?> S = H.mult(P).mult(H.transpose()).plus(R);

        // K = PH'S^(-1)
        ConstMatrix<?> K = P.mult(H.transpose().mult(S.invert()));

        // x = x + Ky
        x = x.plus(K.mult(y));

        // P = (I-kH)P = P - KHP
        P = P.minus(K.mult(H).mult(P));
    }

    @Override public DMatrixRMaj getState() { return x.getMatrix(); }

    @Override public DMatrixRMaj getCovariance() { return P.getMatrix(); }
}
