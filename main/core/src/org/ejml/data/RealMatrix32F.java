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

package org.ejml.data;

/**
 * Interface for all 32 bit floating point rectangular matrices.
 *
 * @author Peter Abeles
 */
public interface RealMatrix32F extends Matrix {

    /**
     * Returns the value of value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public float get(int row, int col);

    /**
     * Same as {@link #get} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public float unsafe_get(int row, int col);

    /**
     * Sets the value of the specified matrix element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val The element's new value.
     */
    public void set(int row, int col, float val);

    /**
     * Same as {@link #set} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val The element's new value.
     */
    public void unsafe_set(int row, int col, float val);

    /**
     * Returns the number of elements in this matrix, which is the number of rows
     * times the number of columns.
     *
     * @return Number of elements in this matrix.
     */
    public int getNumElements();
}
