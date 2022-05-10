/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
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
import org.jetbrains.annotations.Nullable;


/**
 * <p>
 * QR decompositions decompose a rectangular matrix 'A' such that 'A=QR'. Where
 * A &isin; &real; <sup>n &times; m</sup> , n &ge; m, Q &isin; &real; <sup>n &times; n</sup> is an orthogonal matrix,
 * and R &isin; &real; <sup>n &times; m</sup> is an upper triangular matrix. Some implementations
 * of QR decomposition require that A has full rank.
 * </p>
 *
 * Some features of QR decompositions:
 * <ul>
 * <li> Can decompose rectangular matrices. </li>
 * <li> Numerically stable solutions to least-squares problem, but not as stable as SVD </li>
 * <li> Can incrementally add and remove columns from the decomposed matrix. See {@link org.ejml.dense.row.linsol.qr.AdjLinearSolverQr_DDRM} </li>
 * </ul>
 *
 * Orthogonal matrices have the following properties:
 * <ul>
 * <li>QQ<sup>T</sup>=I</li>
 * <li>Q<sup>T</sup>=Q<sup>-1</sup></li>
 * </ul>
 *
 * @author Peter Abeles
 */
public interface QRDecomposition <T extends Matrix>
        extends DecompositionInterface<T> {
    /**
     * <p>
     * Returns the Q matrix from the decomposition. Should only be called after
     * {@link #decompose(org.ejml.data.Matrix)} has been called.
     * </p>
     *
     * @param Q (Input) Storage for Q. Reshaped to correct size automatically. If null a new matrix is created.
     * @param compact If true an m by n matrix is created, otherwise n by n.
     * @return The Q matrix.
     */
    T getQ(@Nullable T Q, boolean compact);

    /**
     * <p>
     * Returns the R matrix from the decomposition. Should only be
     * called after {@link #decompose(org.ejml.data.Matrix)} has been.
     * </p>
     * <p>
     * If setZeros is true then an n &times; m matrix is required and all the elements are set.
     * If setZeros is false then the matrix must be at least m &times; m and only the upper triangular
     * elements are set.
     * </p>
     *
     * @param R (Input) Storage for R. Reshaped to correct size automatically. If null a new matrix is created.
     * @param compact If true only the upper triangular elements are set
     * @return The R matrix.
     */
    T getR(@Nullable T R, boolean compact);
}
