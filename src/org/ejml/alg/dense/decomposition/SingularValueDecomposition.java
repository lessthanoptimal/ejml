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

package org.ejml.alg.dense.decomposition;

import org.ejml.data.DenseMatrix64F;


/**
 * <p>
 * This is an abstract class for computing the singular value decomposition (SVD) of a matrix, which is defined
 * as:<br>
 * <div align=center> A = U * W * V <sup>T</sup> </div><br>
 * where A is m by n, and U and V are orthogonal matrices, and  W is a diagonal matrix.
 * </p>
 *
 * <p>
 * The dimension of U,W,V depends if it is a compact SVD or not.  If not compact then U  is m by m, W is  m by n, V is n by n.
 *  If compact then let s be the number of singular values, U is m by s, W is s by s, and V is n by s.
 * </p>
 *
 * <p>
 *  To create a new instance of SingularValueDecomposition see {@link org.ejml.ops.DecompositionOps#svd()}
 * and {@link org.ejml.ops.SingularOps} contains additional helpful SVD related functions.
 * </p>
 *
 * <p>
 * <b>*Note*</b> that the ordering of singular values is not guaranteed, unless done so by a specific implementation.
 * The singular values can be put into descending order while adjusting U and V using {@link org.ejml.ops.SingularOps#descendingOrder(org.ejml.data.DenseMatrix64F, boolean, org.ejml.data.DenseMatrix64F, org.ejml.data.DenseMatrix64F, boolean)}  SingularOps.descendingOrder()}.
 * </p>
 *
 * @author Peter Abeles
 */
public abstract interface SingularValueDecomposition extends DecompositionInterface {

    /**
     * Returns the singular values.  This is the diagonal elements of the W matrix in the decomposition.
     * <b>Ordering of singular values is not guaranteed.</b>.
     * 
     * @return Singular values. Note this array can be longer than the number of singular values.
     * Extra elements have no meaning.
     */
    public double [] getSingularValues();

    /**
     * The number of singular values in the matrix. This is equal to the length of the smallest side.
     *
     * @return Number of singular values in the matrix.
     */
    public int numberOfSingularValues();

    /**
     * If true then compact matrices are returned.
     *
     * @return true if results use compact notation.
     */
    public boolean isCompact();

    /**
     * <p>
     * Returns the orthogonal 'U' matrix.
     * </p>
     * <p>
     * Internally the SVD algorithm might compute U transposed or it might not.  To avoid an
     *  unnecessary double transpose the option is provided to select if the transpose is returned.
     * </p>
     *
     * @param transposed If the returned U is transposed.
     * @return An orthogonal matrix.
     */
    public DenseMatrix64F getU( boolean transposed );

    /**
     * <p>
     * Returns the orthogonal 'V' matrix.
     * </p>
     *
     * <p>
     * Internally the SVD algorithm might compute V transposed or it might not.  To avoid an
     *  unnecessary double transpose the option is provided to select if the transpose is returned.
     * </p>
     *
     * @param transposed If the returned V is transposed.
     * @return An orthogonal matrix.
     */
    public DenseMatrix64F getV( boolean transposed );

    /**
     * Returns a diagonal matrix with the singular values.  Order of the singular values
     * is not guaranteed.
     *
     * @return Diagonal matrix with singular values along the diagonal.
     * @param W If not null then the W matrix is written to it.  Modified.
     */
    public DenseMatrix64F getW( DenseMatrix64F W );

    /**
     * Number of rows in the decomposed matrix.
     * @return Number of rows in the decomposed matrix.
     */
    public int numRows();

    /**
     * Number of columns in the decomposed matrix.
     * @return Number of columns in the decomposed matrix.
     */
    public int numCols();
}
