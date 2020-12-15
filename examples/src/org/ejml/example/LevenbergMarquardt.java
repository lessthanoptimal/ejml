/*
 * Copyright (c) 2020, Peter Abeles. All Rights Reserved.
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
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;

/**
 * <p>
 * This is a straight forward implementation of the Levenberg-Marquardt (LM) algorithm. LM is used to minimize
 * non-linear cost functions:<br>
 * <br>
 * S(P) = Sum{ i=1:m , [y<sub>i</sub> - f(x<sub>i</sub>,P)]<sup>2</sup>}<br>
 * <br>
 * where P is the set of parameters being optimized.
 * </p>
 *
 * <p>
 * In each iteration the parameters are updated using the following equations:<br>
 * <br>
 * P<sub>i+1</sub> = (H + &lambda; I)<sup>-1</sup> d <br>
 * d =  (1/N) Sum{ i=1..N , (f(x<sub>i</sub>;P<sub>i</sub>) - y<sub>i</sub>) * jacobian(:,i) } <br>
 * H =  (1/N) Sum{ i=1..N , jacobian(:,i) * jacobian(:,i)<sup>T</sup> }
 * </p>
 * <p>
 * Whenever possible the allocation of new memory is avoided.  This is accomplished by reshaping matrices.
 * A matrix that is reshaped won't grow unless the new shape requires more memory than it has available.
 * </p>
 * @author Peter Abeles
 */
public class LevenbergMarquardt {
    // Convergence criteria
    private int maxIterations = 100;
    private double ftol = 1e-12;
    private double gtol = 1e-12;

    // how much the numerical jacobian calculation perturbs the parameters by.
    // In better implementation there are better ways to compute this delta.  See Numerical Recipes.
    private final static double DELTA = 1e-8;

    // Dampening. Larger values means it's more like gradient descent
    private double initialLambda;

    // the function that is optimized
    private ResidualFunction function;

    // the optimized parameters and associated costs
    private DMatrixRMaj candidateParameters = new DMatrixRMaj(1,1);
    private double initialCost;
    private double finalCost;

    // used by matrix operations
    private DMatrixRMaj g = new DMatrixRMaj(1,1);            // gradient
    private DMatrixRMaj H = new DMatrixRMaj(1,1);            // Hessian approximation
    private DMatrixRMaj Hdiag = new DMatrixRMaj(1,1);
    private DMatrixRMaj negativeStep = new DMatrixRMaj(1,1);

    // variables used by the numerical jacobian algorithm
    private DMatrixRMaj temp0 = new DMatrixRMaj(1,1);
    private DMatrixRMaj temp1 = new DMatrixRMaj(1,1);
    // used when computing d and H variables
    private DMatrixRMaj residuals = new DMatrixRMaj(1,1);

    // Where the numerical Jacobian is stored.
    private DMatrixRMaj jacobian = new DMatrixRMaj(1,1);

    public double getInitialCost() {
        return initialCost;
    }

    public double getFinalCost() {
        return finalCost;
    }

    /**
     *
     * @param initialLambda Initial value of dampening parameter. Try 1 to start
     */
    public LevenbergMarquardt(double initialLambda) {
        this.initialLambda = initialLambda;
    }

    /**
     * Specifies convergence criteria
     *
     * @param maxIterations Maximum number of iterations
     * @param ftol convergence based on change in function value. try 1e-12
     * @param gtol convergence based on residual magnitude. Try 1e-12
     */
    public void setConvergence( int maxIterations , double ftol , double gtol ) {
        this.maxIterations = maxIterations;
        this.ftol = ftol;
        this.gtol = gtol;
    }

