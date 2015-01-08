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

package org.ejml.alg.dense.decompose.chol;


import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.ops.CCommonOps;


/**
 *
 * <p>
 * This is an abstract class for a Cholesky decomposition.  It provides the solvers, but the actual
 * decomposition is provided in other classes.
 * </p>
 *
 * @see org.ejml.interfaces.decomposition.CholeskyDecomposition
 * @author Peter Abeles
 */
public abstract class CholeskyDecompositionCommon_CD64
        implements CholeskyDecomposition<CDenseMatrix64F> {

    // it can decompose a matrix up to this width
    protected int maxWidth=-1;

    // width and height of the matrix
    protected int n;

    // the decomposed matrix
    protected CDenseMatrix64F T;
    protected double[] t;

    // tempoary variable used by various functions
    protected double vv[];

    // is it a lower triangular matrix or an upper triangular matrix
    protected boolean lower;

    /**
     * Specifies if a lower or upper variant should be constructed.
     *
     * @param lower should a lower or upper triangular matrix be used.
     */
    public CholeskyDecompositionCommon_CD64(boolean lower) {
        this.lower = lower;
    }

    public void setExpectedMaxSize( int numRows , int numCols ) {
        if( numRows != numCols ) {
            throw new IllegalArgumentException("Can only decompose square matrices");
        }

        this.maxWidth = numCols;

        this.vv = new double[maxWidth];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLower() {
        return lower;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean decompose( CDenseMatrix64F mat ) {
        if( mat.numRows > maxWidth ) {
            setExpectedMaxSize(mat.numRows,mat.numCols);
        } else if( mat.numRows != mat.numCols ) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }

        n = mat.numRows;

        T = mat;
        t = T.data;

        if(lower) {
            return decomposeLower();
        } else {
            return decomposeUpper();
        }
    }

    @Override
    public boolean inputModified() {
        return true;
    }

    /**
     * Performs an lower triangular decomposition.
     *
     * @return true if the matrix was decomposed.
     */
    protected abstract boolean decomposeLower();

    /**
     * Performs an upper triangular decomposition.
     *
     * @return true if the matrix was decomposed.
     */
    protected abstract boolean decomposeUpper();

    @Override
    public CDenseMatrix64F getT( CDenseMatrix64F T ) {
        // see if it needs to declare a new matrix or not
        if( T == null ) {
            T = new CDenseMatrix64F(n,n);
        } else {
            if( T.numRows != n || T.numCols != n )
                throw new IllegalArgumentException("Unexpected matrix dimension for T.");

            CCommonOps.fill(T, 0,0);
        }

        // write the values to T
        if( lower ) {
            for( int i = 0; i < n; i++ ) {
                int index = i*n*2;
                for( int j = 0; j <= i; j++ ) {
                    T.data[index] = this.T.data[index];
                    index++;
                    T.data[index] = this.T.data[index];
                    index++;
                }
            }
        } else {
            for( int i = 0; i < n; i++ ) {
                int index = (i*n + i)*2;
                for( int j = i; j < n; j++ ) {
                    T.data[index] = this.T.data[index];
                    index++;
                    T.data[index] = this.T.data[index];
                    index++;
                }
            }
        }

        return T;
    }

    /**
     * Returns the triangular matrix from the decomposition.
     *
     * @return A lower or upper triangular matrix.
     */
    public CDenseMatrix64F getT() {
        return T;
    }

    public double[] _getVV() {
        return vv;
    }
}