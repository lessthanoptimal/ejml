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

package org.ejml.dense.row;

import org.ejml.LinearSolverSafe;
import org.ejml.UtilEjml;
import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_FDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_FDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.Random;


/**
 * Contains operations specific to covariance matrices.
 *
 * @author Peter Abeles
 */
public class CovarianceOps_FDRM {

    public static float TOL = UtilEjml.TESTP_F32;

    /**
     * This is a fairly light weight check to see of a covariance matrix is valid.
     * It checks to see if the diagonal elements are all positive, which they should be
     * if it is valid.  Not all invalid covariance matrices will be caught by this method.
	 *
	 * @return true if valid and false if invalid
     */
    public static boolean isValidFast( FMatrixRMaj cov ) {
        return MatrixFeatures_FDRM.isDiagonalPositive(cov);
    }

    /**
     * Performs a variety of tests to see if the provided matrix is a valid
     * covariance matrix.
     *
     * @return  0 = is valid 1 = failed positive diagonal, 2 = failed on symmetry, 2 = failed on positive definite
     */
    public static int isValid( FMatrixRMaj cov ) {
        if( !MatrixFeatures_FDRM.isDiagonalPositive(cov) )
            return 1;

        if( !MatrixFeatures_FDRM.isSymmetric(cov,TOL) )
            return 2;

        if( !MatrixFeatures_FDRM.isPositiveSemidefinite(cov) )
            return 3;

        return 0;
    }

    /**
     * Performs a matrix inversion operations that takes advantage of the special
     * properties of a covariance matrix.
     *
     * @param cov On input it is a covariance matrix, on output it is the inverse.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert( FMatrixRMaj cov ) {
        return invert(cov,cov);
    }

    /**
     * Performs a matrix inversion operations that takes advantage of the special
     * properties of a covariance matrix.
     *
     * @param cov A covariance matrix. Not modified.
     * @param cov_inv The inverse of cov.  Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean invert(final FMatrixRMaj cov , final FMatrixRMaj cov_inv ) {
        if( cov.numCols <= 4 ) {
            if( cov.numCols != cov.numRows ) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }

            if( cov.numCols >= 2 )
                UnrolledInverseFromMinor_FDRM.inv(cov,cov_inv);
            else
                cov_inv.data[0] = 1.0f/cov.data[0];

        } else {
            LinearSolverDense<FMatrixRMaj> solver = LinearSolverFactory_FDRM.symmPosDef(cov.numRows);
            // wrap it to make sure the covariance is not modified.
            solver = new LinearSolverSafe<FMatrixRMaj>(solver);
            if( !solver.setA(cov) )
                return false;
            solver.invert(cov_inv);
        }
        return true;
    }

    /**
     * Sets vector to a random value based upon a zero-mean multivariate Gaussian distribution with
     * covariance 'cov'.  If repeat calls are made to this class, consider using {@link CovarianceRandomDraw_FDRM} instead.
     *
     * @param cov The distirbutions covariance.  Not modified.
     * @param vector The random vector. Modified.
     * @param rand Random number generator.
     */
    public static void randomVector( FMatrixRMaj cov ,
                                     FMatrixRMaj vector ,
                                     Random rand  )
    {
        CovarianceRandomDraw_FDRM rng = new CovarianceRandomDraw_FDRM(rand,cov);
        rng.next(vector);
    }
}
