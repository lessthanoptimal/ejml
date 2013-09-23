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
import org.ejml.ops.EjmlUnitTests;
import org.ejml.ops.RandomMatrices;
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

        DenseMatrix64F param = new DenseMatrix64F(3,1, true, 2, -1, 4);

        LevenbergMarquardt alg = new LevenbergMarquardt(func);

        DenseMatrix64F X = RandomMatrices.createRandom(NUM_PTS,1,rand);

        DenseMatrix64F numJacobian = new DenseMatrix64F(3,NUM_PTS);
        DenseMatrix64F analyticalJacobian = new DenseMatrix64F(3,NUM_PTS);

        alg.configure(param,X,new DenseMatrix64F(NUM_PTS,1));
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

        DenseMatrix64F paramInit = new DenseMatrix64F(3,1);
        DenseMatrix64F param = new DenseMatrix64F(3,1, true, 2, -1, 4);

        LevenbergMarquardt alg = new LevenbergMarquardt(func);

        DenseMatrix64F X = RandomMatrices.createRandom(numPoints,1,rand);
        DenseMatrix64F Y = new DenseMatrix64F(numPoints,1);
        func.compute(param,X,Y);

        alg.optimize(paramInit,X,Y);

        DenseMatrix64F foundParam = alg.getParameters();

        assertEquals(0,alg.getFinalCost(),1e-8);
        EjmlUnitTests.assertEquals(param,foundParam,1e-6);
    }

    /**
     * A very simple function to test how well the numerical jacobian is computed.
     */
    private static class JacobianTestFunction implements LevenbergMarquardt.Function
    {

        public void deriv( DenseMatrix64F x, DenseMatrix64F deriv) {
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
        public void compute(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F y) {
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
