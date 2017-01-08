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

import org.ejml.data.RowMatrix_F64;
import org.ejml.factory.LinearSolverFactory_R64;
import org.ejml.interfaces.linsol.LinearSolver;

import static org.ejml.ops.CommonOps_R64.*;

/**
 * A Kalman filter that is implemented using the operations API, which is procedural.  Much of the excessive
 * memory creation/destruction has been reduced from the KalmanFilterSimple. A specialized solver is
 * under to invert the SPD matrix.
 *
 * @author Peter Abeles
 */
public class KalmanFilterOperations implements KalmanFilter{

    // kinematics description
    private RowMatrix_F64 F,Q,H;

    // system state estimate
    private RowMatrix_F64 x,P;

    // these are predeclared for efficiency reasons
    private RowMatrix_F64 a,b;
    private RowMatrix_F64 y,S,S_inv,c,d;
    private RowMatrix_F64 K;

    private LinearSolver<RowMatrix_F64> solver;

    @Override
    public void configure(RowMatrix_F64 F, RowMatrix_F64 Q, RowMatrix_F64 H) {
        this.F = F;
        this.Q = Q;
        this.H = H;

        int dimenX = F.numCols;
        int dimenZ = H.numRows;

        a = new RowMatrix_F64(dimenX,1);
        b = new RowMatrix_F64(dimenX,dimenX);
        y = new RowMatrix_F64(dimenZ,1);
        S = new RowMatrix_F64(dimenZ,dimenZ);
        S_inv = new RowMatrix_F64(dimenZ,dimenZ);
        c = new RowMatrix_F64(dimenZ,dimenX);
        d = new RowMatrix_F64(dimenX,dimenZ);
        K = new RowMatrix_F64(dimenX,dimenZ);

        x = new RowMatrix_F64(dimenX,1);
        P = new RowMatrix_F64(dimenX,dimenX);

        // covariance matrices are symmetric positive semi-definite
        solver = LinearSolverFactory_R64.symmPosDef(dimenX);
    }

    @Override
    public void setState(RowMatrix_F64 x, RowMatrix_F64 P) {
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
    public void update(RowMatrix_F64 z, RowMatrix_F64 R) {
        // y = z - H x
        mult(H,x,y);
        subtract(z, y, y);

        // S = H P H' + R
        mult(H,P,c);
        multTransB(c,H,S);
        addEquals(S,R);

        // K = PH'S^(-1)
        if( !solver.setA(S) ) throw new RuntimeException("Invert failed");
        solver.invert(S_inv);
        multTransA(H,S_inv,d);
        mult(P,d,K);

        // x = x + Ky
        mult(K,y,a);
        addEquals(x,a);

        // P = (I-kH)P = P - (KH)P = P-K(HP)
        mult(H,P,c);
        mult(K,c,b);
        subtractEquals(P, b);
    }

    @Override
    public RowMatrix_F64 getState() {
        return x;
    }

    @Override
    public RowMatrix_F64 getCovariance() {
        return P;
    }
}