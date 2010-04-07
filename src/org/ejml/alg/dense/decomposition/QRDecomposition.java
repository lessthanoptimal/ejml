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
 * QR decompositions decompose a rectangular matrix 'A' such that 'A=QR'.  Where
 * A &isin; &real; <sup>n &times; m</sup> , n &ge; m, Q &isin; &real; <sup>n &times; n</sup> is an orthogonal matrix,
 * and R &isin; &real; <sup>n &times; m</sup> is an upper triangular matrix.  Some implementations
 * of QR decomposition require that A has full rank.
 * </p>
 * <p>
 * Some features of QR decompositions:
 * <ul>
 * <li> Can decompose rectangular matrices. </li>
 * <li> Numerically stable solutions to least-squares problem, but not as stable as SVD </li>
 * <li> Can incrementally add and remove columns from the decomposed matrix.  See {@link org.ejml.alg.dense.linsol.qr.AdjLinearSolverQr} </li>
 * </ul>
 * </p>
 * <p>
 * Orthogonal matrices have the following properties:
 * <ul>
 * <li>QQ<sup>T</sup>=I</li>
 * <li>Q<sup>T</sup>=Q<sup>-1</sup></li>
 * </ul>
 * </p>

 * @see org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholder
 * @see org.ejml.alg.dense.decomposition.qr.QRDecompositionHouseholderColumn
 *
 * @author Peter Abeles
 */
public interface QRDecomposition extends DecompositionInterface {
    /**
     * <p>
     * Returns the Q matrix from the decomposition.  Should only
     * be called after {@link #decompose(org.ejml.data.DenseMatrix64F)} has
     * been called.
     * </p>
     *
     * <p>
     * If parameter Q is not null, then that matrix is used to store the Q matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param Q If not null then the Q matrix is written to it.  Modified.
     * @param compact If true an m by n matrix is created, otherwise n by n.
     * @return The Q matrix.
     */
    public DenseMatrix64F getQ(DenseMatrix64F Q, boolean compact);

    /**
     * <p>
     * Returns the R matrix from the decomposition.  Should only be
     * called after {@link #decompose(org.ejml.data.DenseMatrix64F)} has been.
     * </p>
     * <p>
     * If setZeros is true then an n &times; m matrix is required and all the elements are set.
     * If setZeros is false then the matrix must be at least m &times; m and only the upper triangular
     * elements are set.
     * </p>
     *
     * <p>
     * If parameter R is not null, then that matrix is used to store the R matrix.  Otherwise
     * a new matrix is created.
     * </p>
     *
     * @param R If not null then the R matrix is writen to it. Modified.
     * @param compact If true only the upper triangular elements are set
     * @return The R matrix.
     */
    public DenseMatrix64F getR(DenseMatrix64F R, boolean compact);

    /**
     * Computes the QR decomposition of matrix A.  If the decomposition was able
     * to finish without detecting any major issues this function returns true.
     *
     * @param A The matrix being decomposed. Not modified.
     * @return true if A could be decomposed without error and false if not.
     */
    public boolean decompose( DenseMatrix64F A );
}
