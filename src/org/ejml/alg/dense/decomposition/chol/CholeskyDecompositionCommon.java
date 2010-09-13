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


import org.ejml.alg.dense.decomposition.CholeskyDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 *
 * <p>
 * This is an abstract class for a Cholesky decomposition.  It provides the solvers, but the actual
 * decompsoition is provided in other classes.
 * </p>
 * <p>
 * A Cholesky Decomposition is a special decomposition for positive-definite symmetric matrices
 * that is more efficient than other general purposes decomposition. It refactors matrices
 * using one of the two following equations:<br>
 * <br>
 * L*L<sup>T</sup>=A<br>
 * R<sup>T</sup>*R=A<br>
 * <br>
 * where L is a lower triangular matrix and R is an upper traingular matrix.<br>
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBasic
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionBlock
 * @see org.ejml.alg.dense.decomposition.chol.CholeskyDecompositionLDL
 *
 * @author Peter Abeles
 */
public abstract class CholeskyDecompositionCommon implements CholeskyDecomposition {

    // it can decompose a matrix up to this width
    protected int maxWidth=-1;

    // width and height of the matrix
    protected int n;

    // the decomposed matrix
    protected DenseMatrix64F T;
    protected double[] t;

    // tempoary variable used by various functions
    protected double vv[];

    // should it store the results from the decompose in the matrix that is passed in?
    private boolean decomposeOrig;

    // is it a lower triangular matrix or an upper triangular matrix
    protected boolean lower;

    /**
     * Creates a CholeksyDecomposition capable of decompositong a matrix that is
     * n by n, where n is the width.
     *
     * @param decomposeOrig Should it decompose the matrix that is passed in or declare a new one?
     * @param lower should a lower or upper triangular matrix be used.
     */
    public CholeskyDecompositionCommon(boolean decomposeOrig, boolean lower ) {
        this.lower = lower;

        this.decomposeOrig = decomposeOrig;
    }

    public void setExpectedMaxSize( int numRows , int numCols ) {
        if( numRows != numCols ) {
            throw new IllegalArgumentException("Can only decompose square matrices");
        }

        this.maxWidth = numCols;

        if( !decomposeOrig ) {
            this.T = new DenseMatrix64F(maxWidth,maxWidth);
            this.t = T.data;
        }

        this.vv = new double[maxWidth];
    }

    /**
     * If true the decomposition was for a lower triangular matrix.
     * If false it was for an upper triangular matrix.
     *
     * @return True if lower, false if upper.
     */
    @Override
    public boolean isLower() {
        return lower;
    }

    /**
     * <p>
     * Performs Choleksy decomposition on the provided matrix.
     * </p>
     *
     * <p>
     * If the matrix is not positive definite then this function will return
     * false since it can't complete its computations.  Not all errors will be
     * found.  This is an efficient way to check for positive definiteness.
     * </p>
     * @param mat A symmetric positive definite matrix with n <= widthMax.
     * @return True if it was able to finish the decomposition.
     */
    @Override
    public boolean decompose( DenseMatrix64F mat ) {
        if( mat.numRows > maxWidth ) {
            setExpectedMaxSize(mat.numRows,mat.numCols);
        } else if( mat.numRows != mat.numCols ) {
            throw new IllegalArgumentException("Must be a square matrix.");
        }

        n = mat.numRows;

        if( decomposeOrig ) {
            T = mat;
            t = T.data;
        } else {
//            L.set(mat);
            T.setReshape(mat);
        }

        if(lower) {
            return decomposeLower();
        } else {
            return decomposeUpper();
        }
    }

    @Override
    public boolean modifyInput() {
        return decomposeOrig;
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
    public DenseMatrix64F getT( DenseMatrix64F T ) {
        // see if it needs to declare a new matrix or not
        if( T == null ) {
            T = new DenseMatrix64F(n,n);
        } else {
            if( T.numRows != n || T.numCols != n )
                throw new IllegalArgumentException("Unexpected matrix dimension for T.");

            CommonOps.set(T,0);
        }

        // write the values to T
        if( lower ) {
            for( int i = 0; i < n; i++ ) {
                for( int j = 0; j <= i; j++ ) {
                    T.set(i,j,this.T.get(i,j));
                }
            }
        } else {
            for( int i = 0; i < n; i++ ) {
                for( int j = i; j < n; j++ ) {
                    T.set(i,j,this.T.get(i,j));
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
    public DenseMatrix64F getT() {
        return T;
    }

    public double[] _getVV() {
        return vv;
    }
}