/*
 * Copyright (c) 2009-2013, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DenseMatrix64F;

import static org.ejml.ops.CommonOps.*;

/**
 * A Kalman filter is implemented by calling the generalized operations.  Much of the excessive
 * memory creation/destruction has been reduced from the KalmanFilterSimple.  However, there
 * is still room for improvement by using specialized algorithms directly.  The price paid
 * for this better performance is the need to manually manage memory and the need to have a
 * better understanding for how each of the operations works.
 *
 * @author Peter Abeles
 */
public class KalmanFilterOps implements KalmanFilter{

    // kinematics description
    private DenseMatrix64F F;
    private DenseMatrix64F Q;
    private DenseMatrix64F H;

    // sytem state estimate
    private DenseMatrix64F x;
    private DenseMatrix64F P;

    // these are predeclared for efficency reasons
    private DenseMatrix64F a,b;
    private DenseMatrix64F y,S,S_inv,c,d;
    private DenseMatrix64F K;

    @Override
    public void configure(DenseMatrix64F F, DenseMatrix64F Q, DenseMatrix64F H) {
        this.F = F;
        this.Q = Q;
        this.H = H;

        int dimenX = F.numCols;
        int dimenZ = H.numRows;

        a = new DenseMatrix64F(dimenX,1);
        b = new DenseMatrix64F(dimenX,dimenX);
        y = new DenseMatrix64F(dimenZ,1);
        S = new DenseMatrix64F(dimenZ,dimenZ);
        S_inv = new DenseMatrix64F(dimenZ,dimenZ);
        c = new DenseMatrix64F(dimenZ,dimenX);
        d = new DenseMatrix64F(dimenX,dimenZ);
        K = new DenseMatrix64F(dimenX,dimenZ);

        x = new DenseMatrix64F(dimenX,1);
        P = new DenseMatrix64F(dimenX,dimenX);
    }

    @Override
    public void setState(DenseMatrix64F x, DenseMatrix64F P) {
        this.x.set(x);
        this.P.set(P);
    }

    @Override
    public void predict() {

        // x = F x
        mult(F,x,a);
        x.set(a);

        // P = F P F' + Q
        mult(F,P,b);
        multTransB(b,F, P);
        addEquals(P,Q);
    }

    @Override
    public void update(DenseMatrix64F z, DenseMatrix64F R) {
        // y = z - H x
        mult(H,x,y);
        sub(z,y,y);

        // S = H P H' + R
        mult(H,P,c);
        multTransB(c,H,S);
        addEquals(S,R);

        // K = PH'S^(-1)
        if( !invert(S,S_inv) ) throw new RuntimeException("Invert failed");
        multTransA(H,S_inv,d);
        mult(P,d,K);

        // x = x + Ky
        mult(K,y,a);
        addEquals(x,a);

        // P = (I-kH)P = P - (KH)P = P-K(HP)
        mult(H,P,c);
        mult(K,c,b);
        subEquals(P,b);
    }

    @Override
    public DenseMatrix64F getState() {
        return x;
    }

    @Override
    public DenseMatrix64F getCovariance() {
        return P;
    }
}