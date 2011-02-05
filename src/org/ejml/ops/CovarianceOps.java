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

package org.ejml.ops;

import org.ejml.alg.dense.linsol.LinearSolver;
import org.ejml.alg.dense.linsol.LinearSolverFactory;
import org.ejml.alg.dense.misc.UnrolledInverseFromMinor;
import org.ejml.data.DenseMatrix64F;

import java.util.Random;


/**
 * Contains operations specific to covariance matrices.
 *
 * @author Peter Abeles
 */
public class CovarianceOps {

    public static double TOL = 1e-9;

    /**
     * This is a fairly light weight check to see of a covariance matrix is valid.
     * It checks to see if the diagonal elements are all postive, which they should be
     * if it is valid.  Not all invalid covariance matrices will be caught by this method.
     */
    public static boolean isValidFast( DenseMatrix64F cov ) {
        return MatrixFeatures.isDiagonalPositive(cov);
    }

    /**
     * Performs a variety of tests to see if the provided matrix is a valid
     * covariance matrix.
     *
     * @return  0 = is valid 1 = failed positive diagonal, 2 = failed on symmetry, 2 = failed on positive definite
     */
    public static int isValid( DenseMatrix64F cov ) {
        if( !MatrixFeatures.isDiagonalPositive(cov) )
            return 1;

        if( !MatrixFeatures.isSymmetric(cov,TOL) )
            return 2;

        if( !MatrixFeatures.isPositiveSemidefinite(cov) )
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
    public static boolean invert( DenseMatrix64F cov ) {
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
    public static boolean invert( final DenseMatrix64F cov , final DenseMatrix64F cov_inv ) {
        if( cov.numCols <= 4 ) {
            if( cov.numCols != cov.numRows ) {
                throw new IllegalArgumentException("Must be a square matrix.");
            }

            if( cov.numCols >= 2 )
                UnrolledInverseFromMinor.inv(cov,cov_inv);
            else
                cov_inv.data[0] = 1.0/cov_inv.data[0];

        } else {
            LinearSolver solver = LinearSolverFactory.symmetric();
            if( !solver.setA(cov) )
                return false;
            solver.invert(cov_inv);
        }
        return true;
    }

    /**
     * Sets vector to a random value based upon a zero-mean multivariate Gaussian distribution with
     * covariance 'cov'.  If repeat calls are made to this class, consider using {@link CovarianceRandomDraw} instead.
     *
     * @param cov The distirbutions covariance.  Not modified.
     * @param vector The random vector. Modified.
     * @param rand Random number generator.
     */
    public static void randomVector( DenseMatrix64F cov ,
                                     DenseMatrix64F vector ,
                                     Random rand  )
    {
        CovarianceRandomDraw rng = new CovarianceRandomDraw(rand,cov);
        rng.next(vector);
    }
}
