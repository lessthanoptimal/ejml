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

package org.ejml.dense.row.factory;

import org.ejml.EjmlParameters;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.block.linsol.chol.CholeskyOuterSolver_MT_DDRB;
import org.ejml.dense.block.linsol.qr.QrHouseHolderSolver_MT_DDRB;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_DDRB;
import org.ejml.dense.row.linsol.qr.LinearSolverQrBlock64_DDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_MT_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * A factory for generating solvers for systems of the form A*x=b, where A and B are known and x is unknown.
 *
 * @author Peter Abeles
 */
public class LinearSolverFactory_MT_DDRM {

    /**
     * Creates a linear solver using Cholesky decomposition
     */
    public static LinearSolverDense<DMatrixRMaj> chol( int numRows ) {
        return symmPosDef(numRows);
    }

    /**
     * Creates a linear solver using QR decomposition
     */
    public static LinearSolverDense<DMatrixRMaj> qr( int numRows, int numCols ) {
        return leastSquares(numRows, numCols);
    }

    /**
     * Creates a good general purpose solver for over determined systems and returns the optimal least-squares
     * solution. The A matrix will have dimensions (m,n) where m &ge; n.
     *
     * @param numRows The number of rows that the decomposition is optimized for.
     * @param numCols The number of columns that the decomposition is optimized for.
     * @return A new least-squares solver for over determined systems.
     */
    public static LinearSolverDense<DMatrixRMaj> leastSquares( int numRows, int numCols ) {
        if (numCols < EjmlParameters.SWITCH_BLOCK64_QR) {
            return new LinearSolverQrHouseCol_MT_DDRM();
        } else {
            if (EjmlParameters.MEMORY == EjmlParameters.MemoryUsage.FASTER)
                return new LinearSolverQrBlock64_DDRM(new QrHouseHolderSolver_MT_DDRB());
            else
                return new LinearSolverQrHouseCol_MT_DDRM();
        }
    }

    /**
     * Creates a solver for symmetric positive definite matrices.
     *
     * @return A new solver for symmetric positive definite matrices.
     */
    public static LinearSolverDense<DMatrixRMaj> symmPosDef( int matrixWidth ) {
        return new LinearSolverChol_DDRB(new CholeskyOuterSolver_MT_DDRB());
    }
}
