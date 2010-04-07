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

package org.ejml.alg.dense.decomposition.svd;

import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * <p>
 * This is an abstract class for singular value decomposition (SVD) algorithms.  It provides
 * common data structures and a generic interface for using the results.  Any child of this
 * class will compute the SVD of A:<br>
 * <div align=center> A = U * W * V <sup>T</sup> </div><br>
 * where A is m by n, and U,W,V are all n by n. U and V are orthogonal matrices.
 * W is a diagonal matrix.
 * </p>
 *
 * <p>
 * The inverse from SVD is computed as follows:<br>
 *
 * <div align=center>
 * A<sup>-1</sup>=V*[diag(1/w<sub>j</sub>)]*U<sup>T</sup>
 * </div>
 * <br>
 * </p>
 *
 * @see org.ejml.alg.dense.decomposition.svd.SvdNumericalRecipes
 *
 * @author Peter Abeles
 */
public abstract class SingularValueDecompositionBase implements SingularValueDecomposition {
    // the shape of the matrix that it can decompose
    // m = number of rows
    // n = number of columns
    protected int m, n;

    // the maximum size of the matrix that it can decompose.
    protected int maxRows=-1,maxCols=-1;

    // the refactor matrices
    protected DenseMatrix64F U, V;
    protected double[] u, v, w;

    // data structures used to hold temporary data
    protected double xx[];
    protected double tmp[];

    @Override
    public void setExpectedMaxSize( int numRows , int numCols ) {
        this.maxRows = numRows;
        this.maxCols = numCols;

        U = new DenseMatrix64F(numRows, numCols);
        V = new DenseMatrix64F(numCols, numCols);

        u = U.data;
        w = new double[numCols];
        v = V.data;

        xx = new double[numCols];
        tmp = new double[numCols];
    }

    /**
     * Performs SVD on the specified matrix.  This function must be called first
     * before many of the other functions will function correctly.
     *
     * @param mat The m by n matrix that SVD is to be performed on. Not modified.
     * @return True if it was able to decompose the matrix, false otherwise.
     */
    public abstract boolean decompose(DenseMatrix64F mat);

    /**
     * Returns the singular values.  This is the diagonal elements of the W matrix in the decomposition.
     *
     * @return Singular values.
     */
    public double [] getSingularValues() {
        return w;
    }

    /**
     * Returns the 'U' is the refactoring:
     *
     * A = U W V <sup>T</sup> <br>
     *
     * @return An orthogonal n by n matrix.
     */
    public DenseMatrix64F getU() {
        return U;
    }

    /**
     * Returns the 'V' is the refactoring:
     *
     * A = U W V <sup>T</sup> <br>
     *
     * @return An orthogonal n by n matrix.
     */
    public DenseMatrix64F getV() {
        return V;
    }
    /**
     * Returns a diagonal matrix with the singular values.
     *
     * @return Diagonal matrix with the singular values.
     */
    @Override
    public DenseMatrix64F getW( DenseMatrix64F W ) {
        if( W == null )
            W = new DenseMatrix64F(n,n);
        else
            W.reshape(n,n, false);

        return CommonOps.diag(W,n,w);
    }

    public double[] _getXX() {
        return xx;
    }

    public double[] _getTmp() {
        return tmp;
    }
}