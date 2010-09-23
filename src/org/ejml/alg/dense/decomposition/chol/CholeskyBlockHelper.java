/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;


/**
 * A specialized Cholesky decomposition algorithm that is designed to help out
 * {@link CholeskyDecompositionBlock} perform its calculations.  While decomposing
 * the matrix it will modify its internal lower triangular matrix and the original
 * that is being modified.
 *
 * @author Peter Abeles
 */
class CholeskyBlockHelper {

    // the decomposed matrix
    private DenseMatrix64F L;
    private double[] el;

    /**
     * Creates a CholeksyDecomposition capable of decompositong a matrix that is
     * n by n, where n is the width.
     *
     * @param widthMax The maximum width of a matrix that can be processed.
     */
    public CholeskyBlockHelper(int widthMax ) {

        this.L = new DenseMatrix64F(widthMax,widthMax);
        this.el = L.data;
    }

    /**
     * Decomposes a submatrix.  The results are writen to the submatrix
     * and to its internal matrix L.
     *
     * @param mat A matrix which has a submatrix that needs to be inverted
     * @param indexStart the first index of the submatrix
     * @param n The width of the submatrix that is to be inverted.
     * @return True if it was able to finish the decomposition.
     */
    public boolean decompose( DenseMatrix64F mat , int indexStart , int n ) {
        double m[] = mat.data;

        double el_ii;
        double div_el_ii=0;

        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = m[indexStart+i*mat.numCols+j];

                int iEl = i*n;
                int jEl = j*n;
                int end = iEl+i;
                // k = 0:i-1
                for( ; iEl<end; iEl++,jEl++ ) {
//                    sum -= el[i*n+k]*el[j*n+k];
                    sum -= el[iEl]*el[jEl];
                }

                if( i == j ) {
                    // is it positive-definate?
                    if( sum <= 0.0 )
                        return false;

                    el_ii = Math.sqrt(sum);
                    el[i*n+i] = el_ii;
                    m[indexStart+i*mat.numCols+i] = el_ii;
                    div_el_ii = 1.0/el_ii;
                } else {
                    double v = sum*div_el_ii;
                    el[j*n+i] = v;
                    m[indexStart+j*mat.numCols+i] = v;
                }
            }
        }

        return true;
    }

    /**
     * Returns L matrix from the decomposition.<br>
     * L*L<sup>T</sup>=A
     *
     * @return A lower triangular matrix.
     */
    public DenseMatrix64F getL() {
        return L;
    }
}