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

import org.ejml.data.Complex_F32;
import org.ejml.data.Matrix;

/**
 * Implementation of {@link LUDecomposition} for 64-bit numbers
 *
 * @author Peter Abeles
 */
public interface LUDecomposition_F32<T extends Matrix> extends LUDecomposition<T> {
    /**
     * Computes the matrix's determinant using the LU decomposition.
     *
     * @return The determinant.
     */
    Complex_F32 computeDeterminant();
}
