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

package org.ejml.interfaces.decomposition;

import org.ejml.data.Matrix;


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
 * If compact then let s be the number of singular values, U is m by s, W is s by s, and V is n by s.
 * </p>
 *
 * <p>
 * Accessor functions for decomposed matrices can return an internally constructed matrix if null is passed in for the
 * optional storage parameter.  The exact behavior is implementation specific.  If an internally maintained matrix is
 * returned then on the next call to decompose the matrix will be modified.  The advantage of this approach is reduced
 * memory overhead.
 * </p>
 *
 * <p>
 * To create a new instance of SingularValueDecomposition see {@link org.ejml.factory.DecompositionFactory#svd(int, int, boolean, boolean, boolean)}
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
public abstract interface SingularValueDecomposition <T extends Matrix>
        extends DecompositionInterface<T> {

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
     * unnecessary double transpose the option is provided to select if the transpose is returned.
     * </p>
     *
     * @param U Optional storage for U. If null a new instance or internally maintained matrix is returned.  Modified.
     * @param transposed If the returned U is transposed.
     * @return An orthogonal matrix.
     */
    public T getU( T U , boolean transposed );

    /**
     * <p>
     * Returns the orthogonal 'V' matrix.
     * </p>
     *
     * <p>
     * Internally the SVD algorithm might compute V transposed or it might not.  To avoid an
     * unnecessary double transpose the option is provided to select if the transpose is returned.
     * </p>
     *
     * @param V Optional storage for v. If null a new instance or internally maintained matrix is returned.  Modified.
     * @param transposed If the returned V is transposed.
     * @return An orthogonal matrix.
     */
    public T getV( T V , boolean transposed );

    /**
     * Returns a diagonal matrix with the singular values.  Order of the singular values
     * is not guaranteed.
     *
     * @param W Optional storage for W. If null a new instance or internally maintained matrix is returned.  Modified.
     * @return Diagonal matrix with singular values along the diagonal.
     */
    public T getW( T W );

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
