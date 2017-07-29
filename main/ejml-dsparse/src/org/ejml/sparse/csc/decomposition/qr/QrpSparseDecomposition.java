/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
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

package org.ejml.sparse.csc.decomposition.qr;

import org.ejml.data.Matrix;
import org.ejml.interfaces.decomposition.QRDecomposition;

/**
 * <p>
 * Similar to {@link QRDecomposition} but it can handle the rank deficient case by
 * performing column pivots during the decomposition. The final decomposition has the
 * following structure:<br>
 * P_r*A*P_c=Q*R<br>
 * where A is the original matrix, P is a pivot matrix, Q is an orthogonal matrix, and R is
 * upper triangular.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO make left looking just regular QR
    // TODO move row pivot matrix into generic header
    // TODO update unit tests
public interface QrpSparseDecomposition<T extends Matrix>
        extends QRDecomposition<T>
{
    /**
     * Returns the rank as determined by the algorithm.  This is dependent upon a fixed threshold
     * and might not be appropriate for some applications.
     *
     * @return Matrix's rank
     */
    int getRank();

    /**
     * Ordering of each column after pivoting.   The current column i was original at column pivot[i].
     *
     * @return Order of columns.
     */
    int[] getColPivots();

    /**
     * Creates the column pivot matrix.
     *
     * @param P Optional storage for pivot matrix.  If null a new matrix will be created.
     * @return The pivot matrix.
     */
    T getColPivotMatrix(T P);

    /**
     * Ordering of each row after pivoting.   The current row i was original at row pivot[i].
     *
     * @return Order of rows.
     */
    int[] getRowPivots();

    /**
     * Creates the row pivot matrix.
     *
     * @param P Optional storage for pivot matrix.  If null a new matrix will be created.
     * @return The pivot matrix.
     */
    T getRowPivotMatrix(T P);

    boolean isColumnPivot();

    boolean isRowPivot();
}
