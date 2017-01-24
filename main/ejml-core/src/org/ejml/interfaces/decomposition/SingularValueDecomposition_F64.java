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
 * <p>Implementation of {@link SingularValueDecomposition} for 64-bit floats.
 * </p>
 *
 * @author Peter Abeles
 */
public interface SingularValueDecomposition_F64<T extends Matrix>
        extends SingularValueDecomposition<T> {

    /**
     * Returns the singular values.  This is the diagonal elements of the W matrix in the decomposition.
     * <b>Ordering of singular values is not guaranteed.</b>.
     * 
     * @return Singular values. Note this array can be longer than the number of singular values.
     * Extra elements have no meaning.
     */
    double [] getSingularValues();
}
