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

import org.ejml.data.DMatrixRow_F64;
import org.ejml.equation.Equation;
import org.ejml.equation.Sequence;

/**
 * Example of how the equation interface can greatly simplify code
 *
 * @author Peter Abeles
 */
public class KalmanFilterEquation implements KalmanFilter{

    // system state estimate
    private DMatrixRow_F64 x,P;

    private Equation eq;

    // Storage for precompiled code for predict and update
    Sequence predictX,predictP;
    Sequence updateY,updateK,updateX,updateP;

    @Override
    public void configure(DMatrixRow_F64 F, DMatrixRow_F64 Q, DMatrixRow_F64 H) {
        int dimenX = F.numCols;

        x = new DMatrixRow_F64(dimenX,1);
        P = new DMatrixRow_F64(dimenX,dimenX);

        eq = new Equation();

        // Provide aliases between the symbolic variables and matrices we normally interact with
        // The names do not have to be the same.
        eq.alias(x,"x",P,"P",Q,"Q",F,"F",H,"H");

        // Dummy matrix place holder to avoid compiler errors.  Will be replaced later on
        eq.alias(new DMatrixRow_F64(1,1),"z");
        eq.alias(new DMatrixRow_F64(1,1),"R");

        // Pre-compile so that it doesn't have to compile it each time it's invoked.  More cumbersome
        // but for small matrices the overhead is significant
        predictX = eq.compile("x = F*x");
        predictP = eq.compile("P = F*P*F' + Q");

        updateY = eq.compile("y = z - H*x");
        updateK = eq.compile("K = P*H'*inv( H*P*H' + R )");
        updateX = eq.compile("x = x + K*y");
        updateP = eq.compile("P = P-K*(H*P)");
    }

    @Override
    public void setState(DMatrixRow_F64 x, DMatrixRow_F64 P) {
        this.x.set(x);
        this.P.set(P);
    }

    @Override
    public void predict() {
        predictX.perform();
        predictP.perform();
    }

    @Override
    public void update(DMatrixRow_F64 z, DMatrixRow_F64 R) {

        // Alias will overwrite the reference to the previous matrices with the same name
        eq.alias(z,"z"); eq.alias(R,"R");

        updateY.perform();
        updateK.perform();
        updateX.perform();
        updateP.perform();
    }

    @Override
    public DMatrixRow_F64 getState() {
        return x;
    }

    @Override
    public DMatrixRow_F64 getCovariance() {
        return P;
    }
}