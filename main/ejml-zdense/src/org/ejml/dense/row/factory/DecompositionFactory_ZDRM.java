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

package org.ejml.dense.row.factory;

import org.ejml.data.ZMatrixRMaj;
import org.ejml.dense.row.decompose.chol.CholeskyDecompositionInner_ZDRM;
import org.ejml.dense.row.decompose.lu.LUDecompositionAlt_ZDRM;
import org.ejml.dense.row.decompose.qr.QRDecompositionHouseholderColumn_ZDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.interfaces.decomposition.QRDecomposition;

/**
 * <p>
 * Contains operations related to creating and evaluating the quality of common matrix decompositions.  Except
 * in specialized situations, matrix decompositions should be instantiated from this factory instead of being
 * directly constructed.  Low level implementations are more prone to changes and new algorithms will be
 * automatically placed here.
 * </p>
 *
 * @author Peter Abeles
 */
public class DecompositionFactory_ZDRM {
    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.LUDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return LUDecomposition
     */
    public static LUDecomposition_F64<ZMatrixRMaj> lu(int numRows , int numCols ) {
        return new LUDecompositionAlt_ZDRM();
    }

    /**
     * <p>
     * Returns a {@link org.ejml.interfaces.decomposition.QRDecomposition} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param numRows Number of rows the returned decomposition is optimized for.
     * @param numCols Number of columns that the returned decomposition is optimized for.
     * @return QRDecomposition
     */
    public static QRDecomposition<ZMatrixRMaj> qr(int numRows , int numCols ) {
        return new QRDecompositionHouseholderColumn_ZDRM();
    }

    /**
     * <p>
     * Returns a {@link CholeskyDecomposition_F64} that has been optimized for the specified matrix size.
     * </p>
     *
     * @param size Number of rows and columns it should be optimized for
     * @param lower if true then it will be a lower cholesky.  false for upper.  Try lower.
     * @return QRDecomposition
     */
    public static CholeskyDecomposition_F64<ZMatrixRMaj> chol(int size , boolean lower ) {
        return new CholeskyDecompositionInner_ZDRM(lower);
    }

    /**
     * Decomposes the input matrix 'a' and makes sure it isn't modified.
     */
    public static boolean decomposeSafe(DecompositionInterface<ZMatrixRMaj> decomposition, ZMatrixRMaj a) {

        if( decomposition.inputModified() ) {
            a = a.copy();
        }
        return decomposition.decompose(a);
    }
}
