/*
 * Copyright (c) 2009-2019, Peter Abeles. All Rights Reserved.
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

package org.ejml.simple;

import org.ejml.data.Matrix;
import org.ejml.data.MatrixSparse;

import java.io.Serializable;

/**
 * Extension to {@link SimpleOperations} for sparse matrices
 *
 * @author Peter Abeles
 */
public interface SimpleSparseOperations<S extends MatrixSparse, D extends Matrix>
        extends SimpleOperations<S> , Serializable {

    void extractDiag( S input , D output );

    void multTransA(S A , D B , D output );

    void mult(S A , D B , D output );
}
