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
 * A decomposition is a way in which a matrix is broken up into multiple matrices.  This can
 * allow faster solving and aid in other problems.  All decomposition algorthms will not declare
 * new data if the matrix that is decomposed is less than the expected size.
 *
 * @author Peter Abeles
 */
public interface DecompositionInterface {

    /**
     * Computes and stores internally the decomposition of the provided matrix.
     *
     * @param orig The matrix which is being decomposed.  Not modified.
     * @return Returns if it was able to decompose the matrix.
     */
    public boolean decompose( DenseMatrix64F orig );

    /**
     * Declares internal data structures such that new memory will not need to declared
     * if a matrix is decomposed that is less than or equal to the specified shape.
     *
     * @param numRows Matrices are expected to have less than or equal to this number of rows.
     * @param numCols Matrices are expected to have less than or equal to this number of columns.
     */
    public void setExpectedMaxSize( int numRows , int numCols );
}
