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

package org.ejml.ops;

import org.ejml.data.CD1Matrix64F;

/**
 * <p>
 * Functions for computing the features of complex matrices
 * <p>
 *
 * @author Peter Abeles
 */
public class CMatrixFeatures {
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
    public static boolean isEquals( CD1Matrix64F a, CD1Matrix64F b ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        final int length = a.getDataLength();
        for( int i = 0; i < length; i++ ) {
            if( !(a.data[i] == b.data[i]) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Checks to see if each element in the two matrices are within tolerance of
     * each other: tol &ge; |a<sub>ij</sub> - b<sub>ij</sub>|.
     * <p>
     *
     * <p>
     * NOTE: If any of the elements are not countable then false is returned.<br>
     * NOTE: If a tolerance of zero is passed in this is equivalent to calling
     * {@link #isEquals(org.ejml.data.CD1Matrix64F, org.ejml.data.CD1Matrix64F)}
     * </p>
     *
     * @param a A matrix. Not modified.
     * @param b A matrix. Not modified.
     * @param tol How close to being identical each element needs to be.
     * @return true if equals and false otherwise.
     */
    public static boolean isEquals( CD1Matrix64F a , CD1Matrix64F b , double tol )
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        if( tol == 0.0 )
            return isEquals(a,b);

        final int length = a.getDataLength();

        for( int i = 0; i < length; i++ ) {
            if( !(tol >= Math.abs(a.data[i] - b.data[i])) ) {
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
    public static boolean isIdentical( CD1Matrix64F a, CD1Matrix64F b , double tol ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }
        if( tol < 0 )
            throw new IllegalArgumentException("Tolerance must be greater than or equal to zero.");

        final int length = a.getDataLength();
        for( int i = 0; i < length; i++ ) {
            double valA = a.data[i];
            double valB = b.data[i];

            // if either is negative or positive infinity the result will be positive infinity
            // if either is NaN the result will be NaN
            double diff = Math.abs(valA-valB);

            // diff = NaN == false
            // diff = infinity == false
            if( tol >= diff )
                continue;

            if( Double.isNaN(valA) ) {
                return Double.isNaN(valB);
            } else if( Double.isInfinite(valA) ) {
                return valA == valB;
            } else {
                return false;
            }
        }

        return true;
    }
}
