/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;

import static org.ejml.ops.CommonOps.*;

/**
 * The difference between this and {@link KalmanFilterOps} is that it takes advantage of
 * the covariance matrix being a symetric positive semi-definite matrix.  This allows
 * it to be decomposed using {@link org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner_D64}.  There are two advantages here,
 * 1) all memory is predeclared and 2) CholeksyDecomposition is more efficient than the more
 * generic {@link org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64 LUDecomposition}.  It also makes
 * calls to matrix vector multiplcation operations, which has a slight performance advantage.
 *
 * @author Peter Abeles
 */
public class KalmanFilterAlg implements KalmanFilter{

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

    private LinearSolver<DenseMatrix64F> solver;

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

        // covariance matrices are symmetric positive semi-definite
        solver = LinearSolverFactory.symmPosDef(dimenX);
        // wrap the solver so that it doesn't modify the input
//        solver = new LinearSolverSafe<DenseMatrix64F>(solver);
        // A little bit more performance can be gained by letting S be modified.  In some
        // applications S should not be modified.

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
        MatrixVectorMult.mult(F,x,a);
        x.set(a);

        // P = F P F' + Q
        MatrixMatrixMult.mult_small(F,P,b);
        MatrixMatrixMult.multTransB(b,F, P);
        addEquals(P,Q);
    }

    @Override
    public void update(DenseMatrix64F z, DenseMatrix64F R) {
        // y = z - H x
        MatrixVectorMult.mult(H,x,y);
        sub(z,y,y);

        // S = H P H' + R
        MatrixMatrixMult.mult_small(H,P,c);
        MatrixMatrixMult.multTransB(c,H,S);
        addEquals(S,R);

        // K = PH'S^(-1)
        if( !solver.setA(S) ) throw new RuntimeException("Invert failed");
        solver.invert(S_inv);
        MatrixMatrixMult.multTransA_small(H,S_inv,d);
        MatrixMatrixMult.mult_small(P,d,K);

        // x = x + Ky
        MatrixVectorMult.mult(K,y,a);
        addEquals(x,a);

        // P = (I-kH)P = P - (KH)P = P-K(HP)
        MatrixMatrixMult.mult_small(H,P,c);
        MatrixMatrixMult.mult_small(K,c,b);
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