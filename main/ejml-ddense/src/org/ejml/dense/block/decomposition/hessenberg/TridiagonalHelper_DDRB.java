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

package org.ejml.dense.block.decomposition.hessenberg;

import org.ejml.data.DSubmatrixD1;
import org.ejml.dense.block.Householder_DDRB;
import org.ejml.dense.block.VectorOps_DDRB;

import static org.ejml.dense.block.Householder_DDRB.computeHouseholderRow;

/// Row-form panel operations for the block tridiagonal decomposition. Each routine reduces or
/// updates the upper row block of a symmetric matrix using Householder reflectors, building the
/// auxiliary V block used by the symmetric rank-2 trailing update
/// A = A + U\*V<sup>T</sup> + V\*U<sup>T</sup>.
///
/// Shared parameters, not repeated on each method:
///
/// - blockLength — inner block size.
/// - A — the row block being reduced; on output also holds the computed reflectors u (each with
///   an implicit leading 1). Modified.
/// - V — stores the computed v / y row vectors. Modified.
/// - gammas — per-reflector Householder scalars (γ).
///
/// Each reflector's first element is implicitly 1 on the super-diagonal (at (i, i+1)) and not stored;
/// routines that need it set it temporarily (save/restore) — to be removed when the zeros/ones
/// convention is standardized.
public class TridiagonalHelper_DDRB {

    /// Tridiagonal decomposition of the upper row block. For each row of 'A':
    ///
    /// ```
    /// u      = householder reflector of the row
    /// y      = A*u
    /// v      = y - (1/2)(y^T*u)*u
    /// a(i+1) = a(i) - u*γ*v^T - v*u^T
    /// ```
    public static void tridiagUpperRow(
            final int blockLength, final DSubmatrixD1 A, final double[] gammas, final DSubmatrixD1 V ) {
        int blockHeight = Math.min(blockLength, A.row1 - A.row0);
        if (blockHeight <= 1)
            return;
        int width = A.col1 - A.col0;
        int num = Math.min(width - 1, blockHeight);
        int applyIndex = Math.min(width, blockHeight);

        // step through rows in the block
        for (int i = 0; i < num; i++) {
            // compute the new reflector and save it in a row in 'A'
            computeHouseholderRow(blockLength, A, gammas, i);
            double gamma = gammas[A.row0 + i];

            // compute y
            computeYRow(blockLength, A, V, i, gamma);

            // compute v from y
            computeVRow(blockLength, A, V, i, gamma);

            // Apply the reflectors to the next row in 'A' only
            if (i + 1 < applyIndex) {
                applyReflectorsToRow(blockLength, A, V, i + 1);
            }
        }
    }

    /// Computes the V row block vector for an already-decomposed row block:
    ///
    /// ```
    /// y    = A*u
    /// v(i) = y - (1/2)γ(y^T*u)*u
    /// ```
    public static void computeVBlock(
            final int blockLength, final DSubmatrixD1 A, final double[] gammas, final DSubmatrixD1 V ) {
        int blockHeight = Math.min(blockLength, A.row1 - A.row0);
        if (blockHeight <= 1)
            return;
        int width = A.col1 - A.col0;
        int num = Math.min(width - 1, blockHeight);

        for (int i = 0; i < num; i++) {
            double gamma = gammas[A.row0 + i];

            // compute y
            computeYRow(blockLength, A, V, i, gamma);

            // compute v from y
            computeVRow(blockLength, A, V, i, gamma);
        }
    }

