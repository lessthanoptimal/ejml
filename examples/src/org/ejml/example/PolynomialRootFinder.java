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

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRow_F64;
import org.ejml.dense.row.factory.DecompositionFactory_R64;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

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
 * <p>
 * For more algorithms and robust solution for finding polynomial roots check out http://ddogleg.org
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
     * <p>
     * Coefficients are ordered from least to most significant, e.g: y = c[0] + x*c[1] + x*x*c[2].
     * </p>
     *
     * @param coefficients Coefficients of the polynomial.
     * @return The roots of the polynomial
     */
    public static Complex_F64[] findRoots(double... coefficients) {
        int N = coefficients.length-1;

        // Construct the companion matrix
        DMatrixRow_F64 c = new DMatrixRow_F64(N,N);

        double a = coefficients[N];
        for( int i = 0; i < N; i++ ) {
            c.set(i,N-1,-coefficients[i]/a);
        }
        for( int i = 1; i < N; i++ ) {
            c.set(i,i-1,1);
        }

        // use generalized eigenvalue decomposition to find the roots
        EigenDecomposition_F64<DMatrixRow_F64> evd =  DecompositionFactory_R64.eig(N,false);

        evd.decompose(c);

        Complex_F64[] roots = new Complex_F64[N];

        for( int i = 0; i < N; i++ ) {
            roots[i] = evd.getEigenvalue(i);
        }

        return roots;
    }
}
