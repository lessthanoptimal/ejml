/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
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

package org.ejml.alg.dense.decomposition.chol;

import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyLDLDecomposition;
import org.ejml.ops.CommonOps;


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
public class CholeskyDecompositionLDL_D64
        implements CholeskyLDLDecomposition<DenseMatrix64F> {

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

        L.set(mat);

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

    @Override
    public boolean inputModified() {
        return false;
    }

    /**
     * Diagonal elements of the diagonal D matrix.
     *
     * @return diagonal elements of D
     */
    @Override
    public double[] getDiagonal() {
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

    @Override
    public DenseMatrix64F getL(DenseMatrix64F L) {
        if( L == null ) {
            L = this.L.copy();
        } else {
            L.set(this.L);
        }

        return L;
    }

    @Override
    public DenseMatrix64F getD(DenseMatrix64F D) {
        return CommonOps.diag(D,L.numCols,d);
    }
}