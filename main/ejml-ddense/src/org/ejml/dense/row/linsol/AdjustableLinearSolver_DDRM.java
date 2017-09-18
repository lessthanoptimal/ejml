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

package org.ejml.dense.row.linsol;

import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * In many situations solutions to linear systems that share many of the same data points are needed.
 * This can happen when solving using the most recent data or when rejecting outliers.  In these situations
 * it is possible to solve these related systems much faster than solving the entire data set again.
 *
 * @see LinearSolverDense
 *
 * @author Peter Abeles
 */
public interface AdjustableLinearSolver_DDRM extends LinearSolverDense<DMatrixRMaj> {


    /**
     * Adds a row to A.  This has the same effect as creating a new A and calling {@link #setA}.
     *
     * @param A_row The row in A.
     * @param rowIndex Where the row appears in A.
     * @return if it succeeded or not.
     */
    boolean addRowToA( double []A_row , int rowIndex );

    /**
     * Removes a row from A.  This has the same effect as creating a new A and calling {@link #setA}.
     *
     * @param index which row is removed from A.
     * @return If it succeeded or not.
     */
    boolean removeRowFromA( int index );
}
