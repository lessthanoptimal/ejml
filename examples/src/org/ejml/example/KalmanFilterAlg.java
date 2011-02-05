/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionCommon;
import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.chol.LinearSolverChol;
import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;

import static org.ejml.ops.CommonOps.*;

/**
 * The difference between this and {@link KalmanFilterOps} is that it takes advantage of
 * the covariance matrix being a symetric positive semi-definite matrix.  This allows
 * it to be decomposed using {@link org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionInner}.  There are two advantages here,
 * 1) all memory is predeclared and 2) CholeksyDecomposition is more efficient than the more
 * generic {@link org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt LUDecomposition}.  It also makes
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

    private LinearSolver solver;

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

        CholeskyDecomposition chol = DecompositionFactory.chol(dimenX,false,true);

        solver = new LinearSolverChol((CholeskyDecompositionCommon)chol);

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