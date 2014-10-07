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
 * Interface for all complex 64 bit floating point rectangular matrices.
 *
 * @author Peter Abeles
 */
public interface ComplexMatrix64F extends Matrix {

    /**
     * Returns the complex value of the matrix's element
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param output Storage for the complex number
     */
    public void get( int row , int col , Complex64F output );

    /**
     * Set's the complex value of the matrix's element
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param real The real component
     * @param imaginary The imaginary component
     */
    public void set( int row , int col , double real , double imaginary );

    /**
     * Returns the real component of the matrix's element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public double getReal(int row, int col);


    /**
     * Sets the real component of the matrix's element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public void setReal(int row, int col, double val);

    /**
     * Returns the imaginary component of the matrix's element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @return The specified element's value.
     */
    public double getImaginary(int row, int col);


    /**
     * Sets the imaginary component of the matrix's element.
     *
     * @param row Matrix element's row index..
     * @param col Matrix element's column index.
     * @param val  The element's new value.
     */
    public void setImaginary(int row, int col, double val);

    /**
     * Returns the number of elements in the internal data array
     *
     * @return Number of elements in the data array.
     */
    public int getDataLength();

}
