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

package org.ejml.interfaces.decomposition;

import org.ejml.data.Matrix;


/**
 * <p>
 * An interface for performing matrix decompositions on a {@link org.ejml.data.DenseMatrix64F}.
 * </p>
 *
 * <p>
 * A matrix decomposition is an algorithm which decomposes the input matrix into a set of equivalent
 * matrices that store the same information as the original.  Decompositions are useful
 * in that they allow specialized efficient algorithms to be run on generic input
 * matrices.
 * </p>
 *
 * <p>
 * By default most decompositions will modify the input matrix.  This is done to save
 * memory and simply code by reducing the number of cases which need to be tested.
 * </p>
 *
 * @author Peter Abeles
 */
public interface DecompositionInterface <T extends Matrix> {

    /**
     * Computes the decomposition of the input matrix.  Depending on the implementation
     * the input matrix might be stored internally or modified.  If it is modified then
     * the function {@link #inputModified()} will return true and the matrix should not be
     * modified until the decomposition is no longer needed.
     *
     * @param orig The matrix which is being decomposed.  Modification is implementation dependent.
     * @return Returns if it was able to decompose the matrix.
     */
    public boolean decompose( T orig );

    /**
     * Is the input matrix to {@link #decompose(org.ejml.data.Matrix)} is modified during
     * the decomposition process.
     *
     * @return true if the input matrix to decompose() is modified.
     */
    public boolean inputModified();
}
