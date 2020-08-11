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

package org.ejml.dense.row.factory;

import org.ejml.data.CMatrixRMaj;
import org.ejml.dense.row.decompose.chol.CholeskyDecompositionInner_CDRM;
import org.ejml.dense.row.decompose.lu.LUDecompositionAlt_CDRM;
import org.ejml.dense.row.linsol.chol.LinearSolverChol_CDRM;
import org.ejml.dense.row.linsol.lu.LinearSolverLu_CDRM;
import org.ejml.dense.row.linsol.qr.LinearSolverQrHouseCol_CDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * Factory for creating linear solvers of complex matrices
 *
 * @author Peter Abeles
 */
public class LinearSolverFactory_CDRM {

    /**
     * Creates a linear solver which uses LU decomposition internally
     *
     * @param matrixSize Approximate of rows and columns
     * @return Linear solver
     */
    public static LinearSolverDense<CMatrixRMaj> lu(int matrixSize ) {
        return new LinearSolverLu_CDRM(new LUDecompositionAlt_CDRM());
    }

    /**
     * Creates a linear solver which uses Cholesky decomposition internally
     *
     * @param matrixSize Approximate of rows and columns
     * @return Linear solver
     */
    public static LinearSolverDense<CMatrixRMaj> chol(int matrixSize ) {
        return new LinearSolverChol_CDRM(new CholeskyDecompositionInner_CDRM());
    }

    /**
     * Creates a linear solver which uses QR decomposition internally
     *
     * @param numRows Approximate of rows
     * @param numCols Approximate of columns
     * @return Linear solver
     */
    public static LinearSolverDense<CMatrixRMaj> qr(int numRows , int numCols ) {
        return new LinearSolverQrHouseCol_CDRM();
    }
}
