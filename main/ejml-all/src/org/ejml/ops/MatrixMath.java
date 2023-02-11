/*
 * Copyright (c) 2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.EjmlParameters;
import org.ejml.MatrixDimensionException;
import org.ejml.UtilEjml;
import org.ejml.concurrency.EjmlConcurrency;
import org.ejml.data.*;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_MT_DDRM;
import org.ejml.dense.row.misc.TransposeAlgs_DDRM;
import org.ejml.dense.row.misc.TransposeAlgs_MT_DDRM;
import org.ejml.dense.row.misc.UnrolledInverseFromMinor_DDRM;
import org.ejml.dense.row.mult.MatrixMatrixMult_DDRM;
import org.ejml.dense.row.mult.MatrixMatrixMult_MT_DDRM;
import org.ejml.dense.row.mult.MatrixVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.CommonOps_MT_DSCC;
import org.ejml.sparse.csc.mult.Workspace_MT_DSCC;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

import static org.ejml.UtilEjml.reshapeOrDeclare;
import static org.ejml.concurrency.EjmlConcurrency.useConcurrent;

/**
 * <p>Performs standard matrix operations but has more automation in selecting which algorithm to run and automatically
 * handles all workspace variables. The goal is to simply the procedural API. The downside is that you
 * have less control over memory management and absolute certainty that you know which implementation
 * is going to be run.</p>
 * <p>
 * For example,
 * {@link org.ejml.dense.row.CommonOps_DDRM} is always single threaded and {@link org.ejml.dense.row.CommonOps_MT_DDRM}
 * is always multi-threaded. This will automatically decide which one it should use. It will also handle
 * all workspace variables using a {@link ThreadLocal} to ensure calls are thread safe.
 * </p>
 *
 * <p>FUTURE: Let people provide overrides to call external implementations, such as native or vectorized.</p>
 *
 * @author Peter Abeles
 */
public class MatrixMath {
    // TODO invert symm
    // TODO scale
    // TODO transpose square
    // TODO SVD and Eigen

    // Thread specific workspace variables
    private final static ThreadLocal<GrowArray<Workspace_MT_DSCC>> workspaceMT =
            ThreadLocal.withInitial(() -> new GrowArray<>(Workspace_MT_DSCC::new));
    private final static ThreadLocal<GrowArray<DGrowArray>> workspaceA =
            ThreadLocal.withInitial(() -> new GrowArray<>(DGrowArray::new));
    private final static ThreadLocal<IGrowArray> workspaceGW = ThreadLocal.withInitial(IGrowArray::new);
    private final static ThreadLocal<DGrowArray> workspaceGX = ThreadLocal.withInitial(DGrowArray::new);

    /**
     * @see CommonOps_DDRM#mult(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     */
    public static <T extends DMatrix1Row> T mult( T a, T b, @Nullable T output ) {
        output = reshapeOrDeclare(output, a, a.numRows, b.numCols);
        UtilEjml.checkSameInstance(a, output);
        UtilEjml.checkSameInstance(b, output);

        if (b.numCols == 1) {
            MatrixVectorMult_DDRM.mult(a, b, output);
        } else if (b.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            if (EjmlConcurrency.isUseConcurrent()) {
                MatrixMatrixMult_MT_DDRM.mult_reorder(a, b, output);
            } else {
                MatrixMatrixMult_DDRM.mult_reorder(a, b, output);
            }
        } else {
            MatrixMatrixMult_DDRM.mult_small(a, b, output);
        }

        return output;
    }

    /**
     * @see CommonOps_DSCC#mult(DMatrixSparseCSC, DMatrixRMaj, DMatrixRMaj)
     */
    public static DMatrixSparseCSC mult( DMatrixSparseCSC A, DMatrixSparseCSC B,
                                         @Nullable DMatrixSparseCSC outputC ) {
        if (useConcurrent(A) || useConcurrent(B)) {
            CommonOps_MT_DSCC.mult(A, B, outputC, workspaceMT.get());
        } else {
            CommonOps_DSCC.mult(A, B, outputC, workspaceGW.get(), workspaceGX.get());
        }

        return outputC;
    }

