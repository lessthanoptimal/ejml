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
import org.ejml.dense.row.CommonOps_MT_DDRM;
import org.ejml.dense.row.CommonOps_ZDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.decomposition.TriangularSolver_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_MT_DDRM;
import org.ejml.dense.row.misc.TransposeAlgs_DDRM;
import org.ejml.dense.row.misc.TransposeAlgs_MT_DDRM;
import org.ejml.dense.row.misc.UnrolledCholesky_DDRM;
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
 * <p>
 * Generalized Common Operations for all matrix types. Designed to simplify using the procedural interface
 * by having a central location for everything and managing workspace variables. For high performance users
 * there is a slight downside in the loss of control and ability to tightly control memory.
 * </p>
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
public class CommonOps {
    // TODO SVD and Eigen

    // Thread specific workspace variables
    private final static ThreadLocal<GrowArray<Workspace_MT_DSCC>> workspaceMT =
            ThreadLocal.withInitial(() -> new GrowArray<>(Workspace_MT_DSCC::new));
    private final static ThreadLocal<GrowArray<DGrowArray>> workspaceA =
            ThreadLocal.withInitial(() -> new GrowArray<>(DGrowArray::new));
    private final static ThreadLocal<IGrowArray> workspaceGW = ThreadLocal.withInitial(IGrowArray::new);
    private final static ThreadLocal<DGrowArray> workspaceGX = ThreadLocal.withInitial(DGrowArray::new);

    /**
     * @see CommonOps_DDRM#add(DMatrixD1, DMatrixD1, DMatrixD1)
     */
    public static <T extends DMatrixD1> T add( final T A, final T B, @Nullable T output ) {
        return CommonOps_DDRM.add(A, B, output);
    }

    /**
     * @see CommonOps_ZDRM#add(ZMatrixD1, ZMatrixD1, ZMatrixD1)
     */
    public static <T extends ZMatrixD1> T add( final T A, final T B, @Nullable T output ) {
        return CommonOps_ZDRM.add(A, B, output);
    }

    /**
     * @see CommonOps_DSCC#add(double, DMatrixSparseCSC, double, DMatrixSparseCSC, DMatrixSparseCSC, IGrowArray, DGrowArray)
     */
    public static DMatrixSparseCSC add( double alpha, DMatrixSparseCSC A, double beta, DMatrixSparseCSC B,
                                        @Nullable DMatrixSparseCSC output ) {
        if (EjmlConcurrency.useConcurrent(A)) {
            return CommonOps_MT_DSCC.add(alpha, A, beta, B, output, workspaceMT.get());
        } else {
            return CommonOps_DSCC.add(alpha, A, beta, B, output, workspaceGW.get(), workspaceGX.get());
        }
    }

    /**
     * @see CommonOps_DDRM#scale(double, DMatrixD1, DMatrixD1)
     */
    public static <T extends DMatrixD1> T scale( double alpha, T A, T B ) {
        return CommonOps_DDRM.scale(alpha, A, B);
    }

    /**
     * @see CommonOps_ZDRM#scale(double, double, ZMatrixD1, ZMatrixD1)
     */
    public static <T extends ZMatrixD1> T scale( double alphaReal, double alphaImag, T A, T B ) {
        return CommonOps_ZDRM.scale(alphaReal, alphaImag, A, B);
    }

    /**
     * @see CommonOps_DSCC#scale
     */
    public static void scale( double scalar, DMatrixSparseCSC A, DMatrixSparseCSC outputB ) {
        CommonOps_DSCC.scale(scalar, A, outputB);
    }

    /**
     * @see CommonOps_DDRM#divide(double, DMatrixD1, DMatrixD1)
     */
    public static <T extends DMatrixD1> T divide( double alpha, T A, T B ) {
        return CommonOps_DDRM.divide(alpha, A, B);
    }

    /**
     * @see CommonOps_DSCC#divide
     */
    public static void divide( double scalar, DMatrixSparseCSC A, DMatrixSparseCSC outputB ) {
        CommonOps_DSCC.divide(scalar, A, outputB);
    }

    /**
     * @see CommonOps_DDRM#mult(DMatrix1Row, DMatrix1Row, DMatrix1Row)
     */
    public static <T extends DMatrix1Row> T mult( T A, T B, @Nullable T output ) {
        output = reshapeOrDeclare(output, A, A.numRows, B.numCols);
        UtilEjml.checkSameInstance(A, output);
        UtilEjml.checkSameInstance(B, output);

        if (B.numCols == 1) {
            MatrixVectorMult_DDRM.mult(A, B, output);
        } else if (B.numCols >= EjmlParameters.MULT_COLUMN_SWITCH) {
            if (EjmlConcurrency.isUseConcurrent()) {
                MatrixMatrixMult_MT_DDRM.mult_reorder(A, B, output);
            } else {
                MatrixMatrixMult_DDRM.mult_reorder(A, B, output);
            }
        } else {
            MatrixMatrixMult_DDRM.mult_small(A, B, output);
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
     * @see org.ejml.dense.row.CommonOps_DDRM#transpose(DMatrixRMaj)
     */
    public static void transpose( DMatrixRMaj A ) {
        if (useConcurrent(A)) {
            CommonOps_MT_DDRM.transpose(A);
        } else {
            CommonOps_DDRM.transpose(A);
        }
    }

    /**
     * @see CommonOps_DDRM#det(DMatrixRMaj)
     */
    public static double det( DMatrixRMaj mat ) {
        return CommonOps_DDRM.det(mat);
    }

    /**
     * @see CommonOps_ZDRM#det(ZMatrixRMaj)
     */
    public static Complex_F64 det( ZMatrixRMaj mat ) {
        return CommonOps_ZDRM.det(mat);
    }

    /**
     * @see CommonOps_DSCC#det(DMatrixSparseCSC)
     */
    public static double det( DMatrixSparseCSC mat ) {
        return CommonOps_DSCC.det(mat);
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
    public static boolean invert( DMatrixRMaj A, DMatrixRMaj output ) {
        output.reshape(A.numRows, A.numCols);

        if (A.numCols <= UnrolledInverseFromMinor_DDRM.MAX) {
            if (A.numCols != A.numRows) {
                throw new MatrixDimensionException("Must be a square matrix.");
            }
            if (output.numCols >= 2) {
                UnrolledInverseFromMinor_DDRM.inv(A, output);
            } else {
                output.set(0, 1.0/A.get(0));
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

        solver.invert(output);
        return true;
    }

    /**
     * @see CommonOps_DDRM#invertSPD(DMatrixRMaj, DMatrixRMaj)
     */
    public static boolean invertSPD( DMatrixRMaj A, DMatrixRMaj output ) {
        if (A.numRows != A.numCols)
            throw new IllegalArgumentException("Must be a square matrix");
        output.reshape(A.numRows, A.numCols);

        if (A.numCols <= UnrolledCholesky_DDRM.MAX) {
            // L*L' = A
            if (!UnrolledCholesky_DDRM.lower(A, output))
                return false;
            // L = inv(L)
            TriangularSolver_DDRM.invertLower(output.data, output.numCols);
            // inv(A) = inv(L')*inv(L)
            SpecializedOps_DDRM.multLowerTranA(output);
            return true;
        }

        LinearSolverDense<DMatrixRMaj> solver;

        if (useConcurrent(A)) {
            solver = LinearSolverFactory_MT_DDRM.chol(A.numRows);
        } else {
            solver = LinearSolverFactory_DDRM.chol(A.numRows);
        }
        if (solver.modifiesA())
            A = A.copy();

        if (!solver.setA(A))
            return false;

        solver.invert(output);
        return true;
    }
}
