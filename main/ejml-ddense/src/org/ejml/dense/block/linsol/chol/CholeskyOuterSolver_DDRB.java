/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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
package org.ejml.dense.block.linsol.chol;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.MatrixOps_DDRB;
import org.ejml.dense.block.TriangularSolver_DDRB;
import org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_DDRB;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

//CONCURRENT_INLINE import org.ejml.dense.block.decomposition.chol.CholeskyOuterForm_MT_DDRB;
//CONCURRENT_INLINE import org.ejml.dense.block.TriangularSolver_MT_DDRB;
//CONCURRENT_INLINE import org.ejml.concurrency.EjmlConcurrency;

//CONCURRENT_MACRO MatrixMult_DDRB MatrixMult_MT_DDRB
//CONCURRENT_MACRO TriangularSolver_DDRB TriangularSolver_MT_DDRB
//CONCURRENT_MACRO CholeskyOuterForm_DDRB CholeskyOuterForm_MT_DDRB

/// Linear solver that uses a block Cholesky decomposition. Uses the standard Cholesky solving strategy:
///
/// A=L\*L<sup>T</sup>
/// A\*x=b
/// L\*L<sup>T</sup>\*x = b
/// L\*y = b
/// L<sup>T</sup>\*x = y
/// x = L<sup>-T</sup>y
///
/// It is also possible to use the upper triangular Cholesky decomposition.
///
/// @author Peter Abeles
@SuppressWarnings("NullAway.Init")
public class CholeskyOuterSolver_DDRB implements LinearSolverDense<DMatrixRBlock> {

    // Cholesky decomposition
    private final CholeskyOuterForm_DDRB decomposer;

    // size of a block take from input matrix
    private int blockLength;

    // temporary data structure used in some calculation.
    private final GrowArray<DGrowArray> workspace = new GrowArray<>(DGrowArray::new);

    /// @param lower Will it use a lower up upper triangle decomposition internally
    public CholeskyOuterSolver_DDRB( boolean lower ) {
        decomposer = new CholeskyOuterForm_DDRB(lower);
    }

    public CholeskyOuterSolver_DDRB() {
        this(true);
    }

    /// Decomposes and overwrites the input matrix.
    ///
    /// @param A Semi-Positive Definite (SPD) system matrix. Modified. Reference saved.
    /// @return If the matrix can be decomposed. Will always return false of not SPD.
    @Override
    public boolean setA( DMatrixRBlock A ) {
        // Extract a lower triangular solution
        if (!decomposer.decompose(A))
            return false;

        blockLength = A.blockLength;

        return true;
    }

    @Override
    public /**/double quality() {
        return SpecializedOps_DDRM.qualityTriangular(decomposer.getT(null));
    }

    /// If X == null then the solution is written into B. Otherwise the solution is copied
    /// from B into X.
    @Override
    public void solve( DMatrixRBlock B, @Nullable DMatrixRBlock X ) {
        if (B.blockLength != blockLength)
            throw new IllegalArgumentException("Unexpected blockLength in B.");

        DSubmatrixD1 T = new DSubmatrixD1(decomposer.getT(null));

        if (X == null) {
            X = B.create(T.col1, B.numCols);
        } else {
            X.reshape(T.col1, B.numCols, blockLength, false);
        }

        if (decomposer.isLower()) {
            // L*L^T*X = B

            // Solve for Y:  L*Y = B
            TriangularSolver_DDRB.lsolve(blockLength, false, T, new DSubmatrixD1(B), false);

            // L^T * X = Y
            TriangularSolver_DDRB.lsolve(blockLength, false, T, new DSubmatrixD1(B), true);
        } else {
            //  R^T*R*X = B

            // Solve for Y:  R^T*Y = B
            TriangularSolver_DDRB.lsolve(blockLength, true, T, new DSubmatrixD1(B), true);

            // R * X = Y
            TriangularSolver_DDRB.lsolve(blockLength, true, T, new DSubmatrixD1(B), false);
        }

        if (X != null) {
            // copy the solution from B into X
            MatrixOps_DDRB.extractAligned(B, X);
        }
    }

    @Override
    public void invert( DMatrixRBlock A_inv ) {
        DMatrixRBlock T = decomposer.getT(null);
        if (A_inv.numRows != T.numRows || A_inv.numCols != T.numCols)
            throw new IllegalArgumentException("Unexpected number or rows and/or columns");

        // zero the upper triangular portion of A_inv
        MatrixOps_DDRB.zeroTriangle(decomposer.isLower(), A_inv);

        DSubmatrixD1 subT = new DSubmatrixD1(T);
        DSubmatrixD1 subB = new DSubmatrixD1(A_inv);

        if (decomposer.isLower()) {
            // A = L*L^T, so A^-1 = L^-T * L^-1
            // B = inv(L)
            TriangularSolver_DDRB.invert(blockLength, false, subT, subB, workspace);

            // B = L^-T * B
            // todo could speed up by taking advantage of B being lower triangular
            // todo take advantage of symmetry
            TriangularSolver_DDRB.lsolveLow(blockLength, subT, subB, true);
        } else {
            // A = R^T*R, so A^-1 = R^-1 * R^-T
            // B = inv(R)
            TriangularSolver_DDRB.invert(blockLength, true, subT, subB, workspace);

            // B = R^-1 * B = R^-1 * R^-T
            TriangularSolver_DDRB.rsolveUpp(blockLength, subT, subB, true);
        }
    }

    @Override
    public boolean modifiesA() {
        return decomposer.inputModified();
    }

    @Override
    public boolean modifiesB() {
        return true;
    }

    @Override
    public CholeskyDecomposition_F64<DMatrixRBlock> getDecomposition() {
        return decomposer;
    }
}
