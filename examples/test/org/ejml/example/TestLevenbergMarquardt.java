/*
 * Copyright (c) 2009-2018, Peter Abeles. All Rights Reserved.
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
        DMatrixRMaj param = new DMatrixRMaj(3,1, true, 2, -1, 4);

        LevenbergMarquardt alg = new LevenbergMarquardt(1);

        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(NUM_PTS,1,rand);

        JacobianTestFunction func = new JacobianTestFunction(X,new DMatrixRMaj(NUM_PTS,1));

        DMatrixRMaj numericalJacobian = new DMatrixRMaj(NUM_PTS,3);
        DMatrixRMaj analyticalJacobian = new DMatrixRMaj(NUM_PTS,3);

        alg.configure(func,param.getNumElements());
        alg.computeNumericalJacobian(param,numericalJacobian);
        func.deriv(X,analyticalJacobian);

        EjmlUnitTests.assertEquals(analyticalJacobian,numericalJacobian,1e-6);
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
        DMatrixRMaj found = new DMatrixRMaj(3,1);
        DMatrixRMaj expected = new DMatrixRMaj(3,1, true, 10, -4, 105.2);

        LevenbergMarquardt alg = new LevenbergMarquardt(1e-4);

        DMatrixRMaj X = RandomMatrices_DDRM.rectangle(numPoints,1,rand);
        DMatrixRMaj Y = new DMatrixRMaj(numPoints,1);
        // compute the observed output given the true praameters
        new JacobianTestFunction(X,Y).function(expected,Y);
        JacobianTestFunction func = new JacobianTestFunction(X,Y);

        alg.optimize(func,found);

        assertEquals(0,alg.getFinalCost(), UtilEjml.TEST_F64);
        for (int i = 0; i < expected.getNumElements(); i++) {
            assertEquals(expected.get(i),found.get(i), Math.abs(expected.get(i))*1e-4);
        }
    }

    /**
     * A very simple function to test how well the numerical jacobian is computed.
     */
    private static class JacobianTestFunction implements LevenbergMarquardt.ResidualFunction
    {

        DMatrixRMaj x;
        DMatrixRMaj y;

        public JacobianTestFunction(DMatrixRMaj x, DMatrixRMaj y) {
            this.x = x;
            this.y = y;
        }

        public void deriv(DMatrixRMaj x, DMatrixRMaj deriv) {
            double dataX[] = x.data;

            int length = x.numRows;

            for( int j = 0; j < length; j++ ) {
                double v = dataX[j];

                double dA = 1;
                double dB = v;
                double dC = v*v;

                deriv.set(j,0,dA);
                deriv.set(j,1,dB);
                deriv.set(j,2,dC);
            }

        }

        public void function( DMatrixRMaj param , DMatrixRMaj y ) {
            double a = param.data[0];
            double b = param.data[1];
            double c = param.data[2];

            int length = x.numRows;

            for( int i = 0; i < length; i++ ) {
                double v = x.data[i];

                y.data[i] = a + b*v + c*v*v;
            }
        }

        @Override
        public void compute(DMatrixRMaj param , DMatrixRMaj residual ) {
            double a = param.data[0];
            double b = param.data[1];
            double c = param.data[2];

            int length = x.numRows;

            for( int i = 0; i < length; i++ ) {
                double v = x.data[i];

                residual.data[i] = a + b*v + c*v*v - y.data[i];
            }
        }

        @Override
        public int numFunctions() {
            return y.getNumElements();
        }
    }
}