    /**
     * Finds the best fit parameters.
     *
     * @param function The function being optimized
     * @param parameters (Input/Output) initial parameter estimate and storage for optimized parameters
     * @return true if it succeeded and false if it did not.
     */
    public boolean optimize(ResidualFunction function, DMatrixRMaj parameters )
    {
        configure(function,parameters.getNumElements());

        // save the cost of the initial parameters so that it knows if it improves or not
        double previousCost = initialCost = cost(parameters);

        // iterate until the difference between the costs is insignificant
        double lambda = initialLambda;

        // if it should recompute the Jacobian in this iteration or not
        boolean computeHessian = true;

        for( int iter = 0; iter < maxIterations; iter++ ) {
            if( computeHessian ) {
                // compute some variables based on the gradient
                computeGradientAndHessian(parameters);
                computeHessian = false;

                // check for convergence using gradient test
                boolean converged = true;
                for (int i = 0; i < g.getNumElements(); i++) {
                    if( Math.abs(g.data[i]) > gtol ) {
                        converged = false;
                        break;
                    }
                }
                if( converged )
                    return true;
            }

            // H = H + lambda*I
            for (int i = 0; i < H.numRows; i++) {
                H.set(i,i, Hdiag.get(i) + lambda);
            }

            // In robust implementations failure to solve is handled much better
            if( !CommonOps_DDRM.solve(H, g, negativeStep) ) {
                return false;
            }

            // compute the candidate parameters
            CommonOps_DDRM.subtract(parameters, negativeStep, candidateParameters);

            double cost = cost(candidateParameters);
            if( cost <= previousCost ) {
                // the candidate parameters produced better results so use it
                computeHessian = true;
                parameters.setTo(candidateParameters);

                // check for convergence
                // ftol <= (cost(k) - cost(k+1))/cost(k)
                boolean converged = ftol*previousCost >= previousCost-cost;

                previousCost = cost;
                lambda /= 10.0;

                if( converged ) {
                    return true;
                }
            } else {
                lambda *= 10.0;
            }

        }
        finalCost = previousCost;
        return true;
    }

    /**
     * Performs sanity checks on the input data and reshapes internal matrices.  By reshaping
     * a matrix it will only declare new memory when needed.
     */
    protected void configure(ResidualFunction function , int numParam )
    {
        this.function = function;
        int numFunctions = function.numFunctions();

        // reshaping a matrix means that new memory is only declared when needed
        candidateParameters.reshape(numParam,1);
        g.reshape(numParam,1);
        H.reshape(numParam,numParam);
        negativeStep.reshape(numParam,1);

        // Normally these variables are thought of as row vectors, but it works out easier if they are column
        temp0.reshape(numFunctions,1);
        temp1.reshape(numFunctions,1);
        residuals.reshape(numFunctions,1);
        jacobian.reshape(numFunctions,numParam);
    }

    /**
     * Computes the d and H parameters.
     *
     * d = J'*(f(x)-y)    <--- that's also the gradient
     * H = J'*J
     */
    private void computeGradientAndHessian(DMatrixRMaj param  )
    {
        // residuals = f(x) - y
        function.compute(param, residuals);

        computeNumericalJacobian(param,jacobian);

        CommonOps_DDRM.multTransA(jacobian, residuals, g);
        CommonOps_DDRM.multTransA(jacobian, jacobian,  H);

        CommonOps_DDRM.extractDiag(H,Hdiag);
    }


    /**
     * Computes the "cost" for the parameters given.
     *
     * cost = (1/N) Sum (f(x) - y)^2
     */
    private double cost(DMatrixRMaj param )
    {
        function.compute(param, residuals);

        double error = NormOps_DDRM.normF(residuals);

        return error*error / (double)residuals.numRows;
    }

    /**
     * Computes a simple numerical Jacobian.
     *
     * @param param (input) The set of parameters that the Jacobian is to be computed at.
     * @param jacobian (output) Where the jacobian will be stored
     */
    protected void computeNumericalJacobian( DMatrixRMaj param ,
                                             DMatrixRMaj jacobian )
    {
        double invDelta = 1.0/DELTA;

        function.compute(param, temp0);

        // compute the jacobian by perturbing the parameters slightly
        // then seeing how it effects the results.
        for( int i = 0; i < param.getNumElements(); i++ ) {
            param.data[i] += DELTA;
            function.compute(param, temp1);
            // compute the difference between the two parameters and divide by the delta
            // temp1 = (temp1 - temp0)/delta
            CommonOps_DDRM.add(invDelta,temp1,-invDelta,temp0,temp1);

            // copy the results into the jacobian matrix
            // J(i,:) = temp1
            CommonOps_DDRM.insert(temp1,jacobian,0,i);

            param.data[i] -= DELTA;
        }
    }

    /**
     * The function that is being optimized. Returns the residual. f(x) - y
     */
    public interface ResidualFunction {
        /**
         * Computes the residual vector given the set of input parameters
         * Function which goes from N input to M outputs
         *
         * @param param (Input) N by 1 parameter vector
         * @param residual (Output) M by 1 output vector to store the residual = f(x)-y
         */
        void compute(DMatrixRMaj param , DMatrixRMaj residual );

        /**
         * Number of functions in output
         * @return function count
         */
        int numFunctions();
    }
}
