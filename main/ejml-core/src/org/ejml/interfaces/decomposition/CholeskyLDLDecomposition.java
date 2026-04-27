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
package org.ejml.interfaces.decomposition;

import org.ejml.data.Matrix;
import org.jetbrains.annotations.Nullable;

/// Cholesky LDL<sup>T</sup> decomposition.
///
/// A Cholesky LDL decomposition decomposes positive-definite symmetric matrices into:
///
/// L\*D\*L<sup>T</sup>=A
///
/// where L is a lower triangular matrix and D is a diagonal matrix. The main advantage of LDL versus LL or RR Cholesky is that
/// it avoid a square root operation.
public interface CholeskyLDLDecomposition<MatrixType extends Matrix>
        extends DecompositionInterface<MatrixType> {

    /// Decomposition's lower triangle matrix.
    ///
    /// @param L (Optional) Storage for returned matrix
    /// @return A lower triangular matrix.
    MatrixType getL( @Nullable MatrixType L );

    /// Decomposition's diagonal matrix.
    ///
    /// @param D (Optional) Storage for returned matrix
    /// @return D Square diagonal matrix
    MatrixType getD( @Nullable MatrixType D );
}
