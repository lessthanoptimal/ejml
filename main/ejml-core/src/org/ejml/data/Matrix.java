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

import java.io.Serializable;

/**
 * Base interface for all rectangular matrices
 *
 * @author Peter Abeles
 */
public interface Matrix extends Serializable {

    /**
     * Returns the number of rows in this matrix.
     *
     * @return Number of rows.
     */
    int getNumRows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return Number of columns.
     */
    int getNumCols();

    /**
     * Sets all values inside the matrix to zero
     */
    void zero();

    /**
     * Creates an exact copy of the matrix
     */
    <T extends Matrix> T copy();

    /**
     * Creates a new matrix with the same shape as this matrix
     */
    <T extends Matrix> T createLike();

    /**
     * Creates a new matrix of the same type with the specified shape
     */
    <T extends Matrix> T create( int numRows , int numCols );

    /**
     * Sets this matrix to be identical to the 'original' matrix passed in.
     */
    void set( Matrix original );

    /**
     * Prints the matrix to standard out using standard formatting. This is the same as calling print("%e")
     */
    void print();

    /**
     * Prints the matrix to standard out with the specified formatting.
     *
     * @see java.util.Formatter
     * @param format printf style formatting for a float. E.g. "%f"
     */
    void print( String format );

    /**
     * Returns the type of matrix
     */
    MatrixType getType();
}