    /**
     * @see CommonOps_DDRM#multTransB(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     */
    public static <T extends DMatrix1Row> T multTransB( T A, T B, @Nullable T output ) {
        output = reshapeOrDeclare(output, A, A.numRows, B.numRows);
        UtilEjml.checkSameInstance(A, output);
        UtilEjml.checkSameInstance(B, output);

        if (B.numRows == 1) {
            MatrixVectorMult_DDRM.mult(A, B, output);
        } else if (useConcurrent(A) || useConcurrent(B)) {
            MatrixMatrixMult_MT_DDRM.multTransB(A, B, output);
        } else {
            MatrixMatrixMult_DDRM.multTransB(A, B, output);
        }

        return output;
    }

    /**
     * @see org.ejml.dense.row.CommonOps_DDRM#transpose(DMatrixRMaj, DMatrixRMaj)
     */
    public static DMatrixRMaj transpose( DMatrixRMaj A, @Nullable DMatrixRMaj A_tran ) {
        A_tran = reshapeOrDeclare(A_tran, A.numCols, A.numRows);

        if (A.numRows > EjmlParameters.TRANSPOSE_SWITCH &&
                A.numCols > EjmlParameters.TRANSPOSE_SWITCH) {
            if (EjmlConcurrency.isUseConcurrent()) {
                TransposeAlgs_MT_DDRM.block(A, A_tran, EjmlParameters.BLOCK_WIDTH);
            } else {
                TransposeAlgs_DDRM.block(A, A_tran, EjmlParameters.BLOCK_WIDTH);
            }
        } else {
            TransposeAlgs_DDRM.standard(A, A_tran);
        }

        return A_tran;
    }

    /**
     * @see CommonOps_DDRM#det(DMatrixRMaj)
     */
    public static double det( DMatrixRMaj mat ) {
        return CommonOps_DDRM.det(mat);
    }

    /**
     * @see CommonOps_DDRM#solve(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)
     */
    public static boolean solve( DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj x ) {
        x.reshape(a.numCols, b.numCols);

        LinearSolverDense<DMatrixRMaj> solver;

        if (useConcurrent(a)) {
            // this will use a concurrent QR even for square matrices
            solver = LinearSolverFactory_MT_DDRM.leastSquares(a.numRows, a.numCols);
        } else {
            solver = LinearSolverFactory_DDRM.general(a.numRows, a.numCols);
        }
        if (!solver.setA(a))
            return false;

        solver.solve(b, x);
        return true;
    }

    /**
     * @see CommonOps_DDRM#invert(DMatrixRMaj, DMatrixRMaj)
     */
    public static boolean invert( DMatrixRMaj A, DMatrixRMaj result ) {
        result.reshape(A.numRows, A.numCols);

        if (A.numCols <= UnrolledInverseFromMinor_DDRM.MAX) {
            if (A.numCols != A.numRows) {
                throw new MatrixDimensionException("Must be a square matrix.");
            }
            if (result.numCols >= 2) {
                UnrolledInverseFromMinor_DDRM.inv(A, result);
            } else {
                result.set(0, 1.0/A.get(0));
            }
            return true;
        }

        LinearSolverDense<DMatrixRMaj> solver;

        if (useConcurrent(A)) {
            // this will use a concurrent QR even for square matrices
            solver = LinearSolverFactory_MT_DDRM.leastSquares(A.numRows, A.numCols);
        } else {
            solver = LinearSolverFactory_DDRM.general(A.numRows, A.numCols);
        }
        if (solver.modifiesA())
            A = A.copy();

        if (!solver.setA(A))
            return false;

        solver.invert(result);
        return true;
    }
}
