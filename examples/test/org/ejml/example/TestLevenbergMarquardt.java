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
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestLevenbergMarquardt {
    int NUM_PTS = 50;

    Random rand = new Random(7264);

    /**
     * Give it a simple function and see if it computes something close to it for its results.
     */
    @Test
    public void testNumericalJacobian() {
        JacobianTestFunction func = new JacobianTestFunction();

        DMatrixRMaj param = new DMatrixRMaj(3,1, true, 2, -1, 4);

        LevenbergMarquardt alg = new LevenbergMarquardt(func);

        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(NUM_PTS,1,rand);

        DMatrixRMaj numJacobian = new DMatrixRMaj(3,NUM_PTS);
        DMatrixRMaj analyticalJacobian = new DMatrixRMaj(3,NUM_PTS);

        alg.configure(param,X,new DMatrixRMaj(NUM_PTS,1));
        alg.computeNumericalJacobian(param,X,numJacobian);
        func.deriv(X,analyticalJacobian);

        EjmlUnitTests.assertEquals(analyticalJacobian,numJacobian,1e-6);
    }

    /**
     * See if it can solve an easy optimization problem.
     */
    @Test
    public void testTrivial() {
        // the number of sample points is equal to the max allowed points
        runTrivial(NUM_PTS);
        // do the same thing but with a different number of poitns from the max allowed
        runTrivial(20);
    }

    /**
     * Runs the simple optimization problem with a set of randomly generated inputs.
     *
     * @param numPoints How many sample points there are.
     */
    public void runTrivial( int numPoints ) {
        JacobianTestFunction func = new JacobianTestFunction();

        DMatrixRMaj paramInit = new DMatrixRMaj(3,1);
        DMatrixRMaj param = new DMatrixRMaj(3,1, true, 2, -1, 4);

        LevenbergMarquardt alg = new LevenbergMarquardt(func);

        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(numPoints,1,rand);
        DMatrixRMaj Y = new DMatrixRMaj(numPoints,1);
        func.compute(param,X,Y);

        alg.optimize(paramInit,X,Y);

        DMatrixRMaj foundParam = alg.getParameters();

        assertEquals(0,alg.getFinalCost(), UtilEjml.TEST_F64);
        EjmlUnitTests.assertEquals(param,foundParam,1e-6);
    }

    /**
     * A very simple function to test how well the numerical jacobian is computed.
     */
    private static class JacobianTestFunction implements LevenbergMarquardt.Function
    {

        public void deriv(DMatrixRMaj x, DMatrixRMaj deriv) {
            double dataX[] = x.data;

            int length = x.numRows;

            for( int j = 0; j < length; j++ ) {
                double v = dataX[j];

                double dA = 1;
                double dB = v;
                double dC = v*v;

                deriv.set(0,j,dA);
                deriv.set(1,j,dB);
                deriv.set(2,j,dC);
            }

        }

        @Override
        public void compute(DMatrixRMaj param, DMatrixRMaj x, DMatrixRMaj y) {
            double a = param.data[0];
            double b = param.data[1];
            double c = param.data[2];

            double dataX[] = x.data;
            double dataY[] = y.data;

            int length = x.numRows;

            for( int i = 0; i < length; i++ ) {
                double v = dataX[i];

                dataY[i] = a + b*v + c*v*v;
            }
        }
    }
}