    /// Applies all previously-computed reflectors to row 'row' of A as a symmetric rank-2 update:
    ///
    /// ```
    /// A = A + u*v^T + v*u^T   (only along row 'row')
    /// ```
    public static void applyReflectorsToRow(
            final int blockLength, final DSubmatrixD1 A, final DSubmatrixD1 V, int row ) {
        int height = Math.min(blockLength, A.row1 - A.row0);

        double[] dataA = A.original.data;
        double[] dataV = V.original.data;

        int indexU, indexV;

        // for each previously computed reflector
        for (int i = 0; i < row; i++) {
            int width = Math.min(blockLength, A.col1 - A.col0);

            indexU = A.original.numCols*A.row0 + height*A.col0 + i*width + row;
            indexV = V.original.numCols*V.row0 + height*V.col0 + i*width + row;

            double u_row = (i + 1 == row) ? 1.0 : dataA[indexU];
            double v_row = dataV[indexV];

            // take in account the leading one
            double before = A.get(i, i + 1);
            A.set(i, i + 1, 1);

            // grab only the relevant row from A = A + u*v^T + v*u^T
            VectorOps_DDRB.add_row(blockLength, A, row, 1, V, i, u_row, A, row, row, A.col1 - A.col0);
            VectorOps_DDRB.add_row(blockLength, A, row, 1, A, i, v_row, A, row, row, A.col1 - A.col0);

            A.set(i, i + 1, before);
        }
    }

    /// Computes row 'row' of 'y' for its reflector and stores it in V:
    ///
    /// ```
    /// y = -γ(A + U*V^T + V*U^T)u
    /// ```
    public static void computeYRow(
            final int blockLength, final DSubmatrixD1 A, final DSubmatrixD1 V, int row, double gamma ) {
        // Elements in 'y' before 'row' are known to be zero and the element at 'row'
        // is not used. Thus only elements after row and after are computed.
        // y = A*u
        multSymmRow(blockLength, A, V, row);

        for (int i = 0; i < row; i++) {
            // y = y + u_i*v_i^t*u + v_i*u_i^t*u

            // v_i^t*u
            double dot_v_u = Householder_DDRB.innerProdRow(blockLength, A, row, V, i, 1);

            // u_i^t*u
            double dot_u_u = Householder_DDRB.innerProdRow(blockLength, A, row, A, i, 1);

            // y = y + u_i*(v_i^t*u)
            // the ones in these 'u' are skipped over since the next submatrix of A
            // is only updated
            VectorOps_DDRB.add_row(blockLength, V, row, 1, A, i, dot_v_u, V, row, row + 1, A.col1 - A.col0);

            // y = y + v_i*(u_i^t*u)
            // the 1 in U is taken account above
            VectorOps_DDRB.add_row(blockLength, V, row, 1, V, i, dot_u_u, V, row, row + 1, A.col1 - A.col0);
        }

        // y = -gamma*y
        VectorOps_DDRB.scale_row(blockLength, V, row, -gamma, V, row, row + 1, V.col1 - V.col0);
    }

    /// Multiplies the symmetric 'A' by reflector 'u' from row 'row', storing the result in row 'row' of V.
    ///
    /// ```
    /// y = A*u
    /// ```
    public static void multSymmRow( final int blockLength, final DSubmatrixD1 A, final DSubmatrixD1 V, int row ) {
        int heightMatA = A.row1 - A.row0;

        for (int i = row + 1; i < heightMatA; i++) {

            double val = Householder_DDRB.innerProdRow_symm(blockLength, A, row, A, i, 1);

            V.set(row, i, val);
        }
    }

    /// Finishes one row of 'v', overwriting the intermediate 'y' that [#computeYRow] left in V:
    ///
    /// ```
    /// v = y - (1/2)γ(y^T*u)*u
    /// ```
    public static void computeVRow(
            final int blockLength, final DSubmatrixD1 A, final DSubmatrixD1 V, int row, double gamma ) {
        // val=(y^T*u)
        double val = Householder_DDRB.innerProdRow(blockLength, A, row, V, row, 1);

        // take in account the one
        double before = A.get(row, row + 1);
        A.set(row, row + 1, 1);

        // v = y - (1/2)gamma*val * u
        VectorOps_DDRB.add_row(blockLength, V, row, 1, A, row, -0.5*gamma*val, V, row, row + 1, A.col1 - A.col0);

        A.set(row, row + 1, before);
    }
}
