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

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;

/**
 * <p>
 * Eigenvalue decomposition can be used to find the roots in a polynomial by constructing the
 * so called companion matrix.  While faster techniques do exist for root finding, this is
 * one of the most stable and probably the easiest to implement.
 * </p>
 *
 * <p>
 * Because the companion matrix is not symmetric a generalized eigenvalue decomposition is needed.
 * The roots of the polynomial may also be complex.  Complex eigenvalues is the only instance in
 * which EJML supports complex arithmetic.  Depending on the application one might need to check
 * to see if the eigenvalues are real or complex.
 * </p>
 *
 * @author Peter Abeles
 */
public class PolynomialRootFinder {

    /**
     * <p>
     * Given a set of polynomial coefficients, compute the roots of the polynomial.  Depending on
     * the polynomial being considered the roots may contain complex number.  When complex numbers are
     * present they will come in pairs of complex conjugates.
     * </p>
     *
     * @param coefficients Coefficients of the polynomial.
     * @return The roots of the polynomial
     */
    public static Complex64F[] findRoots(double... coefficients) {
        int N = coefficients.length-1;

        // Construct the companion matrix
        DenseMatrix64F c = new DenseMatrix64F(N,N);

        double a = coefficients[N];
        for( int i = 0; i < N; i++ ) {
            c.set(i,N-1,-coefficients[i]/a);
        }
        for( int i = 1; i < N; i++ ) {
            c.set(i,i-1,1);
        }

        // use generalized eigenvalue decomposition to find the roots
        EigenDecomposition<DenseMatrix64F> evd =  DecompositionFactory.eigGeneral(N, false);

        evd.decompose(c);

        Complex64F[] roots = new Complex64F[N];

        for( int i = 0; i < N; i++ ) {
            roots[i] = evd.getEigenvalue(i);
        }

        return roots;
    }
}
