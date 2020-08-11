/*
 * Copyright (c) 2009-2020, Peter Abeles. All Rights Reserved.
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

import java.util.Iterator;

/**
 * High level interface for sparse matrices float types.
 *
 * @author Peter Abeles
 */
public interface FMatrixSparse extends FMatrix, MatrixSparse {

    /**
     * Returns the value of value of the specified matrix element.
     *
     * @param row           Matrix element's row index..
     * @param col           Matrix element's column index.
     * @param fallBackValue Value to return, if the matrix element is not assigned
     * @return The specified element's value.
     */
    float get(int row, int col, float fallBackValue);


    /**
     * Same as {@link #get} but does not perform bounds check on input parameters.  This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row           Matrix element's row index..
     * @param col           Matrix element's column index.
     * @param fallBackValue Value to return, if the matrix element is not assigned
     * @return The specified element's value or the fallBackValue, if the element is not assigned.
     */
    float unsafe_get(int row, int col, float fallBackValue);

    /**
     * Creates an iterator which will go through each non-zero value in the sparse matrix. Order is not defined
     * and is implementation specific
     *
     * @return Iterator
     */
    Iterator<CoordinateRealValue> createCoordinateIterator();

    /**
     * Value of an element in a sparse matrix
     */
    class CoordinateRealValue {
        /** The coordinate */
        public int row,col;
        /** The value of the coordinate */
        public float value;
    }
}
