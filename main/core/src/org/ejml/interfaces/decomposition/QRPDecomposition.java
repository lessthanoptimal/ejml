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

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix;

/**
 * <p>
 * Similar to {@link QRDecomposition} but it can handle the rank deficient case by
 * performing column pivots during the decomposition. The final decomposition has the
 * following structure:<br>
 * A*P=Q*R<br>
 * where A is the original matrix, P is a pivot matrix, Q is an orthogonal matrix, and R is
 * upper triangular.
 * </p>
 *
 * @author Peter Abeles
 */
public interface QRPDecomposition <T extends Matrix>
        extends QRDecomposition<T>
{
    /**
     * <p>
     * Specifies the threshold used to flag a column as being singular.  The specified threshold is relative
     * and will very depending on the system.  The default value is UtilEJML.EPS.
     * </p>
     *
     * @param threshold Singular threshold.
     */
    public void setSingularThreshold( double threshold );

    /**
     * Returns the rank as determined by the algorithm.  This is dependent upon a fixed threshold
     * and might not be appropriate for some applications.
     *
     * @return Matrix's rank
     */
    public int getRank();

    /**
     * Ordering of each column after pivoting.   The current column i was original at column pivot[i].
     *
     * @return Order of columns.
     */
    public int[] getPivots();

    /**
     * Creates the pivot matrix.
     *
     * @param P Optional storage for pivot matrix.  If null a new matrix will be created.
     * @return The pivot matrix.
     */
    public DenseMatrix64F getPivotMatrix( DenseMatrix64F P );
}
