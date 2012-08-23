/*
 * Copyright (c) 2009-2012, Peter Abeles. All Rights Reserved.
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

package org.ejml.example;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

/**
 * A Kalman filter implemented using SimpleMatrix.  The code tends to be easier to
 * read and write, but the performance is degraded due to excessive creation/destruction of
 * memory and the use of more generic algorithms.  This also demonstrates how code can be
 * seamlessly implemented using both SimpleMatrix and DenseMatrix64F.  This allows code
 * to be quickly prototyped or to be written either by novices or experts.
 *
 * @author Peter Abeles
 */
public class KalmanFilterSimple implements KalmanFilter{

    // kinematics description
    private SimpleMatrix F;
    private SimpleMatrix Q;
    private SimpleMatrix H;

    // sytem state estimate
    private SimpleMatrix x;
    private SimpleMatrix P;


    @Override
    public void configure(DenseMatrix64F F, DenseMatrix64F Q, DenseMatrix64F H) {
        this.F = new SimpleMatrix(F);
        this.Q = new SimpleMatrix(Q);
        this.H = new SimpleMatrix(H);
    }

    @Override
    public void setState(DenseMatrix64F x, DenseMatrix64F P) {
        this.x = new SimpleMatrix(x);
        this.P = new SimpleMatrix(P);
    }

    @Override
    public void predict() {
        // x = F x
        x = F.mult(x);

        // P = F P F' + Q
        P = F.mult(P).mult(F.transpose()).plus(Q);
    }

    @Override
    public void update(DenseMatrix64F _z, DenseMatrix64F _R) {
        // a fast way to make the matrices usable by SimpleMatrix
        SimpleMatrix z = SimpleMatrix.wrap(_z);
        SimpleMatrix R = SimpleMatrix.wrap(_R);

        // y = z - H x
        SimpleMatrix y = z.minus(H.mult(x));

        // S = H P H' + R
        SimpleMatrix S = H.mult(P).mult(H.transpose()).plus(R);

        // K = PH'S^(-1)
        SimpleMatrix K = P.mult(H.transpose().mult(S.invert()));

        // x = x + Ky
        x = x.plus(K.mult(y));

        // P = (I-kH)P = P - KHP
        P = P.minus(K.mult(H).mult(P));
    }

    @Override
    public DenseMatrix64F getState() {
        return x.getMatrix();
    }

    @Override
    public DenseMatrix64F getCovariance() {
        return P.getMatrix();
    }
}
