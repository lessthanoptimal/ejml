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

package org.ejml.factory;

import org.ejml.alg.dense.decompose.lu.LUDecompositionAlt_CD64;
import org.ejml.alg.dense.linsol.lu.LinearSolverLu_CD64;
import org.ejml.alg.dense.linsol.qr.LinearSolverQrHouseCol_CD64;
import org.ejml.data.CDenseMatrix64F;
import org.ejml.interfaces.linsol.LinearSolver;

/**
 * Factory for creating linear solvers of complex matrices
 *
 * @author Peter Abeles
 */
public class CLinearSolverFactory {
    /**
     * Creates a solver for linear systems.  The A matrix will have dimensions (m,m).
     *
     * @return A new linear solver.
     */
    public static LinearSolver<CDenseMatrix64F> linear( int matrixSize ) {
        return new LinearSolverLu_CD64(new LUDecompositionAlt_CD64());
    }

    /**
     * Creates a good general purpose solver for over determined systems and returns the optimal least-squares
     * solution.  The A matrix will have dimensions (m,n) where m &ge; n.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return A new least-squares solver for over determined systems.
     */
    public static LinearSolver<CDenseMatrix64F> leastSquares( int numRows , int numCols ) {
        return new LinearSolverQrHouseCol_CD64();
    }
}
