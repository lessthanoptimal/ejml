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

import org.ejml.alg.dense.decomposition.DecompositionInterface;
import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * This variant on the Cholesky decomposition avoid the need to take the square root
 * by performing the following decomposition:<br>
 * <br>
 * L*D*L<sup>T</sup>=A<br>
 * <br>
 * where L is a lower triangular matrix with zeros on the diagonal. D is a diagonal matrix.
 * The diagonal elements of L are equal to one.
 * </p>
 * <p>
 * Unfortunately the speed advantage of not computing the square root is washed out by the
 * increased number of array accesses.  There only appears to be a slight speed boost for
 * very small matrices.
 * </p>
 *
 * @author Peter Abeles
 */
public class CholeskyDecompositionLDL implements DecompositionInterface {

    // it can decompose a matrix up to this width
    private int maxWidth;
    // width and height of the matrix
    private int n;

    // the decomposed matrix
    private DenseMatrix64F L;
    private double[] el;

    // the D vector
    private double[] d;

    // tempoary variable used by various functions
    double vv[];

    @Override
    public void setExpectedMaxSize( int numRows , int numCols ) {
        if( numRows != numCols ) {
            throw new IllegalArgumentException("Can only decompose square matrices");
        }

        this.maxWidth = numRows;

        this.L = new DenseMatrix64F(maxWidth,maxWidth);
        this.el = L.data;

        this.vv = new double[maxWidth];
        this.d = new double[maxWidth];
    }

    /**
     * <p>
     * Performs Choleksy decomposition on the provided matrix.
     * </p>
     *
     * <p>
     * If the matrix is not positive definite then this function will return
     * false since it can't complete its computations.  Not all errors will be
     * found.
     * </p>
     * @param mat A symetric n by n positive definite matrix.
     * @return True if it was able to finish the decomposition.
     */
    public boolean decompose( DenseMatrix64F mat ) {
        if( mat.numRows > maxWidth ) {
            setExpectedMaxSize(mat.numRows,mat.numCols);
        } else if( mat.numRows != mat.numCols ) {
            throw new RuntimeException("Can only decompose square matrices");
        }
        n = mat.numRows;

        L.setReshape(mat);

        double d_inv=0;
        for( int i = 0; i < n; i++ ) {
            for( int j = i; j < n; j++ ) {
                double sum = el[i*n+j];

                for( int k = 0; k < i; k++ ) {
                    sum -= el[i*n+k]*el[j*n+k]*d[k];
                }

                if( i == j ) {
                    // is it positive-definate?
                    if( sum <= 0.0 )
                        return false;

                    d[i] = sum;
                    d_inv = 1.0/sum;
                    el[i*n+i] = 1;
                } else {
                    el[j*n+i] = sum*d_inv;
                }
            }
        }
        // zero the top right corner.
        for( int i = 0; i < n; i++ ) {
            for( int j = i+1; j < n; j++ ) {
                el[i*n+j] = 0.0;
            }
        }

        return true;
    }

    /**
     * Diagonal elements of the diagonal D matrix.
     *
     * @return diagonal elements of D
     */
    public double[] getD() {
        return d;
    }

    /**
     * Returns L matrix from the decomposition.<br>
     * L*D*L<sup>T</sup>=A
     *
     * @return A lower triangular matrix.
     */
    public DenseMatrix64F getL() {
        return L;
    }

    public double[] _getVV() {
        return vv;
    }
}