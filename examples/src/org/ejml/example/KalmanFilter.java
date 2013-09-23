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

/**
 * <p>
 * This is an interface for a discrete time Kalman filter with no control input:<br>
 * <br>
 * x<sub>k</sub> = F<sub>k</sub> x<sub>k-1</sub> + w<sub>k</sub><br>
 * z<sub>k</sub> = H<sub>k</sub> x<sub>k</sub> + v<sub>k</sub> <br>
 * <br>
 * w<sub>k</sub> ~ N(0,Q<sub>k</sub>)<br>
 * v<sub>k</sub> ~ N(0,R<sub>k</sub>)<br>
 * </p>
 *
 * @author Peter Abeles
 */
public interface KalmanFilter {

    /**
     * Specify the kinematics model of the Kalman filter.  This must be called
     * first before any other functions.
     *
     * @param F State transition matrix.
     * @param Q plant noise.
     * @param H measurement projection matrix.
     */
    public void configure( DenseMatrix64F F, DenseMatrix64F Q ,
                           DenseMatrix64F H);

    /**
     * The prior state estimate and covariance.
     *
     * @param x The estimated system state.
     * @param P The covariance of the estimated system state.
     */
    public void setState( DenseMatrix64F x , DenseMatrix64F P );

    /**
     * Predicts the state of the system forward one time step.
     */
    public void predict();

    /**
     * Updates the state provided the observation from a sensor.
     *
     * @param z Measurement.
     * @param R Measurement covariance.
     */
    public void update( DenseMatrix64F z , DenseMatrix64F R );

    /**
     * Returns the current estimated state of the system.
     *
     * @return The state.
     */
    public DenseMatrix64F getState();

    /**
     * Returns the estimated state's covariance matrix.
     *
     * @return The covariance.
     */
    public DenseMatrix64F getCovariance();
}
