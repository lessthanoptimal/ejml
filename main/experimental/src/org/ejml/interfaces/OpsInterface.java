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

package org.ejml.interfaces;

import org.ejml.data.Matrix;

/**
 * Interface for standard linear algebra operations.
 *
 * @author Peter Abeles
 */
public interface OpsInterface<M extends Matrix> {

    void svd( M A ); // TODO how to set parameters and return output?

    void eig( M A ); // TODO how to set parameters and return output?

    boolean solve( MatrixType typeA, M A , M B , M X );

    double det( M A );

    void mult( M A, M B, M C );
    void mult( double alpha , M A, double beta, M B, M C );

    void multTransA( M A, M B, M C );
    void multTransA( double alpha , M A, double beta, M B, M C );

    void multTransAB( M A, M B, M C );
    void multTransAB( double alpha , M A, double beta, M B, M C );

    void multTransB( M A, M B, M C );
    void multTransB( double alpha , M A, double beta, M B, M C );

    void multAdd( M A, M B, M C );
    void multAdd( double alpha , M A, double beta, M B, M C );

    void multAddTransA( M A, M B, M C );
    void multAddTransA( double alpha , M A, double beta, M B, M C );

    void multAddTransAB( M A, M B, M C );
    void multAddTransAB( double alpha , M A, double beta, M B, M C );

    void multAddTransB( M A, M B, M C );
    void multAddTransB( double alpha , M A, double beta, M B, M C );

    void transpose( M A , M B );
    void transpose( M A );
}
