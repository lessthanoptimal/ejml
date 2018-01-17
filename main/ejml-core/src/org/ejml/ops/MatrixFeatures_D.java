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

package org.ejml.ops;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;

/**
 * Generic (slow) matrix features for real matrices
 *
 * @author Peter Abeles
 */
public class MatrixFeatures_D {

    /**
     * <p>
     * Checks to see if each element in the two matrices are equal:
     * a<sub>ij</sub> == b<sub>ij</sub>
     * <p>
     *
     * <p>
     * NOTE: If any of the elements are NaN then false is returned.  If two corresponding
     * elements are both positive or negative infinity then they are equal.
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @return true if identical and false otherwise.
     */
    public static boolean isEquals(DMatrix a, DMatrix b ) {
        if( a.getNumRows() != b.getNumRows() || a.getNumCols() != b.getNumCols() )
            return false;

        final int numRows = a.getNumRows();
        final int numCols = a.getNumCols();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if( !(a.unsafe_get(row,col) == b.unsafe_get(row,col)))
                    return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Checks to see if each corresponding element in the two matrices are
     * within tolerance of each other or have the some symbolic meaning.  This
     * can handle NaN and Infinite numbers.
     * <p>
     *
     * <p>
     * If both elements are countable then the following equality test is used:<br>
     * |a<sub>ij</sub> - b<sub>ij</sub>| &le; tol.<br>
     * Otherwise both numbers must both be Double.NaN, Double.POSITIVE_INFINITY, or
     * Double.NEGATIVE_INFINITY to be identical.
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @param tol Tolerance for equality.
     * @return true if identical and false otherwise.
     */
    public static boolean isIdentical(DMatrix a, DMatrix b , double tol ) {
        if( a.getNumRows() != b.getNumRows() || a.getNumCols() != b.getNumCols() ) {
            return false;
        }
        if( tol < 0 )
            throw new IllegalArgumentException("Tolerance must be greater than or equal to zero.");

        final int numRows = a.getNumRows();
        final int numCols = a.getNumCols();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if( !UtilEjml.isIdentical(a.unsafe_get(row,col),b.unsafe_get(row,col), tol))
                    return false;
            }
        }
        return true;
    }
}
