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

package org.ejml.interfaces.decomposition;

import org.ejml.data.Matrix;


/**
 * <p>Implementation of {@link TridiagonalSimilarDecomposition} for 64-bit floats</p>
 *
 * @author Peter Abeles
 */
public interface TridiagonalSimilarDecomposition_F64<MatrixType extends Matrix>
        extends TridiagonalSimilarDecomposition<MatrixType> {

    /**
     * Extracts the diagonal and off diagonal elements of the decomposed tridiagonal matrix.
     * Since it is symmetric only one off diagonal array is returned.
     *
     * @param diag Diagonal elements. Modified.
     * @param off off diagonal elements. Modified.
     */
    void getDiagonal( double []diag, double []off );
}
